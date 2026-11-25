package ro.ulbs.p21.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * CONTEXT.md „Factură emisă”: după commit, factura și liniile ei nu se mai modifică și nu se
 * șterg, nici prin SQL direct. În tranzacția care o creează, liniile se pot adăuga.
 */
@SpringBootTest
class IssuedInvoiceImmutableIT {

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private TransactionTemplate tx;

    private long seriesId;
    private long invoiceId;

    @BeforeEach
    void issueInvoice() {
        tx.executeWithoutResult(s -> {
            DbFixtures fx = new DbFixtures(jdbc);
            seriesId = fx.series("FACTURA");
            invoiceId = fx.invoice(seriesId);
            fx.line(invoiceId, 1, "1", "297.52"); // aceeași tranzacție: permis
            jdbc.update("insert into invoice_vat_summary (invoice_id, vat_rate_code, taxable_amount, vat_amount) values (?, 'S21', 297.52, 62.48)", invoiceId);
            jdbc.update("insert into invoice_legal_note (invoice_id, position, text) values (?, 1, 'Mentiune')", invoiceId);
        });
    }

    /** Curățenie de test: doar un superuser poate ocoli trigger-ele, cu session_replication_role. */
    @AfterEach
    void removeInvoice() {
        tx.executeWithoutResult(s -> {
            jdbc.execute("set local session_replication_role = replica");
            jdbc.update("delete from invoice_legal_note where invoice_id = ?", invoiceId);
            jdbc.update("delete from invoice_vat_summary where invoice_id = ?", invoiceId);
            jdbc.update("delete from invoice_line where invoice_id = ?", invoiceId);
            jdbc.update("delete from invoice where id = ?", invoiceId);
            jdbc.update("delete from series_counter where series_id = ?", seriesId);
            jdbc.update("delete from series where id = ?", seriesId);
        });
    }

    @Test
    void theLinesAddedAtCreationAreKept() {
        assertThat(jdbc.queryForObject("select count(*) from invoice_line where invoice_id = ?", Integer.class, invoiceId))
                .isEqualTo(1);
    }

    @Test
    void updatingTheInvoiceIsRejected() {
        assertThatThrownBy(() -> jdbc.update("update invoice set due_date = due_date + 1 where id = ?", invoiceId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void deletingTheInvoiceIsRejected() {
        assertThatThrownBy(() -> jdbc.update("delete from invoice where id = ?", invoiceId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void updatingALineIsRejected() {
        assertThatThrownBy(() -> jdbc.update("update invoice_line set quantity = 2 where invoice_id = ?", invoiceId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void addingALineIsRejected() {
        assertThatThrownBy(() -> new DbFixtures(jdbc).line(invoiceId, 2, "1", "297.52"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void deletingALineIsRejected() {
        assertThatThrownBy(() -> jdbc.update("delete from invoice_line where invoice_id = ?", invoiceId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void changingTheVatSummaryIsRejected() {
        assertThatThrownBy(() -> jdbc.update("update invoice_vat_summary set vat_amount = ? where invoice_id = ?",
                new BigDecimal("62.47"), invoiceId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void changingTheLegalNotesIsRejected() {
        assertThatThrownBy(() -> jdbc.update("delete from invoice_legal_note where invoice_id = ?", invoiceId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void theInvoiceIsUnchanged() {
        assertThat(jdbc.queryForObject("select total::text from invoice where id = ?", String.class, invoiceId))
                .isEqualTo("360.00");
    }
}
