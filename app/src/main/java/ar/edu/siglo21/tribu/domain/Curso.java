package ar.edu.siglo21.tribu.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "curso")
public class Curso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "academia_id", nullable = false)
    private Academia academia;

    @ManyToOne
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(nullable = false, length = 3)
    private String moneda = "ARS";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCurso estado = EstadoCurso.BORRADOR;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fecha_publicacion")
    private LocalDateTime fechaPublicacion;

    @OneToMany(mappedBy = "curso", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden ASC")
    private Set<Modulo> modulos = new LinkedHashSet<>();

    protected Curso() {
    }

    public Curso(Academia academia, Categoria categoria, String titulo, String descripcion, BigDecimal precio) {
        this.academia = academia;
        this.categoria = categoria;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.precio = precio;
    }

    public Long getId() {
        return id;
    }

    public Academia getAcademia() {
        return academia;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public String getMoneda() {
        return moneda;
    }

    public EstadoCurso getEstado() {
        return estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaPublicacion() {
        return fechaPublicacion;
    }

    public Set<Modulo> getModulos() {
        return modulos;
    }

    public Modulo agregarModulo(String titulo) {
        Modulo modulo = new Modulo(this, titulo, modulos.size() + 1);
        modulos.add(modulo);
        return modulo;
    }

    public void publicar() {
        this.estado = EstadoCurso.PUBLICADO;
        this.fechaPublicacion = LocalDateTime.now();
    }

    public void archivar() {
        this.estado = EstadoCurso.ARCHIVADO;
    }

    public int cantidadLecciones() {
        return modulos.stream().mapToInt(m -> m.getLecciones().size()).sum();
    }

    public boolean esGratuito() {
        return precio == null || precio.compareTo(BigDecimal.ZERO) == 0;
    }
}
