package ar.edu.siglo21.tribu.repository;

import ar.edu.siglo21.tribu.domain.Academia;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AcademiaRepository extends JpaRepository<Academia, Long> {
    Optional<Academia> findByUsuarioId(Long usuarioId);

    boolean existsByCuit(String cuit);
}
