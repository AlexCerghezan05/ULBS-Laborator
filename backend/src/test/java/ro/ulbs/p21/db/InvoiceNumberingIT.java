package ro.ulbs.p21.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/**
 * D-005: numărul îl pune trigger-ul la INSERT, unic pe (serie, an); anul vine din data emiterii.
 * Proforma ia număr din seria ei de proforme, nu din seria de facturi, și nu-și schimbă tipul,
 * seria sau numărul.
 */
@SpringBootTest
@Transactional
class InvoiceNumberingIT {

    private static final Map<String, Object> PROFORMA = Map.of("doc_type", "PROFORMA", "series_type", "PROFORMA");

    @Autowired
    private JdbcTemplate jdbc;

    private DbFixtures fx;

    @BeforeEach
    void setUp() {
        fx = new DbFixtures(jdbc);
    }

    @Test
    void numberGivenByTheCallerIsIgnored() {
        long series = fx.series("FACTURA");
        long first = fx.invoice(series, Map.of("number", 75));
        long second = fx.invoice(series, Map.of("number", 1));
        assertThat(fx.numberOf(first)).isEqualTo(1);
        assertThat(fx.numberOf(second)).isEqualTo(2);
    }

    @Test
    void uniqueConstraintOnSeriesYearAndNumberExists() {
        assertThat(jdbc.queryForObject("""
                select pg_get_constraintdef(oid) from pg_constraint where conname = 'invoice_number_unique'""",
                String.class)).isEqualTo("UNIQUE (series_id, fiscal_year, number)");
    }

    @Test
    void sameNumberInAnotherYearIsAccepted() {
        long series = fx.series("FACTURA");
        fx.invoice(series);
        fx.invoice(series, Map.of("issue_date", LocalDate.of(2027, 1, 4), "due_date", LocalDate.of(2027, 1, 4)));
        assertThat(jdbc.queryForList("select fiscal_year || '/' || number from invoice where series_id = ? order by fiscal_year",
                String.class, series)).containsExactly("2026/1", "2027/1");
    }

    @Test
    void proformaOnItsOwnSeriesDoesNotConsumeAnInvoiceNumber() {
        long invoices = fx.series("FACTURA");
        long proformas = fx.series("PROFORMA");
        assertThat(fx.numberOf(fx.invoice(invoices))).isEqualTo(1);
        assertThat(fx.numberOf(fx.invoice(proformas, PROFORMA))).isEqualTo(1);
        assertThat(fx.numberOf(fx.invoice(proformas, PROFORMA))).isEqualTo(2);
        assertThat(fx.numberOf(fx.invoice(invoices))).isEqualTo(2);
    }

    @Test
    void proformaOnAnInvoiceSeriesIsRejected() {
        long invoices = fx.series("FACTURA");
        assertThatThrownBy(() -> fx.invoice(invoices, Map.of("doc_type", "PROFORMA", "series_type", "FACTURA")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void proformaClaimingAProformaSeriesThatIsAnInvoiceSeriesIsRejected() {
        long invoices = fx.series("FACTURA");
        assertThatThrownBy(() -> fx.invoice(invoices, PROFORMA))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void invoiceOnAReceiptSeriesIsRejected() {
        long receipts = fx.series("CHITANTA");
        assertThatThrownBy(() -> fx.invoice(receipts, Map.of("series_type", "CHITANTA")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void turningAProformaIntoAnInvoiceIsRejected() {
        long invoices = fx.series("FACTURA");
        long proforma = fx.invoice(fx.series("PROFORMA"), PROFORMA);
        assertThatThrownBy(() -> jdbc.update(
                "update invoice set doc_type = 'FACTURA', series_type = 'FACTURA', series_id = ? where id = ?",
                invoices, proforma))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void changingTheNumberOfAProformaIsRejected() {
        long proforma = fx.invoice(fx.series("PROFORMA"), PROFORMA);
        assertThatThrownBy(() -> jdbc.update("update invoice set number = 75 where id = ?", proforma))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void movingAProformaToAnotherYearIsRejected() {
        long proforma = fx.invoice(fx.series("PROFORMA"), PROFORMA);
        assertThatThrownBy(() -> jdbc.update(
                "update invoice set issue_date = date '2027-01-04', due_date = date '2027-01-04' where id = ?", proforma))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
