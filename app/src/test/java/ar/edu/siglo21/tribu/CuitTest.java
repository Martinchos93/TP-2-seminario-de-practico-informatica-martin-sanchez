package ar.edu.siglo21.tribu;

import static org.assertj.core.api.Assertions.assertThat;

import ar.edu.siglo21.tribu.service.Cuit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CuitTest {
    @Test
    @DisplayName("CP-23: una CUIT con digito verificador correcto es valida")
    void cuitValida() {
        assertThat(Cuit.esValida("30-71234567-1")).isTrue();
        assertThat(Cuit.esValida("30-71900000-9")).isTrue();
    }

    @Test
    @DisplayName("CP-24: se rechaza la CUIT con digito verificador erroneo o mal formada")
    void cuitInvalida() {
        assertThat(Cuit.esValida("30-71234567-9")).isFalse();
        assertThat(Cuit.esValida("30712345671")).isFalse();
        assertThat(Cuit.esValida("30-7123456-1")).isFalse();
        assertThat(Cuit.esValida(null)).isFalse();

        assertThat(Cuit.esValida("30-71900005-0")).isFalse();
    }
}
