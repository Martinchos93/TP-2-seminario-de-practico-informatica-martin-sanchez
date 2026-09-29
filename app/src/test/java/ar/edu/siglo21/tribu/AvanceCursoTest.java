package ar.edu.siglo21.tribu;

import static org.assertj.core.api.Assertions.assertThat;

import ar.edu.siglo21.tribu.dto.AvanceCurso;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AvanceCursoTest {
    @Test
    @DisplayName("CP-05: 3 de 6 lecciones dan 50 por ciento y el curso no esta completo")
    void calculaPorcentajeParcial() {
        var avance = new AvanceCurso(6, 3, Set.of(1L, 2L, 3L));

        assertThat(avance.porcentaje()).isEqualTo(50);
        assertThat(avance.completo()).isFalse();
    }

    @Test
    @DisplayName("CP-06: todas las lecciones completas dan 100 por ciento")
    void calculaPorcentajeTotal() {
        var avance = new AvanceCurso(6, 6, Set.of(1L, 2L, 3L, 4L, 5L, 6L));

        assertThat(avance.porcentaje()).isEqualTo(100);
        assertThat(avance.completo()).isTrue();
    }

    @Test
    @DisplayName("CP-07: un curso sin lecciones da 0 por ciento y no se considera completo")
    void cursoSinLeccionesNoDivideporCero() {
        var avance = new AvanceCurso(0, 0, Set.of());

        assertThat(avance.porcentaje()).isZero();
        assertThat(avance.completo()).isFalse();
    }

    @Test
    @DisplayName("CP-08: el porcentaje se redondea al entero mas cercano")
    void redondeaPorcentaje() {
        assertThat(new AvanceCurso(3, 1, Set.of(1L)).porcentaje()).isEqualTo(33);

        assertThat(new AvanceCurso(3, 2, Set.of(1L, 2L)).porcentaje()).isEqualTo(67);
    }
}
