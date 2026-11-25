package ro.ulbs.p21.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/** V4: seriile au tip de document (D-005); contorul e pe (serie, an) și se resetează anual. */
@SpringBootTest
@Transactional
class SeriesNumberingIT {

    @Autowired
    private JdbcTemplate jdbc;

    private long series(String name, String docType) {
        return jdbc.queryForObject(
                "insert into series (name, doc_type) values (?, ?) returning id", Long.class, name, docType);
    }

    private int allocate(long seriesId, int year) {
        return jdbc.queryForObject("select allocate_number(?, ?)", Integer.class, seriesId, year);
    }

    @Test
    void numbersAreSequentialPerSeries() {
        long a = series("Tnum", "FACTURA");
        long b = series("Tnum", "PROFORMA");
        assertThat(allocate(a, 2026)).isEqualTo(1);
        assertThat(allocate(a, 2026)).isEqualTo(2);
        assertThat(allocate(b, 2026)).isEqualTo(1);
        assertThat(allocate(a, 2026)).isEqualTo(3);
    }

    @Test
    void numberingRestartsAt1EachYear() {
        long a = series("Tyear", "FACTURA");
        allocate(a, 2026);
        allocate(a, 2026);
        assertThat(allocate(a, 2027)).isEqualTo(1);
        assertThat(allocate(a, 2026)).isEqualTo(3);
    }

    @Test
    void defaultNumberWidthIs4() {
        long a = series("Twidth", "CHITANTA");
        assertThat(jdbc.queryForObject("select number_width from series where id = ?", Integer.class, a))
                .isEqualTo(4);
    }

    @Test
    void sameNameAndTypeIsRejected() {
        series("Tdup", "FACTURA");
        series("Tdup", "CHITANTA");
        assertThatThrownBy(() -> series("Tdup", "FACTURA")).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void stornoIsNotASeriesType() {
        assertThatThrownBy(() -> series("Tst", "STORNO")).isInstanceOf(DataIntegrityViolationException.class);
    }
}
