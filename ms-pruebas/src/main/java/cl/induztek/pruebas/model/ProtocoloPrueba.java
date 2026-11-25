package cl.induztek.pruebas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "protocolo_prueba")
public class ProtocoloPrueba {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProtocolo;

    @Column(nullable = false)
    private Long idEquipo;

    private String codigoEquipo = "";
    private String tipoEquipo = "";

    @Column(nullable = false)
    private Long idUsuarioTecnico;

    private String nombreTecnico = "";

    @Column(nullable = false)
    private String fechaHora;

    @Column(nullable = false)
    private String tipoPrueba;

    @Column(nullable = false)
    private String valorMedicion;

    @Column(nullable = false)
    private String resultado = "PENDIENTE";

    @Column(nullable = false)
    private String estadoProtocolo = "PENDIENTE_APROBACION";

    @Column(length = 1000)
    private String observaciones = "";

    @Column(length = 500)
    private String fotoUri;

    private Double latitud;
    private Double longitud;

    private Long idUsuarioIngeniero;

    @Column(length = 500)
    private String motivoRechazo;
}