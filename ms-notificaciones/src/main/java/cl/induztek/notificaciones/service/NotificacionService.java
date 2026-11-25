package cl.induztek.notificaciones.service;

import org.springframework.stereotype.Service;

import cl.induztek.notificaciones.model.CodigoRecuperacionRequest;
import cl.induztek.notificaciones.model.CorreoGeneralRequest;
import cl.induztek.notificaciones.model.EnviarCorreoHtml;

@Service
public class NotificacionService {
  private final EnviarCorreoHtml enviadorCorreo;

  public NotificacionService(EnviarCorreoHtml enviadorCorreo) {
    this.enviadorCorreo = enviadorCorreo;
  }

  public void enviarCodigoRecuperacion(CodigoRecuperacionRequest req) {
    String html = """
        <div style="font-family: Arial, sans-serif; max-width: 520px; margin: auto; border: 1px solid #e0e6ed; border-radius: 12px; overflow: hidden;">
          <div style="background-color: #0B1F3A; color: #ffffff; padding: 20px; text-align: center;">
            <h2 style="margin: 0;">INDUZTEK</h2>
            <p style="margin: 4px 0 0; font-size: 13px; opacity: 0.85;">Plataforma de Gestión en Terreno</p>
          </div>
          <div style="padding: 24px; color: #1f2937;">
            <p>Hola <strong>%s</strong>,</p>
            <p>Recibimos una solicitud para restablecer tu contraseña. Ingresa el siguiente código de verificación en la aplicación:</p>
            <div style="background-color: #f3f6fa; border-radius: 8px; padding: 16px; text-align: center; margin: 20px 0;">
              <span style="font-size: 32px; font-weight: bold; letter-spacing: 6px; color: #153B6E;">%s</span>
            </div>
            <p style="font-size: 13px; color: #4b5563;">Este código expirará en <strong>%d minutos</strong>. Si no solicitaste este cambio, puedes ignorar este correo.</p>
          </div>
        </div>
        """
        .formatted(req.getNombreUsuario(), req.getCodigo(), req.getMinutosExpiracion());

    enviadorCorreo.enviar(
        req.getDestinatario(),
        "Código de recuperación de contraseña - Induztek",
        html);
  }

  public void enviarCorreoGeneral(CorreoGeneralRequest req) {
    enviadorCorreo.enviar(req.getDestinatario(), req.getAsunto(), req.getCuerpoHtml());
  }

}