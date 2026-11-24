package cl.induztek.notificaciones.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

@ExtendWith(MockitoExtension.class)
class EnviarCorreoHtmlTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EnviarCorreoHtml enviador;

    private MimeMessage mimeMessage;

    @BeforeEach
    void preparar() {
        mimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        ReflectionTestUtils.setField(enviador, "remitente", "noreply@induztek.ejemplo");
        ReflectionTestUtils.setField(enviador, "correoRedireccion", "");
    }

    @Test
    void enviar_sinRedireccionNiAdjunto_enviaCorreoCorrectamente() throws Exception {
        enviador.enviar("destino@induztek.ejemplo", "Asunto Prueba", "<p>Contenido</p>");

        verify(mailSender, times(1)).send(mimeMessage);
        assertEquals("Asunto Prueba", mimeMessage.getSubject());
    }

    @Test
    void enviarConAdjunto_conRedireccionYArchivo_redireccionaYAdjunta() throws Exception {
        ReflectionTestUtils.setField(enviador, "correoRedireccion", "qa@induztek.ejemplo");

        enviador.enviarConAdjunto(
                "cliente@induztek.ejemplo",
                "Reporte",
                "<p>Adjunto</p>",
                "informe.pdf",
                "contenido-pdf".getBytes());

        verify(mailSender, times(1)).send(mimeMessage);
        assertEquals("[Prueba -> cliente@induztek.ejemplo] Reporte", mimeMessage.getSubject());
    }

    @Test
    void enviar_correoInvalido_lanzaRuntimeException() {
        assertThrows(RuntimeException.class,
                () -> enviador.enviar("correo invalido con espacios", "Asunto", "<p>Hola</p>"));
    }
}