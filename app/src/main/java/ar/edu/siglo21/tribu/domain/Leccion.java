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

@Entity
@Table(name = "leccion")
public class Leccion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "modulo_id", nullable = false)
    private Modulo modulo;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false)
    private Integer orden;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_contenido", nullable = false, length = 10)
    private TipoContenido tipoContenido = TipoContenido.VIDEO;

    @Column(name = "url_contenido", length = 500)
    private String urlContenido;

    @Column(columnDefinition = "TEXT")
    private String contenido;

    @Column(name = "duracion_min", nullable = false)
    private Integer duracionMin = 0;

    protected Leccion() {
    }

    Leccion(Modulo modulo, String titulo, int orden, TipoContenido tipoContenido) {
        this.modulo = modulo;
        this.titulo = titulo;
        this.orden = orden;
        this.tipoContenido = tipoContenido;
    }

    public Long getId() {
        return id;
    }

    public Modulo getModulo() {
        return modulo;
    }

    public String getTitulo() {
        return titulo;
    }

    public Integer getOrden() {
        return orden;
    }

    public TipoContenido getTipoContenido() {
        return tipoContenido;
    }

    public String getUrlContenido() {
        return urlContenido;
    }

    public String getContenido() {
        return contenido;
    }

    public Integer getDuracionMin() {
        return duracionMin;
    }
}
