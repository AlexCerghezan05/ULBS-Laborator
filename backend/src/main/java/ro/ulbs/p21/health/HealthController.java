package ro.ulbs.p21.health;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Verificarea de stare a serverului, citită de pagina DS-p21-health. */
@RestController
class HealthController {

    @Operation(summary = "Starea serverului", description = "Public. Citit de pagina DS-p21-health.")
    @ApiResponse(responseCode = "200", description = "Serverul răspunde.", content = @Content(mediaType = "application/json",
            examples = @ExampleObject(name = "UP", value = "{\"status\":\"UP\"}")))
    @GetMapping("/api/health")
    Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
