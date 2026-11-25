package ro.ulbs.p21.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/** V2: firma e o singură înregistrare (D-003); utilizatorii au unul din cele 3 roluri (D-012). */
@SpringBootTest
@Transactional
class CompanyUsersIT {

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void companyAcceptsOnlyTheRowWithId1() {
        assertThatThrownBy(() -> jdbc.update("""
                insert into company (id, name, cif, reg_com, address, city, county_code, vat_payer, vat_on_collection)
                values (2, 'Alta', 'RO12345674', 'J2023000123456', 'Str. X', 'Sibiu', 'RO-SB', true, false)"""))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void companyRoundingRuleIsOneOfTheThreeVariantsOrNull() {
        jdbc.update("""
                insert into company (name, cif, reg_com, address, city, county_code, vat_payer, vat_on_collection)
                values ('F', 'RO12345674', 'J2023000123456', 'Str. X', 'Sibiu', 'RO-SB', true, false)
                on conflict (id) do nothing""");
        jdbc.update("update company set rounding_rule = 'GROSS'");
        jdbc.update("update company set rounding_rule = null");
        assertThatThrownBy(() -> jdbc.update("update company set rounding_rule = 'ALTA'"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void userRoleIsAdminOperatorOrContabil() {
        jdbc.update("insert into app_user (username, full_name, role) values ('t-admin', 'T', 'ADMIN')");
        jdbc.update("insert into app_user (username, full_name, role) values ('t-op', 'T', 'OPERATOR')");
        jdbc.update("insert into app_user (username, full_name, role) values ('t-cont', 'T', 'CONTABIL')");
        assertThatThrownBy(() -> jdbc.update(
                "insert into app_user (username, full_name, role) values ('t-x', 'T', 'EMITENT')"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void usernameIsUnique() {
        jdbc.update("insert into app_user (username, full_name, role) values ('t-dup', 'T', 'ADMIN')");
        assertThatThrownBy(() -> jdbc.update(
                "insert into app_user (username, full_name, role) values ('t-dup', 'T', 'OPERATOR')"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void userWithoutPasswordHashIsAllowed() {
        jdbc.update("insert into app_user (username, full_name, role) values ('t-nopw', 'T', 'CONTABIL')");
        assertThat(jdbc.queryForObject(
                "select password_hash from app_user where username = 't-nopw'", String.class)).isNull();
    }
}
