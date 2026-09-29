package ar.edu.siglo21.tribu;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ar.edu.siglo21.tribu.domain.Curso;
import ar.edu.siglo21.tribu.domain.EstadoInscripcion;
import ar.edu.siglo21.tribu.domain.EstadoPago;
import ar.edu.siglo21.tribu.domain.Inscripcion;
import ar.edu.siglo21.tribu.domain.Leccion;
import ar.edu.siglo21.tribu.domain.MedioPago;
import ar.edu.siglo21.tribu.domain.Usuario;
import ar.edu.siglo21.tribu.dto.AvanceCurso;
import ar.edu.siglo21.tribu.service.CursadoService;
import ar.edu.siglo21.tribu.service.CursoService;
import ar.edu.siglo21.tribu.service.InscripcionService;
import ar.edu.siglo21.tribu.service.ReglaNegocioException;
import ar.edu.siglo21.tribu.service.UsuarioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;

@SpringBootTest
class FlujoInscripcionPagoCursadoIT extends PruebaConMySql {
    private static final long CURSO_REDES = 4L;
    private static final long CURSO_JAVA = 1L;
    private static final String TARJETA_OK = "4509953566233704";
    private static final String TARJETA_SIN_FONDOS = "4509953566230000";

    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private InscripcionService inscripcionService;
    @Autowired
    private CursadoService cursadoService;
    @Autowired
    private CursoService cursoService;

    @Test
    @DisplayName("CP-09: el alumno se inscribe, paga con exito y la inscripcion queda ACTIVA")
    void inscripcionConPagoAprobado() {
        Usuario alumno = usuarioService.registrarAlumno(
                "cp09@test.com", "TribuTest!", "Camila", "Vega");

        Inscripcion inscripcion = inscripcionService.inscribir(alumno, CURSO_JAVA);
        assertThat(inscripcion.getEstado()).isEqualTo(EstadoInscripcion.PENDIENTE_PAGO);

        var pago = inscripcionService.pagar(inscripcion.getId(), MedioPago.TARJETA_CREDITO, TARJETA_OK);

        assertThat(pago.getEstado()).isEqualTo(EstadoPago.APROBADO);
        assertThat(pago.getReferenciaExterna()).isNotBlank();
        assertThat(inscripcionService.porId(inscripcion.getId()).getEstado())
                .isEqualTo(EstadoInscripcion.ACTIVA);
    }

    @Test
    @DisplayName("CP-10: un pago rechazado deja la inscripcion PENDIENTE_PAGO y registra el intento")
    void inscripcionConPagoRechazado() {
        Usuario alumno = usuarioService.registrarAlumno(
                "cp10@test.com", "TribuTest!", "Nicolas", "Bravo");

        Inscripcion inscripcion = inscripcionService.inscribir(alumno, CURSO_JAVA);
        var pago = inscripcionService.pagar(inscripcion.getId(), MedioPago.TARJETA_CREDITO, TARJETA_SIN_FONDOS);

        assertThat(pago.getEstado()).isEqualTo(EstadoPago.RECHAZADO);
        assertThat(pago.getDetalleRechazo()).isEqualTo("Fondos insuficientes");
        assertThat(inscripcionService.porId(inscripcion.getId()).getEstado())
                .isEqualTo(EstadoInscripcion.PENDIENTE_PAGO);

        assertThat(inscripcionService.pagosDe(inscripcion.getId())).hasSize(1);
    }

    @Test
    @DisplayName("CP-11: no se puede acceder al contenido de un curso impago")
    void cursarSinPagarEstaBloqueado() {
        Usuario alumno = usuarioService.registrarAlumno(
                "cp11@test.com", "TribuTest!", "Rocio", "Paz");

        inscripcionService.inscribir(alumno, CURSO_JAVA);

        assertThatThrownBy(() -> cursadoService.inscripcionHabilitada(alumno.getId(), CURSO_JAVA))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("completar el pago");
    }

    @Test
    @DisplayName("CP-12: un alumno no puede inscribirse dos veces al mismo curso")
    void noPermiteInscripcionDuplicada() {
        Usuario alumno = usuarioService.registrarAlumno(
                "cp12@test.com", "TribuTest!", "Tomas", "Leiva");

        inscripcionService.inscribir(alumno, CURSO_JAVA);

        assertThatThrownBy(() -> inscripcionService.inscribir(alumno, CURSO_JAVA))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Ya estas inscripto");
    }

    @Test
    @DisplayName("CP-13: al completar la ultima leccion la inscripcion se cierra y se emite el certificado")
    void completarCursoEmiteCertificado() {
        Usuario alumno = usuarioService.registrarAlumno(
                "cp13@test.com", "TribuTest!", "Valentina", "Ortiz");

        Inscripcion inscripcion = inscripcionService.inscribir(alumno, CURSO_REDES);
        inscripcionService.pagar(inscripcion.getId(), MedioPago.TARJETA_DEBITO, TARJETA_OK);

        var curso = cursoService.conPlanDeEstudio(CURSO_REDES);

        AvanceCurso avance = null;
        for (var modulo : curso.getModulos()) {
            for (var leccion : modulo.getLecciones()) {
                avance = cursadoService.completarLeccion(alumno.getId(), CURSO_REDES, leccion.getId());
            }
        }

        assertThat(avance).isNotNull();
        assertThat(avance.completo()).isTrue();
        assertThat(avance.porcentaje()).isEqualTo(100);
        assertThat(inscripcionService.porId(inscripcion.getId()).getEstado())
                .isEqualTo(EstadoInscripcion.COMPLETADA);
        assertThat(cursadoService.certificadoDe(inscripcion.getId()))
                .isPresent()
                .get()
                .extracting(c -> c.getCodigo())
                .asString()
                .startsWith("TRIBU-");
    }

    @Test
    @DisplayName("CP-14: marcar dos veces la misma leccion no duplica el progreso")
    void completarLeccionEsIdempotente() {
        Usuario alumno = usuarioService.registrarAlumno(
                "cp14@test.com", "TribuTest!", "Ignacio", "Sosa");

        Inscripcion inscripcion = inscripcionService.inscribir(alumno, CURSO_JAVA);
        inscripcionService.pagar(inscripcion.getId(), MedioPago.TARJETA_CREDITO, TARJETA_OK);

        var curso = cursoService.conPlanDeEstudio(CURSO_JAVA);
        var primeraLeccion = primeraLeccionDe(curso);

        cursadoService.completarLeccion(alumno.getId(), CURSO_JAVA, primeraLeccion.getId());
        var avance = cursadoService.completarLeccion(alumno.getId(), CURSO_JAVA, primeraLeccion.getId());

        assertThat(avance.leccionesHechas()).isEqualTo(1);
    }

    @Test
    @DisplayName("CP-15: el listado de usuarios filtra por rol y por texto libre")
    void listadoDeUsuariosFiltra() {
        var soloAlumnos = usuarioService.listar(null, "ALUMNO", PageRequest.of(0, 50));
        assertThat(soloAlumnos.getContent())
                .isNotEmpty()
                .allMatch(u -> u.tieneRol("ALUMNO"));

        var porTexto = usuarioService.listar("lopez", null, PageRequest.of(0, 50));
        assertThat(porTexto.getContent())
                .isNotEmpty()
                .allMatch(u -> u.getApellido().toLowerCase().contains("lopez")
                        || u.getEmail().toLowerCase().contains("lopez"));
    }

    private static Leccion primeraLeccionDe(Curso curso) {
        return curso.getModulos().iterator().next().getLecciones().iterator().next();
    }
}
