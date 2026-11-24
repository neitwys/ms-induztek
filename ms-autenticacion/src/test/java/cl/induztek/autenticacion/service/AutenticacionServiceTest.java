package cl.induztek.autenticacion.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import cl.induztek.autenticacion.model.Usuario;
import cl.induztek.autenticacion.model.UsuarioDTO;
import cl.induztek.autenticacion.repository.AutenticacionRepository;

@ExtendWith(MockitoExtension.class)
class AutenticacionServiceTest {

    @Mock
    private AutenticacionRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AutenticacionService service;

    @BeforeEach
    void preparar() {
        ReflectionTestUtils.setField(service, "restTemplate", restTemplate);
    }

    private Usuario crearUsuarioEntidad() {
        Usuario u = new Usuario();
        u.setId(1L);
        u.setNombre("Matías Godoy");
        u.setCorreo("mgodoy@induztek.ejemplo");
        u.setContrasena("$2a$10$hash");
        u.setRol(1L);
        u.setActivo(true);
        u.setRequiereCambioClave(false);
        return u;
    }

    // --- CREATE ---
    @Test
    void guardarUsuario_conContrasena_guardaYRetornaDTO() {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setNombre("Matías Godoy");
        dto.setCorreo("mgodoy@induztek.ejemplo");
        dto.setContrasena("12345678");
        dto.setRol(1L);

        when(repository.existsByCorreo("mgodoy@induztek.ejemplo")).thenReturn(false);
        when(passwordEncoder.encode("12345678")).thenReturn("$2a$10$hash");
        when(repository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        UsuarioDTO resultado = service.guardarUsuario(dto);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("mgodoy@induztek.ejemplo", resultado.getCorreo());
        verify(restTemplate, never()).postForObject(anyString(), any(), eq(Map.class));
    }

    @Test
    void guardarUsuario_sinContrasena_generaClaveTemporalYEnviaCorreo() {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setNombre("Técnico Terreno");
        dto.setCorreo("tecnico@induztek.ejemplo");
        dto.setRol(2L);

        when(repository.existsByCorreo("tecnico@induztek.ejemplo")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$tempHash");
        when(repository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioDTO resultado = service.guardarUsuario(dto);

        assertTrue(resultado.getRequiereCambioClave());
        verify(restTemplate, times(1)).postForObject(contains("/correo"), any(), eq(Map.class));
    }

    @Test
    void guardarUsuario_correoDuplicado_lanzaExcepcion() {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setCorreo("mgodoy@induztek.ejemplo");

        when(repository.existsByCorreo("mgodoy@induztek.ejemplo")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.guardarUsuario(dto));
    }

    // --- LOGIN ---
    @Test
    void autenticarUsuario_credencialesValidas_retornaDTO() {
        Usuario u = crearUsuarioEntidad();
        when(repository.findByCorreo("mgodoy@induztek.ejemplo")).thenReturn(u);
        when(passwordEncoder.matches("12345678", "$2a$10$hash")).thenReturn(true);

        UsuarioDTO resultado = service.autenticarUsuario("mgodoy@induztek.ejemplo", "12345678");

        assertEquals("mgodoy@induztek.ejemplo", resultado.getCorreo());
    }

    @Test
    void autenticarUsuario_claveIncorrectaOUsuarioDeshabilitado_lanzaExcepcion() {
        Usuario u = crearUsuarioEntidad();
        when(repository.findByCorreo("mgodoy@induztek.ejemplo")).thenReturn(u);
        when(passwordEncoder.matches("mala", "$2a$10$hash")).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> service.autenticarUsuario("mgodoy@induztek.ejemplo", "mala"));

        u.setActivo(false);
        when(passwordEncoder.matches("12345678", "$2a$10$hash")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> service.autenticarUsuario("mgodoy@induztek.ejemplo", "12345678"));
    }

    // --- READ, UPDATE, DELETE ---
    @Test
    void crudBasico_listarBuscarActualizarDeshabilitarEliminar() {
        Usuario u = crearUsuarioEntidad();
        when(repository.findAll()).thenReturn(List.of(u));
        when(repository.findById(1L)).thenReturn(Optional.of(u));
        when(repository.findById(99L)).thenReturn(Optional.empty());
        when(repository.save(any(Usuario.class))).thenReturn(u);
        when(repository.existsById(1L)).thenReturn(true);
        when(repository.existsById(99L)).thenReturn(false);

        assertEquals(1, service.obtenerTodos().size());
        assertTrue(service.obtenerPorId(1L).isPresent());
        assertTrue(service.obtenerPorId(99L).isEmpty());

        UsuarioDTO updateDto = new UsuarioDTO();
        updateDto.setNombre("Nuevo Nombre");
        updateDto.setCorreo("mgodoy@induztek.ejemplo");
        assertTrue(service.actualizar(1L, updateDto).isPresent());
        assertTrue(service.actualizar(99L, updateDto).isEmpty());

        assertTrue(service.deshabilitar(1L));
        assertFalse(service.deshabilitar(99L));

        assertTrue(service.eliminar(1L));
        assertFalse(service.eliminar(99L));
    }

    // --- AVATAR ---
    @Test
    void actualizarAvatar_usuarioExiste_guardaArchivoYActualizaRuta() throws Exception {
        Usuario u = crearUsuarioEntidad();
        when(repository.findById(1L)).thenReturn(Optional.of(u));
        when(repository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        MockMultipartFile archivo = new MockMultipartFile(
                "archivo", "foto.jpg", "image/jpeg", "contenido-imagen".getBytes());

        Optional<UsuarioDTO> resultado = service.actualizarAvatar(1L, archivo);

        assertTrue(resultado.isPresent());
        assertTrue(resultado.get().getFotoPerfil().startsWith("/uploads/avatares/usuario_1_"));

        Path creado = Paths.get(resultado.get().getFotoPerfil().substring(1));
        Files.deleteIfExists(creado);
    }

    // --- CAMBIO Y RECUPERACIÓN DE CONTRASEÑA ---
    @Test
    void cambiarYCrearPrimeraContrasena_flujoExitosoYErrores() {
        Usuario u = crearUsuarioEntidad();
        when(repository.findById(1L)).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("actual123", "$2a$10$hash")).thenReturn(true);
        when(passwordEncoder.matches("mala", "$2a$10$hash")).thenReturn(false);
        when(passwordEncoder.encode("nueva1234")).thenReturn("$2a$10$nuevoHash");

        assertThrows(IllegalArgumentException.class,
                () -> service.cambiarContrasena(1L, "mala", "nueva1234"));
        assertThrows(IllegalArgumentException.class,
                () -> service.cambiarContrasena(1L, "actual123", "corta"));

        service.cambiarContrasena(1L, "actual123", "nueva1234");
        service.crearPrimeraContrasena(1L, "nueva1234");

        assertFalse(u.getRequiereCambioClave());
        verify(repository, times(2)).save(u);
    }

    @Test
    void recuperacionContrasena_solicitarYRestablecerConCodigo() {
        Usuario u = crearUsuarioEntidad();
        when(repository.findByCorreo("mgodoy@induztek.ejemplo")).thenReturn(u);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$codHash");

        // Paso 1: Solicitar código
        service.solicitarCodigoRecuperacion("mgodoy@induztek.ejemplo");
        assertNotNull(u.getCodigoRecuperacion());
        assertNotNull(u.getExpiracionCodigo());
        verify(restTemplate, times(1)).postForObject(contains("/codigo-recuperacion"), any(), eq(Map.class));

        // Paso 2: Restablecer con código válido
        when(passwordEncoder.matches("123456", "$2a$10$codHash")).thenReturn(true);
        service.recuperarContrasenaConCodigo("mgodoy@induztek.ejemplo", "123456", "claveNueva123");

        assertNull(u.getCodigoRecuperacion());
        assertNull(u.getExpiracionCodigo());
    }

    @Test
    void recuperarContrasenaConCodigo_codigoExpirado_lanzaExcepcion() {
        Usuario u = crearUsuarioEntidad();
        u.setCodigoRecuperacion("$2a$10$codHash");
        u.setExpiracionCodigo(LocalDateTime.now().minusMinutes(5)); // Expirado

        when(repository.findByCorreo("mgodoy@induztek.ejemplo")).thenReturn(u);

        assertThrows(IllegalArgumentException.class,
                () -> service.recuperarContrasenaConCodigo("mgodoy@induztek.ejemplo", "123456", "claveNueva123"));
    }

    @Test
    void validarRamasRestantes_actualizarAvatarYErroresContrasena() {
        Usuario u = crearUsuarioEntidad();
        when(repository.findById(1L)).thenReturn(Optional.of(u));
        when(repository.findById(99L)).thenReturn(Optional.empty());
        when(repository.save(any(Usuario.class))).thenReturn(u);

        UsuarioDTO dtoCompleto = new UsuarioDTO();
        dtoCompleto.setNombre("Matías Actualizado");
        dtoCompleto.setCorreo("mgodoy@induztek.ejemplo");
        dtoCompleto.setRol(2L);
        dtoCompleto.setFotoPerfil("/uploads/avatares/foto.jpg");
        dtoCompleto.setEmpresaId(5L);
        dtoCompleto.setActivo(true);
        dtoCompleto.setContrasena("nuevaClave123");
        when(passwordEncoder.encode("nuevaClave123")).thenReturn("$2a$10$hashNuevo");

        assertTrue(service.actualizar(1L, dtoCompleto).isPresent());

        UsuarioDTO dtoDuplicado = new UsuarioDTO();
        dtoDuplicado.setNombre("Matías");
        dtoDuplicado.setCorreo("otro@induztek.ejemplo");
        when(repository.existsByCorreo("otro@induztek.ejemplo")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> service.actualizar(1L, dtoDuplicado));

        MockMultipartFile archivo = new MockMultipartFile("archivo", "a.jpg", "image/jpeg", "123".getBytes());
        assertTrue(service.actualizarAvatar(99L, archivo).isEmpty());

        when(repository.findByCorreo("noexiste@induztek.ejemplo")).thenReturn(null);
        assertThrows(IllegalArgumentException.class,
                () -> service.recuperarContrasenaConCodigo("noexiste@induztek.ejemplo", "123456", "clave1234"));

        u.setCodigoRecuperacion(null);
        when(repository.findByCorreo("mgodoy@induztek.ejemplo")).thenReturn(u);
        assertThrows(IllegalArgumentException.class,
                () -> service.recuperarContrasenaConCodigo("mgodoy@induztek.ejemplo", "123456", "clave1234"));

        assertThrows(IllegalArgumentException.class, () -> service.crearPrimeraContrasena(99L, "clave1234"));
        assertThrows(IllegalArgumentException.class, () -> service.crearPrimeraContrasena(1L, "corta"));
    }
}