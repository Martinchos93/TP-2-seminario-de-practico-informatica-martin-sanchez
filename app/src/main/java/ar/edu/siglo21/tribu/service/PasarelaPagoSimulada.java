package ar.edu.siglo21.tribu.service;

import java.math.BigDecimal;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PasarelaPagoSimulada implements PasarelaPago {
    private static final Logger log = LoggerFactory.getLogger(PasarelaPagoSimulada.class);

    @Override
    public RespuestaAutorizacion autorizar(String claveIdempotencia,
                                           BigDecimal monto,
                                           String moneda,
                                           String numeroTarjeta) {
        String digitos = numeroTarjeta == null ? "" : numeroTarjeta.replaceAll("\\D", "");
        String referencia = "PSP-" + claveIdempotencia.toUpperCase(Locale.ROOT).substring(0, Math.min(12, claveIdempotencia.length()));

        if (digitos.length() < 13 || digitos.length() > 19) {
            log.info("Autorizacion rechazada [{}]: numero de tarjeta invalido", claveIdempotencia);
            return RespuestaAutorizacion.rechazada(referencia, "Numero de tarjeta invalido");
        }

        String ultimos = digitos.substring(digitos.length() - 4);
        if ("0000".equals(ultimos)) {
            log.info("Autorizacion rechazada [{}] tarjeta ****{}: fondos insuficientes", claveIdempotencia, ultimos);
            return RespuestaAutorizacion.rechazada(referencia, "Fondos insuficientes");
        }
        if ("1111".equals(ultimos)) {
            log.info("Autorizacion rechazada [{}] tarjeta ****{}: tarjeta vencida", claveIdempotencia, ultimos);
            return RespuestaAutorizacion.rechazada(referencia, "Tarjeta vencida");
        }

        log.info("Autorizacion aprobada [{}] tarjeta ****{} por {} {}", claveIdempotencia, ultimos, monto, moneda);
        return RespuestaAutorizacion.aprobada(referencia);
    }
}
