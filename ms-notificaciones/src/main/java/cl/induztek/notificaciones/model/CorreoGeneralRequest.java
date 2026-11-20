package cl.induztek.notificaciones.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CorreoGeneralRequest {
    @NotBlank
    @Email
    private String destinatario;

    @NotBlank
    private String asunto;

    @NotBlank
    private String cuerpoHtml;
}