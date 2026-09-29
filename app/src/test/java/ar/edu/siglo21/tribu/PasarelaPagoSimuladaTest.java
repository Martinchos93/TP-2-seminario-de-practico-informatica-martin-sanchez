package ar.edu.siglo21.tribu;

import static org.assertj.core.api.Assertions.assertThat;

import ar.edu.siglo21.tribu.service.PasarelaPago;
import ar.edu.siglo21.tribu.service.PasarelaPagoSimulada;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PasarelaPagoSimuladaTest {
    private final PasarelaPago pasarela = new PasarelaPagoSimulada();

    @Test
    @DisplayName("CP-01: una tarjeta valida se aprueba y devuelve referencia externa")
    void apruebaTarjetaValida() {
        var respuesta = pasarela.autorizar("clave-aprobada-1", new BigDecimal("45000.00"), "ARS", "4509953566233704");

        assertThat(respuesta.aprobado()).isTrue();
        assertThat(respuesta.referenciaExterna()).startsWith("PSP-");
        assertThat(respuesta.motivoRechazo()).isNull();
    }

    @Test
    @DisplayName("CP-02: una tarjeta terminada en 0000 se rechaza por fondos insuficientes")
    void rechazaPorFondos() {
        var respuesta = pasarela.autorizar("clave-rechazo-1", new BigDecimal("45000.00"), "ARS", "4509953566230000");

        assertThat(respuesta.aprobado()).isFalse();
        assertThat(respuesta.motivoRechazo()).isEqualTo("Fondos insuficientes");
    }

    @Test
    @DisplayName("CP-03: una tarjeta terminada en 1111 se rechaza por vencimiento")
    void rechazaPorVencimiento() {
        var respuesta = pasarela.autorizar("clave-rechazo-2", new BigDecimal("45000.00"), "ARS", "4509953566231111");

        assertThat(respuesta.aprobado()).isFalse();
        assertThat(respuesta.motivoRechazo()).isEqualTo("Tarjeta vencida");
    }

    @Test
    @DisplayName("CP-04: un numero con menos de 13 digitos se rechaza sin llegar al cobro")
    void rechazaNumeroInvalido() {
        var respuesta = pasarela.autorizar("clave-invalida", new BigDecimal("45000.00"), "ARS", "1234");

        assertThat(respuesta.aprobado()).isFalse();
        assertThat(respuesta.motivoRechazo()).isEqualTo("Numero de tarjeta invalido");
    }
}
