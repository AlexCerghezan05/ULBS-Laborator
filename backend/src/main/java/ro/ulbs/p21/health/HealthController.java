package ro.ulbs.p21.health;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Verificarea de stare a serverului, citită de pagina DS-p21-health. */
@RestController
class HealthController {

    @GetMapping("/api/health")
    Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
