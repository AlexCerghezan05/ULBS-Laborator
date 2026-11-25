package ro.ulbs.p21.db;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seed-ul din db/seed: firma și clientul S1 anonimizate (SAMPLES.md), utilizatorii de test,
 * seria Aa (D-005) și factura reală S1 (Aa0060) cu sumele exacte (D-026).
 */
@SpringBootTest
@Transactional
class SeedIT {

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void companyIsTheAnonymisedSupplier() {
        assertThat(jdbc.queryForMap("""
                select name, cif, reg_com, city, county_code, vat_payer, vat_on_collection, rounding_rule from company"""))
                .hasSize(8)
                .containsAllEntriesOf(Map.of(
                        "name", "FURNIZOR EXEMPLU S.R.L.", "cif", "RO12345674", "reg_com", "J2023000123456",
                        "city", "Sibiu", "county_code", "RO-SB", "vat_payer", true, "vat_on_collection", false))
                .containsEntry("rounding_rule", null);
    }

    @Test
    void oneTestUserPerRole() {
        assertThat(jdbc.queryForList("select username || ':' || role from app_user where username in ('admin', 'operator', 'contabil') order by role",
                String.class)).containsExactly("admin:ADMIN", "contabil:CONTABIL", "operator:OPERATOR");
    }

    @Test
    void seriesAaIsTheOnlyInvoiceSeriesAndThereIsNoProformaSeries() {
        assertThat(jdbc.queryForObject("select count(*) from series where name = 'Aa' and doc_type = 'FACTURA' and number_width = 4",
                Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("select count(*) from series where name = 'Aa' and doc_type = 'PROFORMA'",
                Integer.class)).isZero();
    }

    @Test
    void invoiceS1HasTheRealAmounts() {
        Map<String, Object> s1 = jdbc.queryForMap("""
                select s.name || lpad(i.number::text, s.number_width, '0') as full_number,
                       i.doc_type, i.issue_date::text as issue_date, i.due_date::text as due_date, i.currency,
                       i.net_total::text as net, i.vat_total::text as vat, i.total::text as total,
                       i.total_ron::text as total_ron,
                       i.client_type, i.client_name, i.client_cif, i.client_reg_com,
                       u.full_name as issued_by
                from invoice i join series s on s.id = i.series_id join app_user u on u.id = i.issued_by
                where s.name = 'Aa' and i.fiscal_year = 2026 and i.number = 60""");
        assertThat(s1).containsExactlyInAnyOrderEntriesOf(Map.ofEntries(
                Map.entry("full_number", "Aa0060"), Map.entry("doc_type", "FACTURA"),
                Map.entry("issue_date", "2026-11-10"), Map.entry("due_date", "2026-11-10"), Map.entry("currency", "RON"),
                Map.entry("net", "297.52"), Map.entry("vat", "62.48"), Map.entry("total", "360.00"),
                Map.entry("total_ron", "360.00"),
                Map.entry("client_type", "PFA"), Map.entry("client_name", "CLIENT EXEMPLU PERSOANA FIZICA AUTORIZATA"),
                Map.entry("client_cif", "76543210"), Map.entry("client_reg_com", "F2022000654321"),
                Map.entry("issued_by", "Ion Ionescu")));
    }

    @Test
    void invoiceS1HasOneLineTheVatSummaryAndTheLegalNote() {
        List<String> lines = jdbc.queryForList("""
                select l.name || '|' || l.unit || '|' || l.quantity::text || '|' || l.unit_price::text || '|'
                       || l.discount_percent::text || '|' || l.vat_rate_code || '|' || l.net_amount::text
                from invoice_line l join invoice i on i.id = l.invoice_id join series s on s.id = i.series_id
                where s.name = 'Aa' and i.number = 60 and i.fiscal_year = 2026""", String.class);
        assertThat(lines).containsExactly("Inchiriere masina 7 zile|1|1|297.52|0|S21|297.52");

        assertThat(jdbc.queryForObject("""
                select v.vat_rate_code || ' ' || v.taxable_amount::text || ' ' || v.vat_amount::text
                from invoice_vat_summary v join invoice i on i.id = v.invoice_id join series s on s.id = i.series_id
                where s.name = 'Aa' and i.number = 60 and i.fiscal_year = 2026""", String.class))
                .isEqualTo("S21 297.52 62.48");

        assertThat(jdbc.queryForList("""
                select n.text from invoice_legal_note n join invoice i on i.id = n.invoice_id join series s on s.id = i.series_id
                where s.name = 'Aa' and i.number = 60 and i.fiscal_year = 2026""", String.class))
                .containsExactly("Factura este valabila fara semnatura si stampila, conform art. 319 alin. 29 din legea 227/2015.");
    }

    @Test
    void nextNumberInSeriesAaFor2026Is61() {
        Long aa = jdbc.queryForObject("select id from series where name = 'Aa' and doc_type = 'FACTURA'", Long.class);
        assertThat(jdbc.queryForObject("select allocate_number(?, 2026)", Integer.class, aa)).isEqualTo(61);
    }
}
