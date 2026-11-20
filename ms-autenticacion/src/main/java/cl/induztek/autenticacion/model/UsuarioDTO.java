package cl.induztek.autenticacion.model;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150, message = "El nombre no puede tener más de 150 caracteres")
    private String nombre;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El correo electrónico debe ser válido")
    @Size(max = 150, message = "El correo electrónico no puede tener más de 150 caracteres")
    private String correo;

    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String contrasena;

    private Long rol;

    @Column(name = "foto_perfil")
    private String fotoPerfil;

    @Column(name = "id_empresa_fk")
    private Long empresaId;
}
