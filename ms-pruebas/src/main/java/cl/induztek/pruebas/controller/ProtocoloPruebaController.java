package cl.induztek.pruebas.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cl.induztek.pruebas.model.ProtocoloPrueba;
import cl.induztek.pruebas.service.ProtocoloPruebaService;

@RestController
@RequestMapping("/api/pruebas")
public class ProtocoloPruebaController {

    private final ProtocoloPruebaService service;

    public ProtocoloPruebaController(ProtocoloPruebaService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ProtocoloPrueba>> listar(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Long idEquipo,
            @RequestParam(required = false) String codigoEquipo
    ) {
        if (estado != null && !estado.isBlank()) {
            return ResponseEntity.ok(service.listarPorEstado(estado));
        }
        if (idEquipo != null || (codigoEquipo != null && !codigoEquipo.isBlank())) {
            return ResponseEntity.ok(service.listarPorEquipo(idEquipo, codigoEquipo));
        }
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProtocoloPrueba> obtenerPorId(@PathVariable Long id) {
        ProtocoloPrueba prueba = service.obtenerPorId(id);
        return prueba != null ? ResponseEntity.ok(prueba) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<ProtocoloPrueba> crear(@RequestBody ProtocoloPrueba protocolo) {
        ProtocoloPrueba creado = service.registrar(protocolo);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProtocoloPrueba> actualizar(
            @PathVariable Long id,
            @RequestBody ProtocoloPrueba protocolo
    ) {
        ProtocoloPrueba actualizado = service.actualizar(id, protocolo);
        return actualizado != null ? ResponseEntity.ok(actualizado) : ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}/aprobar")
    public ResponseEntity<ProtocoloPrueba> aprobar(
            @PathVariable Long id,
            @RequestParam Long idUsuarioIngeniero
    ) {
        ProtocoloPrueba aprobada = service.aprobar(id, idUsuarioIngeniero);
        return aprobada != null ? ResponseEntity.ok(aprobada) : ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}/rechazar")
    public ResponseEntity<?> rechazar(
            @PathVariable Long id,
            @RequestParam Long idUsuarioIngeniero,
            @RequestBody Map<String, String> body
    ) {
        String motivo = body != null ? body.get("motivo") : null;
        try {
            ProtocoloPrueba rechazada = service.rechazar(id, idUsuarioIngeniero, motivo);
            return rechazada != null ? ResponseEntity.ok(rechazada) : ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}