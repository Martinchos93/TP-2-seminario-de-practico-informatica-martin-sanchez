package ar.edu.siglo21.tribu.repository;

import ar.edu.siglo21.tribu.domain.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {
}
