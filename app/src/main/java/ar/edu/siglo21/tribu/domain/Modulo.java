package ar.edu.siglo21.tribu.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "modulo")
public class Modulo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "curso_id", nullable = false)
    private Curso curso;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false)
    private Integer orden;

    @OneToMany(mappedBy = "modulo", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden ASC")
    private Set<Leccion> lecciones = new LinkedHashSet<>();

    protected Modulo() {
    }

    Modulo(Curso curso, String titulo, int orden) {
        this.curso = curso;
        this.titulo = titulo;
        this.orden = orden;
    }

    public Leccion agregarLeccion(String titulo) {
        Leccion leccion = new Leccion(this, titulo, lecciones.size() + 1, TipoContenido.TEXTO);
        lecciones.add(leccion);
        return leccion;
    }

    public Long getId() {
        return id;
    }

    public Curso getCurso() {
        return curso;
    }

    public String getTitulo() {
        return titulo;
    }

    public Integer getOrden() {
        return orden;
    }

    public Set<Leccion> getLecciones() {
        return lecciones;
    }
}
