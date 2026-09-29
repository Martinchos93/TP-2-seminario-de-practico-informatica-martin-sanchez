package ar.edu.siglo21.tribu.repository;

import ar.edu.siglo21.tribu.domain.ProgresoLeccion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgresoLeccionRepository extends JpaRepository<ProgresoLeccion, Long> {
    boolean existsByInscripcionIdAndLeccionId(Long inscripcionId, Long leccionId);

    List<ProgresoLeccion> findByInscripcionId(Long inscripcionId);

    long countByInscripcionId(Long inscripcionId);
}
