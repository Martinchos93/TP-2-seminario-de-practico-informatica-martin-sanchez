package ar.edu.siglo21.tribu.repository;

import ar.edu.siglo21.tribu.domain.EstadoPago;
import ar.edu.siglo21.tribu.domain.Pago;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PagoRepository extends JpaRepository<Pago, Long> {
    Optional<Pago> findByClaveIdempotencia(String claveIdempotencia);

    boolean existsByInscripcionIdAndEstado(Long inscripcionId, EstadoPago estado);

    List<Pago> findByInscripcionIdOrderByFechaSolicitudDesc(Long inscripcionId);
}
