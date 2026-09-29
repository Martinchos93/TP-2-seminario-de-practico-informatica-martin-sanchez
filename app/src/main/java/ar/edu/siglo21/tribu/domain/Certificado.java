package ar.edu.siglo21.tribu.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "certificado")
public class Certificado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "inscripcion_id", nullable = false, unique = true)
    private Inscripcion inscripcion;

    @Column(nullable = false, unique = true, length = 40)
    private String codigo;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDateTime fechaEmision = LocalDateTime.now();

    protected Certificado() {
    }

    public Certificado(Inscripcion inscripcion, String codigo) {
        this.inscripcion = inscripcion;
        this.codigo = codigo;
    }

    public Long getId() {
        return id;
    }

    public Inscripcion getInscripcion() {
        return inscripcion;
    }

    public String getCodigo() {
        return codigo;
    }

    public LocalDateTime getFechaEmision() {
        return fechaEmision;
    }
}
