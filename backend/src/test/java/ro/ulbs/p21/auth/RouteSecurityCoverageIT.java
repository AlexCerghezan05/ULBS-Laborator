package ro.ulbs.p21.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * Fiecare rută a aplicației își declară accesul cu @PreAuthorize (D-012, PATTERN-004).
 * Rutele publice sunt doar cele de mai jos; una nouă se adaugă aici și în SecurityConfig, cu motiv.
 */
@SpringBootTest
class RouteSecurityCoverageIT {

    private static final Set<String> PUBLIC = Set.of(
            "HealthController#health",
            "LoginController#login",
            "LoginController#csrf");

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping mapping;

    @Test
    void everyApplicationRouteDeclaresItsAccess() {
        List<String> missing = new ArrayList<>();
        List<String> publicFound = new ArrayList<>();
        for (HandlerMethod h : mapping.getHandlerMethods().values()) {
            if (!h.getBeanType().getPackageName().startsWith("ro.ulbs.p21")) {
                continue;
            }
            String name = h.getBeanType().getSimpleName() + "#" + h.getMethod().getName();
            boolean annotated = AnnotatedElementUtils.hasAnnotation(h.getMethod(), PreAuthorize.class)
                    || AnnotatedElementUtils.hasAnnotation(h.getBeanType(), PreAuthorize.class);
            if (PUBLIC.contains(name)) {
                publicFound.add(name);
            } else if (!annotated) {
                missing.add(name);
            }
        }
        assertThat(missing).as("rute fără @PreAuthorize").isEmpty();
        assertThat(publicFound).as("rutele publice există").containsExactlyInAnyOrderElementsOf(PUBLIC);
    }
}
