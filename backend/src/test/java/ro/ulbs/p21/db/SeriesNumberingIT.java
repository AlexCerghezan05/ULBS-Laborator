package ro.ulbs.p21.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/**
 * V4: seriile au tip de document (D-005); contorul e pe (serie, an), se resetează anual și se
 * schimbă doar prin emiterea unui document. Seria folosită nu-și mai schimbă numele sau formatul.
 */
@SpringBootTest
@Transactional
class SeriesNumberingIT {

    @Autowired
    private JdbcTemplate jdbc;

    private DbFixtures fx;

    @BeforeEach
    void setUp() {
        fx = new DbFixtures(jdbc);
    }

    private long issue(long seriesId, int year) {
        LocalDate day = LocalDate.of(year, 11, 10);
        return fx.invoice(seriesId, Map.of("issue_date", day, "due_date", day));
    }

    @Test
    void numbersAreSequentialPerSeries() {
        long a = fx.series("FACTURA");
        long b = fx.series("FACTURA");
        assertThat(fx.numberOf(issue(a, 2026))).isEqualTo(1);
        assertThat(fx.numberOf(issue(a, 2026))).isEqualTo(2);
        assertThat(fx.numberOf(issue(b, 2026))).isEqualTo(1);
        assertThat(fx.numberOf(issue(a, 2026))).isEqualTo(3);
    }

    @Test
    void numberingRestartsAt1EachYear() {
        long a = fx.series("FACTURA");
        issue(a, 2026);
        issue(a, 2026);
        assertThat(fx.numberOf(issue(a, 2027))).isEqualTo(1);
        assertThat(fx.numberOf(issue(a, 2026))).isEqualTo(3);
    }

    @Test
    void defaultNumberWidthIs4() {
        long a = fx.series("CHITANTA");
        assertThat(jdbc.queryForObject("select number_width from series where id = ?", Integer.class, a))
                .isEqualTo(4);
    }

    @Test
    void sameNameAndTypeIsRejected() {
        jdbc.update("insert into series (name, doc_type) values ('Tdup', 'FACTURA')");
        jdbc.update("insert into series (name, doc_type) values ('Tdup', 'CHITANTA')");
        assertThatThrownBy(() -> jdbc.update("insert into series (name, doc_type) values ('Tdup', 'FACTURA')"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void stornoIsNotASeriesType() {
        assertThatThrownBy(() -> fx.series("STORNO")).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void callingAllocateNumberDirectlyIsRejected() {
        long a = fx.series("FACTURA");
        assertThatThrownBy(() -> jdbc.queryForObject("select allocate_number(?, 2026)", Integer.class, a))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void insertingACounterDirectlyIsRejected() {
        long a = fx.series("FACTURA");
        assertThatThrownBy(() -> jdbc.update(
                "insert into series_counter (series_id, year, last_number) values (?, 2026, 50)", a))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void updatingTheCounterDirectlyIsRejected() {
        long a = fx.series("FACTURA");
        issue(a, 2026);
        assertThatThrownBy(() -> jdbc.update("update series_counter set last_number = 50 where series_id = ?", a))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void deletingTheCounterIsRejected() {
        long a = fx.series("FACTURA");
        issue(a, 2026);
        assertThatThrownBy(() -> jdbc.update("delete from series_counter where series_id = ?", a))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void unusedSeriesCanBeRenamedAndReformatted() {
        long a = fx.series("FACTURA");
        jdbc.update("update series set name = ?, number_width = 6 where id = ?", DbFixtures.unique("R"), a);
    }

    @Test
    void renamingAUsedSeriesIsRejected() {
        long a = fx.series("FACTURA");
        issue(a, 2026);
        assertThatThrownBy(() -> jdbc.update("update series set name = ? where id = ?", DbFixtures.unique("R"), a))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void changingTheNumberWidthOfAUsedSeriesIsRejected() {
        long a = fx.series("FACTURA");
        issue(a, 2026);
        assertThatThrownBy(() -> jdbc.update("update series set number_width = 6 where id = ?", a))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
