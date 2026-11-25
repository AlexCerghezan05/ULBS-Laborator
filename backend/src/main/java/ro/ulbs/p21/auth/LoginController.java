package ro.ulbs.p21.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Login, utilizatorul curent și tokenul CSRF (C-001). Logout-ul îl face filtrul Spring Security. */
@RestController
class LoginController {

    record LoginRequest(String username, String password) {
    }

    /** Utilizatorul curent și denumirea firmei, pentru antetul DS-p21-app-shell. */
    record Me(String username, String fullName, String role, String companyName) {
    }

    private final LoginService logins;
    private final AppUserRepository users;
    private final SessionAuthenticationStrategy sessionStrategy;
    private final SecurityContextRepository contextRepository;

    LoginController(LoginService logins, AppUserRepository users, SessionAuthenticationStrategy sessionStrategy,
                    SecurityContextRepository contextRepository) {
        this.logins = logins;
        this.users = users;
        this.sessionStrategy = sessionStrategy;
        this.contextRepository = contextRepository;
    }

    private Me toMe(AppPrincipal p) {
        return new Me(p.username(), p.fullName(), p.role(), users.companyName());
    }

    /** Exemplu de răspuns pentru documentație (date inventate). */
    private static final String ME_EXAMPLE =
            "{\"username\":\"admin\",\"fullName\":\"Ion Popescu\",\"role\":\"ADMIN\",\"companyName\":\"Firma Exemplu SRL\"}";

    @Operation(summary = "Login", description = "Public, cere CSRF. Deschide o sesiune nouă: cookie JSESSIONID nou și XSRF-TOKEN nou (D-039).")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = @ExampleObject(
            name = "login", value = "{\"username\":\"admin\",\"password\":\"parola-exemplu\"}")))
    @ApiResponse(responseCode = "200", description = "Login reușit; utilizatorul și firma. role: ADMIN | OPERATOR | CONTABIL; companyName null dacă firma nu e completată.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Me.class),
                    examples = @ExampleObject(name = "OK", value = ME_EXAMPLE)))
    @ApiResponse(responseCode = "401", description = "Nume sau parolă greșite.", content = @Content(mediaType = "application/json",
            schema = @Schema(ref = "#/components/schemas/ApiError"),
            examples = @ExampleObject(name = "BAD_CREDENTIALS", value = "{\"error\":\"BAD_CREDENTIALS\"}")))
    @ApiResponse(responseCode = "403", description = "Cont dezactivat, doar cu parola corectă (D-035).", content = @Content(mediaType = "application/json",
            schema = @Schema(ref = "#/components/schemas/ApiError"),
            examples = @ExampleObject(name = "ACCOUNT_DISABLED", value = "{\"error\":\"ACCOUNT_DISABLED\"}")))
    @ApiResponse(responseCode = "429", description = "Cont blocat 15 minute după 5 încercări greșite (D-036–D-038).", content = @Content(mediaType = "application/json",
            schema = @Schema(ref = "#/components/schemas/ApiError"),
            examples = @ExampleObject(name = "LOGIN_LOCKED", value = "{\"error\":\"LOGIN_LOCKED\"}")))
    @PostMapping("/api/login")
    ResponseEntity<?> login(@RequestBody LoginRequest body, HttpServletRequest request, HttpServletResponse response) {
        LoginService.Result result = logins.login(body.username(), body.password());
        return switch (result.outcome()) {
            case BAD_CREDENTIALS -> ApiError.response(HttpStatus.UNAUTHORIZED, "BAD_CREDENTIALS");
            case ACCOUNT_DISABLED -> ApiError.response(HttpStatus.FORBIDDEN, "ACCOUNT_DISABLED");
            case LOGIN_LOCKED -> ApiError.response(HttpStatus.TOO_MANY_REQUESTS, "LOGIN_LOCKED");
            case OK -> {
                startSession(result.principal(), request, response);
                yield ResponseEntity.ok(toMe(result.principal()));
            }
        };
    }

    /** Id de sesiune nou și token CSRF nou (session fixation), apoi contextul salvat în sesiune. */
    private void startSession(AppPrincipal principal, HttpServletRequest request, HttpServletResponse response) {
        var auth = UsernamePasswordAuthenticationToken.authenticated(principal, null,
                List.of(new SimpleGrantedAuthority("ROLE_" + principal.role())));
        sessionStrategy.onAuthentication(auth, request, response);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        contextRepository.saveContext(context, request, response);
        // Tokenul CSRF nou se scrie în cookie doar când e citit: răspunsul la login îl trimite deja.
        if (request.getAttribute(CsrfToken.class.getName()) instanceof CsrfToken token) {
            token.getToken();
        }
    }

    @Operation(summary = "Utilizatorul curent", description = "Aceeași formă ca răspunsul la login; pentru antetul DS-p21-app-shell.")
    @ApiResponse(responseCode = "200", description = "Utilizatorul din sesiune.", content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = Me.class), examples = @ExampleObject(name = "OK", value = ME_EXAMPLE)))
    @PreAuthorize(Access.READ)
    @GetMapping("/api/me")
    Me me(@AuthenticationPrincipal AppPrincipal principal) {
        return toMe(principal);
    }

    /** Pune cookie-ul XSRF-TOKEN înaintea primului POST (login). */
    @Operation(summary = "Token CSRF", description = "Public. Pune cookie-ul XSRF-TOKEN (citibil din JS), înaintea primului POST.")
    @ApiResponse(responseCode = "204", description = "Cookie-ul XSRF-TOKEN e setat.")
    @GetMapping("/api/csrf")
    ResponseEntity<Void> csrf(@Parameter(hidden = true) CsrfToken token) {
        token.getToken();
        return ResponseEntity.noContent().build();
    }
}
