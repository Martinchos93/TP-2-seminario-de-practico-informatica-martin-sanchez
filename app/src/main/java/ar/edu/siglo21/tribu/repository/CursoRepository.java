package ar.edu.siglo21.tribu.repository;

import ar.edu.siglo21.tribu.domain.Curso;
import ar.edu.siglo21.tribu.domain.EstadoCurso;
import ar.edu.siglo21.tribu.dto.CursoDeAcademia;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CursoRepository extends JpaRepository<Curso, Long> {
    List<Curso> findByEstadoOrderByFechaPublicacionDesc(EstadoCurso estado);

    List<Curso> findByAcademiaIdOrderByFechaCreacionDesc(Long academiaId);

    boolean existsByAcademiaIdAndTituloIgnoreCase(Long academiaId, String titulo);

    @Query("""
            SELECT new ar.edu.siglo21.tribu.dto.CursoDeAcademia(
                       c.id, c.titulo, cat.nombre, c.precio, c.moneda, c.estado, COUNT(l.id))
            FROM Curso c
            JOIN c.categoria cat
            LEFT JOIN c.modulos m
            LEFT JOIN m.lecciones l
            WHERE c.academia.id = :academiaId
            GROUP BY c.id, c.titulo, cat.nombre, c.precio, c.moneda, c.estado, c.fechaCreacion
            ORDER BY c.fechaCreacion DESC
            """)
    List<CursoDeAcademia> resumenPorAcademia(@Param("academiaId") Long academiaId);

    @Query("""
            SELECT c FROM Curso c
            WHERE c.estado = ar.edu.siglo21.tribu.domain.EstadoCurso.PUBLICADO
              AND (:categoriaId IS NULL OR c.categoria.id = :categoriaId)
              AND (:texto IS NULL OR LOWER(c.titulo) LIKE LOWER(CONCAT('%', :texto, '%')))
            ORDER BY c.fechaPublicacion DESC
            """)
    List<Curso> buscarPublicados(@Param("categoriaId") Integer categoriaId, @Param("texto") String texto);

    @Query("""
            SELECT DISTINCT c FROM Curso c
            LEFT JOIN FETCH c.modulos m
            LEFT JOIN FETCH m.lecciones
            WHERE c.id = :id
            """)
    Optional<Curso> findConPlanDeEstudio(@Param("id") Long id);
}
