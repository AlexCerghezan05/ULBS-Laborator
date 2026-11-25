package ro.ulbs.p21.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * Login-ul e doar al aplicației (C-001): Spring Boot nu creează utilizatorul „user” cu parolă
 * generată, care ar fi scrisă în log la fiecare pornire (review-ul de securitate, PR #3).
 */
@SpringBootTest
class NoDefaultUserIT {

    @Autowired
    private ApplicationContext context;

    @Test
    void thereIsNoGeneratedDefaultUser() {
        assertThat(context.getBeansOfType(UserDetailsService.class)).isEmpty();
    }
}
