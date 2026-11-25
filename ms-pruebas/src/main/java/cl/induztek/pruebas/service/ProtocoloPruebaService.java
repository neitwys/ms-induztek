package cl.induztek.pruebas.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;

import cl.induztek.pruebas.model.ProtocoloPrueba;
import cl.induztek.pruebas.repository.ProtocoloPruebaRepository;

@Service
public class ProtocoloPruebaService {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ProtocoloPruebaRepository repository;

    public ProtocoloPruebaService(ProtocoloPruebaRepository repository) {
        this.repository = repository;
    }

    public List<ProtocoloPrueba> listarTodos() {
        return repository.findAll();
    }

    public List<ProtocoloPrueba> listarPorEstado(String estado) {
        return repository.findByEstadoProtocolo(estado);
    }

    public List<ProtocoloPrueba> listarPorEquipo(Long idEquipo, String codigoEquipo) {
        if (codigoEquipo != null && !codigoEquipo.isBlank()) {
            return repository.findByCodigoEquipo(codigoEquipo);
        }
        return repository.findByIdEquipo(idEquipo);
    }

    public ProtocoloPrueba obtenerPorId(Long id) {
        return repository.findById(id).orElse(null);
    }

    public ProtocoloPrueba registrar(ProtocoloPrueba protocolo) {
        protocolo.setIdProtocolo(null);
        if (protocolo.getEstadoProtocolo() == null || protocolo.getEstadoProtocolo().isBlank()) {
            protocolo.setEstadoProtocolo("PENDIENTE_APROBACION");
        }
        if (protocolo.getResultado() == null || protocolo.getResultado().isBlank()) {
            protocolo.setResultado("PENDIENTE");
        }
        if (protocolo.getFechaHora() == null || protocolo.getFechaHora().isBlank()) {
            protocolo.setFechaHora(LocalDateTime.now().format(FORMATO_FECHA));
        }
        return repository.save(protocolo);
    }

    public ProtocoloPrueba actualizar(Long id, ProtocoloPrueba datos) {
        return repository.findById(id).map(existente -> {
            existente.setIdEquipo(datos.getIdEquipo());
            existente.setCodigoEquipo(datos.getCodigoEquipo());
            existente.setTipoEquipo(datos.getTipoEquipo());
            existente.setIdUsuarioTecnico(datos.getIdUsuarioTecnico());
            existente.setNombreTecnico(datos.getNombreTecnico());
            existente.setFechaHora(datos.getFechaHora());
            existente.setTipoPrueba(datos.getTipoPrueba());
            existente.setValorMedicion(datos.getValorMedicion());
            existente.setResultado(datos.getResultado());
            existente.setEstadoProtocolo(datos.getEstadoProtocolo());
            existente.setObservaciones(datos.getObservaciones());
            existente.setFotoUri(datos.getFotoUri());
            existente.setLatitud(datos.getLatitud());
            existente.setLongitud(datos.getLongitud());
            existente.setIdUsuarioIngeniero(datos.getIdUsuarioIngeniero());
            existente.setMotivoRechazo(datos.getMotivoRechazo());
            return repository.save(existente);
        }).orElse(null);
    }

    public ProtocoloPrueba aprobar(Long id, Long idUsuarioIngeniero) {
        return repository.findById(id).map(existente -> {
            String fechaRevision = LocalDateTime.now().format(FORMATO_FECHA);
            existente.setEstadoProtocolo("APROBADA");
            existente.setResultado("APROBADO");
            existente.setIdUsuarioIngeniero(idUsuarioIngeniero);
            existente.setMotivoRechazo(null);
            existente.setObservaciones(agregarNota(
                    existente.getObservaciones(),
                    "[APROBADO por ingeniero #" + idUsuarioIngeniero + " (" + fechaRevision + ")]"
            ));
            return repository.save(existente);
        }).orElse(null);
    }

    public ProtocoloPrueba rechazar(Long id, Long idUsuarioIngeniero, String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("El motivo de rechazo es obligatorio");
        }
        return repository.findById(id).map(existente -> {
            String fechaRevision = LocalDateTime.now().format(FORMATO_FECHA);
            existente.setEstadoProtocolo("RECHAZADA");
            existente.setResultado("RECHAZADO");
            existente.setIdUsuarioIngeniero(idUsuarioIngeniero);
            existente.setMotivoRechazo(motivo.trim());
            existente.setObservaciones(agregarNota(
                    existente.getObservaciones(),
                    "[RECHAZADO por ingeniero #" + idUsuarioIngeniero + " (" + fechaRevision + ") - Motivo: " + motivo.trim() + "]"
            ));
            return repository.save(existente);
        }).orElse(null);
    }

    private String agregarNota(String original, String nota) {
        if (original == null || original.isBlank()) {
            return nota;
        }
        return original + "\n" + nota;
    }
}