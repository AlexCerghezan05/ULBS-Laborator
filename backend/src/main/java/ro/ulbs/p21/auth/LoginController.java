package ro.ulbs.p21.auth;

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

    @PreAuthorize(Access.READ)
    @GetMapping("/api/me")
    Me me(@AuthenticationPrincipal AppPrincipal principal) {
        return toMe(principal);
    }

    /** Pune cookie-ul XSRF-TOKEN înaintea primului POST (login). */
    @GetMapping("/api/csrf")
    ResponseEntity<Void> csrf(CsrfToken token) {
        token.getToken();
        return ResponseEntity.noContent().build();
    }
}
