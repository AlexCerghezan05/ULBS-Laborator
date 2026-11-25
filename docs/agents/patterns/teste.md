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
- **Confirmed:** 2026-11-26

## PATTERN-003 · Tranzacția de emitere: documentul inserat la final, fără I/O, fără savepoint-uri
- **Status:** candidate
- **Date:** 2026-11-25
- **How:** Serviciul care emite o factură, un storno, o proformă sau o chitanță:
  1. Calculează totul înainte (totaluri, TVA după regula de rotunjire, curs BNR, mențiuni), apoi face INSERT-ul documentului cât mai târziu în tranzacție: trigger-ul de numerotare ține lock pe contorul seriei până la commit și blochează toate celelalte emiteri pe aceeași serie și același an.
  2. Nu pune în tranzacție nicio operație externă: PDF, email, apel BNR, upload. Se fac după commit.
  3. Când o tranzacție numerotează două documente (de ex. factura și chitanța încasării în numerar), ordinea e mereu aceeași: întâi documentul din seria facturii, apoi chitanța. Altfel două emiteri simultane pot intra în deadlock pe contoare.
  4. Factura și liniile, TVA-ul pe cote și mențiunile ei se scriu în aceeași tranzacție, fără savepoint-uri (`Propagation.NESTED`, pgjdbc `autosave`): excepția „factura e modificabilă în tranzacția care a creat-o” compară `xmin` cu tranzacția de vârf, deci într-un savepoint factura ar apărea deja blocată.
- **Why:** Review-ul PR #2 (Performance: lock-ul pe contor e intenționat, dar serializează emiterile; Security 6: excepția din `invoice_is_locked` nu vede subtranzacțiile).
- **Example:** `backend/src/main/resources/db/migration/V4__series_numbering.sql` (assign_document_number, allocate_number); `V5__invoices.sql` (invoice_is_locked); `NumberingConcurrencyIT.java`.
- **Source:** agent · confidence medium
- **Confirmed:** 2026-11-25
