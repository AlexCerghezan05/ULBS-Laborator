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

/**
 * V6: încasări parțiale și multiple pe o factură; chitanță doar pentru numerar, numerotată pe
 * seria ei de trigger; încasarea care are chitanță nu se mai schimbă (D-033).
 */
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

    @Test
    void severalPartialPaymentsOnOneInvoiceAreAccepted() {
        fx.payment(invoice, "100.00", "NUMERAR");
        fx.payment(invoice, "160.00", "TRANSFER");
        fx.payment(invoice, "100.00", "NUMERAR");
        assertThat(jdbc.queryForObject("select sum(amount)::text from payment where invoice_id = ?", String.class, invoice))
                .isEqualTo("360.00");
    }

    @Test
    void zeroAmountIsRejected() {
        assertThatThrownBy(() -> fx.payment(invoice, "0.00", "NUMERAR")).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void unknownMethodIsRejected() {
        assertThatThrownBy(() -> fx.payment(invoice, "1.00", "CARD")).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void paymentOnAProformaIsRejected() {
        long proforma = fx.invoice(fx.series("PROFORMA"), Map.of("doc_type", "PROFORMA", "series_type", "PROFORMA"));
        assertThatThrownBy(() -> fx.payment(proforma, "1.00", "NUMERAR")).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void receiptsForCashPaymentsAreNumberedInTheirOwnSeries() {
        long receipts = fx.series("CHITANTA");
        long first = fx.receipt(fx.payment(invoice, "100.00", "NUMERAR"), receipts);
        long second = fx.receipt(fx.payment(invoice, "260.00", "NUMERAR"), receipts);
        assertThat(jdbc.queryForList("select number from receipt where id in (?, ?) order by id",
                Integer.class, first, second)).containsExactly(1, 2);
    }

    @Test
    void receiptNumberGivenByTheCallerIsIgnored() {
        long receipts = fx.series("CHITANTA");
        long payment = fx.payment(invoice, "100.00", "NUMERAR");
        long id = jdbc.queryForObject("""
                insert into receipt (payment_id, series_id, issue_date, number)
                values (?, ?, date '2026-11-10', 21) returning id""", Long.class, payment, receipts);
        assertThat(jdbc.queryForObject("select number from receipt where id = ?", Integer.class, id)).isEqualTo(1);
    }

    @Test
    void receiptForATransferIsRejected() {
        long receipts = fx.series("CHITANTA");
        long payment = fx.payment(invoice, "360.00", "TRANSFER");
        assertThatThrownBy(() -> fx.receipt(payment, receipts)).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void secondReceiptForTheSamePaymentIsRejected() {
        long receipts = fx.series("CHITANTA");
        long payment = fx.payment(invoice, "360.00", "NUMERAR");
        fx.receipt(payment, receipts);
        assertThatThrownBy(() -> fx.receipt(payment, receipts)).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void receiptOnAnInvoiceSeriesIsRejected() {
        long invoices = fx.series("FACTURA");
        long payment = fx.payment(invoice, "360.00", "NUMERAR");
        assertThatThrownBy(() -> fx.receipt(payment, invoices)).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void paymentAmountWithMoreThan2DecimalsIsRejected() {
        assertThatThrownBy(() -> fx.payment(invoice, "10.005", "NUMERAR")).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void paymentWithoutReceiptCanBeCorrected() {
        long payment = fx.payment(invoice, "100.00", "TRANSFER");
        jdbc.update("update payment set amount = 120.00 where id = ?", payment);
    }

    @Test
    void changingAPaymentThatHasAReceiptIsRejected() {
        long payment = fx.payment(invoice, "360.00", "NUMERAR");
        fx.receipt(payment, fx.series("CHITANTA"));
        assertThatThrownBy(() -> jdbc.update("update payment set amount = 1.00 where id = ?", payment))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void movingAPaymentThatHasAReceiptToAnotherInvoiceIsRejected() {
        long other = fx.invoice(fx.series("FACTURA"));
        long payment = fx.payment(invoice, "360.00", "NUMERAR");
        fx.receipt(payment, fx.series("CHITANTA"));
        assertThatThrownBy(() -> jdbc.update("update payment set invoice_id = ? where id = ?", other, payment))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void deletingAPaymentThatHasAReceiptIsRejected() {
        long payment = fx.payment(invoice, "360.00", "NUMERAR");
        fx.receipt(payment, fx.series("CHITANTA"));
        assertThatThrownBy(() -> jdbc.update("delete from payment where id = ?", payment))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
