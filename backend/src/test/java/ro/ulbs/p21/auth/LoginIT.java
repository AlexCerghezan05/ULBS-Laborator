package ro.ulbs.p21.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ro.ulbs.p21.auth.AuthFixtures.PASSWORD;
import static ro.ulbs.p21.auth.AuthFixtures.login;
import static ro.ulbs.p21.auth.AuthFixtures.session;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

/** Login, /api/me, mesajele de eșec (D-035) și logout (C-001). */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LoginIT {

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
    void successfulLoginStartsANewSessionAndMeReturnsTheUser() throws Exception {
        String username = users.user("OPERATOR");
        MockHttpSession before = new MockHttpSession();
        String idBefore = before.getId();

        MvcResult result = mvc.perform(post("/api/login").with(csrf()).session(before)
                        .contentType("application/json").content(AuthFixtures.body(username, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"username\":\"" + username + "\",\"fullName\":\"Test OPERATOR\",\"role\":\"OPERATOR\"}", true))
                .andReturn();

        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        assertThat(session).isNotNull();
        assertThat(session.getId()).as("id-ul sesiunii se schimbă la login (session fixation)").isNotEqualTo(idBefore);

        mvc.perform(get("/api/me").session(session))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"username\":\"" + username + "\",\"fullName\":\"Test OPERATOR\",\"role\":\"OPERATOR\"}", true));
    }

    @Test
    void wrongPasswordIs401WithTheGenericError() throws Exception {
        String username = users.user("OPERATOR");
        login(mvc, username, "gresita")
                .andExpect(status().isUnauthorized())
                .andExpect(content().json("{\"error\":\"BAD_CREDENTIALS\"}", true));
    }

    @Test
    void unknownUserGetsTheSameResponseAsAWrongPassword() throws Exception {
        login(mvc, "nu-exista-" + System.nanoTime(), PASSWORD)
                .andExpect(status().isUnauthorized())
                .andExpect(content().json("{\"error\":\"BAD_CREDENTIALS\"}", true));
    }

    @Test
    void userWithoutPasswordHashCannotLogIn() throws Exception {
        String username = users.user("OPERATOR");
        jdbc.update("update app_user set password_hash = null where username = ?", username);
        login(mvc, username, PASSWORD)
                .andExpect(status().isUnauthorized())
                .andExpect(content().json("{\"error\":\"BAD_CREDENTIALS\"}", true));
    }

    @Test
    void disabledAccountWithTheCorrectPasswordIs403AccountDisabled() throws Exception {
        String username = users.user("CONTABIL", false);
        login(mvc, username, PASSWORD)
                .andExpect(status().isForbidden())
                .andExpect(content().json("{\"error\":\"ACCOUNT_DISABLED\"}", true));
    }

    @Test
    void disabledAccountWithAWrongPasswordGetsTheGenericError() throws Exception {
        String username = users.user("CONTABIL", false);
        login(mvc, username, "gresita")
                .andExpect(status().isUnauthorized())
                .andExpect(content().json("{\"error\":\"BAD_CREDENTIALS\"}", true));
    }

    @Test
    void logoutInvalidatesTheSession() throws Exception {
        MockHttpSession session = session(mvc, users.user("ADMIN"));

        mvc.perform(post("/api/logout").with(csrf()).session(session))
                .andExpect(status().isNoContent());

        assertThat(session.isInvalid()).isTrue();
        mvc.perform(get("/api/me").session(session))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json("{\"error\":\"UNAUTHENTICATED\"}", true));
    }
}
