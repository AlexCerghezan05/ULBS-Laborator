# Open questions · Proiect: stack, comenzi, livrare, deploy, screenshot

> Topic file. The index is in `../OPEN-QUESTIONS.md`.

## Q-001 · What will this repo hold, and in which stack?
- **Status:** answered
- **Answer:** Proiectul P21 din catalogul Web Avansat (docs/brief.md): facturare + export e-Factura (UBL 2.1, RO_CIUS) pentru clientul real Aura Logistic SRL. Stack: Java 21 + Spring Boot, PostgreSQL, Flyway, OpenAPI, React + TypeScript (Alexandru Cerghezan, 19.11.2026; D-002).
- **Blocks:** AGENTS.md sections 1–3, CONTEXT.md terms, ARCHITECTURE.md.
- **Who answers:** Alexandru Cerghezan

## Q-002 · Which build, run and test commands does the project use?
- **Status:** answered
- **Answer:** `cp .env.example .env` (o dată, cu parola completată); rulare: `docker compose up --build` (frontend http://localhost:8090, backend http://localhost:8081); build: `docker compose build`; teste: `docker compose --profile test run --rm backend-test` (`mvn -B verify` pe `db-test`); frontend-ul nu are încă teste (Alexandru Cerghezan, 19.11.2026, planul „Scheletul proiectului”; D-023).
- **Blocks:** AGENTS.md sections 2 and 6 (definition of done).
- **Who answers:** Alexandru Cerghezan (after Q-001)

## Q-003 · How is work released or handed in?
- **Status:** open
- **Blocks:** AGENTS.md section 5 (branches, tags, delivery).
- **Who answers:** Alexandru Cerghezan
- **Confirmed:** 2026-11-19

## Q-021 · Unde rulează deploy-ul live HTTPS
- **Status:** open
- **Context:** Cerințele comune cer deploy live cu HTTPS. Nu s-a stabilit pe ce server sau serviciu, cu ce domeniu și cum se obține certificatul HTTPS.
- **Blocks:** Deploy-ul live; AGENTS.md §5.
- **Who answers:** Alexandru Cerghezan
- **Source:** docs/brief.md (cerințe comune); interviul cu Alexandru Cerghezan, 19.11.2026
- **Confirmed:** 2026-11-19

## Q-022 · Cum facem screenshot-urile aplicației
- **Status:** open
- **Context:** Procedura de design cere screenshot-uri light și dark pentru fiecare ecran schimbat (DESIGN.md → Procedure). Încă nu există cod de UI și nici o comandă de screenshot. Pe host nu se instalează unelte native (D-001), deci comanda va rula probabil într-un container (de ex. Playwright în Docker), dar nu e stabilită.
- **Blocks:** AGENTS.md §2 (comanda de screenshot) și §6 (Definition of Done pentru UI).
- **Who answers:** Alexandru Cerghezan
- **Source:** setup-ul de design, 19.11.2026
- **Confirmed:** 2026-11-19

## Q-035 · CSP complet pentru aplicație
- **Status:** open
- **Context:** D-024 pune în nginx doar `X-Content-Type-Options`, `X-Frame-Options` și `Referrer-Policy`. Un `Content-Security-Policy` complet (surse permise pentru scripturi, stiluri, imagini, `connect-src`, `form-action`) lipsește. Contează odată cu formularele de facturare (DS-p21-invoice-form), care trimit date spre `/api`.
- **Blocks:** Headerele de securitate pentru formularele de facturare; o eventuală extindere a D-024.
- **Who answers:** Alexandru Cerghezan
- **Source:** review-ul PR #1 (Security), 19.11.2026; owner-ul a amânat CSP-ul

## Q-038 · Utilizatorul Postgres al aplicației în producție
- **Status:** open
- **Context:** Trigger-ele de immutabilitate (facturile emise, chitanțele, D-033) și de numerotare pot fi ocolite de un superuser cu `set session_replication_role = replica`, iar proprietarul tabelelor le poate dezactiva (`ALTER TABLE ... DISABLE TRIGGER`) sau înlocui (`CREATE OR REPLACE FUNCTION`). Local și în teste, aplicația se conectează cu `POSTGRES_USER`, care în imaginea Docker este superuser. În producție, aplicația trebuie să folosească un utilizator Postgres fără superuser și fără drept de proprietar pe tabele. Ce utilizator, cu ce drepturi (SELECT/INSERT/UPDATE/DELETE pe tabele, fără TRUNCATE), cine rulează migrările Flyway (un utilizator separat, proprietar al schemei?) și unde se creează (script de inițializare, migrare)?
- **Condiții de deploy** (review-ul de securitate al PR #2, 25.11.2026; cerute de Alexandru Cerghezan):
  1. `REVOKE TEMP ON DATABASE <baza> FROM PUBLIC`: utilizatorul aplicației nu poate crea tabele temporare. Funcțiile au deja `search_path` fixat cu `pg_temp` ultimul, deci un tabel temporar numit `invoice` nu mai ocolește blocarea; revocarea e a doua barieră.
  2. Seed-ul nu rulează în producție: `SPRING_FLYWAY_LOCATIONS` doar `classpath:db/migration`, fără `db/seed`. Compose-ul de acum încarcă seed-ul și pe serviciul `backend`; la deploy, seed-ul ar crea conturile de test `admin`/`operator`/`contabil` și factura S1 (Aa0060), care nu se mai poate șterge și ocupă numerotarea reală a clientului.
- **Blocks:** Deploy-ul live (Q-021); garanția că factura emisă nu se modifică nici prin SQL direct în producție.
- **Who answers:** Alexandru Cerghezan
- **Source:** raportul de build „Schema bazei de date”, 25.11.2026 (observație); review-ul de securitate al PR #2, 25.11.2026 (punctele 1 și 8)
