# Patterns · Teste: unit și integrare în backend

> Topic file. The index is in `../PATTERNS.md`.

## PATTERN-001 · Testele de integrare se numesc *IT și rulează pe db-test
- **Status:** approved
- **Date:** 2026-11-19
- **How:** Un test care are nevoie de PostgreSQL (`@SpringBootTest`, migrări, SQL) se numește `<Ceva>IT` și rulează cu maven-failsafe la `mvn verify`, pe Postgres-ul `db-test` din profilul `test` (D-023). Un test fără bază de date (de ex. `@WebMvcTest`) se numește `<Ceva>Test` și rulează cu surefire.
- **Why:** Testele fără DB rămân rapide și pică primele; cele de integrare folosesc o bază reală, aceeași local și în CI.
- **Example:** `backend/src/test/java/ro/ulbs/p21/health/HealthControllerTest.java` (unit); `backend/src/test/java/ro/ulbs/p21/db/VatRateMigrationIT.java` (integrare); `backend/pom.xml` (failsafe).
- **Source:** agent · confidence medium
- **Confirmed:** 2026-11-25

## PATTERN-002 · Regulile de integritate stau în baza de date și se testează direct cu SQL
- **Status:** candidate
- **Date:** 2026-11-25
- **How:** O regulă care trebuie să țină și la SQL scris direct (unicitate, tipuri, legături între documente, immutabilitate) se pune în migrare: CHECK, FK compus pe `(id, tip)` sau trigger cu `errcode = 'restrict_violation'` (Spring îl traduce în `DataIntegrityViolationException`). Testul `*IT` folosește `DbFixtures` și JdbcTemplate în `@Transactional` (rollback); instrucțiunea care trebuie să pice e ultima din test, pentru că Postgres anulează tranzacția după eroare. Testele care au nevoie de commit (concurență, immutabilitate după emitere) folosesc `TransactionTemplate`, își creează seria lor și curăță în `@AfterEach`, cu `set local session_replication_role = replica` unde trigger-ele blochează ștergerea.
- **Why:** Codul Java care va veni nu poate ocoli regula; testele rulează pe aceeași bază reală local și în CI.
- **Example:** `backend/src/main/resources/db/migration/V5__invoices.sql`; `backend/src/test/java/ro/ulbs/p21/db/DbFixtures.java`; `IssuedInvoiceImmutableIT.java`; `NumberingConcurrencyIT.java`.
- **Source:** agent · confidence medium
