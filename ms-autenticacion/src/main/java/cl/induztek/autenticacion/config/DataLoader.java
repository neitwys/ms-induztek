package cl.induztek.autenticacion.config;

import cl.induztek.autenticacion.model.Usuario;
import cl.induztek.autenticacion.repository.AutenticacionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {

    private final AutenticacionRepository repository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(AutenticacionRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return; // Si existen usuarios en la base de datos, no se cargan los datos lol
        }

        List<Usuario> usuariosIniciales = List.of(
                crearUsuario(
                        "Matías Godoy Herrera",
                        "mgodoy@induztek.ejemplo",
                        "123456789",
                        1L, // Técnico de Terreno
                        1,
                        "toby",
                        null),
                crearUsuario(
                        "Andrea Contreras Silva",
                        "acontreras@induztek.ejemplo",
                        "987654321",
                        2L, // Ingeniero de Pruebas
                        2,
                        "los prisioneros",
                        null),
                crearUsuario(
                        "Ricardo Espinoza Lagos",
                        "respinoza@induztek.ejemplo",
                        "135792468",
                        3L, // Jefe Técnico
                        3,
                        "azul",
                        null),
                crearUsuario(
                        "Marcela Fuentes Rojas",
                        "mfuentes@envpacifico.ejemplo",
                        "246813579",
                        4L, // Representante de Empresa
                        1,
                        "cachupin",
                        1L));

        repository.saveAll(usuariosIniciales);
        System.out.println(">>> DataLoader: 4 usuarios iniciales cargados en db_autenticacion.");
    }

    private Usuario crearUsuario(
            String nombre,
            String correo,
            String contrasenaPlana,
            Long rol,
            Integer tipoPregunta,
            String respuestaPlana,
            Long empresaId) {
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setCorreo(correo.trim().toLowerCase());
        u.setContrasena(passwordEncoder.encode(contrasenaPlana));
        u.setRol(rol);
        u.setTipoPreguntaClave(tipoPregunta);
        u.setRespuestaPreguntaClave(passwordEncoder.encode(respuestaPlana.trim().toLowerCase()));
        u.setFotoPerfil(null);
        u.setEmpresaId(empresaId);
        return u;
    }
}