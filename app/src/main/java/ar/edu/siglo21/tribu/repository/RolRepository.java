package ar.edu.siglo21.tribu.repository;

import ar.edu.siglo21.tribu.domain.Rol;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolRepository extends JpaRepository<Rol, Integer> {
    Optional<Rol> findByNombre(String nombre);
}
