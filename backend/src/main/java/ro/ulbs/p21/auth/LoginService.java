package ro.ulbs.p21.auth;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.ulbs.p21.auth.AppUserRepository.LoginRow;

/**
 * Verificările de login, în ordinea din decizii: blocarea (D-036), parola, apoi contul dezactivat,
 * doar după o parolă corectă (D-035). Încercările greșite se numără pe cont (D-037, D-038).
 */
@Service
class LoginService {

    static final int MAX_FAILURES = 5;
    static final Duration LOCK = Duration.ofMinutes(15);

    enum Outcome { OK, BAD_CREDENTIALS, ACCOUNT_DISABLED, LOGIN_LOCKED }

    record Result(Outcome outcome, AppPrincipal principal) {
        static Result of(Outcome outcome) {
            return new Result(outcome, null);
        }
    }

    private final AppUserRepository users;
    private final PasswordEncoder encoder;
    /** Hash cu care se compară parola când contul nu există sau n-are parolă: timpul de răspuns e același. */
    private final String dummyHash;

    LoginService(AppUserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
        this.dummyHash = encoder.encode(UUID.randomUUID().toString());
    }

    /**
     * O singură tranzacție, cu lock pe rândul contului: blocarea, parola și numărătoarea se
     * verifică și se scriu fără ca o altă încercare pe același cont să intervină (PR #3).
     */
    @Transactional
    Result login(String username, String password) {
        String pw = password == null ? "" : password;
        Optional<LoginRow> found = username == null ? Optional.empty() : users.findForLogin(username);
        if (found.isEmpty()) {
            encoder.matches(pw, dummyHash);
            return Result.of(Outcome.BAD_CREDENTIALS);
        }
        LoginRow user = found.get();
        if (user.locked()) {
            return Result.of(Outcome.LOGIN_LOCKED);
        }
        if (!passwordMatches(pw, user.passwordHash())) {
            users.recordFailure(user.id(), MAX_FAILURES, LOCK);
            return Result.of(Outcome.BAD_CREDENTIALS);
        }
        if (!user.active()) {
            return Result.of(Outcome.ACCOUNT_DISABLED);
        }
        users.resetFailures(user.id());
        return new Result(Outcome.OK, new AppPrincipal(user.id(), user.username(), user.fullName(), user.role()));
    }

    /** Un cont fără parolă (seed fără hash) nu se poate loga; compararea cu hash-ul fals ține timpul egal. */
    private boolean passwordMatches(String password, String hash) {
        if (hash == null) {
            encoder.matches(password, dummyHash);
            return false;
        }
        return encoder.matches(password, hash);
    }
}
