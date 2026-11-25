package ro.ulbs.p21.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ro.ulbs.p21.auth.AuthFixtures.session;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rolurile verificate pe server (D-012): o rută de test pe fiecare nivel de acces din {@link Access}.
 * Admin poate tot, deci nu primește 403 nicăieri. Contul dezactivat își pierde sesiunea (D-041).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(AuthorizationIT.Routes.class)
class AuthorizationIT {

    private static final String FORBIDDEN = "{\"error\":\"FORBIDDEN\"}";
    private static final String UNAUTHENTICATED = "{\"error\":\"UNAUTHENTICATED\"}";

    @RestController
    static class Routes {
        @PreAuthorize(Access.ADMIN)
        @GetMapping("/api/test-access/admin")
        Map<String, String> admin() { return Map.of(); }

        @PreAuthorize(Access.WRITE)
        @GetMapping("/api/test-access/write")
        Map<String, String> write() { return Map.of(); }

        @PreAuthorize(Access.REPORTS)
        @GetMapping("/api/test-access/reports")
        Map<String, String> reports() { return Map.of(); }

        @PreAuthorize(Access.READ)
        @GetMapping("/api/test-access/read")
        Map<String, String> read() { return Map.of(); }
    }

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    private AuthFixtures users;

    @BeforeEach
    void setUp() {
        users = new AuthFixtures(jdbc);
    }

    private void expect(MockHttpSession session, String level, int status) throws Exception {
        mvc.perform(get("/api/test-access/" + level).session(session)).andExpect(status().is(status));
    }

    @Test
    void protectedRoutesWithoutSessionAre401() throws Exception {
        for (String path : new String[] {"/api/me", "/api/test-access/read", "/api/test-access/admin"}) {
            mvc.perform(get(path)).andExpect(status().isUnauthorized()).andExpect(content().json(UNAUTHENTICATED, true));
        }
    }

    @Test
    void adminCanUseEveryAccessLevel() throws Exception {
        MockHttpSession s = session(mvc, users.user("ADMIN"));
        expect(s, "admin", 200);
        expect(s, "write", 200);
        expect(s, "reports", 200);
        expect(s, "read", 200);
    }

    @Test
    void operatorIsForbiddenFromAdminAndReports() throws Exception {
        MockHttpSession s = session(mvc, users.user("OPERATOR"));
        expect(s, "write", 200);
        expect(s, "read", 200);
        mvc.perform(get("/api/test-access/admin").session(s)).andExpect(status().isForbidden()).andExpect(content().json(FORBIDDEN, true));
        mvc.perform(get("/api/test-access/reports").session(s)).andExpect(status().isForbidden()).andExpect(content().json(FORBIDDEN, true));
    }

    @Test
    void contabilIsForbiddenFromAdminAndWrite() throws Exception {
        MockHttpSession s = session(mvc, users.user("CONTABIL"));
        expect(s, "reports", 200);
        expect(s, "read", 200);
        mvc.perform(get("/api/test-access/admin").session(s)).andExpect(status().isForbidden()).andExpect(content().json(FORBIDDEN, true));
        mvc.perform(get("/api/test-access/write").session(s)).andExpect(status().isForbidden()).andExpect(content().json(FORBIDDEN, true));
    }

    @Test
    void aDisabledAccountLosesItsSessionOnTheNextRequest() throws Exception {
        String username = users.user("OPERATOR");
        MockHttpSession s = session(mvc, username);
        mvc.perform(get("/api/me").session(s)).andExpect(status().isOk());

        jdbc.update("update app_user set active = false where username = ?", username);
        mvc.perform(get("/api/me").session(s))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json(UNAUTHENTICATED, true));
        assertThat(s.isInvalid()).isTrue();

        jdbc.update("update app_user set active = true where username = ?", username);
        mvc.perform(get("/api/me").session(s)).andExpect(status().isUnauthorized());
    }

    @Test
    void aDeletedAccountLosesItsSession() throws Exception {
        String username = users.user("CONTABIL");
        MockHttpSession s = session(mvc, username);
        users.deleteUser(username);
        mvc.perform(get("/api/me").session(s)).andExpect(status().isUnauthorized());
        assertThat(s.isInvalid()).isTrue();
    }
}
