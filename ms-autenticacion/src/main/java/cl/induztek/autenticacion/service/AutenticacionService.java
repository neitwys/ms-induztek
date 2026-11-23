package cl.induztek.autenticacion.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import cl.induztek.autenticacion.repository.AutenticacionRepository;
import cl.induztek.autenticacion.model.Usuario;
import cl.induztek.autenticacion.model.UsuarioDTO;

@Service
public class AutenticacionService {
    private final AutenticacionRepository repository;
    private final PasswordEncoder passwordEncoder;
    // Para la conexión con el microservicio de notificaciones
    private final RestTemplate restTemplate = new RestTemplate();
    private final SecureRandom random = new SecureRandom();

    public AutenticacionService(AutenticacionRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    // Entidad -> DTO
    private UsuarioDTO aDTO(Usuario usuario) {
        UsuarioDTO usuarioDTO = new UsuarioDTO();
        usuarioDTO.setId(usuario.getId());
        usuarioDTO.setNombre(usuario.getNombre());
        usuarioDTO.setCorreo(usuario.getCorreo());
        usuarioDTO.setRol(usuario.getRol());
        usuarioDTO.setFotoPerfil(usuario.getFotoPerfil());
        usuarioDTO.setEmpresaId(usuario.getEmpresaId());
        usuarioDTO.setActivo(usuario.getActivo() != null ? usuario.getActivo() : true);
        usuarioDTO.setRequiereCambioClave(Boolean.TRUE.equals(usuario.getRequiereCambioClave()));
        return usuarioDTO;
    }

    // DTO -> Entidad
    private Usuario aEntidad(UsuarioDTO usuarioDTO) {
        Usuario usuario = new Usuario();
        usuario.setId(usuarioDTO.getId());
        usuario.setNombre(usuarioDTO.getNombre().trim());
        usuario.setCorreo(usuarioDTO.getCorreo().trim().toLowerCase());
        usuario.setRol(usuarioDTO.getRol());
        usuario.setFotoPerfil(usuarioDTO.getFotoPerfil());
        usuario.setEmpresaId(usuarioDTO.getEmpresaId());
        usuario.setActivo(usuarioDTO.getActivo() != null ? usuario.getActivo() : true);
        if (usuarioDTO.getContrasena() != null && !usuarioDTO.getContrasena().isBlank()) {
            usuario.setContrasena(passwordEncoder.encode(usuarioDTO.getContrasena().trim()));
        }
        return usuario;
    }

    // CREATE
    public UsuarioDTO guardarUsuario(UsuarioDTO usuarioDTO) {
        String correoNormalizado = usuarioDTO.getCorreo().trim().toLowerCase();
        if (repository.existsByCorreo(correoNormalizado)) {
            throw new IllegalArgumentException("El correo electrónico ya está registrado");
        }

        Usuario nuevoUsuario = aEntidad(usuarioDTO);
        nuevoUsuario.setCorreo(correoNormalizado);

        String claveTemporal = null;
        if (usuarioDTO.getContrasena() == null || usuarioDTO.getContrasena().isBlank()) {
            claveTemporal = generarClaveTemporal(10);
            nuevoUsuario.setContrasena(passwordEncoder.encode(claveTemporal));
            nuevoUsuario.setRequiereCambioClave(true);
        }

        if (nuevoUsuario.getActivo() == null) {
            nuevoUsuario.setActivo(true);
        }

        Usuario guardado = repository.save(nuevoUsuario);

        if (claveTemporal != null) {
            enviarCorreoBienvenidaClaveTemporal(guardado, claveTemporal);
        }

        return aDTO(guardado);
    }

    private String generarClaveTemporal(int longitud) {
        final String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789@#$%";
        StringBuilder sb = new StringBuilder(longitud);
        for (int i = 0; i < longitud; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private void enviarCorreoBienvenidaClaveTemporal(Usuario usuario, String claveTemporal) {
        try {
            String url = "http://ms-notificaciones:8082/api/notificaciones/correo";
            String cuerpoHtml = """
                    <div style="font-family: Arial, sans-serif; max-width: 520px; margin: auto; border: 1px solid #e0e6ed; border-radius: 12px; overflow: hidden;">
                      <div style="background-color: #0B1F3A; color: #ffffff; padding: 20px; text-align: center;">
                        <h2 style="margin: 0;">INDUZTEK</h2>
                        <p style="margin: 4px 0 0; font-size: 13px; opacity: 0.85;">Plataforma de Gestión en Terreno</p>
                      </div>
                      <div style="padding: 24px; color: #1f2937;">
                        <p>Hola <strong>%s</strong>,</p>
                        <p>Se ha creado tu cuenta en la plataforma. Usa la siguiente clave temporal para iniciar sesión y activar tu acceso:</p>
                        <div style="background-color: #f3f6fa; border-radius: 8px; padding: 16px; text-align: center; margin: 20px 0;">
                          <span style="font-size: 24px; font-weight: bold; letter-spacing: 3px; color: #153B6E;">%s</span>
                        </div>
                        <p style="font-size: 13px; color: #4b5563;">Te recomendamos cambiar esta clave una vez que ingreses por primera vez.</p>
                      </div>
                    </div>
                    """
                    .formatted(usuario.getNombre(), claveTemporal);

            Map<String, Object> datosCorreo = Map.of(
                    "destinatario", usuario.getCorreo(),
                    "asunto", "Activación de cuenta - Induztek",
                    "cuerpoHtml", cuerpoHtml);
            restTemplate.postForObject(url, datosCorreo, Map.class);
        } catch (Exception e) {
            System.out.println("Aviso: no se pudo enviar correo de bienvenida: " + e.getMessage());
        }
    }

    // LOGIN
    public UsuarioDTO autenticarUsuario(String correo, String contrasenaPlana) {
        System.out.println("DEBUG LOGIN -> correo=[" + correo + "], pass=[" + contrasenaPlana + "]");

        Usuario usuario = repository.findByCorreo(correo.trim().toLowerCase());
        if (usuario == null || !passwordEncoder.matches(contrasenaPlana.trim(), usuario.getContrasena())) {
            throw new IllegalArgumentException("Correo o contraseña incorrectos");
        }
        if (Boolean.FALSE.equals(usuario.getActivo())) {
            throw new IllegalArgumentException("Esta cuenta ha sido deshabilitada");
        }
        return aDTO(usuario);
    }

    // READ
    public List<UsuarioDTO> obtenerTodos() {
        List<Usuario> usuarios = repository.findAll();
        List<UsuarioDTO> lista = new ArrayList<>();
        for (Usuario usuario : usuarios) {
            lista.add(aDTO(usuario));
        }
        return lista;
    }

    public Optional<UsuarioDTO> obtenerPorId(Long id) {
        return repository.findById(id).map(this::aDTO);
    }

    // UPDATE
    public Optional<UsuarioDTO> actualizar(Long id, UsuarioDTO dto) {
        Optional<Usuario> usuarioOpt = repository.findById(id);
        if (usuarioOpt.isEmpty()) {
            return Optional.empty();
        }

        Usuario usuarioExistente = usuarioOpt.get();
        String nuevoCorreo = dto.getCorreo().trim().toLowerCase();

        if (!usuarioExistente.getCorreo().equals(nuevoCorreo) && repository.existsByCorreo(nuevoCorreo)) {
            throw new IllegalArgumentException("El correo electrónico ya está registrado");
        }

        usuarioExistente.setNombre(dto.getNombre().trim());
        usuarioExistente.setCorreo(nuevoCorreo);

        if (dto.getRol() != null) {
            usuarioExistente.setRol(dto.getRol());
        }
        if (dto.getFotoPerfil() != null) {
            usuarioExistente.setFotoPerfil(dto.getFotoPerfil());
        }
        if (dto.getEmpresaId() != null) {
            usuarioExistente.setEmpresaId(dto.getEmpresaId());
        }
        if (dto.getActivo() != null) {
            usuarioExistente.setActivo(dto.getActivo());
        }
        if (dto.getContrasena() != null && !dto.getContrasena().isBlank()) {
            usuarioExistente.setContrasena(passwordEncoder.encode(dto.getContrasena().trim()));
        }

        return Optional.of(aDTO(repository.save(usuarioExistente)));
    }

    // SOFT DELETE (DESHABILITAR)
    public boolean deshabilitar(Long id) {
        Optional<Usuario> usuarioOpt = repository.findById(id);
        if (usuarioOpt.isEmpty()) {
            return false;
        }
        Usuario usuario = usuarioOpt.get();
        usuario.setActivo(false);
        repository.save(usuario);
        return true;
    }

    // DELETE
    public boolean eliminar(Long id) {
        if (!repository.existsById(id)) {
            return false;
        }
        repository.deleteById(id);
        return true;
    }

    // UPDATE AVATAR
    public Optional<UsuarioDTO> actualizarAvatar(Long id, org.springframework.web.multipart.MultipartFile archivo) {
        Optional<Usuario> usuarioOpt = repository.findById(id);
        if (usuarioOpt.isEmpty()) {
            return Optional.empty();
        }
        try {
            java.nio.file.Path carpeta = java.nio.file.Paths.get("uploads/avatares");
            java.nio.file.Files.createDirectories(carpeta);

            String nombreArchivo = "usuario_" + id + "_" + System.currentTimeMillis() + ".jpg";
            java.nio.file.Path destino = carpeta.resolve(nombreArchivo);
            java.nio.file.Files.copy(archivo.getInputStream(), destino,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            Usuario usuario = usuarioOpt.get();
            usuario.setFotoPerfil("/uploads/avatares/" + nombreArchivo);
            return Optional.of(aDTO(repository.save(usuario)));
        } catch (java.io.IOException e) {
            throw new RuntimeException("No se pudo guardar la imagen en el servidor", e);
        }
    }

    // CAMBIAR CONTRASEÑA DESDE PERFIL
    public void cambiarContrasena(Long id, String contrasenaActual, String nuevaContrasena) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (!passwordEncoder.matches(contrasenaActual.trim(), usuario.getContrasena())) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta");
        }
        if (nuevaContrasena == null || nuevaContrasena.trim().length() < 8) {
            throw new IllegalArgumentException("La nueva contraseña debe tener al menos 8 caracteres");
        }

        usuario.setContrasena(passwordEncoder.encode(nuevaContrasena.trim()));
        repository.save(usuario);
    }

    // PASOS PARA RECUPERACIÓN DE CONTRASEÑA
    // PASO 1 RECUPERACIÓN: GENERAR CÓDIGO Y LLAMAR A MS-NOTIFICACIONES
    public void solicitarCodigoRecuperacion(String correo) {
        Usuario usuario = repository.findByCorreo(correo.trim().toLowerCase());
        if (usuario == null) {
            throw new IllegalArgumentException("El correo ingresado no se encuentra registrado");
        }
        if (Boolean.FALSE.equals(usuario.getActivo())) {
            throw new IllegalArgumentException("Esta cuenta ha sido deshabilitada");
        }

        String codigoPlano = String.format("%06d", random.nextInt(1_000_000));
        usuario.setCodigoRecuperacion(passwordEncoder.encode(codigoPlano));
        usuario.setExpiracionCodigo(LocalDateTime.now().plusMinutes(10));
        repository.save(usuario);

        String url = "http://ms-notificaciones:8082/api/notificaciones/codigo-recuperacion";
        Map<String, Object> datosCorreo = Map.of(
                "destinatario", usuario.getCorreo(),
                "nombreUsuario", usuario.getNombre(),
                "codigo", codigoPlano,
                "minutosExpiracion", 10);

        restTemplate.postForObject(url, datosCorreo, Map.class);
    }

    // PASO 2 RECUPERACIÓN: VALIDAR CÓDIGO Y ACTUALIZAR CONTRASEÑA
    public void recuperarContrasenaConCodigo(String correo, String codigo, String nuevaContrasena) {
        Usuario usuario = repository.findByCorreo(correo.trim().toLowerCase());
        if (usuario == null) {
            throw new IllegalArgumentException("Usuario no encontrado");
        }
        if (usuario.getCodigoRecuperacion() == null || usuario.getExpiracionCodigo() == null) {
            throw new IllegalArgumentException("Primero solicita un código de verificación");
        }
        if (LocalDateTime.now().isAfter(usuario.getExpiracionCodigo())) {
            throw new IllegalArgumentException("El código ha expirado. Solicita uno nuevo");
        }
        if (!passwordEncoder.matches(codigo.trim(), usuario.getCodigoRecuperacion())) {
            throw new IllegalArgumentException("El código de verificación es incorrecto");
        }
        if (nuevaContrasena == null || nuevaContrasena.trim().length() < 8) {
            throw new IllegalArgumentException("La nueva contraseña debe tener al menos 8 caracteres");
        }

        usuario.setContrasena(passwordEncoder.encode(nuevaContrasena.trim()));
        usuario.setCodigoRecuperacion(null);
        usuario.setExpiracionCodigo(null);
        repository.save(usuario);
    }

    // CREAR PRIMERA CONTRASEÑA
    public void crearPrimeraContrasena(Long id, String nuevaContrasena) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (nuevaContrasena == null || nuevaContrasena.trim().length() < 8) {
            throw new IllegalArgumentException("La nueva contraseña debe tener al menos 8 caracteres");
        }
        usuario.setContrasena(passwordEncoder.encode(nuevaContrasena.trim()));
        usuario.setRequiereCambioClave(false);
        repository.save(usuario);
    }
}