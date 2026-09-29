package ar.edu.siglo21.tribu.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pago")
public class Pago {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "inscripcion_id", nullable = false)
    private Inscripcion inscripcion;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(nullable = false, length = 3)
    private String moneda = "ARS";

    @Enumerated(EnumType.STRING)
    @Column(name = "medio_pago", nullable = false, length = 20)
    private MedioPago medioPago;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EstadoPago estado = EstadoPago.INICIADO;

    @Column(name = "clave_idempotencia", nullable = false, unique = true, length = 64)
    private String claveIdempotencia;

    @Column(name = "referencia_externa", length = 64)
    private String referenciaExterna;

    @Column(name = "detalle_rechazo", length = 200)
    private String detalleRechazo;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud = LocalDateTime.now();

    @Column(name = "fecha_confirmacion")
    private LocalDateTime fechaConfirmacion;

    protected Pago() {
    }

    public Pago(Inscripcion inscripcion, BigDecimal monto, MedioPago medioPago, String claveIdempotencia) {
        this.inscripcion = inscripcion;
        this.monto = monto;
        this.moneda = inscripcion.getCurso().getMoneda();
        this.medioPago = medioPago;
        this.claveIdempotencia = claveIdempotencia;
    }

    public Long getId() {
        return id;
    }

    public Inscripcion getInscripcion() {
        return inscripcion;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public String getMoneda() {
        return moneda;
    }

    public MedioPago getMedioPago() {
        return medioPago;
    }

    public EstadoPago getEstado() {
        return estado;
    }

    public String getClaveIdempotencia() {
        return claveIdempotencia;
    }

    public String getReferenciaExterna() {
        return referenciaExterna;
    }

    public String getDetalleRechazo() {
        return detalleRechazo;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public LocalDateTime getFechaConfirmacion() {
        return fechaConfirmacion;
    }

    public void aprobar(String referenciaExterna) {
        this.estado = EstadoPago.APROBADO;
        this.referenciaExterna = referenciaExterna;
        this.fechaConfirmacion = LocalDateTime.now();
    }

    public void rechazar(String referenciaExterna, String motivo) {
        this.estado = EstadoPago.RECHAZADO;
        this.referenciaExterna = referenciaExterna;
        this.detalleRechazo = motivo;
        this.fechaConfirmacion = LocalDateTime.now();
    }
}
