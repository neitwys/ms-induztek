package cl.induztek.clientes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import cl.induztek.clientes.model.Empresa;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
    Optional<Empresa> findByRut(String rut);
}
