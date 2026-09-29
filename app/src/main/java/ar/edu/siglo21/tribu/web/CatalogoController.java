package ar.edu.siglo21.tribu.web;

import ar.edu.siglo21.tribu.service.CursoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CatalogoController {
    private final CursoService cursoService;

    public CatalogoController(CursoService cursoService) {
        this.cursoService = cursoService;
    }

    @GetMapping("/catalogo")
    public String listar(@RequestParam(required = false) Integer categoria,
                         @RequestParam(required = false) String q,
                         Model model) {
        model.addAttribute("cursos", cursoService.catalogo(categoria, q));
        model.addAttribute("categorias", cursoService.categorias());
        model.addAttribute("categoriaSeleccionada", categoria);
        model.addAttribute("q", q);
        return "curso/catalogo";
    }

    @GetMapping("/catalogo/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        model.addAttribute("curso", cursoService.conPlanDeEstudio(id));
        return "curso/detalle";
    }
}
