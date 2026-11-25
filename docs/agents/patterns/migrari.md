# Patterns · Migrări: date pe medii

> Topic file. The index is in `../PATTERNS.md`.

## PATTERN-008 · Datele care există doar într-un mediu stau într-o locație Flyway proprie, ca migrare repetabilă idempotentă
- **Status:** candidate
- **Date:** 2026-11-26
- **How:** Schema e în `db/migration` (V). Datele de pornire ale unui mediu stau într-o locație separată, aleasă din `SPRING_FLYWAY_LOCATIONS`: `db/seed` local și în teste, `db/prod` pe Render. Fiecare e o migrare `R__` (rulează după toate V și din nou doar când se schimbă), iar fiecare pas e idempotent (`on conflict do nothing`, `where not exists`). Valorile secrete vin ca placeholder-e Flyway din mediu, doar ca hash, și se validează în SQL cu `raise exception`. Un test `*IT` rulează migrările cu locația mediului pe o bază nouă, cu un rol fără superuser (`ProdStartIT`).
- **Why:** Un `V100` de date ar bloca migrările V care vin după el; locațiile separate țin datele de test departe de producție.
- **Example:** `backend/src/main/resources/db/seed/R__seed.sql`; `backend/src/main/resources/db/prod/R__prod_start.sql`; `backend/src/test/java/ro/ulbs/p21/db/ProdStartIT.java`.
- **Source:** agent · confidence medium
