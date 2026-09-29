package ar.edu.siglo21.tribu.repository;

import ar.edu.siglo21.tribu.domain.Leccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LeccionRepository extends JpaRepository<Leccion, Long> {
    @Query("SELECT COUNT(l) FROM Leccion l WHERE l.modulo.curso.id = :cursoId")
    long contarPorCurso(@Param("cursoId") Long cursoId);
}
