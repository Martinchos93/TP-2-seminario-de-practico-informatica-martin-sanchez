package ar.edu.siglo21.tribu.service;

import java.math.BigDecimal;

public interface PasarelaPago {
    RespuestaAutorizacion autorizar(String claveIdempotencia,
                                    BigDecimal monto,
                                    String moneda,
                                    String numeroTarjeta);

    record RespuestaAutorizacion(boolean aprobado, String referenciaExterna, String motivoRechazo) {
        public static RespuestaAutorizacion aprobada(String referencia) {
            return new RespuestaAutorizacion(true, referencia, null);
        }

        public static RespuestaAutorizacion rechazada(String referencia, String motivo) {
            return new RespuestaAutorizacion(false, referencia, motivo);
        }
    }
}
