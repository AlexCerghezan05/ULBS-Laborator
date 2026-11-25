package ro.ulbs.p21.auth;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * Fiecare rută a aplicației își declară accesul cu @PreAuthorize (D-012, PATTERN-004).
 * Rutele publice sunt doar cele de mai jos; una nouă se adaugă aici și în SecurityConfig, cu motiv.
 * Testul compară lista cu regulile din SecurityConfig și interzice permitAll() / isAuthenticated()
 * pe rutele care modifică date (review-ul de securitate, PR #3).
 */
@SpringBootTest
class RouteSecurityCoverageIT {

    private static final Set<String> PUBLIC = Set.of(
            "HealthController#health",
            "LoginController#login",
            "LoginController#csrf");

    /** Expresii care ar lăsa orice utilizator (sau oricine) să modifice date, ocolind rolurile din D-012. */
    private static final Set<String> FORBIDDEN_ON_WRITES = Set.of("permitAll()", "isAuthenticated()");

    private static final Set<RequestMethod> WRITES =
            Set.of(RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE);

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping mapping;

    @Autowired
    private SecurityFilterChain chain;

    private List<Map.Entry<RequestMappingInfo, HandlerMethod>> applicationRoutes() {
        return mapping.getHandlerMethods().entrySet().stream()
                .filter(e -> e.getValue().getBeanType().getPackageName().startsWith("ro.ulbs.p21"))
                .toList();
    }

    private static String name(HandlerMethod h) {
        return h.getBeanType().getSimpleName() + "#" + h.getMethod().getName();
    }

    private static PreAuthorize preAuthorize(HandlerMethod h) {
        PreAuthorize onMethod = AnnotatedElementUtils.findMergedAnnotation(h.getMethod(), PreAuthorize.class);
        return onMethod != null ? onMethod : AnnotatedElementUtils.findMergedAnnotation(h.getBeanType(), PreAuthorize.class);
    }

    /** Metodele HTTP ale rutei; o rută fără metodă declarată le acceptă pe toate. */
    private static Set<RequestMethod> methods(RequestMappingInfo info) {
        Set<RequestMethod> declared = info.getMethodsCondition().getMethods();
        return declared.isEmpty() ? Set.of(RequestMethod.values()) : declared;
    }

    @Test
    void everyApplicationRouteDeclaresItsAccess() {
        List<String> missing = new ArrayList<>();
        List<String> publicFound = new ArrayList<>();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> route : applicationRoutes()) {
            String name = name(route.getValue());
            if (PUBLIC.contains(name)) {
                publicFound.add(name);
            } else if (preAuthorize(route.getValue()) == null) {
                missing.add(name);
            }
        }
        assertThat(missing).as("rute fără @PreAuthorize").isEmpty();
        assertThat(publicFound).as("rutele publice există").containsExactlyInAnyOrderElementsOf(PUBLIC);
    }

    @Test
    void routesThatChangeDataRequireARole() {
        List<String> open = new ArrayList<>();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> route : applicationRoutes()) {
            String name = name(route.getValue());
            PreAuthorize rule = preAuthorize(route.getValue());
            boolean writes = methods(route.getKey()).stream().anyMatch(WRITES::contains);
            if (writes && !PUBLIC.contains(name) && rule != null
                    && FORBIDDEN_ON_WRITES.contains(rule.value().replaceAll("\\s", ""))) {
                open.add(name + " (" + rule.value() + ")");
            }
        }
        assertThat(open).as("rute care modifică date fără rol (D-012)").isEmpty();
    }

    /**
     * Regulile din SecurityConfig, întrebate direct pentru un vizitator fără login: publice trebuie
     * să fie exact rutele din PUBLIC. Nu se trece prin @PreAuthorize, ca o regulă prea largă în
     * config (de ex. permitAll() pe /api/**) să nu fie ascunsă de adnotări.
     */
    @Test
    void securityConfigOpensExactlyThePublicRoutes() {
        AuthorizationManager<HttpServletRequest> rules = chain.getFilters().stream()
                .filter(AuthorizationFilter.class::isInstance)
                .map(f -> ((AuthorizationFilter) f).getAuthorizationManager())
                .findFirst().orElseThrow();
        AnonymousAuthenticationToken visitor = new AnonymousAuthenticationToken(
                "test", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));

        Set<String> openInConfig = new TreeSet<>();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> route : applicationRoutes()) {
            for (String pattern : route.getKey().getPatternValues()) {
                String path = pattern.replaceAll("\\{[^}]*}", "1");
                for (RequestMethod method : methods(route.getKey())) {
                    MockHttpServletRequest request = new MockHttpServletRequest(method.name(), path);
                    AuthorizationResult result = rules.authorize(() -> visitor, request);
                    if (result != null && result.isGranted()) {
                        openInConfig.add(name(route.getValue()));
                    }
                }
            }
        }
        assertThat(openInConfig).as("rutele publice din SecurityConfig").containsExactlyInAnyOrderElementsOf(PUBLIC);
    }
}
