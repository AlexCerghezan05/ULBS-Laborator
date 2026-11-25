package ro.ulbs.p21.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/** V7: cursurile BNR (D-008), setările reminder-elor (valori: Q-019) și istoricul lor. */
@SpringBootTest
@Transactional
class BnrRemindersIT {

    @Autowired
    private JdbcTemplate jdbc;

    private DbFixtures fx;

    @BeforeEach
    void setUp() {
        fx = new DbFixtures(jdbc);
    }

    @Test
    void oneRatePerDayAndCurrency() {
        jdbc.update("insert into bnr_rate (rate_date, currency, rate) values (date '2001-01-03', 'EUR', 5.0912)");
        jdbc.update("insert into bnr_rate (rate_date, currency, rate) values (date '2001-01-03', 'USD', 4.3871)");
        assertThatThrownBy(() -> jdbc.update(
                "insert into bnr_rate (rate_date, currency, rate) values (date '2001-01-03', 'EUR', 5.1000)"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rateWithMoreThan4DecimalsIsRejected() {
        assertThatThrownBy(() -> jdbc.update(
                "insert into bnr_rate (rate_date, currency, rate) values (date '2001-01-04', 'EUR', 5.09123)"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void onlyEurAndUsdRates() {
        assertThatThrownBy(() -> jdbc.update(
                "insert into bnr_rate (rate_date, currency, rate) values (date '2001-01-04', 'GBP', 5.9)"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void reminderSettingsIsOneRowWithNoValuesYet() {
        assertThat(jdbc.queryForObject("select count(*) from reminder_settings", Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForMap("""
                select days_after_due, repeat_every_days, email_subject, email_body from reminder_settings""")
                .values()).containsOnlyNulls();
        assertThatThrownBy(() -> jdbc.update("insert into reminder_settings (id) values (2)"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void manualReminderNeedsTheUserAndAutomaticOneHasNone() {
        long invoice = fx.invoice(fx.series("FACTURA"));
        long user = fx.user();
        jdbc.update("insert into reminder_log (invoice_id, recipient_email, kind, sent_by) values (?, 'c@example.com', 'MANUAL', ?)",
                invoice, user);
        jdbc.update("insert into reminder_log (invoice_id, recipient_email, kind) values (?, 'c@example.com', 'AUTO')", invoice);
        assertThatThrownBy(() -> jdbc.update(
                "insert into reminder_log (invoice_id, recipient_email, kind) values (?, 'c@example.com', 'MANUAL')", invoice))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void reminderOnAProformaIsRejected() {
        long proforma = fx.invoice(fx.series("PROFORMA"), Map.of("doc_type", "PROFORMA", "series_type", "PROFORMA"));
        assertThatThrownBy(() -> jdbc.update(
                "insert into reminder_log (invoice_id, recipient_email, kind) values (?, 'c@example.com', 'AUTO')", proforma))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
