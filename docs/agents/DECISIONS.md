# Decisions — ULBS-Laborator

<!-- graffino-agentic:begin v1 -->
> Constraints the code must obey. Status: `approved` (obey) · `proposed` (do not
> build on it) · `superseded-by <ID>` (kept to explain old code).
> Adding or changing an entry needs the user's OK (AGENTS.md → The gate).
<!-- graffino-agentic:end -->

## Index
- D-001 · Servers and databases run in Docker

---

## D-001 · Servers and databases run in Docker
- **Status:** approved
- **Date:** 2026-11-05
- **Rule:** Every server, database or backend service runs in a Docker container (Dockerfile or docker-compose.yml). Never install one natively on the host.
- **Why:** The owner installed Docker for this purpose and asked for all server work to go through it.
- **Rejected:** Native installs on Windows.
- **Evidence:** owner instruction, 2026-11-05; Docker 29.9.2 present on the host.
- **Source:** user · confidence high
