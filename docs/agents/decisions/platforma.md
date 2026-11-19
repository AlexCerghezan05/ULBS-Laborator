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
