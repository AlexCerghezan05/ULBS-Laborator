package ro.ulbs.p21.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/**
 * D-007, D-031: banii și cantitățile sunt numeric, niciodată float. Coloanele nu au scale fix,
 * ci un CHECK pe scale(x), ca baza să respingă zecimalele în plus în loc să le rotunjească.
 */
@SpringBootTest
@Transactional
class MoneyColumnsIT {

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void noFloatingPointColumns() {
        List<String> floats = jdbc.queryForList("""
                select table_name || '.' || column_name from information_schema.columns
                where table_schema = 'public' and data_type in ('real', 'double precision')""", String.class);
        assertThat(floats).isEmpty();
    }

    @Test
    void everyNumericColumnHasNoFixedScaleAndAScaleCheck() {
        // vat_rate.percent (V1) e numeric(5,2): procent, nu bani; V1 nu se mai schimbă (D-020).
        List<Map<String, Object>> columns = jdbc.queryForList("""
                select c.table_name, c.column_name, c.numeric_scale,
                       exists (select 1 from pg_constraint k
                               where k.conrelid = (quote_ident(c.table_name))::regclass and k.contype = 'c'
                                 and pg_get_constraintdef(k.oid) ~ ('scale\\(' || c.column_name || '\\) <= [0-9]'))
                           as has_scale_check
                from information_schema.columns c
                where c.table_schema = 'public' and c.data_type = 'numeric'
                  and not (c.table_name = 'vat_rate' and c.column_name = 'percent')
                  and c.table_name <> 'flyway_schema_history'""");

        assertThat(columns).isNotEmpty();
        assertThat(columns).allSatisfy(col -> {
            assertThat(col.get("numeric_scale")).as("scale fix pe %s", col).isNull();
            assertThat(col.get("has_scale_check")).as("CHECK scale() pe %s", col).isEqualTo(true);
        });
    }

    @Test
    void priceWithMoreThan4DecimalsIsRejectedNotRounded() {
        jdbc.update("insert into product (name, unit, unit_price, vat_rate_code) values ('P', 'buc', 297.5206, 'S21')");
        assertThatThrownBy(() -> jdbc.update(
                "insert into product (name, unit, unit_price, vat_rate_code) values ('P', 'buc', 297.52066, 'S21')"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
