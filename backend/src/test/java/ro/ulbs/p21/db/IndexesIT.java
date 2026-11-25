package ro.ulbs.p21.db;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Review-ul de performanță al PR #2: Postgres nu indexează automat coloanele care referă (FK).
 * Indexurile pe FK-urile și coloanele după care se caută (client, dată, restante) există.
 */
@SpringBootTest
class IndexesIT {

    @Autowired
    private JdbcTemplate jdbc;

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "payment_invoice_idx      | CREATE INDEX payment_invoice_idx ON public.payment USING btree (invoice_id)",
            "payment_paid_on_idx      | CREATE INDEX payment_paid_on_idx ON public.payment USING btree (paid_on)",
            "invoice_original_idx     | CREATE INDEX invoice_original_idx ON public.invoice USING btree (original_invoice_id) WHERE (original_invoice_id IS NOT NULL)",
            "invoice_client_idx       | CREATE INDEX invoice_client_idx ON public.invoice USING btree (client_id, issue_date)",
            "invoice_issue_idx        | CREATE INDEX invoice_issue_idx ON public.invoice USING btree (issue_date)",
            "invoice_due_idx          | CREATE INDEX invoice_due_idx ON public.invoice USING btree (due_date) WHERE ((doc_type)::text = 'FACTURA'::text)",
            "reminder_log_invoice_idx | CREATE INDEX reminder_log_invoice_idx ON public.reminder_log USING btree (invoice_id, sent_at)"})
    void indexExists(String name, String definition) {
        assertThat(jdbc.queryForList("select indexdef from pg_indexes where schemaname = 'public' and indexname = ?",
                String.class, name)).containsExactly(definition);
    }
}
