package ar.edu.siglo21.tribu;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class PantallasWebIT extends PruebaConMySql {
    private static final String ADMIN = "admin@tribu.com.ar";
    private static final String ACADEMIA = "contacto@codeacademy.com.ar";
    private static final String ALUMNO = "ana.lopez@gmail.com";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("CP-16: el catalogo es publico y lista solo los cursos publicados")
    void catalogoEsPublico() throws Exception {
        mockMvc.perform(get("/catalogo"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Java desde cero")))

                .andExpect(content().string(
                        org.hamcrest.Matchers.not(
                                org.hamcrest.Matchers.containsString("Proceso Unificado de Desarrollo"))));
    }

    @Test
    @DisplayName("CP-17: un usuario anonimo es redirigido al login en las zonas privadas")
    void zonaPrivadaRedirigeAlLogin() throws Exception {
        mockMvc.perform(get("/admin/usuarios"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithUserDetails(ADMIN)
    @DisplayName("CP-18: el ADMIN ve el listado de usuarios")
    void adminVeListadoDeUsuarios() throws Exception {
        mockMvc.perform(get("/admin/usuarios"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Lopez, Ana")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Total de usuarios")));
    }

    @Test
    @WithUserDetails(ACADEMIA)
    @DisplayName("CP-19: la ACADEMIA ve su panel de cursos con la cantidad de lecciones")
    void academiaVeSusCursos() throws Exception {
        mockMvc.perform(get("/academia/cursos"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Code Academy SRL")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Java desde cero")));
    }

    @Test
    @WithUserDetails(ACADEMIA)
    @DisplayName("CP-20: la ACADEMIA no puede entrar al panel del ADMIN")
    void academiaNoAccedeAlPanelDeAdmin() throws Exception {
        mockMvc.perform(get("/admin/usuarios"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(ALUMNO)
    @DisplayName("CP-21: el ALUMNO ve sus inscripciones")
    void alumnoVeSusCursos() throws Exception {
        mockMvc.perform(get("/alumno/mis-cursos"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Java desde cero")));
    }

    @Test
    @WithUserDetails(ALUMNO)
    @DisplayName("CP-22: la pantalla de cursado muestra el plan de estudio y el avance")
    void alumnoVePantallaDeCursado() throws Exception {
        mockMvc.perform(get("/alumno/cursar/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Que es la JVM")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("3 de 6 lecciones")));
    }

    @Test
    @WithUserDetails(ADMIN)
    @DisplayName("CP-31: el ADMIN da de alta una academia desde el formulario")
    void adminDaDeAltaAcademia() throws Exception {
        mockMvc.perform(get("/admin/academias/nueva"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Dar de alta una academia")));

        mockMvc.perform(post("/admin/academias").with(csrf())
                        .param("razonSocial", "Academia CP31 SRL")
                        .param("cuit", "30-71900007-6")
                        .param("descripcion", "")
                        .param("nombre", "Tomas")
                        .param("apellido", "Zuniga")
                        .param("email", "cp31@test.com")
                        .param("password", "TribuTest!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/usuarios"));
    }

    @Test
    @WithUserDetails(ADMIN)
    @DisplayName("CP-32: el formulario de alta muestra el error si la CUIT es invalida")
    void altaConCuitInvalidaMuestraError() throws Exception {
        mockMvc.perform(post("/admin/academias").with(csrf())
                        .param("razonSocial", "Academia CP32 SRL")
                        .param("cuit", "30-71900008-0")
                        .param("nombre", "Sol")
                        .param("apellido", "Zapico")
                        .param("email", "cp32@test.com")
                        .param("password", "TribuTest!"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("no es valida")));
    }

    @Test
    @WithUserDetails(ACADEMIA)
    @DisplayName("CP-33: la ACADEMIA crea un curso y vuelve a su panel")
    void academiaCreaCurso() throws Exception {
        mockMvc.perform(get("/academia/cursos/nuevo"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Guardar en borrador")));

        mockMvc.perform(post("/academia/cursos").with(csrf())
                        .param("titulo", "Docker para desarrolladores")
                        .param("categoriaId", "1")
                        .param("descripcion", "Contenedores e imagenes")
                        .param("precio", "25000")
                        .param("tituloModulo", "Primeros pasos")
                        .param("lecciones", "Que es un contenedor\nImagenes y capas\n"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/academia/cursos"));
    }
}
