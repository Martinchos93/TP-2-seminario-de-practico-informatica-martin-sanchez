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
import java.time.LocalDateTime;

@Entity
@Table(name = "inscripcion")
public class Inscripcion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "alumno_id", nullable = false)
    private Usuario alumno;

    @ManyToOne
    @JoinColumn(name = "curso_id", nullable = false)
    private Curso curso;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoInscripcion estado = EstadoInscripcion.PENDIENTE_PAGO;

    @Column(name = "fecha_alta", nullable = false)
    private LocalDateTime fechaAlta = LocalDateTime.now();

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    protected Inscripcion() {
    }

    public Inscripcion(Usuario alumno, Curso curso) {
        this.alumno = alumno;
        this.curso = curso;

        this.estado = curso.esGratuito() ? EstadoInscripcion.ACTIVA : EstadoInscripcion.PENDIENTE_PAGO;
    }

    public Long getId() {
        return id;
    }

    public Usuario getAlumno() {
        return alumno;
    }

    public Curso getCurso() {
        return curso;
    }

    public EstadoInscripcion getEstado() {
        return estado;
    }

    public LocalDateTime getFechaAlta() {
        return fechaAlta;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public void activar() {
        this.estado = EstadoInscripcion.ACTIVA;
    }

    public void completar() {
        this.estado = EstadoInscripcion.COMPLETADA;
        this.fechaFin = LocalDateTime.now();
    }

    public void cancelar() {
        this.estado = EstadoInscripcion.CANCELADA;
        this.fechaFin = LocalDateTime.now();
    }

    public boolean permiteCursar() {
        return estado == EstadoInscripcion.ACTIVA || estado == EstadoInscripcion.COMPLETADA;
    }
}
