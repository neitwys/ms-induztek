package cl.induztek.pruebas.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.induztek.pruebas.model.ProtocoloPrueba;

public interface ProtocoloPruebaRepository extends JpaRepository<ProtocoloPrueba, Long> {
    List<ProtocoloPrueba> findByEstadoProtocolo(String estadoProtocolo);
    List<ProtocoloPrueba> findByCodigoEquipo(String codigoEquipo);
    List<ProtocoloPrueba> findByIdEquipo(Long idEquipo);
}