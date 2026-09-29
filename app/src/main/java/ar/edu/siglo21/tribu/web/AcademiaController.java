package ar.edu.siglo21.tribu.web;

import ar.edu.siglo21.tribu.config.UsuarioAutenticado;
import ar.edu.siglo21.tribu.domain.Academia;
import ar.edu.siglo21.tribu.domain.Curso;
import ar.edu.siglo21.tribu.service.CursoService;
import ar.edu.siglo21.tribu.service.InscripcionService;
import ar.edu.siglo21.tribu.service.ReglaNegocioException;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/academia")
public class AcademiaController {
    private final CursoService cursoService;
    private final InscripcionService inscripcionService;

    public AcademiaController(CursoService cursoService, InscripcionService inscripcionService) {
        this.cursoService = cursoService;
        this.inscripcionService = inscripcionService;
    }

    @GetMapping("/cursos")
    public String misCursos(@AuthenticationPrincipal UsuarioAutenticado principal, Model model) {
        Academia academia = cursoService.academiaDe(principal.getId());
        model.addAttribute("academia", academia);
        model.addAttribute("cursos", cursoService.cursosDeAcademia(academia.getId()));
        return "academia/cursos";
    }

    @GetMapping("/cursos/nuevo")
    public String formularioCurso(Model model) {
        model.addAttribute("form", new CursoForm("", null, "", BigDecimal.ZERO, "", ""));
        model.addAttribute("categorias", cursoService.categorias());
        return "academia/nuevo-curso";
    }

    @PostMapping("/cursos")
    public String crearCurso(@AuthenticationPrincipal UsuarioAutenticado principal,
                             @Validated @ModelAttribute("form") CursoForm form,
                             BindingResult binding,
                             Model model,
                             RedirectAttributes redirect) {
        if (!binding.hasErrors()) {
            try {
                Academia academia = cursoService.academiaDe(principal.getId());
                Curso curso = cursoService.crear(
                        academia.getId(), form.categoriaId(), form.titulo(), form.descripcion(),
                        form.precio(), form.tituloModulo(), form.leccionesComoLista());
                redirect.addFlashAttribute("mensaje",
                        "Se creo el curso %s en borrador con %d lecciones."
                                .formatted(curso.getTitulo(), curso.cantidadLecciones()));
                return "redirect:/academia/cursos";
            } catch (ReglaNegocioException e) {
                binding.reject("regla", e.getMessage());
            }
        }
        model.addAttribute("categorias", cursoService.categorias());
        return "academia/nuevo-curso";
    }

    @PostMapping("/cursos/{id}/publicar")
    public String publicar(@AuthenticationPrincipal UsuarioAutenticado principal,
                           @PathVariable Long id,
                           RedirectAttributes redirect) {
        Academia academia = cursoService.academiaDe(principal.getId());
        cursoService.publicar(academia.getId(), id);
        redirect.addFlashAttribute("mensaje", "El curso se publico en el catalogo.");
        return "redirect:/academia/cursos";
    }

    @PostMapping("/cursos/{id}/archivar")
    public String archivar(@AuthenticationPrincipal UsuarioAutenticado principal,
                           @PathVariable Long id,
                           RedirectAttributes redirect) {
        Academia academia = cursoService.academiaDe(principal.getId());
        cursoService.archivar(academia.getId(), id);
        redirect.addFlashAttribute("mensaje", "El curso se retiro del catalogo.");
        return "redirect:/academia/cursos";
    }

    @GetMapping("/cursos/{id}/inscriptos")
    public String inscriptos(@AuthenticationPrincipal UsuarioAutenticado principal,
                             @PathVariable Long id,
                             Model model) {
        Academia academia = cursoService.academiaDe(principal.getId());
        var curso = cursoService.porId(id);

        if (!curso.getAcademia().getId().equals(academia.getId())) {
            throw new ReglaNegocioException(
                    "El curso no pertenece a tu academia");
        }
        model.addAttribute("curso", curso);
        model.addAttribute("inscripciones", inscripcionService.inscriptosDelCurso(id));
        return "academia/inscriptos";
    }

    public record CursoForm(
            @NotBlank(message = "Ingresa el titulo del curso") @Size(max = 150) String titulo,
            @NotNull(message = "Elegi una categoria") Integer categoriaId,
            @NotBlank(message = "Ingresa una descripcion") String descripcion,
            @NotNull(message = "Ingresa el precio")
            @DecimalMin(value = "0.00", message = "El precio no puede ser negativo")
            @Digits(integer = 8, fraction = 2, message = "El precio admite hasta 8 enteros y 2 decimales") BigDecimal precio,
            @Size(max = 150) String tituloModulo,
            String lecciones) {
        List<String> leccionesComoLista() {
            if (lecciones == null) {
                return List.of();
            }
            return lecciones.lines().map(String::trim).filter(l -> !l.isEmpty()).toList();
        }
    }
}
