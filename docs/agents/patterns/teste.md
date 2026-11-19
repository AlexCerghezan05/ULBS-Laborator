# Patterns · Teste: unit și integrare în backend

> Topic file. The index is in `../PATTERNS.md`.

## PATTERN-001 · Testele de integrare se numesc *IT și rulează pe db-test
- **Status:** candidate
- **Date:** 2026-11-19
- **How:** Un test care are nevoie de PostgreSQL (`@SpringBootTest`, migrări, SQL) se numește `<Ceva>IT` și rulează cu maven-failsafe la `mvn verify`, pe Postgres-ul `db-test` din profilul `test` (D-023). Un test fără bază de date (de ex. `@WebMvcTest`) se numește `<Ceva>Test` și rulează cu surefire.
- **Why:** Testele fără DB rămân rapide și pică primele; cele de integrare folosesc o bază reală, aceeași local și în CI.
- **Example:** `backend/src/test/java/ro/ulbs/p21/health/HealthControllerTest.java` (unit); `backend/src/test/java/ro/ulbs/p21/db/VatRateMigrationIT.java` (integrare); `backend/pom.xml` (failsafe).
- **Source:** agent · confidence medium
