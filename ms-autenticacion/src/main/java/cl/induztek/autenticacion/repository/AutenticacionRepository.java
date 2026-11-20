package cl.induztek.autenticacion.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.induztek.autenticacion.model.Usuario;

public interface AutenticacionRepository extends JpaRepository<Usuario, Long> {
    Usuario findByCorreo(String correo);

    Usuario findByRol(Long rol);

    boolean existsByCorreo(String correo);

}
