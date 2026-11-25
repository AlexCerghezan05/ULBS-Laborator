package ro.ulbs.p21.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ro.ulbs.p21.auth.AuthFixtures.PASSWORD;
import static ro.ulbs.p21.auth.AuthFixtures.login;

import java.time.OffsetDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Blocarea după 5 încercări greșite, 15 minute (D-036), numărate pe cont (D-037), fără prelungire
 * și cu numărătoarea de la zero după blocare (D-038). Expirarea se simulează mutând locked_until.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LoginLockoutIT {

    private static final String LOCKED = "{\"error\":\"LOGIN_LOCKED\"}";
    private static final String BAD = "{\"error\":\"BAD_CREDENTIALS\"}";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    private String username;

    @BeforeEach
    void setUp() {
        username = new AuthFixtures(jdbc).user("OPERATOR");
    }

    private void wrong(int times) throws Exception {
        for (int i = 0; i < times; i++) {
            login(mvc, username, "gresita").andExpect(status().isUnauthorized()).andExpect(content().json(BAD, true));
        }
    }

    private OffsetDateTime lockedUntil() {
        return jdbc.queryForObject("select locked_until from app_user where username = ?", OffsetDateTime.class, username);
    }

    private void expireLock() {
        jdbc.update("update app_user set locked_until = now() - interval '1 second' where username = ?", username);
    }

    @Test
    void theSixthAttemptIsBlockedEvenWithTheCorrectPassword() throws Exception {
        wrong(5);
        login(mvc, username, PASSWORD).andExpect(status().isTooManyRequests()).andExpect(content().json(LOCKED, true));
    }

    @Test
    void theLockLastsFifteenMinutes() throws Exception {
        wrong(5);
        OffsetDateTime now = jdbc.queryForObject("select now()", OffsetDateTime.class);
        assertThat(lockedUntil()).isAtSameInstantAs(now.plusMinutes(15));
    }

    @Test
    void attemptsDuringTheLockDoNotExtendIt() throws Exception {
        wrong(5);
        OffsetDateTime until = lockedUntil();
        login(mvc, username, "gresita").andExpect(status().isTooManyRequests()).andExpect(content().json(LOCKED, true));
        login(mvc, username, PASSWORD).andExpect(status().isTooManyRequests());
        assertThat(lockedUntil()).isAtSameInstantAs(until);
    }

    @Test
    void theLockExpiresByItself() throws Exception {
        wrong(5);
        expireLock();
        login(mvc, username, PASSWORD).andExpect(status().isOk());
    }

    @Test
    void afterTheLockExpiresThereAreFiveFreshAttempts() throws Exception {
        wrong(5);
        expireLock();
        wrong(4);
        login(mvc, username, PASSWORD).andExpect(status().isOk());
    }

    @Test
    void aSuccessfulLoginResetsTheCount() throws Exception {
        wrong(4);
        login(mvc, username, PASSWORD).andExpect(status().isOk());
        wrong(4);
        login(mvc, username, PASSWORD).andExpect(status().isOk());
    }

    @Test
    void attemptsAreCountedPerAccount() throws Exception {
        String other = new AuthFixtures(jdbc).user("OPERATOR");
        wrong(5);
        login(mvc, other, PASSWORD).andExpect(status().isOk());
    }
}
