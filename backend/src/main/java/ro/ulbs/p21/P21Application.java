package ro.ulbs.p21;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;

/**
 * Login-ul e doar al aplicației (C-001): fără utilizatorul implicit al Spring Boot, care și-ar
 * scrie parola generată în log la fiecare pornire.
 */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class P21Application {

    public static void main(String[] args) {
        SpringApplication.run(P21Application.class, args);
    }
}
