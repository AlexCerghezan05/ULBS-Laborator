package ro.ulbs.p21.auth;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/** Citirile și scrierile din app_user pentru login. Timpul e cel al bazei (now()). */
@Repository
public class AppUserRepository {

    record LoginRow(long id, String username, String fullName, String passwordHash, String role,
                    boolean active, boolean locked) {
    }

    private final JdbcTemplate jdbc;

    AppUserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    Optional<LoginRow> findForLogin(String username) {
        List<LoginRow> rows = jdbc.query("""
                select id, username, full_name, password_hash, role, active,
                       coalesce(locked_until > now(), false) as locked
                from app_user where username = ?""",
                (rs, n) -> new LoginRow(rs.getLong("id"), rs.getString("username"), rs.getString("full_name"),
                        rs.getString("password_hash"), rs.getString("role"), rs.getBoolean("active"), rs.getBoolean("locked")),
                username);
        return rows.stream().findFirst();
    }

    /**
     * O încercare greșită (D-036, D-037, D-038). Un singur UPDATE, ca două încercări simultane să nu
     * se piardă; în timpul blocării nu se numără și nu prelungește blocarea.
     */
    void recordFailure(long id, int maxFailures, Duration lock) {
        jdbc.update("""
                update app_user set
                    failed_login_count = case when failed_login_count + 1 >= ? then 0 else failed_login_count + 1 end,
                    locked_until = case when failed_login_count + 1 >= ? then now() + ? * interval '1 second' else locked_until end
                where id = ? and (locked_until is null or locked_until <= now())""",
                maxFailures, maxFailures, lock.toSeconds(), id);
    }

    /** Login reușit: numărătoarea de la zero (D-037). */
    void resetFailures(long id) {
        jdbc.update("update app_user set failed_login_count = 0, locked_until = null where id = ? and (failed_login_count <> 0 or locked_until is not null)", id);
    }

    /** Denumirea firmei furnizor pentru antet (D-003: un singur rând); null dacă firma nu e completată încă. */
    String companyName() {
        return jdbc.queryForList("select name from company where id = 1", String.class).stream().findFirst().orElse(null);
    }

    /** D-041: false și pentru un utilizator care nu mai există. */
    boolean isActive(long id) {
        return jdbc.queryForList("select active from app_user where id = ?", Boolean.class, id).stream()
                .findFirst().orElse(false);
    }
}
