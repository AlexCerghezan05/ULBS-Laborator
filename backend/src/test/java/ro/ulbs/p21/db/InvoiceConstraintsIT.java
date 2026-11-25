package ro.ulbs.p21.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/** V5: termen de plată (D-016), monedă și curs (D-008), totaluri, discount (D-013). */
@SpringBootTest
@Transactional
class InvoiceConstraintsIT {

    @Autowired
    private JdbcTemplate jdbc;

    private DbFixtures fx;
    private long series;

    @BeforeEach
    void setUp() {
        fx = new DbFixtures(jdbc);
        series = fx.series("FACTURA");
    }

    @Test
    void dueDateBeforeIssueDateIsRejected() {
        assertThatThrownBy(() -> fx.invoice(series, Map.of("due_date", LocalDate.of(2026, 11, 9))))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void eurInvoiceWithRateFromThePreviousBankingDayIsAccepted() {
        fx.invoice(series, Map.of("currency", "EUR",
                "exchange_rate", new BigDecimal("5.0912"), "exchange_rate_date", LocalDate.of(2026, 11, 9),
                "net_total_ron", new BigDecimal("1514.75"), "vat_total_ron", new BigDecimal("318.10"),
                "total_ron", new BigDecimal("1832.85")));
    }

    @Test
    void eurInvoiceWithoutRateIsRejected() {
        assertThatThrownBy(() -> fx.invoice(series, Map.of("currency", "EUR")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rateFromTheIssueDayIsRejected() {
        assertThatThrownBy(() -> fx.invoice(series, Map.of("currency", "EUR",
                "exchange_rate", new BigDecimal("5.0912"), "exchange_rate_date", LocalDate.of(2026, 11, 10))))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void ronInvoiceWithARateIsRejected() {
        assertThatThrownBy(() -> fx.invoice(series, Map.of(
                "exchange_rate", new BigDecimal("1.0000"), "exchange_rate_date", LocalDate.of(2026, 11, 9))))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void otherCurrencyIsRejected() {
        assertThatThrownBy(() -> fx.invoice(series, Map.of("currency", "GBP",
                "exchange_rate", new BigDecimal("5.9"), "exchange_rate_date", LocalDate.of(2026, 11, 9))))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void totalDifferentFromNetPlusVatIsRejected() {
        assertThatThrownBy(() -> fx.invoice(series, Map.of("total", new BigDecimal("360.01"),
                "total_ron", new BigDecimal("360.01"))))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void ronEquivalentDifferentFromTheTotalOfARonInvoiceIsRejected() {
        assertThatThrownBy(() -> fx.invoice(series, Map.of("net_total_ron", new BigDecimal("297.00"),
                "vat_total_ron", new BigDecimal("63.00"))))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void discountAbove100IsRejected() {
        long invoice = fx.invoice(series);
        jdbc.update("""
                insert into invoice_line (invoice_id, position, name, unit, quantity, unit_price, discount_percent, vat_rate_code, net_amount)
                values (?, 1, 'X', 'buc', 1, 100, 10, 'S21', 90)""", invoice);
        assertThatThrownBy(() -> jdbc.update("""
                insert into invoice_line (invoice_id, position, name, unit, quantity, unit_price, discount_percent, vat_rate_code, net_amount)
                values (?, 2, 'X', 'buc', 1, 100, 101, 'S21', 0)""", invoice))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void lineAndVatSummaryCopyThePercentAndCategoryOfTheRate() {
        long invoice = fx.invoice(series);
        fx.line(invoice, 1, "1", "297.52");
        jdbc.update("""
                insert into invoice_vat_summary (invoice_id, vat_rate_code, taxable_amount, vat_amount)
                values (?, 'S21', 297.52, 62.48)""", invoice);
        assertThat(jdbc.queryForObject("""
                select l.vat_percent::text || ' ' || l.vat_category_code || ' / ' || v.vat_percent::text || ' ' || v.vat_category_code
                from invoice_line l join invoice_vat_summary v on v.invoice_id = l.invoice_id where l.invoice_id = ?""",
                String.class, invoice)).isEqualTo("21.00 S / 21.00 S");
    }

    @Test
    void aPercentGivenByTheCallerIsReplacedByTheRate() {
        long invoice = fx.invoice(series);
        jdbc.update("""
                insert into invoice_line (invoice_id, position, name, unit, quantity, unit_price, vat_rate_code,
                                          vat_percent, vat_category_code, net_amount)
                values (?, 1, 'X', 'buc', 1, 100, 'AE', 19.00, 'S', 100)""", invoice);
        assertThat(jdbc.queryForObject("select vat_percent::text || ' ' || vat_category_code from invoice_line where invoice_id = ?",
                String.class, invoice)).isEqualTo("0.00 AE");
    }

    @Test
    void lineWithAnUnknownVatRateIsRejected() {
        long invoice = fx.invoice(series);
        assertThatThrownBy(() -> jdbc.update("""
                insert into invoice_line (invoice_id, position, name, unit, quantity, unit_price, vat_rate_code, net_amount)
                values (?, 1, 'X', 'buc', 1, 100, 'S19', 100)""", invoice))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
