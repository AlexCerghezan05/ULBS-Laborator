# Architecture — ULBS-Laborator

<!-- graffino-agentic:begin v1 -->
> How this repo is built, generated from the code at backfill and kept current by
> whoever changes the structure. The code wins: when this page is wrong, fix it.
<!-- graffino-agentic:end -->

## Overview
Scheletul (2026-11-19): un backend Spring Boot, un frontend React servit de nginx și
PostgreSQL, toate în Docker Compose (D-001, D-002, D-023). Funcții de business: încă
niciuna; există doar `GET /api/health` și tabelul cotelor de TVA (D-020).

## Parts
| Part | Path | Responsibility | Talks to |
|---|---|---|---|
| backend | `backend/` | API REST (Java 21, Spring Boot 4.1, Maven); rulează migrările Flyway la pornire | db |
| frontend | `frontend/` | UI React + TypeScript (Vite), servit de nginx; `/api/` trimis spre backend | backend (prin nginx) |
| db | `docker-compose.yml` → `db` | PostgreSQL 18, volumul `db-data` | — |
| teste | `docker-compose.yml` → profilul `test` | `backend-test` rulează `mvn -B verify` pe `db-test` (tmpfs) | db-test |
| CI | `.github/workflows/ci.yml` | la fiecare push: testele (aceeași comandă) și ledger-lint | — |

## Data flow
Browser → nginx (`frontend`, port 8080) → `/api/*` → `backend:8080` → PostgreSQL (`db`).
Pagina DS-p21-health cere `GET /api/health` o dată, la deschidere.

## State and storage
| What | Where | Owner | Notes |
|---|---|---|---|
| Cotele de TVA | tabelul `vat_rate` | migrarea `V1__vat_rate.sql` | D-020: o cotă nouă = o migrare nouă; motivul de scutire lipsește (Q-034) |
| Istoricul migrărilor | `flyway_schema_history` | Flyway | — |

## Configuration
| Name | Where set | Used by | Notes |
|---|---|---|---|
| `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` | `.env` (din `.env.example`, nu intră în git) | `db`, `db-test`, `backend`, `backend-test` | obligatorii: fără ele Compose se oprește |
| `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` | `docker-compose.yml`, din variabilele de mai sus | backend | — |
<!-- names only, never values -->

## Build, deploy, runtime
Comenzile: AGENTS.md §2 (D-023). Imaginile: `backend/Dockerfile` (Maven → JRE 21),
`frontend/Dockerfile` (Node → nginx). Deploy: încă nu (Q-021).

## Boundaries
- `GET /api/health` → `{"status":"UP"}`, folosit doar de DS-p21-health. Fără autentificare (D-012 vine în alt plan).
