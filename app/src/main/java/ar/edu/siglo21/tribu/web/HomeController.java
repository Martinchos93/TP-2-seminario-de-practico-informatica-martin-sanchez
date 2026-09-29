package ar.edu.siglo21.tribu.web;

import ar.edu.siglo21.tribu.config.UsuarioAutenticado;
import ar.edu.siglo21.tribu.service.ReglaNegocioException;
import ar.edu.siglo21.tribu.service.UsuarioService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HomeController {
    private final UsuarioService usuarioService;

    public HomeController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/")
    public String portada() {
        return "redirect:/catalogo";
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/inicio")
    public String inicio(@AuthenticationPrincipal UsuarioAutenticado principal) {
        if (principal.getUsuario().tieneRol("ADMIN")) {
            return "redirect:/admin/usuarios";
        }
        if (principal.getUsuario().tieneRol("ACADEMIA")) {
            return "redirect:/academia/cursos";
        }
        return "redirect:/alumno/mis-cursos";
    }

    @GetMapping("/registro")
    public String formularioRegistro(Model model) {
        model.addAttribute("form", new RegistroForm("", "", "", ""));
        return "auth/registro";
    }

    @PostMapping("/registro")
    public String registrar(@Validated @ModelAttribute("form") RegistroForm form,
                            BindingResult binding,
                            RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            return "auth/registro";
        }
        try {
            usuarioService.registrarAlumno(form.email(), form.password(), form.nombre(), form.apellido());
        } catch (ReglaNegocioException e) {
            binding.rejectValue("email", "duplicado", e.getMessage());
            return "auth/registro";
        }
        redirect.addFlashAttribute("mensaje", "Cuenta creada. Ya podes iniciar sesion.");
        return "redirect:/login";
    }

    public record RegistroForm(
            @NotBlank(message = "Ingresa tu nombre") String nombre,
            @NotBlank(message = "Ingresa tu apellido") String apellido,
            @NotBlank(message = "Ingresa tu email") @Email(message = "El email no tiene un formato valido") String email,
            @NotBlank(message = "Ingresa una contrasena")
            @Size(min = 8, message = "La contrasena debe tener al menos 8 caracteres") String password) {
    }
}
