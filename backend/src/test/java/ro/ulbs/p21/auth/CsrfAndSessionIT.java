package ro.ulbs.p21.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ro.ulbs.p21.auth.AuthFixtures.PASSWORD;
import static ro.ulbs.p21.auth.AuthFixtures.session;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/** CSRF pe cererile care modifică ceva și sesiunea expirată (D-039, C-001). */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CsrfAndSessionIT {

    private static final String CSRF = "{\"error\":\"CSRF\"}";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    private AuthFixtures users;

    @BeforeEach
    void setUp() {
        users = new AuthFixtures(jdbc);
    }

    @Test
    void loginWithoutCsrfTokenIsRejected() throws Exception {
        String username = users.user("OPERATOR");
        mvc.perform(post("/api/login").contentType("application/json").content(AuthFixtures.body(username, PASSWORD)))
                .andExpect(status().isForbidden())
                .andExpect(content().json(CSRF, true));
    }

    @Test
    void logoutWithoutCsrfTokenIsRejectedAndTheSessionStays() throws Exception {
        MockHttpSession s = session(mvc, users.user("OPERATOR"));
        mvc.perform(post("/api/logout").session(s))
                .andExpect(status().isForbidden())
                .andExpect(content().json(CSRF, true));
        mvc.perform(get("/api/me").session(s)).andExpect(status().isOk());
    }

    @Test
    void anExpiredSessionIs401SessionExpired() throws Exception {
        mvc.perform(get("/api/me").with(request -> {
                    request.setRequestedSessionId("expirata");
                    request.setRequestedSessionIdValid(false);
                    return request;
                }))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json("{\"error\":\"SESSION_EXPIRED\"}", true));
    }

    @Test
    void csrfEndpointIsPublic() throws Exception {
        mvc.perform(get("/api/csrf")).andExpect(status().isNoContent());
    }
}
