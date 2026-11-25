package ro.ulbs.p21.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * D-041: la fiecare cerere autentificată, contul trebuie să fie încă activ. Altfel sesiunea se
 * invalidează și cererea continuă ca anonimă: o rută protejată răspunde 401, una publică (login) merge.
 * Nu e @Component, ca să nu fie înregistrat și ca filtru servlet separat.
 */
class ActiveUserFilter extends OncePerRequestFilter {

    /** Marchează cererea, ca răspunsul să fie UNAUTHENTICATED, nu SESSION_EXPIRED. */
    static final String SESSION_REVOKED = ActiveUserFilter.class.getName() + ".revoked";

    private final AppUserRepository users;

    ActiveUserFilter(AppUserRepository users) {
        this.users = users;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AppPrincipal p && !users.isActive(p.id())) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            SecurityContextHolder.clearContext();
            request.setAttribute(SESSION_REVOKED, true);
        }
        chain.doFilter(request, response);
    }
}
