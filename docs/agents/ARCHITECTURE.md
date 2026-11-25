# Architecture — ULBS-Laborator

<!-- graffino-agentic:begin v1 -->
> How this repo is built, generated from the code at backfill and kept current by
> whoever changes the structure. The code wins: when this page is wrong, fix it.
<!-- graffino-agentic:end -->

## Overview
Scheletul (2026-11-19): un backend Spring Boot, un frontend React servit de nginx și
PostgreSQL, toate în Docker Compose (D-001, D-002, D-040). Funcții de business: încă
niciuna în API. Autentificarea (2026-11-25): login, sesiune pe server și roluri verificate pe
server (D-012, D-034–D-041, C-001); frontend-ul are DS-p21-login și cadrul DS-p21-app-shell. Schema bazei de date (2026-11-25, V1–V7 și
seed-ul) acoperă firma, utilizatorii, clienții, produsele, seriile, facturile, încasările,
chitanțele, cursurile BNR și reminder-ele; regulile de integritate stau în baza de date
(PATTERN-002).

## Parts
| Part | Path | Responsibility | Talks to |
|---|---|---|---|
| backend | `backend/` | API REST (Java 21, Spring Boot 4.1, Maven); rulează migrările Flyway la pornire | db |
| frontend | `frontend/` | UI React + TypeScript (Vite, react-router), servit de nginx; `/api/` trimis spre backend; rutele protejate cer `GET /api/me` (`src/auth.tsx`) | backend (prin nginx) |
| db | `docker-compose.yml` → `db` | PostgreSQL 18, volumul `db-data` | — |
| teste | `docker-compose.yml` → profilul `test` | `backend-test` rulează `mvn -B verify` pe `db-test` (tmpfs); `frontend-test` rulează `npm ci` și `vitest run` (D-040) | db-test |
| CI | `.github/workflows/ci.yml` | la fiecare push: testele backend și frontend (aceleași comenzi) și ledger-lint | — |

## Data flow
Browser → nginx (`frontend`, port 8090 pe host) → `/api/*` → `backend:8080` → PostgreSQL (`db`).
Autentificarea (C-001): `GET /api/csrf` pune cookie-ul `XSRF-TOKEN`; `POST /api/login` creează sesiunea (cookie `JSESSIONID`, HttpOnly, SameSite=Lax);
fiecare pagină protejată cere `GET /api/me`; un 401 trimite la `/login?next=…` (D-034). Fiecare cerere autentificată verifică `app_user.active` (D-041).

## State and storage
| What | Where | Owner | Notes |
|---|---|---|---|
| Cotele de TVA | tabelul `vat_rate` | migrarea `V1__vat_rate.sql` | D-020: o cotă nouă = o migrare nouă; motivul de scutire lipsește (Q-034) |
| Firma, utilizatorii | `company` (un rând, D-003), `app_user` (D-012, D-029) | `V2__company_users.sql` | `rounding_rule` null până la Q-007 |
| Clienți, produse | `client` (PJ/PFA/PF), `product` | `V3__clients_products.sql` | D-010, D-019, D-021, D-022, D-032 |
| Serii și numerotare | `series`, `series_counter`, funcțiile `allocate_number` și `assign_document_number` | `V4__series_numbering.sql` | D-005: numărul îl pune trigger-ul la INSERT; contor pe (serie, an), lock pe rând până la commit, fără goluri și fără duplicate; contorul se schimbă doar din trigger; seria folosită nu-și mai schimbă numele sau formatul |
| Facturi, storno, proforme | `invoice`, `invoice_line`, `invoice_vat_summary`, `invoice_legal_note` | `V5__invoices.sql` | Copii la emitere, inclusiv procentul și categoria TVA (D-027); trigger-ele blochează orice schimbare pe FACTURA/STORNO după tranzacția care le-a creat; storno pe același client și aceeași monedă; tranzacția de emitere: PATTERN-003 |
| Încasări, chitanțe | `payment`, `receipt` | `V6__payments_receipts.sql` | D-028; chitanța doar pe numerar; chitanța și încasarea ei nemodificabile (D-033) |
| Cursuri BNR, reminder-e | `bnr_rate`, `reminder_settings` (un rând), `reminder_log` | `V7__bnr_reminders.sql` | Valorile reminder-elor: Q-019 |
| Seed local | firma și clientul S1, 3 utilizatori de test, seria `Aa`, factura S1 | `db/seed/R__seed.sql` (repetabilă, idempotentă) | Încărcat doar prin `SPRING_FLYWAY_LOCATIONS` din Compose; S1 își importă numărul 60 cu trigger-ele oprite (superuser); la deploy fără seed (Q-038) |
| Sesiunile | memoria backend-ului (Tomcat), 30 de minute de inactivitate | Spring Security | D-039: o repornire a backend-ului închide toate sesiunile |
| Încercările greșite de login | `app_user.failed_login_count`, `app_user.locked_until` | `V8__login_lockout.sql`, `LoginService` | D-036–D-038: pe cont; la a 5-a, blocare 15 minute și numărătoarea la 0 |
| Istoricul migrărilor | `flyway_schema_history` | Flyway | — |

Banii și cantitățile sunt `numeric` fără scale fix, cu `CHECK (scale(x) <= n)` (D-031).
Toate funcțiile au `search_path = pg_catalog, public, pg_temp`; TRUNCATE e refuzat pe tabelele cu documente și pe contor. Utilizatorul Postgres din producție: Q-038.

## Configuration
| Name | Where set | Used by | Notes |
|---|---|---|---|
| `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` | `.env` (din `.env.example`, nu intră în git) | `db`, `db-test`, `backend`, `backend-test` | obligatorii: fără ele Compose se oprește |
| `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` | `docker-compose.yml`, din variabilele de mai sus | backend | — |
| `SPRING_FLYWAY_LOCATIONS` | `docker-compose.yml` (`x-seed-env`) | backend, backend-test | `db/migration` + `db/seed`; la deploy, fără `db/seed` |
| `P21_COOKIE_SECURE` | `docker-compose.yml` (`backend`: `false`) | backend (`server.servlet.session.cookie.secure`) | D-039: implicit `true`; `false` doar local, pe http://localhost |
| `P21_SEED_PASSWORD_HASH` | `.env` (opțional) | seed-ul, ca placeholder Flyway `seed_password_hash` | hash BCrypt, între ghilimele simple; gol = conturi de test fără login |
<!-- names only, never values -->

## Build, deploy, runtime
Comenzile: AGENTS.md §2 (D-040). Imaginile: `backend/Dockerfile` (Maven → JRE 21),
`frontend/Dockerfile` (Node → nginx). Deploy: încă nu (Q-021).

## Boundaries
- API-ul de autentificare și `GET /api/health`: C-001. Publice doar `/api/health`, `GET /api/csrf`, `POST /api/login`; restul cer sesiune, iar fiecare rută are `@PreAuthorize` cu un nivel din `Access` (D-012, PATTERN-004).
