package ar.edu.siglo21.tribu.service;

import ar.edu.siglo21.tribu.domain.Curso;
import ar.edu.siglo21.tribu.domain.EstadoCurso;
import ar.edu.siglo21.tribu.domain.EstadoInscripcion;
import ar.edu.siglo21.tribu.domain.EstadoPago;
import ar.edu.siglo21.tribu.domain.Inscripcion;
import ar.edu.siglo21.tribu.domain.MedioPago;
import ar.edu.siglo21.tribu.domain.Pago;
import ar.edu.siglo21.tribu.domain.Usuario;
import ar.edu.siglo21.tribu.repository.CursoRepository;
import ar.edu.siglo21.tribu.repository.InscripcionRepository;
import ar.edu.siglo21.tribu.repository.PagoRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InscripcionService {
    private final InscripcionRepository inscripcionRepository;
    private final CursoRepository cursoRepository;
    private final PagoRepository pagoRepository;
    private final PasarelaPago pasarelaPago;

    public InscripcionService(InscripcionRepository inscripcionRepository,
                              CursoRepository cursoRepository,
                              PagoRepository pagoRepository,
                              PasarelaPago pasarelaPago) {
        this.inscripcionRepository = inscripcionRepository;
        this.cursoRepository = cursoRepository;
        this.pagoRepository = pagoRepository;
        this.pasarelaPago = pasarelaPago;
    }

    @Transactional
    public Inscripcion inscribir(Usuario alumno, Long cursoId) {
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new ReglaNegocioException("No existe el curso solicitado"));

        if (curso.getEstado() != EstadoCurso.PUBLICADO) {
            throw new ReglaNegocioException("El curso no esta disponible para inscripcion");
        }
        if (inscripcionRepository.existsByAlumnoIdAndCursoId(alumno.getId(), cursoId)) {
            throw new ReglaNegocioException("Ya estas inscripto en este curso");
        }

        return inscripcionRepository.save(new Inscripcion(alumno, curso));
    }

    @Transactional
    public Pago pagar(Long inscripcionId, MedioPago medioPago, String numeroTarjeta) {
        Inscripcion inscripcion = inscripcionRepository.findById(inscripcionId)
                .orElseThrow(() -> new ReglaNegocioException("No existe la inscripcion"));

        if (inscripcion.getEstado() != EstadoInscripcion.PENDIENTE_PAGO) {
            throw new ReglaNegocioException("La inscripcion no esta pendiente de pago");
        }

        if (pagoRepository.existsByInscripcionIdAndEstado(inscripcionId, EstadoPago.APROBADO)) {
            throw new ReglaNegocioException("Esta inscripcion ya tiene un pago aprobado");
        }

        String claveIdempotencia = UUID.randomUUID().toString();
        Pago pago = new Pago(inscripcion, inscripcion.getCurso().getPrecio(), medioPago, claveIdempotencia);

        PasarelaPago.RespuestaAutorizacion respuesta = pasarelaPago.autorizar(
                claveIdempotencia,
                pago.getMonto(),
                pago.getMoneda(),
                numeroTarjeta);

        if (respuesta.aprobado()) {
            pago.aprobar(respuesta.referenciaExterna());
            inscripcion.activar();
            inscripcionRepository.save(inscripcion);
        } else {
            pago.rechazar(respuesta.referenciaExterna(), respuesta.motivoRechazo());
        }

        return pagoRepository.save(pago);
    }

    @Transactional(readOnly = true)
    public List<Inscripcion> misInscripciones(Long alumnoId) {
        return inscripcionRepository.findByAlumnoIdOrderByFechaAltaDesc(alumnoId);
    }

    @Transactional(readOnly = true)
    public Inscripcion porId(Long id) {
        return inscripcionRepository.findById(id)
                .orElseThrow(() -> new ReglaNegocioException("No existe la inscripcion"));
    }

    @Transactional(readOnly = true)
    public List<Inscripcion> inscriptosDelCurso(Long cursoId) {
        return inscripcionRepository.findPorCursoConAlumno(cursoId);
    }

    @Transactional(readOnly = true)
    public List<Pago> pagosDe(Long inscripcionId) {
        return pagoRepository.findByInscripcionIdOrderByFechaSolicitudDesc(inscripcionId);
    }
}
