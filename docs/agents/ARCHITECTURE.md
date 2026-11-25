# Architecture — ULBS-Laborator

<!-- graffino-agentic:begin v1 -->
> How this repo is built, generated from the code at backfill and kept current by
> whoever changes the structure. The code wins: when this page is wrong, fix it.
<!-- graffino-agentic:end -->

## Overview
Scheletul (2026-11-19): un backend Spring Boot, un frontend React servit de nginx și
PostgreSQL, toate în Docker Compose (D-001, D-002, D-023). Funcții de business: încă
niciuna în API; există doar `GET /api/health`. Schema bazei de date (2026-11-25, V1–V7 și
seed-ul) acoperă firma, utilizatorii, clienții, produsele, seriile, facturile, încasările,
chitanțele, cursurile BNR și reminder-ele; regulile de integritate stau în baza de date
(PATTERN-002).

## Parts
| Part | Path | Responsibility | Talks to |
|---|---|---|---|
| backend | `backend/` | API REST (Java 21, Spring Boot 4.1, Maven); rulează migrările Flyway la pornire | db |
| frontend | `frontend/` | UI React + TypeScript (Vite), servit de nginx; `/api/` trimis spre backend | backend (prin nginx) |
| db | `docker-compose.yml` → `db` | PostgreSQL 18, volumul `db-data` | — |
| teste | `docker-compose.yml` → profilul `test` | `backend-test` rulează `mvn -B verify` pe `db-test` (tmpfs) | db-test |
| CI | `.github/workflows/ci.yml` | la fiecare push: testele (aceeași comandă) și ledger-lint | — |

## Data flow
Browser → nginx (`frontend`, port 8090 pe host) → `/api/*` → `backend:8080` → PostgreSQL (`db`).
Pagina DS-p21-health cere `GET /api/health` o dată, la deschidere.

## State and storage
| What | Where | Owner | Notes |
|---|---|---|---|
| Cotele de TVA | tabelul `vat_rate` | migrarea `V1__vat_rate.sql` | D-020: o cotă nouă = o migrare nouă; motivul de scutire lipsește (Q-034) |
| Firma, utilizatorii | `company` (un rând, D-003), `app_user` (D-012, D-029) | `V2__company_users.sql` | `rounding_rule` null până la Q-007 |
| Clienți, produse | `client` (PJ/PFA/PF), `product` | `V3__clients_products.sql` | D-010, D-019, D-021, D-022, D-032 |
| Serii și numerotare | `series`, `series_counter`, funcția `allocate_number` | `V4__series_numbering.sql` | D-005: contor pe (serie, an), lock pe rând până la commit, fără goluri și fără duplicate |
| Facturi, storno, proforme | `invoice`, `invoice_line`, `invoice_vat_summary`, `invoice_legal_note` | `V5__invoices.sql` | Copii la emitere (D-027); trigger-ele blochează orice schimbare pe FACTURA/STORNO după tranzacția care le-a creat |
| Încasări, chitanțe | `payment`, `receipt` | `V6__payments_receipts.sql` | D-028; chitanța doar pe numerar, nemodificabilă (D-033) |
| Cursuri BNR, reminder-e | `bnr_rate`, `reminder_settings` (un rând), `reminder_log` | `V7__bnr_reminders.sql` | Valorile reminder-elor: Q-019 |
| Seed local | firma și clientul S1, 3 utilizatori de test, seria `Aa`, factura S1 | `db/seed/R__seed.sql` (repetabilă, idempotentă) | Încărcat doar prin `SPRING_FLYWAY_LOCATIONS` din Compose |
| Istoricul migrărilor | `flyway_schema_history` | Flyway | — |

Banii și cantitățile sunt `numeric` fără scale fix, cu `CHECK (scale(x) <= n)` (D-031).

## Configuration
| Name | Where set | Used by | Notes |
|---|---|---|---|
| `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` | `.env` (din `.env.example`, nu intră în git) | `db`, `db-test`, `backend`, `backend-test` | obligatorii: fără ele Compose se oprește |
| `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` | `docker-compose.yml`, din variabilele de mai sus | backend | — |
| `SPRING_FLYWAY_LOCATIONS` | `docker-compose.yml` (`x-seed-env`) | backend, backend-test | `db/migration` + `db/seed`; la deploy, fără `db/seed` |
| `P21_SEED_PASSWORD_HASH` | `.env` (opțional) | seed-ul, ca placeholder Flyway `seed_password_hash` | hash BCrypt, între ghilimele simple; gol = conturi de test fără login |
<!-- names only, never values -->

## Build, deploy, runtime
Comenzile: AGENTS.md §2 (D-023). Imaginile: `backend/Dockerfile` (Maven → JRE 21),
`frontend/Dockerfile` (Node → nginx). Deploy: încă nu (Q-021).

## Boundaries
- `GET /api/health` → `{"status":"UP"}`, folosit doar de DS-p21-health. Fără autentificare (D-012 vine în alt plan).
