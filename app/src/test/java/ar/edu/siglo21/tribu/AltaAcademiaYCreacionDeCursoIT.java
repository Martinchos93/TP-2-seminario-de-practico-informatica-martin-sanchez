package ar.edu.siglo21.tribu;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ar.edu.siglo21.tribu.domain.Academia;
import ar.edu.siglo21.tribu.domain.Curso;
import ar.edu.siglo21.tribu.domain.EstadoCurso;
import ar.edu.siglo21.tribu.repository.AcademiaRepository;
import ar.edu.siglo21.tribu.repository.UsuarioRepository;
import ar.edu.siglo21.tribu.service.AcademiaService;
import ar.edu.siglo21.tribu.service.CursoService;
import ar.edu.siglo21.tribu.service.ReglaNegocioException;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AltaAcademiaYCreacionDeCursoIT extends PruebaConMySql {
    private static final int PROGRAMACION = 1;

    @Autowired
    private AcademiaService academiaService;
    @Autowired
    private CursoService cursoService;
    @Autowired
    private AcademiaRepository academiaRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    @DisplayName("CP-25: el alta crea la academia y su titular con el rol ACADEMIA")
    void altaDeAcademia() {
        Academia academia = academiaService.darDeAlta(
                "Academia CP25 SRL", "30-71900000-9", "Cursos de prueba",
                "CP25@Test.com", "TribuTest!", "Paula", "Zarate");

        assertThat(academia.getId()).isNotNull();
        assertThat(academia.isActiva()).isTrue();
        var titular = usuarioRepository.findByEmail("cp25@test.com").orElseThrow();
        assertThat(titular.tieneRol("ACADEMIA")).isTrue();
        assertThat(titular.getPasswordHash()).startsWith("$2");
        assertThat(academiaRepository.findByUsuarioId(titular.getId())).isPresent();
    }

    @Test
    @DisplayName("CP-26: se rechaza el alta con una CUIT invalida o ya registrada")
    void altaConCuitInvalidaODuplicada() {
        assertThatThrownBy(() -> academiaService.darDeAlta(
                "Academia CP26 SRL", "30-71900001-0", null,
                "cp26a@test.com", "TribuTest!", "Rosa", "Zapata"))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("no es valida");

        assertThatThrownBy(() -> academiaService.darDeAlta(
                "Academia CP26 SRL", "30-71234567-1", null,
                "cp26b@test.com", "TribuTest!", "Rosa", "Zapata"))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Ya existe una academia");

        assertThat(usuarioRepository.existsByEmail("cp26a@test.com")).isFalse();
        assertThat(usuarioRepository.existsByEmail("cp26b@test.com")).isFalse();
    }

    @Test
    @DisplayName("CP-27: se rechaza el alta si el email del titular ya tiene cuenta")
    void altaConEmailDuplicado() {
        long academiasAntes = academiaRepository.count();

        assertThatThrownBy(() -> academiaService.darDeAlta(
                "Academia CP27 SRL", "30-71900002-5", null,
                "ana.lopez@gmail.com", "TribuTest!", "Ana", "Lopez"))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Ya existe una cuenta");

        assertThat(academiaRepository.count()).isEqualTo(academiasAntes);
    }

    @Test
    @DisplayName("CP-28: el curso nuevo queda en BORRADOR con su plan y se puede publicar")
    void creacionYPublicacionDeCurso() {
        Academia academia = academiaService.darDeAlta(
                "Academia CP28 SRL", "30-71900003-3", null,
                "cp28@test.com", "TribuTest!", "Ivan", "Zabala");

        Curso curso = cursoService.crear(
                academia.getId(), PROGRAMACION, "Kotlin para backend", "Kotlin sobre la JVM",
                new BigDecimal("30000.00"), "Fundamentos",
                List.of("Sintaxis basica", "Null safety", "Corrutinas"));

        assertThat(curso.getEstado()).isEqualTo(EstadoCurso.BORRADOR);
        assertThat(curso.cantidadLecciones()).isEqualTo(3);
        assertThat(cursoService.catalogo(null, "Kotlin")).isEmpty();

        cursoService.publicar(academia.getId(), curso.getId());

        assertThat(cursoService.catalogo(null, "Kotlin"))
                .extracting(Curso::getTitulo)
                .containsExactly("Kotlin para backend");
    }

    @Test
    @DisplayName("CP-29: la academia no puede repetir el titulo de uno de sus cursos")
    void tituloDuplicadoEnLaAcademia() {
        Academia academia = academiaService.darDeAlta(
                "Academia CP29 SRL", "30-71900004-1", null,
                "cp29@test.com", "TribuTest!", "Eva", "Zamora");
        cursoService.crear(academia.getId(), PROGRAMACION, "Go desde cero", "Go",
                BigDecimal.ZERO, null, List.of("Hola mundo"));

        assertThatThrownBy(() -> cursoService.crear(
                academia.getId(), PROGRAMACION, "GO DESDE CERO", "Otra vez",
                BigDecimal.ZERO, null, List.of("Hola mundo")))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Ya tenes un curso");
    }

    @Test
    @DisplayName("CP-30: un curso creado sin lecciones se guarda pero no se puede publicar")
    void cursoSinLeccionesNoSePublica() {
        Academia academia = academiaService.darDeAlta(
                "Academia CP30 SRL", "30-71900006-8", null,
                "cp30@test.com", "TribuTest!", "Leo", "Zeballos");

        Curso curso = cursoService.crear(academia.getId(), PROGRAMACION, "Rust inicial", "Rust",
                new BigDecimal("15000.00"), null, List.of());

        assertThat(curso.getId()).isNotNull();
        assertThat(curso.cantidadLecciones()).isZero();
        assertThatThrownBy(() -> cursoService.publicar(academia.getId(), curso.getId()))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("sin lecciones");
    }
}
