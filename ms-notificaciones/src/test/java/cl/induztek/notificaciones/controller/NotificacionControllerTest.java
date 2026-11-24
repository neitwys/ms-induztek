package cl.induztek.notificaciones.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

import com.fasterxml.jackson.databind.ObjectMapper;

import cl.induztek.notificaciones.model.CodigoRecuperacionRequest;
import cl.induztek.notificaciones.model.CorreoGeneralRequest;
import cl.induztek.notificaciones.service.NotificacionService;

@WebMvcTest(NotificacionController.class)
class NotificacionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NotificacionService service;

    @Test
    void debeEnviarCodigoRecuperacion() throws Exception {
        CodigoRecuperacionRequest req = new CodigoRecuperacionRequest();
        req.setDestinatario("mgodoy@induztek.ejemplo");
        req.setNombreUsuario("Matías Godoy");
        req.setCodigo("123456");
        req.setMinutosExpiracion(10);

        doNothing().when(service).enviarCodigoRecuperacion(any(CodigoRecuperacionRequest.class));

        mockMvc.perform(post("/api/notificaciones/codigo-recuperacion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Código enviado correctamente"));

        verify(service, times(1)).enviarCodigoRecuperacion(any(CodigoRecuperacionRequest.class));
    }

    @Test
    void debeEnviarCorreoGeneral() throws Exception {
        CorreoGeneralRequest req = new CorreoGeneralRequest();
        req.setDestinatario("cliente@induztek.ejemplo");
        req.setAsunto("Aviso de servicio");
        req.setCuerpoHtml("<h1>Hola</h1>");

        doNothing().when(service).enviarCorreoGeneral(any(CorreoGeneralRequest.class));

        mockMvc.perform(post("/api/notificaciones/correo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Correo enviado correctamente"));

        verify(service, times(1)).enviarCorreoGeneral(any(CorreoGeneralRequest.class));
    }

    @Test
    void debeRetornar400CuandoDatosSonInvalidos() throws Exception {
        CorreoGeneralRequest invalido = new CorreoGeneralRequest();
        invalido.setDestinatario("correo-sin-arroba");
        invalido.setAsunto("");
        invalido.setCuerpoHtml("");

        mockMvc.perform(post("/api/notificaciones/correo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest());

        verify(service, never()).enviarCorreoGeneral(any());
    }
}