package ro.ulbs.p21.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

/**
 * Pornirea în producție (D-044, D-045, D-046, Q-038): pe Render aplicația nu e superuser, ci
 * proprietarul bazei. Testul creează un astfel de rol și o bază nouă, rulează migrările plus
 * db/prod (fără db/seed) și verifică datele S1, contoarele, conturile și că trigger-ele rămân
 * pornite. Baza și rolul se șterg la final.
 */
@SpringBootTest
class ProdStartIT {

    /** Hash BCrypt valid (cost 10) al unei parole de test generate, nu al unei parole reale. */
    private static final String HASH = "$2a$10$" + "a".repeat(53);
    private static final String OTHER_HASH = "$2a$10$" + "b".repeat(53);

    @Autowired
    private JdbcTemplate admin;

    @Value("${spring.datasource.url}")
    private String url;

    private String role;
    private String password;
    private String database;
    private JdbcTemplate owner;

    @BeforeEach
    void createOwnerAndDatabase() {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        role = "p21_owner_" + suffix;
        database = "p21_prod_" + suffix;
        password = UUID.randomUUID().toString();
        admin.execute("create role " + role + " login nosuperuser nocreaterole nocreatedb password '" + password + "'");
        admin.execute("create database " + database + " owner " + role);
        owner = new JdbcTemplate(dataSource());
    }

    @AfterEach
    void dropDatabaseAndOwner() {
        admin.execute("drop database if exists " + database + " with (force)");
        admin.execute("drop role if exists " + role);
    }

    private SimpleDriverDataSource dataSource() {
        String dbUrl = url.replaceFirst("/[^/?]+(\\?|$)", "/" + database + "$1");
        return new SimpleDriverDataSource(new org.postgresql.Driver(), dbUrl, role, password);
    }

    private static Map<String, String> accounts(String... keyValues) {
        Map<String, String> p = new HashMap<>(Map.of(
                "seed_password_hash", "",
                "admin_username", "", "admin_password_hash", "",
                "operator_username", "", "operator_password_hash", "",
                "contabil_username", "", "contabil_password_hash", ""));
        for (int i = 0; i < keyValues.length; i += 2) {
            p.put(keyValues[i], keyValues[i + 1]);
        }
        return p;
    }

    private void migrate(Map<String, String> placeholders, String... locations) {
        Flyway.configure()
                .dataSource(dataSource())
                .locations(locations)
                .placeholders(placeholders)
                .load()
                .migrate();
    }

    private void migrateProd(Map<String, String> placeholders) {
        migrate(placeholders, "classpath:db/migration", "classpath:db/prod");
    }

    private void migrateProd() {
        migrateProd(accounts("admin_username", "sefu", "admin_password_hash", HASH));
    }

    private int counter(String docType) {
        return owner.queryForObject("""
                select c.last_number from series_counter c join series s on s.id = c.series_id
                where s.name = 'Aa' and s.doc_type = ? and c.year = 2026""", Integer.class, docType);
    }

    private long series(String docType) {
        return owner.queryForObject("select id from series where name = 'Aa' and doc_type = ?", Long.class, docType);
    }

    @Test
    void migrationsRunAsTheDatabaseOwnerWithoutSuperuser() {
        assertThat(owner.queryForObject("select rolsuper from pg_roles where rolname = current_user", Boolean.class)).isFalse();
        migrateProd();
        assertThat(owner.queryForObject("select count(*) from flyway_schema_history where not success", Integer.class)).isZero();
        assertThat(owner.queryForList("select script from flyway_schema_history where script like 'R__%'", String.class))
                .containsExactly("R__prod_start.sql");
    }

    @Test
    void s1IsImportedWithItsNumberAndExactAmounts() {
        migrateProd();
        Map<String, Object> s1 = owner.queryForMap("""
                select number, fiscal_year, issue_date::text as issue_date, client_name, supplier_name,
                       net_total::text as net, vat_total::text as vat, total::text as total
                from invoice where series_id = ?""", series("FACTURA"));
        assertThat(s1).containsAllEntriesOf(Map.of(
                "number", 60, "issue_date", "2026-11-10",
                "client_name", "CLIENT EXEMPLU PERSOANA FIZICA AUTORIZATA", "supplier_name", "FURNIZOR EXEMPLU S.R.L.",
                "net", "297.52", "vat", "62.48", "total", "360.00"));
        assertThat(owner.queryForObject("select count(*) from invoice", Integer.class)).isEqualTo(1);
        assertThat(owner.queryForObject("""
                select vat_percent::text || ' ' || vat_category_code || ' ' || net_amount::text from invoice_line""", String.class))
                .isEqualTo("21.00 S 297.52");
        assertThat(owner.queryForObject("select vat_amount::text from invoice_vat_summary", String.class)).isEqualTo("62.48");
        assertThat(owner.queryForObject("select text from invoice_legal_note", String.class)).contains("art. 319 alin. 29");
        assertThat(owner.queryForObject("select name from company", String.class)).isEqualTo("FURNIZOR EXEMPLU S.R.L.");
        assertThat(owner.queryForObject("select count(*) from client", Integer.class)).isEqualTo(1);
    }

    @Test
    void s1HasItsCashPaymentAndReceipt21() {
        migrateProd();
        assertThat(owner.queryForObject("select amount::text || ' ' || method from payment", String.class))
                .isEqualTo("360.00 NUMERAR");
        Map<String, Object> receipt = owner.queryForMap("select number, fiscal_year, series_id from receipt");
        assertThat(receipt).containsEntry("number", 21).containsEntry("series_id", series("CHITANTA"));
    }

    @Test
    void countersStartAt60And21() {
        migrateProd();
        assertThat(counter("FACTURA")).isEqualTo(60);
        assertThat(counter("CHITANTA")).isEqualTo(21);
    }

    @Test
    void nextInvoiceIs61AndNextReceiptIs22() {
        migrateProd();
        DbFixtures fx = new DbFixtures(owner);
        long invoice = fx.invoice(series("FACTURA"));
        assertThat(fx.numberOf(invoice)).isEqualTo(61);
        long receipt = fx.receipt(fx.payment(invoice, "360.00", "NUMERAR"), series("CHITANTA"));
        assertThat(owner.queryForObject("select number from receipt where id = ?", Integer.class, receipt)).isEqualTo(22);
    }

    @Test
    void allTriggersAreEnabledAfterTheImport() {
        migrateProd();
        assertThat(owner.queryForList("""
                select tgname from pg_trigger where not tgisinternal and tgenabled = 'D'""", String.class)).isEmpty();
    }

    @Test
    void s1StaysImmutableAfterTheImport() {
        migrateProd();
        assertThatThrownBy(() -> owner.update("update invoice set due_date = due_date + 1"))
                .isInstanceOf(DataAccessException.class).hasMessageContaining("e emisă");
        assertThatThrownBy(() -> owner.update("delete from receipt")).isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> owner.update("update series_counter set last_number = 1"))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void noTestAccountsAndNoSeed() {
        migrateProd();
        assertThat(owner.queryForList("select username from app_user", String.class)).containsExactly("sefu");
    }

    @Test
    void adminIsCreatedFromTheSecretHash() {
        migrateProd();
        assertThat(owner.queryForMap("select full_name, password_hash, role, active from app_user where username = 'sefu'"))
                .containsAllEntriesOf(Map.of("full_name", "Administrator", "password_hash", HASH, "role", "ADMIN", "active", true));
    }

    @Test
    void s1IsIssuedByTheAdmin() {
        migrateProd();
        assertThat(owner.queryForObject("""
                select u.username from invoice i join app_user u on u.id = i.issued_by""", String.class)).isEqualTo("sefu");
    }

    @Test
    void optionalOperatorAndContabilAreCreatedWhenGiven() {
        migrateProd(accounts("admin_username", "sefu", "admin_password_hash", HASH,
                "operator_username", "ana", "operator_password_hash", OTHER_HASH,
                "contabil_username", "dan", "contabil_password_hash", OTHER_HASH));
        assertThat(owner.queryForList("select username || ' ' || role || ' ' || full_name from app_user order by id", String.class))
                .containsExactly("sefu ADMIN Administrator", "ana OPERATOR Operator", "dan CONTABIL Contabil");
    }

    @Test
    void missingAdminHashFailsTheMigration() {
        assertThatThrownBy(() -> migrateProd(accounts("admin_username", "sefu")))
                .isInstanceOf(FlywayException.class).hasMessageContaining("P21_ADMIN_PASSWORD_HASH");
        assertThat(owner.queryForObject("select count(*) from app_user", Integer.class)).isZero();
    }

    @Test
    void missingAdminUsernameFailsTheMigration() {
        assertThatThrownBy(() -> migrateProd(accounts("admin_password_hash", HASH)))
                .isInstanceOf(FlywayException.class).hasMessageContaining("P21_ADMIN_USERNAME");
    }

    @Test
    void aPlainPasswordInsteadOfAHashFailsTheMigration() {
        assertThatThrownBy(() -> migrateProd(accounts("admin_username", "sefu", "admin_password_hash", "parola-in-clar")))
                .isInstanceOf(FlywayException.class).hasMessageContaining("BCrypt");
    }

    @Test
    void anOptionalAccountWithOnlyOneOfItsTwoVariablesFailsTheMigration() {
        assertThatThrownBy(() -> migrateProd(accounts("admin_username", "sefu", "admin_password_hash", HASH,
                "operator_username", "ana")))
                .isInstanceOf(FlywayException.class).hasMessageContaining("P21_OPERATOR_PASSWORD_HASH");
    }

    /** Scriptul rulat din nou (de ex. după o schimbare a lui) nu dublează și nu schimbă nimic. */
    @Test
    void runningTheScriptAgainChangesNothing() throws Exception {
        migrateProd();
        String sql = new ClassPathResource("db/prod/R__prod_start.sql").getContentAsString(StandardCharsets.UTF_8);
        for (Map.Entry<String, String> p : accounts("admin_username", "sefu", "admin_password_hash", OTHER_HASH).entrySet()) {
            sql = sql.replace("${" + p.getKey() + "}", p.getValue());
        }
        owner.execute(sql);
        assertThat(owner.queryForObject("select count(*) from invoice", Integer.class)).isEqualTo(1);
        assertThat(owner.queryForObject("select count(*) from receipt", Integer.class)).isEqualTo(1);
        assertThat(owner.queryForObject("select count(*) from series", Integer.class)).isEqualTo(2);
        assertThat(owner.queryForObject("select password_hash from app_user where username = 'sefu'", String.class)).isEqualTo(HASH);
        assertThat(counter("FACTURA")).isEqualTo(60);
        assertThat(counter("CHITANTA")).isEqualTo(21);
    }

    /** O serie care a emis deja documente (contor existent) nu primește S1 și nu-și pierde contorul. */
    @Test
    void anExistingCounterIsNotOverwritten() {
        migrate(accounts(), "classpath:db/migration");
        DbFixtures fx = new DbFixtures(owner);
        owner.update("insert into series (name, doc_type) values ('Aa', 'FACTURA'), ('Aa', 'CHITANTA')");
        long invoice = fx.invoice(series("FACTURA"));
        fx.receipt(fx.payment(invoice, "360.00", "NUMERAR"), series("CHITANTA"));

        migrateProd();

        assertThat(counter("FACTURA")).isEqualTo(1);
        assertThat(counter("CHITANTA")).isEqualTo(1);
        assertThat(owner.queryForList("select number from invoice", Integer.class)).containsExactly(1);
        assertThat(owner.queryForList("select number from receipt", Integer.class)).containsExactly(1);
    }
}
