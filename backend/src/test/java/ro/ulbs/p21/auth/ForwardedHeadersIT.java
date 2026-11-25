package ro.ulbs.p21.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static ro.ulbs.p21.auth.AuthFixtures.PASSWORD;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * În spatele nginx și al proxy-ului Render (D-044): browserul vorbește HTTPS cu adresa
 * frontend-ului, iar backend-ul află asta din X-Forwarded-Proto/Host. Cookie-urile de sesiune și
 * CSRF sunt Secure, fără Domain (rămân pe adresa frontend-ului, D-039), iar documentația API
 * (D-043) arată adresa frontend-ului, nu pe a backend-ului.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ForwardedHeadersIT {

    private static final String FRONTEND = "p21-frontend.example";

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbc;

    private final HttpClient http = HttpClient.newHttpClient();
    private AuthFixtures users;
    private String username;

    @BeforeEach
    void setUp() {
        users = new AuthFixtures(jdbc);
        username = users.user("ADMIN");
    }

    @AfterEach
    void tearDown() {
        users.deleteUser(username);
    }

    private HttpResponse<String> send(String method, String path, String body, String... headers) throws Exception {
        List<String> all = new ArrayList<>(List.of("X-Forwarded-Proto", "https", "X-Forwarded-Host", FRONTEND));
        all.addAll(List.of(headers));
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body))
                .headers(all.toArray(String[]::new));
        return http.send(b.build(), HttpResponse.BodyHandlers.ofString());
    }

    private static String setCookie(HttpResponse<?> r, String name) {
        return r.headers().allValues("Set-Cookie").stream()
                .filter(c -> c.startsWith(name + "=") && !c.startsWith(name + "=;")).reduce((a, b) -> b).orElseThrow();
    }

    private static String value(String setCookie) {
        int end = setCookie.indexOf(';');
        return setCookie.substring(setCookie.indexOf('=') + 1, end < 0 ? setCookie.length() : end);
    }

    @Test
    void cookiesAreSecureWithoutDomainAndTheFlowWorks() throws Exception {
        HttpResponse<String> csrf = send("GET", "/api/csrf", null);
        String xsrfCookie = setCookie(csrf, "XSRF-TOKEN");
        assertThat(xsrfCookie).containsIgnoringCase("; Secure").doesNotContainIgnoringCase("Domain=");
        String xsrf = value(xsrfCookie);

        HttpResponse<String> login = send("POST", "/api/login", AuthFixtures.body(username, PASSWORD),
                "Content-Type", "application/json", "Cookie", "XSRF-TOKEN=" + xsrf, "X-XSRF-TOKEN", xsrf);
        assertThat(login.statusCode()).isEqualTo(200);
        String session = setCookie(login, "JSESSIONID");
        assertThat(session).containsIgnoringCase("Secure").containsIgnoringCase("HttpOnly")
                .containsIgnoringCase("SameSite=Lax").doesNotContainIgnoringCase("Domain=");
        String newXsrfCookie = setCookie(login, "XSRF-TOKEN");
        assertThat(newXsrfCookie).containsIgnoringCase("; Secure").doesNotContainIgnoringCase("Domain=");

        String cookies = "JSESSIONID=" + value(session) + "; XSRF-TOKEN=" + value(newXsrfCookie);
        assertThat(send("GET", "/api/me", null, "Cookie", cookies).statusCode()).isEqualTo(200);

        HttpResponse<String> docs = send("GET", "/api/docs", null, "Cookie", cookies);
        assertThat(docs.statusCode()).isEqualTo(200);
        assertThat(docs.body()).contains("\"url\":\"https://" + FRONTEND + "\"");

        assertThat(send("POST", "/api/logout", null, "Cookie", cookies, "X-XSRF-TOKEN", value(newXsrfCookie)).statusCode())
                .isEqualTo(204);
    }
}
