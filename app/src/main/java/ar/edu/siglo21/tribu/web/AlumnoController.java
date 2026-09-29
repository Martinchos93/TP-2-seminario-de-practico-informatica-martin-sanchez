package ar.edu.siglo21.tribu.web;

import ar.edu.siglo21.tribu.config.UsuarioAutenticado;
import ar.edu.siglo21.tribu.domain.Curso;
import ar.edu.siglo21.tribu.domain.EstadoPago;
import ar.edu.siglo21.tribu.domain.Inscripcion;
import ar.edu.siglo21.tribu.domain.MedioPago;
import ar.edu.siglo21.tribu.domain.Pago;
import ar.edu.siglo21.tribu.service.CursadoService;
import ar.edu.siglo21.tribu.service.CursoService;
import ar.edu.siglo21.tribu.service.InscripcionService;
import ar.edu.siglo21.tribu.service.ReglaNegocioException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/alumno")
public class AlumnoController {
    private final InscripcionService inscripcionService;
    private final CursadoService cursadoService;
    private final CursoService cursoService;

    public AlumnoController(InscripcionService inscripcionService,
                            CursadoService cursadoService,
                            CursoService cursoService) {
        this.inscripcionService = inscripcionService;
        this.cursadoService = cursadoService;
        this.cursoService = cursoService;
    }

    @GetMapping("/mis-cursos")
    public String misCursos(@AuthenticationPrincipal UsuarioAutenticado principal, Model model) {
        model.addAttribute("inscripciones", inscripcionService.misInscripciones(principal.getId()));
        return "alumno/mis-cursos";
    }

    @PostMapping("/inscribir/{cursoId}")
    public String inscribir(@AuthenticationPrincipal UsuarioAutenticado principal,
                            @PathVariable Long cursoId,
                            RedirectAttributes redirect) {
        Inscripcion inscripcion = inscripcionService.inscribir(principal.getUsuario(), cursoId);

        if (inscripcion.permiteCursar()) {
            redirect.addFlashAttribute("mensaje", "Ya estas inscripto. El curso es gratuito, podes empezar.");
            return "redirect:/alumno/cursar/" + cursoId;
        }
        return "redirect:/alumno/pago/" + inscripcion.getId();
    }

    @GetMapping("/pago/{inscripcionId}")
    public String formularioPago(@AuthenticationPrincipal UsuarioAutenticado principal,
                                 @PathVariable Long inscripcionId,
                                 Model model) {
        Inscripcion inscripcion = inscripcionSegura(principal, inscripcionId);
        model.addAttribute("inscripcion", inscripcion);
        model.addAttribute("mediosPago", MedioPago.values());
        model.addAttribute("intentosPrevios", inscripcionService.pagosDe(inscripcionId));
        return "alumno/pago";
    }

    @PostMapping("/pago/{inscripcionId}")
    public String pagar(@AuthenticationPrincipal UsuarioAutenticado principal,
                        @PathVariable Long inscripcionId,
                        @RequestParam MedioPago medioPago,
                        @RequestParam String numeroTarjeta,
                        RedirectAttributes redirect) {
        inscripcionSegura(principal, inscripcionId);
        Pago pago = inscripcionService.pagar(inscripcionId, medioPago, numeroTarjeta);

        if (pago.getEstado() == EstadoPago.APROBADO) {
            redirect.addFlashAttribute("mensaje",
                    "Pago aprobado (referencia %s). Ya podes cursar.".formatted(pago.getReferenciaExterna()));
            return "redirect:/alumno/cursar/" + pago.getInscripcion().getCurso().getId();
        }

        redirect.addFlashAttribute("error", "El pago fue rechazado: " + pago.getDetalleRechazo());
        return "redirect:/alumno/pago/" + inscripcionId;
    }

    @GetMapping("/cursar/{cursoId}")
    public String cursar(@AuthenticationPrincipal UsuarioAutenticado principal,
                         @PathVariable Long cursoId,
                         Model model) {
        Inscripcion inscripcion = cursadoService.inscripcionHabilitada(principal.getId(), cursoId);
        Curso curso = cursoService.conPlanDeEstudio(cursoId);

        model.addAttribute("curso", curso);
        model.addAttribute("inscripcion", inscripcion);
        model.addAttribute("avance", cursadoService.avance(inscripcion));
        model.addAttribute("certificado", cursadoService.certificadoDe(inscripcion.getId()).orElse(null));
        return "alumno/cursar";
    }

    @PostMapping("/cursar/{cursoId}/leccion/{leccionId}")
    public String completarLeccion(@AuthenticationPrincipal UsuarioAutenticado principal,
                                   @PathVariable Long cursoId,
                                   @PathVariable Long leccionId,
                                   RedirectAttributes redirect) {
        var avance = cursadoService.completarLeccion(principal.getId(), cursoId, leccionId);
        if (avance.completo()) {
            redirect.addFlashAttribute("mensaje",
                    "Felicitaciones, completaste el curso. Tu certificado ya esta disponible.");
        }
        return "redirect:/alumno/cursar/" + cursoId;
    }

    private Inscripcion inscripcionSegura(UsuarioAutenticado principal, Long inscripcionId) {
        Inscripcion inscripcion = inscripcionService.porId(inscripcionId);
        if (!inscripcion.getAlumno().getId().equals(principal.getId())) {
            throw new ReglaNegocioException("La inscripcion no te pertenece");
        }
        return inscripcion;
    }
}
