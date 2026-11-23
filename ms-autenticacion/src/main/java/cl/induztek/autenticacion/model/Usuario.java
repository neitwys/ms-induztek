package cl.induztek.autenticacion.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "usuario")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(nullable = false, unique = true, length = 150)
    private String correo;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    @Column(nullable = false)
    private String contrasena;

    @Column(name = "id_rol", nullable = false)
    private Long rol;

    @Column(name = "foto_perfil")
    private String fotoPerfil;

    @Column(name = "id_empresa_fk")
    private Long empresaId;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "codigo_recuperacion")
    private String codigoRecuperacion;

    @Column(name = "expiracion_codigo")
    private LocalDateTime expiracionCodigo;

    @Column(name = "requiere_cambio_clave")
    private Boolean requiereCambioClave = false;
}