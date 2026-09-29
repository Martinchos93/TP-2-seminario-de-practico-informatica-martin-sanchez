package ar.edu.siglo21.tribu.repository;

import ar.edu.siglo21.tribu.domain.Usuario;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("""
            SELECT DISTINCT u FROM Usuario u
            LEFT JOIN u.roles r
            WHERE (:texto IS NULL
                   OR LOWER(u.nombre)   LIKE LOWER(CONCAT('%', :texto, '%'))
                   OR LOWER(u.apellido) LIKE LOWER(CONCAT('%', :texto, '%'))
                   OR LOWER(u.email)    LIKE LOWER(CONCAT('%', :texto, '%')))
              AND (:rol IS NULL OR r.nombre = :rol)
            """)
    Page<Usuario> buscar(@Param("texto") String texto, @Param("rol") String rol, Pageable pageable);
}
