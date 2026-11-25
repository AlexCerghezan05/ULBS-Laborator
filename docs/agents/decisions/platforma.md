# Decisions · Platformă: Docker, stack, firmă, roluri

> Topic file. The index is in `../DECISIONS.md`.

## D-001 · Servers and databases run in Docker
- **Status:** approved
- **Date:** 2026-11-05
- **Rule:** Every server, database or backend service runs in a Docker container (Dockerfile or docker-compose.yml). Never install one natively on the host.
- **Why:** The owner installed Docker for this purpose and asked for all server work to go through it.
- **Rejected:** Native installs on Windows.
- **Evidence:** owner instruction, 2026-11-05; Docker 29.9.2 present on the host.
- **Source:** user · confidence high

## D-002 · Stack: Java 21 + Spring Boot, PostgreSQL, React + TypeScript
- **Status:** approved
- **Date:** 2026-11-19
- **Rule:** Backend Java 21 + Spring Boot; PostgreSQL cu migrări Flyway; API REST documentat cu OpenAPI; frontend React + TypeScript. Schematron-ul RO_CIUS rulează în Java (ph-schematron sau Saxon), în CI. Totul în Docker (D-001).
- **Why:** Schematron-ul RO_CIUS este XSLT 2.0 și rulează nativ în Java; `BigDecimal` și job-urile programate sunt incluse în platformă.
- **Rejected:** Node.js + NestJS, Python + Django (ambele ar cere un pas Saxon separat în CI).
- **Evidence:** interviul de scope, 19.11.2026 (întrebarea 10); Q-001.
- **Source:** user · confidence high
- **Confirmed:** 2026-11-19

## D-003 · O singură firmă furnizor pe instalare
- **Status:** approved
- **Date:** 2026-11-19
- **Rule:** Aplicația servește o singură firmă furnizor. Datele firmei sunt o singură înregistrare, editată de Admin. Fără multi-tenant.
- **Why:** Clientul real este o singură firmă (Aura Logistic SRL).
- **Rejected:** Multi-tenant (mai multe firme furnizor, date separate pe firmă).
- **Evidence:** interviul de scope, 19.11.2026 (întrebarea 7); INTERVIEW.md.
- **Source:** user · confidence high
- **Confirmed:** 2026-11-19

## D-012 · Roluri: Admin, Operator, Contabil, cu drepturile pe ecrane
- **Status:** approved
- **Date:** 2026-11-19
- **Rule:** Trei roluri, verificate pe server. Clientul are cont de Admin.
  - Admin: tot (setări firmă, serii, utilizatori, șablonul și regula reminder-elor, rapoarte, cursuri BNR și tot ce poate Operatorul).
  - Operator: clienți, produse/servicii, facturi, proforme, storno, încasări, chitanțe, export XML; trimite manual un reminder pe o factură restantă; citește istoricul reminder-elor și cursurile BNR. Nu vede rapoartele.
  - Contabil: doar citire (clienți, produse, facturi, încasări, sold per client, cursuri BNR, istoricul reminder-elor), rapoarte și export (CSV/XLSX, PDF, XML).
  - Detaliul pe fiecare ecran: `docs/design/registry.yaml` → `roles`.
- **Why:** Fișa cere emitent / contabil / admin; „emitent” este termen de evitat în CONTEXT.md, deci rolul se numește Operator. Lista ecranelor din 19.11.2026 a stabilit ce vede fiecare rol.
- **Rejected:** Păstrarea numelui „emitent” pentru rol; Operator cu acces la rapoarte; reminder-e configurabile de Operator.
- **Evidence:** interviul de scope, 19.11.2026 (întrebarea 6); aprobarea listei de ecrane, 19.11.2026; `docs/design/registry.yaml`; INTERVIEW.md; CONTEXT.md.
- **Source:** user · confidence high
- **Confirmed:** 2026-11-19

## D-023 · Build și rulare: Maven, testele în profilul `test` din Compose, frontend în nginx
- **Status:** superseded-by D-040
- **Date:** 2026-11-19
- **Rule:** Backend-ul se construiește cu Maven. Testele rulează în profilul `test` din `docker-compose.yml`: un container Maven rulează `mvn -B verify` pe un Postgres separat (`db-test`, date în tmpfs); aceeași comandă local și în CI. Frontend-ul se construiește cu Vite și e servit de nginx, care trimite `/api` spre backend.
- **Why:** Pe host nu se instalează Java sau Node (D-001); o comandă identică local și în CI, fără acces la socket-ul Docker; nginx seamănă cu ce va rula la deploy.
- **Rejected:** Gradle; Testcontainers; serverul de dezvoltare Vite în Compose (singur sau ca profil `dev`).
- **Evidence:** planul „Scheletul proiectului”, aprobat 19.11.2026 (întrebările Build Java, DB în teste, Frontend).
- **Source:** user · confidence high

## D-024 · Securitate de bază: nginx, Docker, CI, fără indexare
- **Status:** approved
- **Date:** 2026-11-19
- **Rule:**
  - nginx: `server_tokens off`; pe toate răspunsurile `X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY`, `Referrer-Policy: no-referrer`.
  - Aplicația nu se indexează: `<meta name="robots" content="noindex, nofollow">` în `index.html`, `X-Robots-Tag: noindex, nofollow` din nginx și `robots.txt` cu `Disallow: /`.
  - Backend-ul rulează în container ca utilizator non-root.
  - Local, porturile publicate de Compose sunt legate doar la `127.0.0.1`.
  - CI: `permissions: contents: read`; acțiunile GitHub sunt fixate pe SHA de commit, cu tagul în comentariu.
- **Why:** Aplicația e internă, cu autentificare (D-012). Review-ul de securitate și SEO/GEO al PR #1 a găsit aceste lipsuri.
- **Rejected:** none recorded.
- **Evidence:** review-ul PR #1 cu trei sub-agenți (Performance, Security, SEO & GEO), 19.11.2026; aprobarea owner-ului, 19.11.2026.
- **Source:** user · confidence high
- **Confirmed:** 2026-11-19

## D-029 · Un utilizator are un singur rol
- **Status:** approved
- **Date:** 2026-11-25
- **Rule:** Fiecare utilizator are exact unul din rolurile Admin, Operator, Contabil (D-012), în coloana `app_user.role`.
- **Why:** Admin-ul include deja tot ce poate Operatorul; nu e nevoie de roluri combinate.
- **Rejected:** Mai multe roluri pe utilizator.
- **Evidence:** planul „Schema bazei de date”, aprobat 25.11.2026 (A3); `V2__company_users.sql`.
- **Source:** user · confidence high
- **Confirmed:** 2026-11-25

## D-040 · Build și rulare: Maven și Vitest în profilul `test` din Compose, frontend în nginx
- **Status:** approved
- **Date:** 2026-11-25
- **Rule:** Backend-ul se construiește cu Maven. Testele rulează în profilul `test` din `docker-compose.yml`: `backend-test` (container Maven) rulează `mvn -B verify` pe un Postgres separat (`db-test`, date în tmpfs); `frontend-test` (container Node) rulează `npm ci` și `vitest run`. Aceleași comenzi local și în CI. Frontend-ul se construiește cu Vite și e servit de nginx, care trimite `/api` spre backend.
- **Why:** Ca D-023, plus testele de frontend cerute de planul de autentificare (redirectul sigur, D-034), fără Node pe host (D-001).
- **Rejected:** Gradle; Testcontainers; serverul de dezvoltare Vite în Compose; verificarea frontend-ului doar de mână.
- **Evidence:** planul „Autentificare și autorizare”, aprobat de Alexandru Cerghezan, 25.11.2026 (D-023 → superseded-by D-040).
- **Source:** user · confidence high
- **Confirmed:** 2026-11-25

## D-043 · Documentația API e la /api/docs, doar pentru Admin
- **Status:** approved
- **Date:** 2026-11-25
- **Rule:** Specificația OpenAPI (springdoc) e la `GET /api/docs` și Swagger UI la `/api/docs/ui`, sub `/api/`, unde nginx trimite deja cererile la backend. Accesul e doar pentru rolul Admin, în toate mediile, inclusiv în producție: fără sesiune 401 `UNAUTHENTICATED`, alt rol 403 `FORBIDDEN` (C-001). Regula stă în `SecurityConfig`, pentru că rutele springdoc nu sunt controllerele aplicației. „Try it out” trimite `X-XSRF-TOKEN` din cookie-ul `XSRF-TOKEN` (`springdoc.swagger-ui.csrf.enabled=true`, D-039).
- **Why:** D-002 cere API documentat cu OpenAPI, dar API-ul nu trebuie expus public: aplicația e internă (D-024).
- **Rejected:** documentație publică; documentația dezactivată în producție; un `API.md` scris de mână (nu mai e nevoie: springdoc 3.2.0 e compatibil cu Boot 4.1).
- **Evidence:** planul „Documentația API cu OpenAPI (Q-044)”, aprobat de Alexandru Cerghezan, 25.11.2026; `backend/src/main/java/ro/ulbs/p21/auth/SecurityConfig.java`; `backend/src/test/java/ro/ulbs/p21/docs/ApiDocsIT.java`.
- **Source:** user · confidence high
- **Confirmed:** 2026-11-25
