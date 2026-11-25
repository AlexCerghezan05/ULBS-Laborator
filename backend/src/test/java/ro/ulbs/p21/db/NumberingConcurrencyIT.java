package ro.ulbs.p21.db;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * D-005: emiteri simultane pe aceeași serie primesc numere fără goluri și fără duplicate,
 * chiar dacă unele tranzacții fac rollback. Tranzacțiile sunt reale (commit), deci testul își
 * creează propria serie și o șterge la final.
 */
@SpringBootTest
class NumberingConcurrencyIT {

    private static final int THREADS = 20;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private TransactionTemplate tx;

    private long seriesId;

    @BeforeEach
    void createSeries() {
        seriesId = jdbc.queryForObject("insert into series (name, doc_type) values (?, 'FACTURA') returning id",
                Long.class, "C" + UUID.randomUUID().toString().substring(0, 8));
    }

    @AfterEach
    void dropSeries() {
        jdbc.update("delete from series_counter where series_id = ?", seriesId);
        jdbc.update("delete from series where id = ?", seriesId);
    }

    @Test
    void concurrentAllocationsWithRollbacksLeaveNoGapsAndNoDuplicates() throws Exception {
        List<Integer> committed = new CopyOnWriteArrayList<>();
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        try {
            List<Future<?>> futures = IntStream.range(0, THREADS).<Future<?>>mapToObj(i -> pool.submit(() -> {
                start.await();
                Integer number = tx.execute(status -> {
                    int n = jdbc.queryForObject("select allocate_number(?, 2026)", Integer.class, seriesId);
                    try {
                        Thread.sleep(20); // ține lock-ul cât timp celelalte fire așteaptă
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    if (i % 3 == 0) {
                        status.setRollbackOnly(); // emitere eșuată: numărul nu trebuie să se piardă
                        return null;
                    }
                    return n;
                });
                if (number != null) {
                    committed.add(number);
                }
                return null;
            })).toList();
            start.countDown();
            for (Future<?> f : futures) {
                f.get(60, TimeUnit.SECONDS);
            }
        } finally {
            pool.shutdownNow();
        }

        int expected = (int) IntStream.range(0, THREADS).filter(i -> i % 3 != 0).count();
        assertThat(committed).hasSize(expected);
        assertThat(committed).containsExactlyInAnyOrderElementsOf(IntStream.rangeClosed(1, expected).boxed().toList());
        assertThat(jdbc.queryForObject(
                "select last_number from series_counter where series_id = ? and year = 2026", Integer.class, seriesId))
                .isEqualTo(expected);
    }
}
