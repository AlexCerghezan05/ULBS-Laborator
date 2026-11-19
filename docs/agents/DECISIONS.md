# Decisions — ULBS-Laborator

<!-- graffino-agentic:begin v1 -->
> Constraints the code must obey. Status: `approved` (obey) · `proposed` (do not
> build on it) · `superseded-by <ID>` (kept to explain old code).
> Adding or changing an entry needs the user's OK (AGENTS.md → The gate).
<!-- graffino-agentic:end -->

## Index
- D-001 · Servers and databases run in Docker
- D-002 · Stack: Java 21 + Spring Boot, PostgreSQL, React + TypeScript
- D-003 · O singură firmă furnizor pe instalare
- D-004 · Roluri: Admin, Operator, Contabil
- D-005 · Serii multiple configurabile, numerotare fără goluri
- D-006 · Toate regimurile de TVA, configurabile; facturi de test separate
- D-007 · Aritmetică decimală, rotunjire în trei variante configurabile
- D-008 · Facturi în RON, EUR și USD cu curs BNR din ziua emiterii
- D-009 · e-Factura: doar export XML, fără trimitere în SPV
- D-010 · Doar clienți din România

---

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

## D-004 · Roluri: Admin, Operator, Contabil
- **Status:** approved
- **Date:** 2026-11-19
- **Rule:** Trei roluri, verificate pe server: Admin (tot: setări firmă, serii, utilizatori și tot ce poate Operatorul), Operator (facturi, proforme, storno, încasări, chitanțe, export XML, reminder-e), Contabil (doar citire, rapoarte, export). Clientul are cont de Admin.
- **Why:** Fișa cere emitent / contabil / admin; clientul vrea acces la toate operațiunile admin-ului; „emitent” este termen de evitat în CONTEXT.md, deci rolul se numește Operator.
- **Rejected:** Păstrarea numelui „emitent” pentru rol.
- **Evidence:** interviul de scope, 19.11.2026 (întrebarea 6); INTERVIEW.md; CONTEXT.md.
- **Source:** user · confidence high
- **Confirmed:** 2026-11-19

## D-005 · Serii multiple configurabile, numerotare fără goluri
- **Status:** approved
- **Date:** 2026-11-19
- **Rule:** Admin-ul configurează serii; fiecare are tipul de document (factură, proformă, storno, chitanță) și formatul de număr. Seed: seria `Aa`. Storno-ul folosește seria facturii. Numărul este secvențial, fără goluri și fără duplicate sub emiteri simultane. Proforma nu consumă număr din seria de facturi. Resetarea anuală și formatul după 9999 așteaptă Q-008.
- **Why:** Fișa (twist) cere numerotare sigură sub concurență și proforme fără număr de factură; clientul folosește seria `Aa` și stornează pe aceeași serie.
- **Rejected:** Doar seria `Aa` fixă, cu o serie de proforme separată și fixă.
- **Evidence:** interviul de scope, 19.11.2026 (întrebarea 2); INTERVIEW.md; docs/brief.md.
- **Source:** user · confidence high
- **Confirmed:** 2026-11-19

## D-006 · Toate regimurile de TVA, configurabile; facturi de test separate
- **Status:** approved
- **Date:** 2026-11-19
- **Rule:** Aplicația suportă toate regimurile de TVA din fișă, configurabile: plătitor/neplătitor, TVA la încasare, taxare inversă, scutire, cote multiple pe aceeași factură, cu mențiunile legale. Cazurile pe care clientul nu le folosește se testează cu facturi de test, ținute separat de facturile reale din SAMPLES.md.
- **Why:** Fișa le cere, iar clientul folosește doar 21%, fără TVA la încasare, taxare inversă sau scutire.
- **Rejected:** Doar cazul clientului (plătitor, 21%, o singură cotă).
- **Evidence:** interviul de scope, 19.11.2026 (întrebarea 1); INTERVIEW.md; docs/brief.md.
- **Source:** user · confidence high
- **Confirmed:** 2026-11-19

## D-007 · Aritmetică decimală, rotunjire în trei variante configurabile
- **Status:** approved
- **Date:** 2026-11-19
- **Rule:** Toată aritmetica financiară se face în decimal/întregi (bani). Regula de rotunjire are trei variante, configurabile în setările firmei: TVA pe linie, TVA pe total, calcul pornind de la prețul cu TVA. Fiecare variantă e testată, inclusiv pe S1. Varianta implicită așteaptă Q-007.
- **Why:** Regula clientului nu e cunoscută (Q-007); pe S1 ambele calcule dau același rezultat.
- **Rejected:** O singură variantă fixă (TVA pe linie din net); amânarea rotunjirii până la Q-007.
- **Evidence:** interviul de scope, 19.11.2026 (întrebarea 4); SAMPLES.md (S1).
- **Source:** user · confidence high
- **Confirmed:** 2026-11-19

## D-008 · Facturi în RON, EUR și USD cu curs BNR din ziua emiterii
- **Status:** approved
- **Date:** 2026-11-19
- **Rule:** Facturile pot fi în RON, EUR sau USD. La EUR/USD se folosește cursul BNR din ziua emiterii: import zilnic din XML-ul oficial BNR, fallback la ultimul curs disponibil, marcat pe factură. Factura afișează echivalentul în RON. Clientul real facturează doar în RON, deci facturile în EUR/USD sunt facturi de test, ca la D-006.
- **Why:** Fișa și Definition of Done cer facturi în EUR/USD cu echivalent corect în RON.
- **Rejected:** Doar RON, cu importul BNR păstrat fără folosire pe facturi (propunerea inițială, schimbată de user).
- **Evidence:** decizia userului, 19.11.2026; docs/brief.md; INTERVIEW.md (valută); Q-009.
- **Source:** user · confidence high
- **Confirmed:** 2026-11-19

## D-009 · e-Factura: doar export XML, fără trimitere în SPV
- **Status:** approved
- **Date:** 2026-11-19
- **Rule:** e-Factura înseamnă export XML UBL 2.1 conform RO_CIUS, per factură și în lot, generat cu o bibliotecă XML (nu prin concatenare de stringuri). Aplicația nu trimite direct în SPV.
- **Why:** Fișa cere doar exportul; trimiterea în SPV ar cere un certificat digital calificat.
- **Rejected:** Trimitere directă în SPV prin API-ul ANAF.
- **Evidence:** interviul de scope, 19.11.2026 (întrebarea 13); docs/brief.md.
- **Source:** user · confidence high
- **Confirmed:** 2026-11-19

## D-010 · Doar clienți din România
- **Status:** approved
- **Date:** 2026-11-19
- **Rule:** Clienții sunt din România: persoane juridice, PFA și persoane fizice. Codul de țară este mereu RO.
- **Why:** Clientul real nu are clienți din străinătate.
- **Rejected:** Clienți din alte țări.
- **Evidence:** interviul de scope, 19.11.2026 (întrebarea 11); INTERVIEW.md.
- **Source:** user · confidence high
- **Confirmed:** 2026-11-19
