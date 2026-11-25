package ro.ulbs.p21.auth;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/**
 * Utilizatori de test cu parolă: parola se generează la fiecare rulare, deci nu stă în repo.
 * Costul BCrypt mic ține testele rapide; verificarea merge la orice cost.
 */
final class AuthFixtures {

    static final String PASSWORD = UUID.randomUUID().toString();
    private static final String HASH = new BCryptPasswordEncoder(4).encode(PASSWORD);

    private final JdbcTemplate jdbc;

    AuthFixtures(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Un utilizator nou cu rolul dat (ADMIN, OPERATOR, CONTABIL); întoarce numele de utilizator. */
    String user(String role, boolean active) {
        String username = "t-" + role.toLowerCase() + "-" + UUID.randomUUID().toString().substring(0, 8);
        jdbc.update("insert into app_user (username, full_name, password_hash, role, active) values (?, ?, ?, ?, ?)",
                username, "Test " + role, HASH, role, active);
        return username;
    }

    String user(String role) {
        return user(role, true);
    }

    void deleteUser(String username) {
        jdbc.update("delete from app_user where username = ?", username);
    }

    static String body(String username, String password) {
        return "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
    }

    static ResultActions login(MockMvc mvc, String username, String password) throws Exception {
        return mvc.perform(post("/api/login").with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(body(username, password)));
    }

    /** Login reușit; întoarce sesiunea, pentru cererile următoare. */
    static MockHttpSession session(MockMvc mvc, String username) throws Exception {
        return (MockHttpSession) login(mvc, username, PASSWORD).andReturn().getRequest().getSession(false);
    }
}
