package cl.induztek.notificaciones.model;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class EnviarCorreoHtml {
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String remitente;

    @Value("${mail.redirect-to:}")
    private String correoRedireccion;

    public EnviarCorreoHtml(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviar(String destinatario, String asunto, String html) {
        enviarConAdjunto(destinatario, asunto, html, null, null);
    }

    public void enviarConAdjunto(
            String destinatario,
            String asunto,
            String html,
            String nombreArchivo,
            byte[] archivoAdjunto) {
        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            boolean tieneAdjunto = nombreArchivo != null && archivoAdjunto != null && archivoAdjunto.length > 0;
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, tieneAdjunto, "UTF-8");

            String destinoFinal = destinatario.trim();
            String asuntoFinal = asunto;

            if (correoRedireccion != null && !correoRedireccion.isBlank()) {
                destinoFinal = correoRedireccion.trim();
                asuntoFinal = "[Prueba -> " + destinatario.trim() + "] " + asunto;
            }

            helper.setFrom(remitente);
            helper.setTo(destinoFinal);
            helper.setSubject(asuntoFinal);
            helper.setText(html, true);

            if (tieneAdjunto) {
                helper.addAttachment(nombreArchivo, new ByteArrayResource(archivoAdjunto));
            }

            mailSender.send(mensaje);
        } catch (MessagingException e) {
            throw new RuntimeException("No se pudo enviar el correo a " + destinatario, e);
        }
    }
}
