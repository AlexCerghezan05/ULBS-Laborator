package ro.ulbs.p21.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/** V6: încasări parțiale și multiple pe o factură; chitanță doar pentru numerar, numerotată pe seria ei. */
@SpringBootTest
@Transactional
class PaymentReceiptIT {

    @Autowired
    private JdbcTemplate jdbc;

    private DbFixtures fx;
    private long invoice;

    @BeforeEach
    void setUp() {
        fx = new DbFixtures(jdbc);
        invoice = fx.invoice(fx.series("FACTURA"));
    }

    private long pay(long invoiceId, String amount, String method) {
        return jdbc.queryForObject("""
                insert into payment (invoice_id, paid_on, amount, method)
                values (?, date '2026-11-10', ?, ?) returning id""", Long.class, invoiceId, new BigDecimal(amount), method);
    }

    private long receipt(long paymentId, long seriesId) {
        return jdbc.queryForObject("""
                insert into receipt (payment_id, series_id, issue_date, number)
                values (?, ?, date '2026-11-10', allocate_number(?, 2026)) returning id""",
                Long.class, paymentId, seriesId, seriesId);
    }

    @Test
    void severalPartialPaymentsOnOneInvoiceAreAccepted() {
        pay(invoice, "100.00", "NUMERAR");
        pay(invoice, "160.00", "TRANSFER");
        pay(invoice, "100.00", "NUMERAR");
        assertThat(jdbc.queryForObject("select sum(amount)::text from payment where invoice_id = ?", String.class, invoice))
                .isEqualTo("360.00");
    }

    @Test
    void zeroAmountIsRejected() {
        assertThatThrownBy(() -> pay(invoice, "0.00", "NUMERAR")).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void unknownMethodIsRejected() {
        assertThatThrownBy(() -> pay(invoice, "1.00", "CARD")).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void paymentOnAProformaIsRejected() {
        long proforma = fx.invoice(fx.series("PROFORMA"), Map.of("doc_type", "PROFORMA", "series_type", "PROFORMA"));
        assertThatThrownBy(() -> pay(proforma, "1.00", "NUMERAR")).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void receiptsForCashPaymentsAreNumberedInTheirOwnSeries() {
        long receipts = fx.series("CHITANTA");
        long first = receipt(pay(invoice, "100.00", "NUMERAR"), receipts);
        long second = receipt(pay(invoice, "260.00", "NUMERAR"), receipts);
        assertThat(jdbc.queryForList("select number from receipt where id in (?, ?) order by number",
                Integer.class, first, second)).containsExactly(1, 2);
    }

    @Test
    void receiptForATransferIsRejected() {
        long receipts = fx.series("CHITANTA");
        long payment = pay(invoice, "360.00", "TRANSFER");
        assertThatThrownBy(() -> receipt(payment, receipts)).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void secondReceiptForTheSamePaymentIsRejected() {
        long receipts = fx.series("CHITANTA");
        long payment = pay(invoice, "360.00", "NUMERAR");
        receipt(payment, receipts);
        assertThatThrownBy(() -> receipt(payment, receipts)).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void receiptOnAnInvoiceSeriesIsRejected() {
        long invoices = fx.series("FACTURA");
        long payment = pay(invoice, "360.00", "NUMERAR");
        assertThatThrownBy(() -> receipt(payment, invoices)).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void duplicateReceiptNumberIsRejected() {
        long receipts = fx.series("CHITANTA");
        long p1 = pay(invoice, "100.00", "NUMERAR");
        long p2 = pay(invoice, "100.00", "NUMERAR");
        jdbc.update("insert into receipt (payment_id, series_id, issue_date, number) values (?, ?, date '2026-11-10', 5)", p1, receipts);
        assertThatThrownBy(() -> jdbc.update(
                "insert into receipt (payment_id, series_id, issue_date, number) values (?, ?, date '2026-11-10', 5)", p2, receipts))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void paymentAmountWithMoreThan2DecimalsIsRejected() {
        assertThatThrownBy(() -> pay(invoice, "10.005", "NUMERAR")).isInstanceOf(DataIntegrityViolationException.class);
    }
}
