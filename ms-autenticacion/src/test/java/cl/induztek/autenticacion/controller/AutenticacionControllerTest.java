package cl.induztek.autenticacion.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import cl.induztek.autenticacion.model.LoginDTO;
import cl.induztek.autenticacion.model.UsuarioDTO;
import cl.induztek.autenticacion.service.AutenticacionService;

@WebMvcTest(controllers = AutenticacionController.class, excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class
})
class AutenticacionControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private AutenticacionService service;

        private UsuarioDTO crearUsuarioDTOValido(Long id) {
                UsuarioDTO dto = new UsuarioDTO();
                dto.setId(id);
                dto.setNombre("Matías Godoy");
                dto.setCorreo("mgodoy@induztek.ejemplo");
                dto.setContrasena("12345678");
                dto.setRol(1L);
                dto.setActivo(true);
                return dto;
        }

        @Test
        void debeCrearUsuario() throws Exception {
                UsuarioDTO request = crearUsuarioDTOValido(null);
                UsuarioDTO creado = crearUsuarioDTOValido(1L);

                when(service.guardarUsuario(any(UsuarioDTO.class))).thenReturn(creado);

                mockMvc.perform(post("/api/autenticacion/crear")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.correo").value("mgodoy@induztek.ejemplo"))
                                .andExpect(jsonPath("$.rol").value(1));
        }

        @Test
        void debeListarYBuscarUsuarios() throws Exception {
                when(service.obtenerTodos()).thenReturn(List.of(crearUsuarioDTOValido(1L)));
                when(service.obtenerPorId(1L)).thenReturn(Optional.of(crearUsuarioDTOValido(1L)));
                when(service.obtenerPorId(99L)).thenReturn(Optional.empty());

                mockMvc.perform(get("/api/autenticacion/listar"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].id").value(1));

                mockMvc.perform(get("/api/autenticacion/buscar/1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1));

                mockMvc.perform(get("/api/autenticacion/buscar/99"))
                                .andExpect(status().isNotFound());
        }

        @Test
        void debeActualizarDeshabilitarYEliminarUsuario() throws Exception {
                UsuarioDTO dto = crearUsuarioDTOValido(1L);
                when(service.actualizar(eq(1L), any(UsuarioDTO.class))).thenReturn(Optional.of(dto));
                when(service.actualizar(eq(99L), any(UsuarioDTO.class))).thenReturn(Optional.empty());
                when(service.deshabilitar(1L)).thenReturn(true);
                when(service.deshabilitar(99L)).thenReturn(false);
                when(service.eliminar(1L)).thenReturn(true);
                when(service.eliminar(99L)).thenReturn(false);

                mockMvc.perform(put("/api/autenticacion/actualizar/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))
                                .andExpect(status().isOk());

                mockMvc.perform(put("/api/autenticacion/actualizar/99")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))
                                .andExpect(status().isNotFound());

                mockMvc.perform(patch("/api/autenticacion/usuarios/1/deshabilitar"))
                                .andExpect(status().isNoContent());

                mockMvc.perform(patch("/api/autenticacion/usuarios/99/deshabilitar"))
                                .andExpect(status().isNotFound());

                mockMvc.perform(delete("/api/autenticacion/eliminar/1"))
                                .andExpect(status().isNoContent());

                mockMvc.perform(delete("/api/autenticacion/eliminar/99"))
                                .andExpect(status().isNotFound());
        }

        @Test
        void debeHacerLoginExitosoYFallido() throws Exception {
                LoginDTO loginOk = new LoginDTO("mgodoy@induztek.ejemplo", "12345678");
                when(service.autenticarUsuario("mgodoy@induztek.ejemplo", "12345678"))
                                .thenReturn(crearUsuarioDTOValido(1L));

                mockMvc.perform(post("/api/autenticacion/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginOk)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.correo").value("mgodoy@induztek.ejemplo"));

                LoginDTO loginFail = new LoginDTO("malo@induztek.ejemplo", "1234");
                when(service.autenticarUsuario("malo@induztek.ejemplo", "1234"))
                                .thenThrow(new IllegalArgumentException("Correo o contraseña incorrectos"));

                mockMvc.perform(post("/api/autenticacion/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginFail)))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.error").value("Correo o contraseña incorrectos"));
        }

        @Test
        void debeSubirAvatar() throws Exception {
                MockMultipartFile archivo = new MockMultipartFile(
                                "archivo", "avatar.jpg", "image/jpeg", "bytes".getBytes());

                when(service.actualizarAvatar(eq(1L), any())).thenReturn(Optional.of(crearUsuarioDTOValido(1L)));
                when(service.actualizarAvatar(eq(99L), any())).thenReturn(Optional.empty());

                mockMvc.perform(multipart("/api/autenticacion/1/avatar").file(archivo))
                                .andExpect(status().isOk());

                mockMvc.perform(multipart("/api/autenticacion/99/avatar").file(archivo))
                                .andExpect(status().isNotFound());
        }

        @Test
        void debeGestionarContrasenasYRecuperacion() throws Exception {
                doNothing().when(service).cambiarContrasena(1L, "actual", "nueva1234");
                doThrow(new IllegalArgumentException("Clave actual incorrecta"))
                                .when(service).cambiarContrasena(1L, "mala", "nueva1234");

                mockMvc.perform(put("/api/autenticacion/1/contrasena")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                                Map.of("contrasenaActual", "actual", "nuevaContrasena", "nueva1234"))))
                                .andExpect(status().isOk());

                mockMvc.perform(put("/api/autenticacion/1/contrasena")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                                Map.of("contrasenaActual", "mala", "nuevaContrasena", "nueva1234"))))
                                .andExpect(status().isBadRequest());

                doNothing().when(service).solicitarCodigoRecuperacion("mgodoy@induztek.ejemplo");
                doThrow(new IllegalArgumentException("No encontrado"))
                                .when(service).solicitarCodigoRecuperacion("noexiste@induztek.ejemplo");
                doThrow(new RuntimeException("Fallo SMTP"))
                                .when(service).solicitarCodigoRecuperacion("fallo@induztek.ejemplo");

                mockMvc.perform(post("/api/autenticacion/solicitar-codigo")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(Map.of("correo", "mgodoy@induztek.ejemplo"))))
                                .andExpect(status().isOk());

                mockMvc.perform(post("/api/autenticacion/solicitar-codigo")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper
                                                .writeValueAsString(Map.of("correo", "noexiste@induztek.ejemplo"))))
                                .andExpect(status().isNotFound());

                mockMvc.perform(post("/api/autenticacion/solicitar-codigo")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(Map.of("correo", "fallo@induztek.ejemplo"))))
                                .andExpect(status().isBadGateway());

                doNothing().when(service).recuperarContrasenaConCodigo("mgodoy@induztek.ejemplo", "123456",
                                "nueva1234");
                mockMvc.perform(put("/api/autenticacion/recuperar-contrasena")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                                Map.of("correo", "mgodoy@induztek.ejemplo", "codigo", "123456",
                                                                "nuevaContrasena",
                                                                "nueva1234"))))
                                .andExpect(status().isOk());

                doNothing().when(service).crearPrimeraContrasena(1L, "nueva1234");
                mockMvc.perform(put("/api/autenticacion/1/primera-contrasena")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(Map.of("nuevaContrasena", "nueva1234"))))
                                .andExpect(status().isOk());
        }
}