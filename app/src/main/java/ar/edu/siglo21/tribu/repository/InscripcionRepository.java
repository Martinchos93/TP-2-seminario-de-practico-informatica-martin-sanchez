package ar.edu.siglo21.tribu.repository;

import ar.edu.siglo21.tribu.domain.Inscripcion;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {
    Optional<Inscripcion> findByAlumnoIdAndCursoId(Long alumnoId, Long cursoId);

    boolean existsByAlumnoIdAndCursoId(Long alumnoId, Long cursoId);

    List<Inscripcion> findByAlumnoIdOrderByFechaAltaDesc(Long alumnoId);

    @Query("""
            SELECT i FROM Inscripcion i
            JOIN FETCH i.alumno
            WHERE i.curso.id = :cursoId
            ORDER BY i.fechaAlta DESC
            """)
    List<Inscripcion> findPorCursoConAlumno(@Param("cursoId") Long cursoId);
}
