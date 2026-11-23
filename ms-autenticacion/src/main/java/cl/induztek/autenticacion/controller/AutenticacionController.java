package cl.induztek.autenticacion.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cl.induztek.autenticacion.model.LoginDTO;
import cl.induztek.autenticacion.model.UsuarioDTO;
import cl.induztek.autenticacion.service.AutenticacionService;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/autenticacion")
public class AutenticacionController {
    private final AutenticacionService service;

    public AutenticacionController(AutenticacionService service) {
        this.service = service;
    }

    // -- CRUD -- //
    // CREATE
    @PostMapping("/crear")
    public ResponseEntity<UsuarioDTO> crear(@Valid @RequestBody UsuarioDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.guardarUsuario(dto));
    }

    // READ
    @GetMapping("/listar")
    public ResponseEntity<List<UsuarioDTO>> obtenerTodos() {
        return ResponseEntity.ok(service.obtenerTodos());
    }

    @GetMapping("/buscar/{id}")
    public ResponseEntity<UsuarioDTO> obtenerPorId(@PathVariable Long id) {
        return service.obtenerPorId(id)
                .map(ResponseEntity::ok) // 200
                .orElse(ResponseEntity.notFound().build()); // 404
    }

    // UPDATE
    @PutMapping("/actualizar/{id}")
    public ResponseEntity<UsuarioDTO> actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioDTO dto) {
        return service.actualizar(id, dto)
                .map(ResponseEntity::ok) // 200
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/contrasena")
    public ResponseEntity<?> cambiarContrasena(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            service.cambiarContrasena(id, body.get("contrasenaActual"), body.get("nuevaContrasena"));
            return ResponseEntity.ok(Map.of("mensaje", "Contraseña actualizada"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // SOFT DELETE (DESHABILITAR)
    @PatchMapping("/usuarios/{id}/deshabilitar")
    public ResponseEntity<Void> deshabilitar(@PathVariable Long id) {
        if (service.deshabilitar(id)) {
            return ResponseEntity.noContent().build(); // 204
        } else {
            return ResponseEntity.notFound().build(); // 404
        }
    }

    // DELETE
    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (service.eliminar(id)) {
            return ResponseEntity.noContent().build(); // 204
        } else {
            return ResponseEntity.notFound().build(); // 404
        }
    }

    // -- LOGIN -- //
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO request) {
        try {
            UsuarioDTO usuario = service.autenticarUsuario(request.getCorreo(), request.getContrasena());
            return ResponseEntity.ok(usuario);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // -- AVATAR -- //
    @PostMapping("/{id}/avatar")
    public ResponseEntity<?> subirAvatar(
            @PathVariable Long id,
            @RequestParam("archivo") org.springframework.web.multipart.MultipartFile archivo) {
        return service.actualizarAvatar(id, archivo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // -- RECUPERACION DE CONTRASEÑA -- //
    @PostMapping("/solicitar-codigo")
    public ResponseEntity<?> solicitarCodigo(@RequestBody Map<String, String> body) {
        try {
            service.solicitarCodigoRecuperacion(body.get("correo"));
            return ResponseEntity.ok(Map.of("mensaje", "Código enviado al correo"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", "No se pudo enviar el correo de verificación"));
        }
    }

    @PutMapping("/recuperar-contrasena")
    public ResponseEntity<?> recuperarContrasena(@RequestBody Map<String, String> body) {
        try {
            service.recuperarContrasenaConCodigo(
                    body.get("correo"),
                    body.get("codigo"),
                    body.get("nuevaContrasena"));
            return ResponseEntity.ok(Map.of("mensaje", "Contraseña restablecida con éxito"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // -- PRIMERA CONTRASEÑA -- //
    @PutMapping("/{id}/primera-contrasena")
    public ResponseEntity<?> crearPrimeraContrasena(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            // Tendrás que crear este método en tu AutenticacionService
            service.crearPrimeraContrasena(id, body.get("nuevaContrasena"));
            return ResponseEntity.ok(Map.of("mensaje", "Primera contraseña guardada con éxito"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}