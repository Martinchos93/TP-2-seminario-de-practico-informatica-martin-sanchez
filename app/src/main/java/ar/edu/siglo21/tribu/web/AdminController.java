package ar.edu.siglo21.tribu.web;

import ar.edu.siglo21.tribu.domain.Academia;
import ar.edu.siglo21.tribu.domain.Usuario;
import ar.edu.siglo21.tribu.service.AcademiaService;
import ar.edu.siglo21.tribu.service.ReglaNegocioException;
import ar.edu.siglo21.tribu.service.UsuarioService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private static final int TAMANIO_PAGINA = 10;

    private final UsuarioService usuarioService;
    private final AcademiaService academiaService;

    public AdminController(UsuarioService usuarioService, AcademiaService academiaService) {
        this.usuarioService = usuarioService;
        this.academiaService = academiaService;
    }

    @GetMapping("/usuarios")
    public String listarUsuarios(@RequestParam(required = false) String q,
                                 @RequestParam(required = false) String rol,
                                 @RequestParam(defaultValue = "0") int pagina,
                                 Model model) {
        Page<Usuario> resultado = usuarioService.listar(
                q, rol,
                PageRequest.of(pagina, TAMANIO_PAGINA, Sort.by("apellido").ascending()));

        model.addAttribute("pagina", resultado);
        model.addAttribute("q", q);
        model.addAttribute("rol", rol);
        return "admin/usuarios";
    }

    @PostMapping("/usuarios/{id}/estado")
    public String cambiarEstado(@PathVariable Long id,
                                @RequestParam boolean activo,
                                RedirectAttributes redirect) {
        Usuario usuario = usuarioService.cambiarEstado(id, activo);
        redirect.addFlashAttribute("mensaje",
                "El usuario %s quedo %s".formatted(
                        usuario.getEmail(),
                        activo ? "habilitado" : "deshabilitado"));
        return "redirect:/admin/usuarios";
    }

    @GetMapping("/academias/nueva")
    public String formularioAcademia(Model model) {
        model.addAttribute("form", new AltaAcademiaForm("", "", "", "", "", "", ""));
        return "admin/nueva-academia";
    }

    @PostMapping("/academias")
    public String darDeAltaAcademia(@Validated @ModelAttribute("form") AltaAcademiaForm form,
                                    BindingResult binding,
                                    RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            return "admin/nueva-academia";
        }
        Academia academia;
        try {
            academia = academiaService.darDeAlta(
                    form.razonSocial(), form.cuit(), form.descripcion(),
                    form.email(), form.password(), form.nombre(), form.apellido());
        } catch (ReglaNegocioException e) {
            binding.reject("regla", e.getMessage());
            return "admin/nueva-academia";
        }
        redirect.addFlashAttribute("mensaje",
                "Se dio de alta la academia %s. Su titular ya puede ingresar con %s."
                        .formatted(academia.getRazonSocial(), academia.getUsuario().getEmail()));
        return "redirect:/admin/usuarios";
    }

    public record AltaAcademiaForm(
            @NotBlank(message = "Ingresa la razon social") @Size(max = 150) String razonSocial,
            @NotBlank(message = "Ingresa la CUIT")
            @Pattern(regexp = "\\d{2}-\\d{8}-\\d", message = "La CUIT debe tener el formato 30-12345678-9") String cuit,
            @Size(max = 500) String descripcion,
            @NotBlank(message = "Ingresa el nombre del titular") String nombre,
            @NotBlank(message = "Ingresa el apellido del titular") String apellido,
            @NotBlank(message = "Ingresa el email del titular") @Email(message = "El email no tiene un formato valido") String email,
            @NotBlank(message = "Ingresa una contrasena inicial")
            @Size(min = 8, message = "La contrasena debe tener al menos 8 caracteres") String password) {
    }
}
