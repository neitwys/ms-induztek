package cl.induztek.pruebas.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cl.induztek.pruebas.model.ProtocoloPrueba;
import cl.induztek.pruebas.repository.ProtocoloPruebaRepository;

@ExtendWith(MockitoExtension.class)
class ProtocoloPruebaServiceTest {

    @Mock
    private ProtocoloPruebaRepository repository;

    @InjectMocks
    private ProtocoloPruebaService service;

    private ProtocoloPrueba base;

    @BeforeEach
    void setUp() {
        base = new ProtocoloPrueba();
        base.setIdProtocolo(10L);
        base.setIdEquipo(5L);
        base.setCodigoEquipo("EQ-005");
        base.setTipoEquipo("Transformador");
        base.setIdUsuarioTecnico(2L);
        base.setNombreTecnico("Juan Pérez");
        base.setFechaHora("2026-11-24 10:00:00");
        base.setTipoPrueba("Resistencia de aislamiento");
        base.setValorMedicion("500 MOhm");
        base.setResultado("PENDIENTE");
        base.setEstadoProtocolo("PENDIENTE_APROBACION");
        base.setObservaciones("Sin novedad");
        base.setFotoUri("content://media/foto1.jpg");
        base.setLatitud(-33.35);
        base.setLongitud(-70.73);
    }

    @Test
    void listarTodos_retornaLista() {
        when(repository.findAll()).thenReturn(List.of(base));

        List<ProtocoloPrueba> resultado = service.listarTodos();

        assertEquals(1, resultado.size());
        verify(repository).findAll();
    }

    @Test
    void listarPorEstado_filtraPorEstado() {
        when(repository.findByEstadoProtocolo("APROBADA")).thenReturn(List.of(base));

        List<ProtocoloPrueba> resultado = service.listarPorEstado("APROBADA");

        assertEquals(1, resultado.size());
        verify(repository).findByEstadoProtocolo("APROBADA");
    }

    @Test
    void listarPorEquipo_conCodigoEquipo_buscaPorCodigo() {
        when(repository.findByCodigoEquipo("EQ-005")).thenReturn(List.of(base));

        List<ProtocoloPrueba> resultado = service.listarPorEquipo(5L, "EQ-005");

        assertEquals(1, resultado.size());
        verify(repository).findByCodigoEquipo("EQ-005");
        verify(repository, never()).findByIdEquipo(any());
    }

    @Test
    void listarPorEquipo_sinCodigoEquipo_buscaPorIdEquipo() {
        when(repository.findByIdEquipo(5L)).thenReturn(List.of(base));

        List<ProtocoloPrueba> resultado = service.listarPorEquipo(5L, "   ");

        assertEquals(1, resultado.size());
        verify(repository).findByIdEquipo(5L);
    }

    @Test
    void obtenerPorId_existenteYNoExistente() {
        when(repository.findById(10L)).thenReturn(Optional.of(base));
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertEquals(10L, service.obtenerPorId(10L).getIdProtocolo());
        assertNull(service.obtenerPorId(99L));
    }

    @Test
    void registrar_asignaValoresPorDefectoCuandoFaltan() {
        ProtocoloPrueba nuevo = new ProtocoloPrueba();
        nuevo.setIdProtocolo(999L);
        nuevo.setTipoPrueba("Prueba dieléctrica");
        nuevo.setValorMedicion("2.5 kV");

        when(repository.save(any(ProtocoloPrueba.class))).thenAnswer(inv -> inv.getArgument(0));

        ProtocoloPrueba guardado = service.registrar(nuevo);

        assertNull(guardado.getIdProtocolo());
        assertEquals("PENDIENTE_APROBACION", guardado.getEstadoProtocolo());
        assertEquals("PENDIENTE", guardado.getResultado());
        assertNotNull(guardado.getFechaHora());
    }

    @Test
    void registrar_conservaValoresEnviados() {
        when(repository.save(any(ProtocoloPrueba.class))).thenAnswer(inv -> inv.getArgument(0));

        ProtocoloPrueba guardado = service.registrar(base);

        assertEquals("PENDIENTE_APROBACION", guardado.getEstadoProtocolo());
        assertEquals("PENDIENTE", guardado.getResultado());
        assertEquals("2026-11-24 10:00:00", guardado.getFechaHora());
    }

    @Test
    void actualizar_existenteYNoExistente() {
        ProtocoloPrueba nuevosDatos = new ProtocoloPrueba();
        nuevosDatos.setIdEquipo(8L);
        nuevosDatos.setCodigoEquipo("EQ-008");
        nuevosDatos.setTipoEquipo("Motor");
        nuevosDatos.setIdUsuarioTecnico(3L);
        nuevosDatos.setNombreTecnico("Ana Soto");
        nuevosDatos.setFechaHora("2026-11-24 12:00:00");
        nuevosDatos.setTipoPrueba("Continuidad");
        nuevosDatos.setValorMedicion("0.2 Ohm");
        nuevosDatos.setResultado("APROBADO");
        nuevosDatos.setEstadoProtocolo("APROBADA");
        nuevosDatos.setObservaciones("OK");
        nuevosDatos.setFotoUri("content://media/foto2.jpg");
        nuevosDatos.setLatitud(-33.40);
        nuevosDatos.setLongitud(-70.65);
        nuevosDatos.setIdUsuarioIngeniero(7L);
        nuevosDatos.setMotivoRechazo(null);

        when(repository.findById(10L)).thenReturn(Optional.of(base));
        when(repository.save(any(ProtocoloPrueba.class))).thenAnswer(inv -> inv.getArgument(0));
        when(repository.findById(99L)).thenReturn(Optional.empty());

        ProtocoloPrueba actualizado = service.actualizar(10L, nuevosDatos);

        assertEquals("EQ-008", actualizado.getCodigoEquipo());
        assertEquals("0.2 Ohm", actualizado.getValorMedicion());
        assertNull(service.actualizar(99L, nuevosDatos));
    }

    @Test
    void aprobar_conObservacionesPreviasYSinObservaciones() {
        when(repository.findById(10L)).thenReturn(Optional.of(base));
        when(repository.save(any(ProtocoloPrueba.class))).thenAnswer(inv -> inv.getArgument(0));

        ProtocoloPrueba aprobada = service.aprobar(10L, 4L);

        assertEquals("APROBADA", aprobada.getEstadoProtocolo());
        assertEquals("APROBADO", aprobada.getResultado());
        assertEquals(4L, aprobada.getIdUsuarioIngeniero());
        assertNull(aprobada.getMotivoRechazo());
        assertTrue(aprobada.getObservaciones().contains("[APROBADO por ingeniero #4"));

        base.setObservaciones(null);
        ProtocoloPrueba aprobadaSinObs = service.aprobar(10L, 4L);
        assertTrue(aprobadaSinObs.getObservaciones().startsWith("[APROBADO por ingeniero #4"));

        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertNull(service.aprobar(99L, 4L));
    }

    @Test
    void rechazar_validaMotivoYActualizaEstado() {
        assertThrows(IllegalArgumentException.class, () -> service.rechazar(10L, 4L, "  "));
        assertThrows(IllegalArgumentException.class, () -> service.rechazar(10L, 4L, null));

        when(repository.findById(10L)).thenReturn(Optional.of(base));
        when(repository.save(any(ProtocoloPrueba.class))).thenAnswer(inv -> inv.getArgument(0));

        ProtocoloPrueba rechazada = service.rechazar(10L, 4L, "  Medición fuera de rango  ");

        assertEquals("RECHAZADA", rechazada.getEstadoProtocolo());
        assertEquals("RECHAZADO", rechazada.getResultado());
        assertEquals("Medición fuera de rango", rechazada.getMotivoRechazo());
        assertTrue(rechazada.getObservaciones().contains("Motivo: Medición fuera de rango"));

        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertNull(service.rechazar(99L, 4L, "Falla"));
    }
}