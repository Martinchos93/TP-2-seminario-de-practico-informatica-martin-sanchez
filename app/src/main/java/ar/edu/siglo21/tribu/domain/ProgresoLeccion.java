package ar.edu.siglo21.tribu.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "progreso_leccion")
public class ProgresoLeccion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "inscripcion_id", nullable = false)
    private Inscripcion inscripcion;

    @ManyToOne
    @JoinColumn(name = "leccion_id", nullable = false)
    private Leccion leccion;

    @Column(name = "fecha_completada", nullable = false)
    private LocalDateTime fechaCompletada = LocalDateTime.now();

    protected ProgresoLeccion() {
    }

    public ProgresoLeccion(Inscripcion inscripcion, Leccion leccion) {
        this.inscripcion = inscripcion;
        this.leccion = leccion;
    }

    public Long getId() {
        return id;
    }

    public Inscripcion getInscripcion() {
        return inscripcion;
    }

    public Leccion getLeccion() {
        return leccion;
    }

    public LocalDateTime getFechaCompletada() {
        return fechaCompletada;
    }
}
