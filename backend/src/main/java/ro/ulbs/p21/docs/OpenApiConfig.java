package ro.ulbs.p21.docs;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Specificația OpenAPI (D-002, D-043): ce nu se vede din adnotările controllerelor. Erorile comune
 * din C-001 pe fiecare rută ({"error":"COD"}, PATTERN-005) și logout-ul, pe care îl face filtrul
 * Spring Security, nu un controller.
 */
@Configuration
class OpenApiConfig {

    static final String ERROR_SCHEMA = "#/components/schemas/ApiError";
    private static final String JSON = "application/json";

    /** Rutele fără sesiune (SecurityConfig); logout-ul răspunde 204 și fără sesiune. */
    private static final Set<String> NO_SESSION = Set.of("/api/login", "/api/csrf", "/api/health", "/api/logout");

    private static final Set<PathItem.HttpMethod> WRITES = Set.of(PathItem.HttpMethod.POST, PathItem.HttpMethod.PUT,
            PathItem.HttpMethod.PATCH, PathItem.HttpMethod.DELETE);

    @Bean
    OpenAPI p21OpenApi() {
        return new OpenAPI()
                .info(new Info().title("P21 Facturare API").version("v1").description(
                        "API-ul aplicației, prin nginx la /api/. Erorile au corpul {\"error\":\"COD\"}; textul pentru "
                                + "utilizator îl alege frontend-ul după cod. Cererile POST/PUT/PATCH/DELETE trimit "
                                + "header-ul X-XSRF-TOKEN cu valoarea cookie-ului XSRF-TOKEN (GET /api/csrf)."))
                // Relativ: în spatele nginx, URL-ul detectat ar pierde portul.
                .servers(List.of(new Server().url("/")))
                .components(new Components()
                        .addSchemas("ApiError", new ObjectSchema()
                                .addProperty("error", new StringSchema().example("UNAUTHENTICATED"))
                                .addRequiredItem("error"))
                        .addSecuritySchemes("session", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY).in(SecurityScheme.In.COOKIE).name("JSESSIONID"))
                        .addSecuritySchemes("csrf", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY).in(SecurityScheme.In.HEADER).name("X-XSRF-TOKEN")));
    }

    @Bean
    OpenApiCustomizer c001() {
        return api -> {
            api.path("/api/logout", new PathItem().post(new Operation()
                    .operationId("logout")
                    .summary("Logout")
                    .description("Invalidează sesiunea și șterge cookie-ul JSESSIONID. Cere CSRF.")
                    .responses(new ApiResponses().addApiResponse("204", new ApiResponse()
                            .description("Sesiunea a fost închisă.")))));

            api.getPaths().forEach((path, item) -> item.readOperationsMap().forEach((method, op) -> {
                boolean session = !NO_SESSION.contains(path);
                boolean writes = WRITES.contains(method);
                if (session) {
                    error(op, "401", "Fără sesiune validă.", "UNAUTHENTICATED", "SESSION_EXPIRED");
                    error(op, "403", "Rol fără drept pe rută (D-012).", "FORBIDDEN");
                    op.addSecurityItem(new SecurityRequirement().addList("session"));
                }
                if (writes) {
                    error(op, "403", "Token CSRF lipsă sau greșit.", "CSRF");
                    op.addSecurityItem(new SecurityRequirement().addList("csrf"));
                }
            }));
        };
    }

    /** Adaugă codurile ca exemple pe răspunsul cu statusul dat; păstrează exemplele din adnotări. */
    private static void error(Operation op, String status, String description, String... codes) {
        ApiResponses responses = op.getResponses() != null ? op.getResponses() : new ApiResponses();
        op.setResponses(responses);
        ApiResponse response = responses.computeIfAbsent(status, s -> new ApiResponse().description(description));
        if (response.getContent() == null) {
            response.setContent(new Content());
        }
        MediaType json = response.getContent().computeIfAbsent(JSON, t -> new MediaType());
        if (json.getSchema() == null) {
            json.setSchema(new Schema<>().$ref(ERROR_SCHEMA));
        }
        for (String code : codes) {
            json.addExamples(code, new Example().value(Map.of("error", code)));
        }
    }
}
