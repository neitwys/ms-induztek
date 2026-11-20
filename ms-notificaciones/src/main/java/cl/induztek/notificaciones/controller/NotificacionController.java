package cl.induztek.notificaciones.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.induztek.notificaciones.model.CodigoRecuperacionRequest;
import cl.induztek.notificaciones.model.CorreoGeneralRequest;
import cl.induztek.notificaciones.service.NotificacionService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {
    private final NotificacionService service;

    public NotificacionController(NotificacionService service) {
        this.service = service;
    }

    @PostMapping("/codigo-recuperacion")
    public ResponseEntity<?> enviarCodigoRecuperacion(@Valid @RequestBody CodigoRecuperacionRequest request) {
        service.enviarCodigoRecuperacion(request);
        return ResponseEntity.ok(Map.of("mensaje", "Código enviado correctamente"));
    }

    @PostMapping("/correo")
    public ResponseEntity<?> enviarCorreo(@Valid @RequestBody CorreoGeneralRequest request) {
        service.enviarCorreoGeneral(request);
        return ResponseEntity.ok(Map.of("mensaje", "Correo enviado correctamente"));
    }

}
