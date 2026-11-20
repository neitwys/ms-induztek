package cl.induztek.notificaciones.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CodigoRecuperacionRequest {
    @NotBlank
    @Email
    private String destinatario;

    @NotBlank
    private String nombreUsuario;

    @NotBlank
    private String codigo;

    private int minutosExpiracion = 10;
}