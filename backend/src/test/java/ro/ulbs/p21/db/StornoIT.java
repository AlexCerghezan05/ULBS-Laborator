package ro.ulbs.p21.db;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/** D-005: storno-ul e legat de o factură și ia număr din seria ei; totalurile lui sunt negative. */
@SpringBootTest
@Transactional
class StornoIT {

    @Autowired
    private JdbcTemplate jdbc;

    private DbFixtures fx;

    @BeforeEach
    void setUp() {
        fx = new DbFixtures(jdbc);
    }

    private static Map<String, Object> storno(Long originalId) {
        Map<String, Object> v = new HashMap<>();
        v.put("doc_type", "STORNO");
        v.put("original_invoice_id", originalId);
        v.put("original_doc_type", originalId == null ? null : "FACTURA");
        v.put("net_total", new BigDecimal("-297.52"));
        v.put("vat_total", new BigDecimal("-62.48"));
        v.put("total", new BigDecimal("-360.00"));
        v.put("net_total_ron", new BigDecimal("-297.52"));
        v.put("vat_total_ron", new BigDecimal("-62.48"));
        v.put("total_ron", new BigDecimal("-360.00"));
        return v;
    }

    @Test
    void stornoOfAnInvoiceInTheSameSeriesIsAccepted() {
        long series = fx.series("FACTURA");
        long original = fx.invoice(series);
        fx.invoice(series, storno(original));
    }

    @Test
    void stornoWithoutOriginalIsRejected() {
        long series = fx.series("FACTURA");
        assertThatThrownBy(() -> fx.invoice(series, storno(null)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void stornoInAnotherSeriesThanTheOriginalIsRejected() {
        long original = fx.invoice(fx.series("FACTURA"));
        long other = fx.series("FACTURA");
        assertThatThrownBy(() -> fx.invoice(other, storno(original)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void stornoOfAStornoIsRejected() {
        long series = fx.series("FACTURA");
        long original = fx.invoice(series);
        long first = fx.invoice(series, storno(original));
        Map<String, Object> second = storno(first);
        second.put("original_doc_type", "STORNO");
        assertThatThrownBy(() -> fx.invoice(series, second))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void invoiceWithAnOriginalIsRejected() {
        long series = fx.series("FACTURA");
        long original = fx.invoice(series);
        assertThatThrownBy(() -> fx.invoice(series,
                Map.of("original_invoice_id", original, "original_doc_type", "FACTURA")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void stornoWithAPositiveTotalIsRejected() {
        long series = fx.series("FACTURA");
        long original = fx.invoice(series);
        Map<String, Object> v = storno(original);
        v.put("net_total", new BigDecimal("297.52"));
        v.put("vat_total", new BigDecimal("62.48"));
        v.put("total", new BigDecimal("360.00"));
        assertThatThrownBy(() -> fx.invoice(series, v)).isInstanceOf(DataIntegrityViolationException.class);
    }
}
