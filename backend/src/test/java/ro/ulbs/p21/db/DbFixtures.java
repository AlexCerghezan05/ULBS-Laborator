package ro.ulbs.p21.db;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.JdbcTemplate;

/** Date de pornire pentru testele de integrare pe schemă: utilizator, client, serie, factură. */
final class DbFixtures {

    private final JdbcTemplate jdbc;

    DbFixtures(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    static String unique(String prefix) {
        return prefix + UUID.randomUUID().toString().substring(0, 8);
    }

    long user() {
        return jdbc.queryForObject("insert into app_user (username, full_name, role) values (?, 'Test', 'OPERATOR') returning id",
                Long.class, unique("u-"));
    }

    long client() {
        return jdbc.queryForObject("""
                insert into client (client_type, name, cif, address, city, county_code)
                values ('PFA', 'Client test', '76543210', 'Str. X', 'Sibiu', 'RO-SB') returning id""", Long.class);
    }

    long series(String docType) {
        return jdbc.queryForObject("insert into series (name, doc_type) values (?, ?) returning id",
                Long.class, unique("T"), docType);
    }

    int allocate(long seriesId, int year) {
        return jdbc.queryForObject("select allocate_number(?, ?)", Integer.class, seriesId, year);
    }

    /** O factură RON de 297.52 + 62.48 = 360.00 pe seria dată; overrides înlocuiesc coloane. */
    long invoice(long seriesId, Map<String, Object> overrides) {
        Map<String, Object> v = new LinkedHashMap<>();
        v.put("doc_type", "FACTURA");
        v.put("series_id", seriesId);
        v.put("series_type", "FACTURA");
        v.put("issue_date", LocalDate.of(2026, 11, 10));
        v.put("due_date", LocalDate.of(2026, 11, 10));
        v.put("client_id", client());
        v.put("client_type", "PFA");
        v.put("client_name", "Client test");
        v.put("client_cif", "76543210");
        v.put("client_address", "Str. X");
        v.put("client_city", "Sibiu");
        v.put("client_county_code", "RO-SB");
        v.put("supplier_name", "Furnizor test");
        v.put("supplier_cif", "RO12345674");
        v.put("supplier_reg_com", "J2023000123456");
        v.put("supplier_address", "Str. Y");
        v.put("supplier_city", "Sibiu");
        v.put("supplier_county_code", "RO-SB");
        v.put("currency", "RON");
        v.put("net_total", new BigDecimal("297.52"));
        v.put("vat_total", new BigDecimal("62.48"));
        v.put("total", new BigDecimal("360.00"));
        v.put("net_total_ron", new BigDecimal("297.52"));
        v.put("vat_total_ron", new BigDecimal("62.48"));
        v.put("total_ron", new BigDecimal("360.00"));
        v.put("issued_by", user());
        v.putAll(overrides);
        if (!v.containsKey("number")) {
            int year = ((LocalDate) v.get("issue_date")).getYear();
            v.put("number", allocate(seriesId, year));
        }
        String cols = String.join(", ", v.keySet());
        String marks = v.keySet().stream().map(k -> "?").collect(Collectors.joining(", "));
        return jdbc.queryForObject("insert into invoice (" + cols + ") values (" + marks + ") returning id",
                Long.class, v.values().toArray());
    }

    long invoice(long seriesId) {
        return invoice(seriesId, Map.of());
    }

    void line(long invoiceId, int position, String quantity, String netAmount) {
        jdbc.update("""
                insert into invoice_line (invoice_id, position, name, unit, quantity, unit_price, vat_rate_code, net_amount)
                values (?, ?, 'Inchiriere masina 7 zile', '1', ?, 297.52, 'S21', ?)""",
                invoiceId, position, new BigDecimal(quantity), new BigDecimal(netAmount));
    }
}
