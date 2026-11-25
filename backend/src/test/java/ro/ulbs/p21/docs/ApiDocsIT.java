package ro.ulbs.p21.docs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import java.net.URI;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Documentația API (D-002, D-043, Q-044): specificația OpenAPI la /api/docs și Swagger UI la
 * /api/docs/ui, doar pentru Admin. Rutele din C-001 apar cu codurile lor de eroare.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiDocsIT {

    private static final String PASSWORD = UUID.randomUUID().toString();
    private static final String HASH = new BCryptPasswordEncoder(4).encode(PASSWORD);

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    private final JsonMapper json = JsonMapper.builder().build();

    private MockHttpSession session(String role) throws Exception {
        String username = "t-docs-" + role.toLowerCase() + "-" + UUID.randomUUID().toString().substring(0, 8);
        jdbc.update("insert into app_user (username, full_name, password_hash, role, active) values (?, ?, ?, ?, true)",
                username, "Test " + role, HASH, role);
        return (MockHttpSession) mvc.perform(post("/api/login").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getRequest().getSession(false);
    }

    private JsonNode spec() throws Exception {
        String body = mvc.perform(get("/api/docs").session(session("ADMIN")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(body);
    }

    /** Calea unui redirect, fără schemă și host. */
    private static String path(String location) {
        URI uri = URI.create(location);
        return uri.getRawPath() + (uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery());
    }

    /** Pagina Swagger UI, după redirectul de la /api/docs/ui; întoarce calea ei. */
    private String swaggerUiPage(MockHttpSession admin) throws Exception {
        MockHttpServletResponse redirect = mvc.perform(get("/api/docs/ui").session(admin))
                .andExpect(status().is3xxRedirection())
                .andReturn().getResponse();
        String page = path(redirect.getRedirectedUrl());
        assertThat(page).as("Swagger UI rămâne sub /api/docs/ (nginx trimite doar /api/ la backend)").startsWith("/api/docs/");
        return page;
    }

    @Test
    void docsWithoutSessionAre401() throws Exception {
        for (String path : new String[] {"/api/docs", "/api/docs/ui"}) {
            mvc.perform(get(path))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().json("{\"error\":\"UNAUTHENTICATED\"}", true));
        }
    }

    @Test
    void docsAreForbiddenForOperatorAndContabil() throws Exception {
        for (String role : new String[] {"OPERATOR", "CONTABIL"}) {
            MockHttpSession s = session(role);
            for (String path : new String[] {"/api/docs", "/api/docs/ui"}) {
                mvc.perform(get(path).session(s))
                        .andExpect(status().isForbidden())
                        .andExpect(content().json("{\"error\":\"FORBIDDEN\"}", true));
            }
        }
    }

    @Test
    void specificationListsTheExistingRoutes() throws Exception {
        JsonNode paths = spec().path("paths");
        assertThat(paths.path("/api/login").has("post")).as("POST /api/login").isTrue();
        assertThat(paths.path("/api/me").has("get")).as("GET /api/me").isTrue();
        assertThat(paths.path("/api/logout").has("post")).as("POST /api/logout").isTrue();
        assertThat(paths.path("/api/csrf").has("get")).as("GET /api/csrf").isTrue();
        assertThat(paths.path("/api/health").has("get")).as("GET /api/health").isTrue();
    }

    @Test
    void specificationDescribesTheErrorCodesOfC001() throws Exception {
        JsonNode spec = spec();
        String text = spec.toString();
        JsonNode login = spec.path("paths").path("/api/login").path("post").path("responses");
        assertThat(login.has("200")).isTrue();
        assertThat(login.path("401").toString()).contains("BAD_CREDENTIALS");
        assertThat(login.path("403").toString()).contains("ACCOUNT_DISABLED");
        assertThat(login.path("429").toString()).contains("LOGIN_LOCKED");

        JsonNode me = spec.path("paths").path("/api/me").path("get").path("responses");
        assertThat(me.path("401").toString()).contains("UNAUTHENTICATED").contains("SESSION_EXPIRED");
        assertThat(me.path("403").toString()).contains("FORBIDDEN");

        JsonNode logout = spec.path("paths").path("/api/logout").path("post").path("responses");
        assertThat(logout.has("204")).isTrue();
        assertThat(logout.path("403").toString()).contains("CSRF");

        assertThat(text).as("schema erorilor {\"error\":\"COD\"}").contains("ApiError");
        assertThat(spec.path("servers").path(0).path("url").asString()).as("server relativ, prin nginx").isEqualTo("/");
    }

    @Test
    void swaggerUiLoadsForAdmin() throws Exception {
        MockHttpSession admin = session("ADMIN");
        String page = swaggerUiPage(admin);
        mvc.perform(get(page).session(admin))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));
    }

    /**
     * „Try it out” trimite tokenul CSRF: Swagger UI citește cookie-ul XSRF-TOKEN și pune header-ul
     * X-XSRF-TOKEN pe cereri, deci POST /api/logout din documentație nu primește 403 CSRF (D-039).
     */
    @Test
    void tryItOutSendsTheCsrfTokenOnPostLogout() throws Exception {
        MockHttpSession admin = session("ADMIN");
        String page = swaggerUiPage(admin);
        String initializer = URI.create(page).resolve("swagger-initializer.js").getRawPath();
        String script = mvc.perform(get(initializer).session(admin))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(script).contains("XSRF-TOKEN").contains("X-XSRF-TOKEN");
        assertThat(spec().path("paths").path("/api/logout").has("post")).isTrue();
    }
}
