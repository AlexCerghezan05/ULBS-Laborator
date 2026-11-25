package ro.ulbs.p21.db;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * TRUNCATE ocolește trigger-ele pe rânduri, deci tabelele cu documente emise și contorul au un
 * trigger BEFORE TRUNCATE. Fiecare încercare rulează într-o tranzacție marcată pentru rollback,
 * ca un eșec al protecției să nu golească baza de test.
 */
@SpringBootTest
class TruncateIT {

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private TransactionTemplate tx;

    @ParameterizedTest
    @ValueSource(strings = {"invoice", "invoice_line", "invoice_vat_summary", "invoice_legal_note",
            "payment", "receipt", "series_counter"})
    void truncateIsRejected(String table) {
        assertThatThrownBy(() -> tx.executeWithoutResult(s -> {
            s.setRollbackOnly();
            jdbc.execute("truncate table " + table + " cascade");
        })).isInstanceOf(DataIntegrityViolationException.class);
    }
}
