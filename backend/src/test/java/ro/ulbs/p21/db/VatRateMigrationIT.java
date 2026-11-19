package ro.ulbs.p21.db;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

/** D-020: migrarea Flyway creează exact cele 4 cote de TVA, cu codul de categorie pentru XML. */
@SpringBootTest
class VatRateMigrationIT {

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void migrationCreatesTheFourVatRates() {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "select code, label, percent::text as percent, category_code from vat_rate order by code");

        assertThat(rows).containsExactly(
                Map.of("code", "AE", "label", "Taxare inversă", "percent", "0.00", "category_code", "AE"),
                Map.of("code", "E", "label", "Scutit", "percent", "0.00", "category_code", "E"),
                Map.of("code", "S11", "label", "11%", "percent", "11.00", "category_code", "S"),
                Map.of("code", "S21", "label", "21%", "percent", "21.00", "category_code", "S"));
    }
}
