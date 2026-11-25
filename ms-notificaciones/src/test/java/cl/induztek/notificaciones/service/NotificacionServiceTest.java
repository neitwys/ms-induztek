package cl.induztek.notificaciones.service;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cl.induztek.notificaciones.model.CodigoRecuperacionRequest;
import cl.induztek.notificaciones.model.CorreoGeneralRequest;
import cl.induztek.notificaciones.model.EnviarCorreoHtml;

@ExtendWith(MockitoExtension.class)
class NotificacionServiceTest {

    @Mock
    private EnviarCorreoHtml enviadorCorreo;

    @InjectMocks
    private NotificacionService service;

    @Test
    void enviarCodigoRecuperacion_formateaHtmlYEnviaCorreo() {
        CodigoRecuperacionRequest req = new CodigoRecuperacionRequest();
        req.setDestinatario("mgodoy@induztek.ejemplo");
        req.setNombreUsuario("Matías Godoy");
        req.setCodigo("482910");
        req.setMinutosExpiracion(10);

        service.enviarCodigoRecuperacion(req);

        verify(enviadorCorreo, times(1)).enviar(
                eq("mgodoy@induztek.ejemplo"),
                eq("Código de recuperación de contraseña - Induztek"),
                contains("482910"));
    }

    @Test
    void enviarCorreoGeneral_reenviaParametrosAEnviador() {
        CorreoGeneralRequest req = new CorreoGeneralRequest();
        req.setDestinatario("cliente@induztek.ejemplo");
        req.setAsunto("Activación de cuenta");
        req.setCuerpoHtml("<p>Bienvenido a Induztek</p>");

        service.enviarCorreoGeneral(req);

        verify(enviadorCorreo, times(1)).enviar(
                "cliente@induztek.ejemplo",
                "Activación de cuenta",
                "<p>Bienvenido a Induztek</p>");
    }
}