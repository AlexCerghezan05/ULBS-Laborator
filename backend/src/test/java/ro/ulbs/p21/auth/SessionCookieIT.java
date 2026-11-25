package ro.ulbs.p21.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static ro.ulbs.p21.auth.AuthFixtures.PASSWORD;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;
import org.apache.catalina.Context;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.tomcat.TomcatWebServer;
import org.springframework.boot.web.server.servlet.context.ServletWebServerApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Fluxul real, pe Tomcat (D-039): cookie-ul CSRF, login, atributele cookie-ului de sesiune,
 * un POST cu tokenul primit după login, logout și sesiunea expirată. Fără tranzacție de test:
 * serverul rulează pe alt fir, deci utilizatorul se șterge la final.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "P21_COOKIE_SECURE=true")
class SessionCookieIT {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private ServletWebServerApplicationContext context;

    private final HttpClient http = HttpClient.newHttpClient();
    private AuthFixtures users;
    private String username;

    @BeforeEach
    void setUp() {
        users = new AuthFixtures(jdbc);
        username = users.user("OPERATOR");
    }

    @AfterEach
    void tearDown() {
        users.deleteUser(username);
    }

    private HttpResponse<String> send(String method, String path, String body, String... headers) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        if (headers.length > 0) {
            b.headers(headers);
        }
        return http.send(b.build(), HttpResponse.BodyHandlers.ofString());
    }

    /** Header-ul Set-Cookie pentru cookie-ul cu numele dat (ultimul, dacă sunt mai multe). */
    private static Optional<String> setCookie(HttpResponse<?> r, String name) {
        List<String> all = r.headers().allValues("Set-Cookie").stream().filter(c -> c.startsWith(name + "=")).toList();
        return all.isEmpty() ? Optional.empty() : Optional.of(all.getLast());
    }

    private static String value(String setCookie) {
        return setCookie.substring(setCookie.indexOf('=') + 1, setCookie.indexOf(';') < 0 ? setCookie.length() : setCookie.indexOf(';'));
    }

    @Test
    void fullFlowWithRealCookies() throws Exception {
        HttpResponse<String> csrf = send("GET", "/api/csrf", null);
        assertThat(csrf.statusCode()).isEqualTo(204);
        String xsrf = value(setCookie(csrf, "XSRF-TOKEN").orElseThrow());

        HttpResponse<String> login = send("POST", "/api/login", AuthFixtures.body(username, PASSWORD),
                "Content-Type", "application/json", "Cookie", "XSRF-TOKEN=" + xsrf, "X-XSRF-TOKEN", xsrf);
        assertThat(login.statusCode()).isEqualTo(200);

        String sessionCookie = setCookie(login, "JSESSIONID").orElseThrow();
        assertThat(sessionCookie).containsIgnoringCase("HttpOnly").containsIgnoringCase("Secure").containsIgnoringCase("SameSite=Lax");
        String sessionId = value(sessionCookie);

        // După login tokenul CSRF e unul nou (CsrfAuthenticationStrategy); răspunsul la login îl trimite.
        String newXsrf = value(setCookie(login, "XSRF-TOKEN").filter(c -> !value(c).isEmpty()).orElseThrow());
        assertThat(newXsrf).isNotEqualTo(xsrf);

        String cookies = "JSESSIONID=" + sessionId + "; XSRF-TOKEN=" + newXsrf;
        assertThat(send("GET", "/api/me", null, "Cookie", cookies).statusCode()).isEqualTo(200);

        HttpResponse<String> logout = send("POST", "/api/logout", null, "Cookie", cookies, "X-XSRF-TOKEN", newXsrf);
        assertThat(logout.statusCode()).isEqualTo(204);
        // Cookie-ul de sesiune e șters: valoare goală și expirat (Tomcat folosește Expires în 1970).
        assertThat(setCookie(logout, "JSESSIONID").orElseThrow())
                .matches("(?i)JSESSIONID=;.*(Max-Age=0|Expires=Thu, 01 Jan 1970).*");

        HttpResponse<String> stale = send("GET", "/api/me", null, "Cookie", "JSESSIONID=" + sessionId);
        assertThat(stale.statusCode()).isEqualTo(401);
        assertThat(stale.body()).isEqualTo("{\"error\":\"SESSION_EXPIRED\"}");

        HttpResponse<String> none = send("GET", "/api/me", null);
        assertThat(none.statusCode()).isEqualTo(401);
        assertThat(none.body()).isEqualTo("{\"error\":\"UNAUTHENTICATED\"}");
    }

    @Test
    void sessionsExpireAfterThirtyMinutesOfInactivity() {
        TomcatWebServer server = (TomcatWebServer) context.getWebServer();
        Context app = (Context) server.getTomcat().getHost().findChildren()[0];
        assertThat(app.getSessionTimeout()).isEqualTo(30);
    }
}
