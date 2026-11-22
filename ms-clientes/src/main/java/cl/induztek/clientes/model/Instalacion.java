package cl.induztek.clientes.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "instalaciones")
public class Instalacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idInstalacion;

    @Column(nullable = false)
    private String nombreInstalacion;

    private String ubicacion;
    private String comuna;
    private String region;
    private String observacionesAcceso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empresa_fk", nullable = false)
    @JsonIgnore // El culpable de que no pudiera pasar a crearUsuarioEmpresa
    private Empresa empresa;
}
