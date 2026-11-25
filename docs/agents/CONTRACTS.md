# Contracts — ULBS-Laborator

<!-- graffino-agentic:begin v1 -->
> Promises across a boundary. **Provided:** this repo owns the contract; the text
> lives here only. **Consumed:** a link to the provider's entry plus the fields this
> repo relies on. **External:** what we rely on from a third-party service.
> Changing a provided contract is a PROPOSAL (AGENTS.md → The gate) and the plan
> lists the consumers' files too. Status: `approved` · `proposed` · `superseded-by`.
<!-- graffino-agentic:end -->

## Index
- C-001 · API-ul de autentificare (backend → frontend) · provided

---

## Provided

### C-001 · API-ul de autentificare (backend → frontend)
- **Status:** approved
- **Date:** 2026-11-25
- **Interface:** Aceeași origine, prin nginx (`/api/`). Erorile au corpul `{"error":"<COD>"}`; textul pentru utilizator îl alege frontend-ul după cod (PATTERN-005).
  - `GET /api/csrf` (public) → 204 și cookie-ul `XSRF-TOKEN` (citibil din JS).
  - `POST /api/login` (public, cere CSRF) cu `{"username","password"}` → 200 `{"username","fullName","role","companyName"}` (`role`: `ADMIN` | `OPERATOR` | `CONTABIL`; `companyName`: `company.name`, `null` dacă firma nu e completată), cookie `JSESSIONID` nou și `XSRF-TOKEN` nou · 401 `BAD_CREDENTIALS` · 403 `ACCOUNT_DISABLED` (doar cu parola corectă, D-035) · 429 `LOGIN_LOCKED` (D-036–D-038).
  - `GET /api/me` → 200, aceeași formă ca răspunsul la login (antetul DS-p21-app-shell).
  - `POST /api/logout` (cere CSRF) → 204; sesiunea invalidată, `JSESSIONID` șters.
  - `GET /api/health` (public) → 200 `{"status":"UP"}`.
  - Orice rută protejată: 401 `SESSION_EXPIRED` (cookie de sesiune care nu mai e valid) sau 401 `UNAUTHENTICATED` (fără sesiune, după logout, cont dezactivat: D-041) · 403 `FORBIDDEN` (rol greșit, D-012) · 403 `CSRF` (token CSRF lipsă sau greșit).
  - CSRF: orice POST/PUT/PATCH/DELETE trimite header-ul `X-XSRF-TOKEN` cu valoarea cookie-ului `XSRF-TOKEN` (D-039).
- **Documentation:** specificația OpenAPI la `GET /api/docs` și Swagger UI la `/api/docs/ui`, cu exemple pentru fiecare rută de mai sus. Doar Admin: fără sesiune 401 `UNAUTHENTICATED`, alt rol 403 `FORBIDDEN` (D-043).
- **Consumers:** `frontend/src/api.ts`, `frontend/src/pages/LoginPage.tsx`, `frontend/src/auth.tsx`.
- **Evidence:** planul „Autentificare și autorizare”, aprobat 25.11.2026; `companyName` cerut de Alexandru Cerghezan pentru antet, 25.11.2026; trimiterea la documentație, planul „Documentația API cu OpenAPI (Q-044)”, aprobat 25.11.2026 (`backend/src/main/java/ro/ulbs/p21/docs/OpenApiConfig.java`); `backend/src/main/java/ro/ulbs/p21/auth/LoginController.java`, `SecurityConfig.java`; testele din `backend/src/test/java/ro/ulbs/p21/auth/`.
- **Source:** agent · confidence high
- **Confirmed:** 2026-11-25

## Consumed

_None yet._

## External

_None yet._
