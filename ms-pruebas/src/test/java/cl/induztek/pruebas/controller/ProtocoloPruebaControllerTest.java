package cl.induztek.pruebas.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import cl.induztek.pruebas.model.ProtocoloPrueba;
import cl.induztek.pruebas.service.ProtocoloPruebaService;

@ExtendWith(MockitoExtension.class)
class ProtocoloPruebaControllerTest {

    @Mock
    private ProtocoloPruebaService service;

    @InjectMocks
    private ProtocoloPruebaController controller;

    private ProtocoloPrueba prueba;

    @BeforeEach
    void setUp() {
        prueba = new ProtocoloPrueba();
        prueba.setIdProtocolo(1L);
        prueba.setCodigoEquipo("EQ-001");
        prueba.setTipoPrueba("Aislamiento");
        prueba.setValorMedicion("100 MOhm");
        prueba.setEstadoProtocolo("PENDIENTE_APROBACION");
    }

    @Test
    void listar_cubreTodosLosFiltros() {
        when(service.listarPorEstado("APROBADA")).thenReturn(List.of(prueba));
        when(service.listarPorEquipo(1L, null)).thenReturn(List.of(prueba));
        when(service.listarTodos()).thenReturn(List.of(prueba));

        assertEquals(HttpStatus.OK, controller.listar("APROBADA", null, null).getStatusCode());
        assertEquals(HttpStatus.OK, controller.listar(null, 1L, null).getStatusCode());
        assertEquals(HttpStatus.OK, controller.listar(null, null, null).getStatusCode());
    }

    @Test
    void obtenerPorId_existenteYNoExistente() {
        when(service.obtenerPorId(1L)).thenReturn(prueba);
        when(service.obtenerPorId(2L)).thenReturn(null);

        assertEquals(HttpStatus.OK, controller.obtenerPorId(1L).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.obtenerPorId(2L).getStatusCode());
    }

    @Test
    void crear_retornaCreated() {
        when(service.registrar(prueba)).thenReturn(prueba);

        ResponseEntity<ProtocoloPrueba> resp = controller.crear(prueba);

        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        assertEquals(1L, resp.getBody().getIdProtocolo());
    }

    @Test
    void actualizar_existenteYNoExistente() {
        when(service.actualizar(1L, prueba)).thenReturn(prueba);
        when(service.actualizar(2L, prueba)).thenReturn(null);

        assertEquals(HttpStatus.OK, controller.actualizar(1L, prueba).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.actualizar(2L, prueba).getStatusCode());
    }

    @Test
    void aprobar_existenteYNoExistente() {
        when(service.aprobar(1L, 9L)).thenReturn(prueba);
        when(service.aprobar(2L, 9L)).thenReturn(null);

        assertEquals(HttpStatus.OK, controller.aprobar(1L, 9L).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.aprobar(2L, 9L).getStatusCode());
    }

    @Test
    void rechazar_ok_notFound_yBadRequest() {
        when(service.rechazar(1L, 9L, "Sin sello")).thenReturn(prueba);
        when(service.rechazar(2L, 9L, "Sin sello")).thenReturn(null);
        when(service.rechazar(3L, 9L, null)).thenThrow(new IllegalArgumentException("El motivo de rechazo es obligatorio"));

        assertEquals(HttpStatus.OK, controller.rechazar(1L, 9L, Map.of("motivo", "Sin sello")).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.rechazar(2L, 9L, Map.of("motivo", "Sin sello")).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.rechazar(3L, 9L, null).getStatusCode());
    }
}