package ar.edu.siglo21.tribu.service;

import ar.edu.siglo21.tribu.domain.Rol;
import ar.edu.siglo21.tribu.domain.Usuario;
import ar.edu.siglo21.tribu.repository.RolRepository;
import ar.edu.siglo21.tribu.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          RolRepository rolRepository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Usuario registrarAlumno(String email, String password, String nombre, String apellido) {
        String emailNormalizado = email.trim().toLowerCase();
        if (usuarioRepository.existsByEmail(emailNormalizado)) {
            throw new ReglaNegocioException("Ya existe una cuenta registrada con el email " + emailNormalizado);
        }

        Usuario usuario = new Usuario(
                emailNormalizado,
                passwordEncoder.encode(password),
                nombre.trim(),
                apellido.trim());

        Rol alumno = rolRepository.findByNombre("ALUMNO")
                .orElseThrow(() -> new IllegalStateException("El rol ALUMNO no esta cargado en la base"));
        usuario.agregarRol(alumno);

        return usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public Page<Usuario> listar(String texto, String rol, Pageable pageable) {
        return usuarioRepository.buscar(
                StringUtils.hasText(texto) ? texto.trim() : null,
                StringUtils.hasText(rol) ? rol : null,
                pageable);
    }

    @Transactional(readOnly = true)
    public Usuario porEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ReglaNegocioException("No existe el usuario " + email));
    }

    @Transactional(readOnly = true)
    public Usuario porId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ReglaNegocioException("No existe el usuario con id " + id));
    }

    @Transactional
    public Usuario cambiarEstado(Long id, boolean activo) {
        Usuario usuario = porId(id);
        usuario.setActivo(activo);
        return usuarioRepository.save(usuario);
    }
}
