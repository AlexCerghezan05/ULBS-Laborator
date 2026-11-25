package ro.ulbs.p21.db;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/** V3: tipurile de client (D-010, D-019), email opțional (D-022), produsul cu cotă din vat_rate (D-020, D-021). */
@SpringBootTest
@Transactional
class ClientConstraintsIT {

    @Autowired
    private JdbcTemplate jdbc;

    private int insertClient(String type, String cif, String cnp, String email) {
        return jdbc.update("""
                insert into client (client_type, name, cif, cnp, address, city, county_code, email)
                values (?, 'Test', ?, ?, 'Str. X', 'Sibiu', 'RO-SB', ?)""", type, cif, cnp, email);
    }

    @Test
    void personWithoutCnpAndWithoutEmailIsAccepted() {
        insertClient("PF", null, null, null);
        insertClient("PF", null, "1960101123456", "pf@example.com");
    }

    @Test
    void cnpWithWrongFormatIsRejected() {
        assertThatThrownBy(() -> insertClient("PF", null, "12345", null))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void cnpOnACompanyIsRejected() {
        assertThatThrownBy(() -> insertClient("PJ", "RO12345674", "1960101123456", null))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void companyAndPfaNeedACif() {
        insertClient("PJ", "RO12345674", null, null);
        insertClient("PFA", "76543210", null, null);
        assertThatThrownBy(() -> insertClient("PJ", null, null, null))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void pfaWithoutCifIsRejected() {
        assertThatThrownBy(() -> insertClient("PFA", null, null, null))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void personWithACifIsRejected() {
        assertThatThrownBy(() -> insertClient("PF", "76543210", null, null))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void unknownClientTypeIsRejected() {
        assertThatThrownBy(() -> insertClient("SRL", "RO12345674", null, null))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void productNeedsAnExistingVatRate() {
        jdbc.update("insert into product (name, unit, unit_price, vat_rate_code) values ('P', 'buc', 297.52, 'S21')");
        assertThatThrownBy(() -> jdbc.update(
                "insert into product (name, unit, unit_price, vat_rate_code) values ('P', 'buc', 1, 'S19')"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
