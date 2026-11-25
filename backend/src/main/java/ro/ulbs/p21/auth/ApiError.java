package ro.ulbs.p21.auth;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/** Erorile API: {"error":"COD"}, cu un cod stabil; textul pentru utilizator îl pune frontend-ul (C-001). */
final class ApiError {

    private ApiError() {
    }

    static ResponseEntity<Map<String, String>> response(HttpStatus status, String code) {
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(Map.of("error", code));
    }

    /** Pentru filtrele Spring Security, care scriu direct în răspuns. Codurile sunt constante, fără escape. */
    static void write(HttpServletResponse response, HttpStatus status, String code) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"error\":\"" + code + "\"}");
    }
}
