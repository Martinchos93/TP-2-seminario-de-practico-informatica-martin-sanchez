package ar.edu.siglo21.tribu.dto;

import ar.edu.siglo21.tribu.domain.EstadoCurso;
import java.math.BigDecimal;

public record CursoDeAcademia(
        Long id,
        String titulo,
        String categoria,
        BigDecimal precio,
        String moneda,
        EstadoCurso estado,
        long cantidadLecciones) {
    public boolean esGratuito() {
        return precio == null || precio.compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean publicado() {
        return estado == EstadoCurso.PUBLICADO;
    }
}
