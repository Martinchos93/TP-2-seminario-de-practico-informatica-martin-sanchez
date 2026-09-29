package ar.edu.siglo21.tribu.service;

import ar.edu.siglo21.tribu.domain.Academia;
import ar.edu.siglo21.tribu.domain.Rol;
import ar.edu.siglo21.tribu.domain.Usuario;
import ar.edu.siglo21.tribu.repository.AcademiaRepository;
import ar.edu.siglo21.tribu.repository.RolRepository;
import ar.edu.siglo21.tribu.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AcademiaService {
    private final AcademiaRepository academiaRepository;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public AcademiaService(AcademiaRepository academiaRepository,
                           UsuarioRepository usuarioRepository,
                           RolRepository rolRepository,
                           PasswordEncoder passwordEncoder) {
        this.academiaRepository = academiaRepository;
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Academia darDeAlta(String razonSocial, String cuit, String descripcion,
                              String email, String password, String nombre, String apellido) {
        String cuitNormalizada = cuit.trim();
        if (!Cuit.esValida(cuitNormalizada)) {
            throw new ReglaNegocioException("La CUIT " + cuitNormalizada + " no es valida");
        }
        if (academiaRepository.existsByCuit(cuitNormalizada)) {
            throw new ReglaNegocioException("Ya existe una academia registrada con la CUIT " + cuitNormalizada);
        }
        String emailNormalizado = email.trim().toLowerCase();
        if (usuarioRepository.existsByEmail(emailNormalizado)) {
            throw new ReglaNegocioException("Ya existe una cuenta registrada con el email " + emailNormalizado);
        }

        Usuario titular = new Usuario(
                emailNormalizado,
                passwordEncoder.encode(password),
                nombre.trim(),
                apellido.trim());
        Rol rolAcademia = rolRepository.findByNombre("ACADEMIA")
                .orElseThrow(() -> new IllegalStateException("El rol ACADEMIA no esta cargado en la base"));
        titular.agregarRol(rolAcademia);
        usuarioRepository.save(titular);

        Academia academia = new Academia(
                titular,
                razonSocial.trim(),
                cuitNormalizada,
                StringUtils.hasText(descripcion) ? descripcion.trim() : null);
        return academiaRepository.save(academia);
    }
}
