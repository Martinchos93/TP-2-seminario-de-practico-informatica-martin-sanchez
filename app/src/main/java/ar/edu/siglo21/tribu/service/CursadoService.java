package ar.edu.siglo21.tribu.service;

import ar.edu.siglo21.tribu.domain.Certificado;
import ar.edu.siglo21.tribu.domain.EstadoInscripcion;
import ar.edu.siglo21.tribu.domain.Inscripcion;
import ar.edu.siglo21.tribu.domain.Leccion;
import ar.edu.siglo21.tribu.domain.ProgresoLeccion;
import ar.edu.siglo21.tribu.dto.AvanceCurso;
import ar.edu.siglo21.tribu.repository.CertificadoRepository;
import ar.edu.siglo21.tribu.repository.InscripcionRepository;
import ar.edu.siglo21.tribu.repository.LeccionRepository;
import ar.edu.siglo21.tribu.repository.ProgresoLeccionRepository;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CursadoService {
    private final InscripcionRepository inscripcionRepository;
    private final LeccionRepository leccionRepository;
    private final ProgresoLeccionRepository progresoRepository;
    private final CertificadoRepository certificadoRepository;

    public CursadoService(InscripcionRepository inscripcionRepository,
                          LeccionRepository leccionRepository,
                          ProgresoLeccionRepository progresoRepository,
                          CertificadoRepository certificadoRepository) {
        this.inscripcionRepository = inscripcionRepository;
        this.leccionRepository = leccionRepository;
        this.progresoRepository = progresoRepository;
        this.certificadoRepository = certificadoRepository;
    }

    @Transactional(readOnly = true)
    public Inscripcion inscripcionHabilitada(Long alumnoId, Long cursoId) {
        Inscripcion inscripcion = inscripcionRepository.findByAlumnoIdAndCursoId(alumnoId, cursoId)
                .orElseThrow(() -> new ReglaNegocioException("No estas inscripto en este curso"));

        if (!inscripcion.permiteCursar()) {
            throw new ReglaNegocioException(
                    "Para acceder al contenido primero tenes que completar el pago de la inscripcion");
        }
        return inscripcion;
    }

    @Transactional(readOnly = true)
    public AvanceCurso avance(Inscripcion inscripcion) {
        int totales = (int) leccionRepository.contarPorCurso(inscripcion.getCurso().getId());
        Set<Long> hechas = progresoRepository.findByInscripcionId(inscripcion.getId()).stream()
                .map(p -> p.getLeccion().getId())
                .collect(Collectors.toSet());
        return new AvanceCurso(totales, hechas.size(), hechas);
    }

    @Transactional
    public AvanceCurso completarLeccion(Long alumnoId, Long cursoId, Long leccionId) {
        Inscripcion inscripcion = inscripcionHabilitada(alumnoId, cursoId);

        Leccion leccion = leccionRepository.findById(leccionId)
                .orElseThrow(() -> new ReglaNegocioException("No existe la leccion"));

        if (!leccion.getModulo().getCurso().getId().equals(cursoId)) {
            throw new ReglaNegocioException("La leccion no pertenece a este curso");
        }

        if (!progresoRepository.existsByInscripcionIdAndLeccionId(inscripcion.getId(), leccionId)) {
            progresoRepository.save(new ProgresoLeccion(inscripcion, leccion));
        }

        AvanceCurso avance = avance(inscripcion);
        if (avance.completo() && inscripcion.getEstado() != EstadoInscripcion.COMPLETADA) {
            inscripcion.completar();
            inscripcionRepository.save(inscripcion);
            emitirCertificado(inscripcion);
        }
        return avance;
    }

    @Transactional(readOnly = true)
    public Optional<Certificado> certificadoDe(Long inscripcionId) {
        return certificadoRepository.findByInscripcionId(inscripcionId);
    }

    private void emitirCertificado(Inscripcion inscripcion) {
        if (certificadoRepository.findByInscripcionId(inscripcion.getId()).isPresent()) {
            return;
        }
        String codigo = "TRIBU-%d-%06d".formatted(
                inscripcion.getFechaFin().getYear(),
                inscripcion.getId());
        certificadoRepository.save(new Certificado(inscripcion, codigo));
    }
}
