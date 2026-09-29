package ar.edu.siglo21.tribu.repository;

import ar.edu.siglo21.tribu.domain.Certificado;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CertificadoRepository extends JpaRepository<Certificado, Long> {
    Optional<Certificado> findByInscripcionId(Long inscripcionId);

    Optional<Certificado> findByCodigo(String codigo);
}
