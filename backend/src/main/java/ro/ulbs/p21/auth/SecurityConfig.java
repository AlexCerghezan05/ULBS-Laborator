package ro.ulbs.p21.auth;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfAuthenticationStrategy;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.security.web.savedrequest.NullRequestCache;

/**
 * D-012, D-039, D-041, C-001: sesiune pe server, CSRF cu cookie (SPA), răspunsuri JSON în loc de
 * redirect. Publice sunt doar rutele de mai jos; restul cer login, iar rolul îl verifică
 * @PreAuthorize pe fiecare rută (PATTERN-004).
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    /** La login: id de sesiune nou și token CSRF nou. Repository-ul de cookie e fără stare, ca cel din spa(). */
    @Bean
    SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new CompositeSessionAuthenticationStrategy(List.of(
                new ChangeSessionIdAuthenticationStrategy(),
                new CsrfAuthenticationStrategy(CookieCsrfTokenRepository.withHttpOnlyFalse())));
    }

    @Bean
    SecurityFilterChain api(HttpSecurity http, AppUserRepository users, SecurityContextRepository contexts) throws Exception {
        AuthenticationEntryPoint unauthorized = (request, response, e) -> {
            boolean expired = request.getAttribute(ActiveUserFilter.SESSION_REVOKED) == null
                    && request.getRequestedSessionId() != null && !request.isRequestedSessionIdValid();
            ApiError.write(response, HttpStatus.UNAUTHORIZED, expired ? "SESSION_EXPIRED" : "UNAUTHENTICATED");
        };
        AccessDeniedHandler forbidden = (request, response, e) ->
                ApiError.write(response, HttpStatus.FORBIDDEN, e instanceof CsrfException ? "CSRF" : "FORBIDDEN");

        http
                .authorizeHttpRequests(a -> a
                        .requestMatchers(HttpMethod.POST, "/api/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/csrf", "/api/health").permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated())
                .csrf(c -> c.spa())
                .securityContext(s -> s.securityContextRepository(contexts))
                .requestCache(r -> r.requestCache(new NullRequestCache()))
                .exceptionHandling(e -> e.authenticationEntryPoint(unauthorized).accessDeniedHandler(forbidden))
                .logout(l -> l.logoutUrl("/api/logout")
                        .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT))
                        .deleteCookies("JSESSIONID"))
                .addFilterBefore(new ActiveUserFilter(users), AnonymousAuthenticationFilter.class);
        return http.build();
    }
}
