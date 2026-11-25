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
 * D-005: numărul e unic pe (serie, an); anul vine din data emiterii. Proforma ia număr din
 * seria ei de proforme, nu din seria de facturi.
 */
@SpringBootTest
@Transactional
class InvoiceNumberingIT {

    @Autowired
    private JdbcTemplate jdbc;

    private DbFixtures fx;

    @BeforeEach
    void setUp() {
        fx = new DbFixtures(jdbc);
    }

    @Test
    void sameNumberInSameSeriesAndYearIsRejected() {
        long series = fx.series("FACTURA");
        fx.invoice(series, Map.of("number", 7));
        assertThatThrownBy(() -> fx.invoice(series, Map.of("number", 7)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void sameNumberInAnotherYearIsAccepted() {
        long series = fx.series("FACTURA");
        fx.invoice(series, Map.of("number", 1));
        fx.invoice(series, Map.of("number", 1,
                "issue_date", LocalDate.of(2027, 1, 4), "due_date", LocalDate.of(2027, 1, 4)));
        assertThat(jdbc.queryForList("select fiscal_year from invoice where series_id = ? order by fiscal_year",
                Integer.class, series)).containsExactly(2026, 2027);
    }

    @Test
    void proformaOnItsOwnSeriesDoesNotConsumeAnInvoiceNumber() {
        long invoices = fx.series("FACTURA");
        long proformas = fx.series("PROFORMA");
        assertThat(fx.allocate(invoices, 2026)).isEqualTo(1);
        fx.invoice(proformas, Map.of("doc_type", "PROFORMA", "series_type", "PROFORMA"));
        fx.invoice(proformas, Map.of("doc_type", "PROFORMA", "series_type", "PROFORMA"));
        assertThat(fx.allocate(invoices, 2026)).isEqualTo(2);
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
        assertThatThrownBy(() -> fx.invoice(invoices, Map.of("doc_type", "PROFORMA", "series_type", "PROFORMA")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void invoiceOnAReceiptSeriesIsRejected() {
        long receipts = fx.series("CHITANTA");
        assertThatThrownBy(() -> fx.invoice(receipts, Map.of("series_type", "CHITANTA")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
