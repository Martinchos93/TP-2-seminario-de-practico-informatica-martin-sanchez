package ar.edu.siglo21.tribu.service;

import ar.edu.siglo21.tribu.domain.Academia;
import ar.edu.siglo21.tribu.domain.Categoria;
import ar.edu.siglo21.tribu.domain.Curso;
import ar.edu.siglo21.tribu.dto.CursoDeAcademia;
import ar.edu.siglo21.tribu.repository.AcademiaRepository;
import ar.edu.siglo21.tribu.repository.CategoriaRepository;
import ar.edu.siglo21.tribu.repository.CursoRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class CursoService {
    private final CursoRepository cursoRepository;
    private final CategoriaRepository categoriaRepository;
    private final AcademiaRepository academiaRepository;

    public CursoService(CursoRepository cursoRepository,
                        CategoriaRepository categoriaRepository,
                        AcademiaRepository academiaRepository) {
        this.cursoRepository = cursoRepository;
        this.categoriaRepository = categoriaRepository;
        this.academiaRepository = academiaRepository;
    }

    @Transactional(readOnly = true)
    public List<Curso> catalogo(Integer categoriaId, String texto) {
        return cursoRepository.buscarPublicados(
                categoriaId,
                StringUtils.hasText(texto) ? texto.trim() : null);
    }

    @Transactional(readOnly = true)
    public List<Categoria> categorias() {
        return categoriaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Curso porId(Long id) {
        return cursoRepository.findById(id)
                .orElseThrow(() -> new ReglaNegocioException("No existe el curso solicitado"));
    }

    @Transactional(readOnly = true)
    public Curso conPlanDeEstudio(Long id) {
        return cursoRepository.findConPlanDeEstudio(id)
                .orElseThrow(() -> new ReglaNegocioException("No existe el curso solicitado"));
    }

    @Transactional(readOnly = true)
    public Academia academiaDe(Long usuarioId) {
        return academiaRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ReglaNegocioException("El usuario no tiene una academia asociada"));
    }

    @Transactional(readOnly = true)
    public List<CursoDeAcademia> cursosDeAcademia(Long academiaId) {
        return cursoRepository.resumenPorAcademia(academiaId);
    }

    @Transactional
    public Curso crear(Long academiaId, Integer categoriaId, String titulo, String descripcion,
                       BigDecimal precio, String tituloModulo, List<String> lecciones) {
        Academia academia = academiaRepository.findById(academiaId)
                .orElseThrow(() -> new ReglaNegocioException("No existe la academia solicitada"));
        if (!academia.isActiva()) {
            throw new ReglaNegocioException("La academia esta inactiva y no puede crear cursos");
        }
        String tituloNormalizado = titulo.trim();
        if (cursoRepository.existsByAcademiaIdAndTituloIgnoreCase(academiaId, tituloNormalizado)) {
            throw new ReglaNegocioException("Ya tenes un curso con el titulo " + tituloNormalizado);
        }
        if (precio.signum() < 0) {
            throw new ReglaNegocioException("El precio no puede ser negativo");
        }
        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new ReglaNegocioException("No existe la categoria elegida"));

        Curso curso = new Curso(academia, categoria, tituloNormalizado, descripcion.trim(), precio);
        if (!lecciones.isEmpty()) {
            var modulo = curso.agregarModulo(
                    StringUtils.hasText(tituloModulo) ? tituloModulo.trim() : "Modulo 1");
            lecciones.forEach(modulo::agregarLeccion);
        }
        return cursoRepository.save(curso);
    }

    @Transactional
    public Curso publicar(Long academiaId, Long cursoId) {
        Curso curso = porId(cursoId);
        if (!curso.getAcademia().getId().equals(academiaId)) {
            throw new ReglaNegocioException("El curso no pertenece a tu academia");
        }
        if (curso.cantidadLecciones() == 0) {
            throw new ReglaNegocioException("No se puede publicar un curso sin lecciones cargadas");
        }
        curso.publicar();
        return cursoRepository.save(curso);
    }

    @Transactional
    public Curso archivar(Long academiaId, Long cursoId) {
        Curso curso = porId(cursoId);
        if (!curso.getAcademia().getId().equals(academiaId)) {
            throw new ReglaNegocioException("El curso no pertenece a tu academia");
        }
        curso.archivar();
        return cursoRepository.save(curso);
    }
}
