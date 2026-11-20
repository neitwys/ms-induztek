package cl.induztek.autenticacion.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import cl.induztek.autenticacion.repository.AutenticacionRepository;
import cl.induztek.autenticacion.model.Usuario;
import cl.induztek.autenticacion.model.UsuarioDTO;

@Service
public class AutenticacionService {
    private final AutenticacionRepository repository;
    private final PasswordEncoder passwordEncoder;

    public AutenticacionService(AutenticacionRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    // Entidad -> DTO
    private UsuarioDTO aDTO(Usuario usuario) {
        UsuarioDTO usuarioDTO = new UsuarioDTO();
        usuarioDTO.setId(usuario.getId());
        usuarioDTO.setNombre(usuario.getNombre());
        usuarioDTO.setCorreo(usuario.getCorreo());
        usuarioDTO.setRol(usuario.getRol());
        usuarioDTO.setTipoPreguntaClave(usuario.getTipoPreguntaClave());
        usuarioDTO.setFotoPerfil(usuario.getFotoPerfil());
        usuarioDTO.setEmpresaId(usuario.getEmpresaId());
        return usuarioDTO;
    }

    // DTO -> Entidad
    private Usuario aEntidad(UsuarioDTO usuarioDTO) {
        Usuario usuario = new Usuario();
        usuario.setId(usuarioDTO.getId());
        usuario.setNombre(usuarioDTO.getNombre().trim());
        usuario.setCorreo(usuarioDTO.getCorreo().trim().toLowerCase());
        usuario.setRol(usuarioDTO.getRol());
        usuario.setTipoPreguntaClave(usuarioDTO.getTipoPreguntaClave());
        usuario.setFotoPerfil(usuarioDTO.getFotoPerfil());
        usuario.setEmpresaId(usuarioDTO.getEmpresaId());
        if (usuarioDTO.getContrasena() != null && !usuarioDTO.getContrasena().isBlank()) {
            usuario.setContrasena(passwordEncoder.encode(usuarioDTO.getContrasena().trim()));
        }
        if (usuarioDTO.getRespuestaPreguntaClave() != null && !usuarioDTO.getRespuestaPreguntaClave().isBlank()) {
            String respNormalizada = usuarioDTO.getRespuestaPreguntaClave().trim().toLowerCase();
            usuario.setRespuestaPreguntaClave(passwordEncoder.encode(respNormalizada));
        }
        return usuario;
    }

    // CREATE
    public UsuarioDTO guardarUsuario(UsuarioDTO usuarioDTO) {
        if (repository.existsByCorreo(usuarioDTO.getCorreo().trim().toLowerCase())) {
            throw new IllegalArgumentException("El correo electrónico ya está registrado");
        }

        Usuario nuevoUsuario = aEntidad(usuarioDTO);
        Usuario guardado = repository.save(nuevoUsuario);
        return aDTO(guardado);
    }

    // READ
    public UsuarioDTO autenticarUsuario(String correo, String contrasenaPlana) {
        Usuario usuario = repository.findByCorreo(correo.trim().toLowerCase());
        if (usuario == null || !passwordEncoder.matches(contrasenaPlana.trim(), usuario.getContrasena())) {
            throw new IllegalArgumentException("Correo o contraseña incorrectos");
        }
        return aDTO(usuario);
    }

    public List<UsuarioDTO> obtenerTodos() {
        List<Usuario> usuarios = repository.findAll();
        List<UsuarioDTO> lista = new ArrayList<>();
        for (Usuario usuario : usuarios) {
            lista.add(aDTO(usuario));
        }
        return lista;
    }

    public Optional<UsuarioDTO> obtenerPorId(Long id) {
        return repository.findById(id).map(this::aDTO);
    }

    // UPDATE
    public Optional<UsuarioDTO> actualizar(Long id, UsuarioDTO dto) {
        Optional<Usuario> usuarioOpt = repository.findById(id);
        if (usuarioOpt.isEmpty()) {
            return Optional.empty();
        }

        Usuario usuarioExistente = usuarioOpt.get();
        String nuevoCorreo = dto.getCorreo().trim().toLowerCase();

        if (!usuarioExistente.getCorreo().equals(nuevoCorreo) && repository.existsByCorreo(nuevoCorreo)) {
            throw new IllegalArgumentException("El correo electrónico ya está registrado");
        }

        usuarioExistente.setNombre(dto.getNombre().trim());
        usuarioExistente.setCorreo(nuevoCorreo);

        if (dto.getRol() != null) {
            usuarioExistente.setRol(dto.getRol());
        }
        if (dto.getTipoPreguntaClave() != null) {
            usuarioExistente.setTipoPreguntaClave(dto.getTipoPreguntaClave());
        }
        if (dto.getFotoPerfil() != null) {
            usuarioExistente.setFotoPerfil(dto.getFotoPerfil());
        }
        if (dto.getEmpresaId() != null) {
            usuarioExistente.setEmpresaId(dto.getEmpresaId());
        }
        if (dto.getContrasena() != null && !dto.getContrasena().isBlank()) {
            usuarioExistente.setContrasena(passwordEncoder.encode(dto.getContrasena().trim()));
        }
        if (dto.getRespuestaPreguntaClave() != null && !dto.getRespuestaPreguntaClave().isBlank()) {
            String respNormalizada = dto.getRespuestaPreguntaClave().trim().toLowerCase();
            usuarioExistente.setRespuestaPreguntaClave(passwordEncoder.encode(respNormalizada));
        }

        return Optional.of(aDTO(repository.save(usuarioExistente)));
    }

    // DELETE
    public boolean eliminar(Long id) {
        if (!repository.existsById(id)) {
            return false;
        }
        repository.deleteById(id);
        return true;
    }

    // UPDATE AVATAR
    public Optional<UsuarioDTO> actualizarAvatar(Long id, org.springframework.web.multipart.MultipartFile archivo) {
        Optional<Usuario> usuarioOpt = repository.findById(id);
        if (usuarioOpt.isEmpty()) {
            return Optional.empty();
        }
        try {
            java.nio.file.Path carpeta = java.nio.file.Paths.get("uploads/avatares");
            java.nio.file.Files.createDirectories(carpeta);

            String nombreArchivo = "usuario_" + id + "_" + System.currentTimeMillis() + ".jpg";
            java.nio.file.Path destino = carpeta.resolve(nombreArchivo);
            java.nio.file.Files.copy(archivo.getInputStream(), destino,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            Usuario usuario = usuarioOpt.get();
            usuario.setFotoPerfil("/uploads/avatares/" + nombreArchivo);
            return Optional.of(aDTO(repository.save(usuario)));
        } catch (java.io.IOException e) {
            throw new RuntimeException("No se pudo guardar la imagen en el servidor", e);
        }
    }
}