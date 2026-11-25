package ro.ulbs.p21.db;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

/** D-033: chitanța emisă nu se modifică și nu se șterge (numerotare fără goluri, ca la facturi). */
@SpringBootTest
class ReceiptImmutableIT {

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private TransactionTemplate tx;

    private long invoiceSeries;
    private long receiptSeries;
    private long invoiceId;
    private long paymentId;
    private long receiptId;

    @BeforeEach
    void issueReceipt() {
        tx.executeWithoutResult(s -> {
            DbFixtures fx = new DbFixtures(jdbc);
            invoiceSeries = fx.series("FACTURA");
            receiptSeries = fx.series("CHITANTA");
            invoiceId = fx.invoice(invoiceSeries);
            paymentId = fx.payment(invoiceId, "360.00", "NUMERAR");
            receiptId = fx.receipt(paymentId, receiptSeries);
        });
    }

    /** Curățenie de test: doar un superuser poate ocoli trigger-ele, cu session_replication_role. */
    @AfterEach
    void removeReceipt() {
        tx.executeWithoutResult(s -> {
            jdbc.execute("set local session_replication_role = replica");
            jdbc.update("delete from receipt where id = ?", receiptId);
            jdbc.update("delete from payment where id = ?", paymentId);
            jdbc.update("delete from invoice where id = ?", invoiceId);
            jdbc.update("delete from series_counter where series_id in (?, ?)", invoiceSeries, receiptSeries);
            jdbc.update("delete from series where id in (?, ?)", invoiceSeries, receiptSeries);
        });
    }

    @Test
    void updatingAReceiptIsRejected() {
        assertThatThrownBy(() -> jdbc.update("update receipt set issue_date = issue_date + 1 where id = ?", receiptId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void deletingAReceiptIsRejected() {
        assertThatThrownBy(() -> jdbc.update("delete from receipt where id = ?", receiptId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
