package ro.ulbs.p21.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static ro.ulbs.p21.auth.AuthFixtures.PASSWORD;
import static ro.ulbs.p21.auth.AuthFixtures.login;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Blocarea (D-036–D-038) ține și sub cereri simultane pe același cont: încercările se fac una după
 * alta, sub lock pe rândul contului (review-ul de securitate, PR #3). Fără @Transactional: fiecare
 * cerere face commit, ca în producție. Verificarea parolei durează aici 50 ms, cam cât BCrypt cu
 * costul din producție; cu hash-urile rapide din teste cursa s-ar vedea rar.
 */
@SpringBootTest
@AutoConfigureMockMvc
class LoginConcurrencyIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @MockitoSpyBean
    private PasswordEncoder encoder;

    private AuthFixtures fixtures;
    private String username;
    /** Câte verificări de parolă s-au făcut pe hash-ul contului de test. */
    private final AtomicInteger checks = new AtomicInteger();

    @BeforeEach
    void setUp() {
        fixtures = new AuthFixtures(jdbc);
        username = fixtures.user("OPERATOR");
        String hash = jdbc.queryForObject("select password_hash from app_user where username = ?", String.class, username);
        doAnswer(inv -> {
            if (hash.equals(inv.getArgument(1))) {
                checks.incrementAndGet();
            }
            Thread.sleep(50);
            return inv.callRealMethod();
        }).when(encoder).matches(any(), any());
    }

    @AfterEach
    void tearDown() {
        fixtures.deleteUser(username);
    }

    /** Trimite cererile deodată și întoarce statusurile HTTP, numărate. */
    private Map<Integer, Long> simultaneously(List<String> passwords) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(passwords.size());
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<Integer>> results = new ArrayList<>();
            for (String pw : passwords) {
                results.add(pool.submit(() -> {
                    start.await();
                    return login(mvc, username, pw).andReturn().getResponse().getStatus();
                }));
            }
            start.countDown();
            List<Integer> statuses = new ArrayList<>();
            for (Future<Integer> r : results) {
                statuses.add(r.get(60, TimeUnit.SECONDS));
            }
            return statuses.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        } finally {
            pool.shutdownNow();
        }
    }

    private boolean locked() {
        return jdbc.queryForObject("select coalesce(locked_until > now(), false) from app_user where username = ?",
                Boolean.class, username);
    }

    private OffsetDateTime lockedUntil() {
        return jdbc.queryForObject("select locked_until from app_user where username = ?", OffsetDateTime.class, username);
    }

    @Test
    void twentySimultaneousWrongAttemptsCheckThePasswordAtMostFiveTimes() throws Exception {
        Map<Integer, Long> statuses = simultaneously(java.util.Collections.nCopies(20, "gresita"));

        assertThat(checks.get()).as("verificări de parolă").isLessThanOrEqualTo(5);
        assertThat(statuses).containsExactlyInAnyOrderEntriesOf(Map.of(401, 5L, 429, 15L));
        assertThat(locked()).as("contul e blocat la final").isTrue();
        assertThat(login(mvc, username, PASSWORD).andReturn().getResponse().getStatus())
                .as("parola corectă în timpul blocării").isEqualTo(429);
    }

    @Test
    void aCorrectAttemptAmongSimultaneousOnesDuringTheLockDoesNotPass() throws Exception {
        for (int i = 0; i < 5; i++) {
            login(mvc, username, "gresita");
        }
        OffsetDateTime until = lockedUntil();
        checks.set(0);
        List<String> passwords = new ArrayList<>(java.util.Collections.nCopies(10, "gresita"));
        passwords.add(PASSWORD);

        Map<Integer, Long> statuses = simultaneously(passwords);

        assertThat(statuses).containsExactlyInAnyOrderEntriesOf(Map.of(429, 11L));
        assertThat(checks.get()).as("verificări de parolă în timpul blocării").isZero();
        assertThat(lockedUntil()).isAtSameInstantAs(until);
    }
}
