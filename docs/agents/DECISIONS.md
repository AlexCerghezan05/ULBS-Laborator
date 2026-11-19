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
- D-005 · Serii multiple configurabile, numerotare fără goluri
- D-006 · Toate regimurile de TVA, configurabile; facturi de test separate
- D-007 · Aritmetică decimală, rotunjire în trei variante configurabile
- D-008 · Facturi în RON, EUR și USD cu curs BNR din ziua emiterii
- D-009 · e-Factura: doar export XML, fără trimitere în SPV
- D-010 · Doar clienți din România
- D-011 · Design tokens și stil UI din DESIGN.md
- D-012 · Roluri: Admin, Operator, Contabil, cu drepturile pe ecrane
- D-013 · Discount ca procent pe linie
- D-014 · Seria proformei aleasă la salvare
- D-015 · Mențiunile legale sunt automate și needitabile
- D-016 · Termenul de plată implicit egal cu data emiterii

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

## D-011 · Design tokens și stil UI din DESIGN.md
- **Status:** approved
- **Date:** 2026-11-19
- **Rule:** UI-ul folosește doar tokens-urile din `docs/design/DESIGN.md` → Tokens: accent albastru-ardezie `#3E6A87` (dark `#7FA9C7`) pe bază neutră gri, font Inter cu cifre tabulare (PDF-ul în Helvetica/Arial), stil sobru și compact (14 px în tabele), spațiere pe pași de 4 px, colțuri de 4–6 px, borduri subțiri, umbră doar la meniuri și dialoguri, fără gradienți și fără animații decorative. Întâi pentru calculator, utilizabil pe telefon de la 360 px.
- **Why:** Clientul nu are logo sau alt material de brand; accentul vine din PDF-ul SmartBill pe care îl folosește deja (S1), ca aplicația și factura să arate unitar. Utilizatorii lucrează zilnic cu liste dense de facturi.
- **Rejected:** Altă culoare de accent; UI complet neutru; fonturile sistemului; un font ales separat; stil aerisit cu text de 15–16 px; ton prietenos cu „tu”; font monospace pentru coduri.
- **Evidence:** interviul de stil, 19.11.2026 (DESIGN.md → Style interview); `samples/factura_Aa0060_anonimizata.pdf`; contrast verificat pentru fiecare pereche de culori, în ambele teme.
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

## D-013 · Discount ca procent pe linie
- **Status:** approved
- **Date:** 2026-11-19
- **Rule:** Fiecare linie are un discount procentual (implicit 0%), aplicat la valoarea liniei: cantitate × preț unitar fără TVA × (1 − discount/100). TVA-ul se calculează din valoarea după discount. Nu există discount pe total.
- **Why:** Fișa cere linii cu discount; forma a fost stabilită de user (Q-023).
- **Rejected:** Discount ca valoare; discount pe total; discount ca linie separată.
- **Evidence:** răspunsul la Q-023, 19.11.2026; `docs/design/mocks/DS-p21-invoice-form.html` (U.M. și discount la „Mai multe”).
- **Source:** user · confidence high
- **Confirmed:** 2026-11-19

## D-014 · Seria proformei aleasă la salvare
- **Status:** approved
- **Date:** 2026-11-19
- **Rule:** La „Salvează ca proformă”, Operatorul alege seria dintr-o listă cu seriile de tip proformă (D-005); prima din listă e preselectată. Cazul fără nicio serie de proformă: Q-025.
- **Why:** D-005 permite mai multe serii pe tip de document; răspunsul la Q-024.
- **Rejected:** O singură serie de proformă implicită, fără alegere.
- **Evidence:** răspunsul la Q-024, 19.11.2026; `docs/design/mocks/DS-p21-invoice-form.html` (starea „Salvează ca proformă”).
- **Source:** user · confidence high
- **Confirmed:** 2026-11-19

## D-015 · Mențiunile legale sunt automate și needitabile
- **Status:** approved
- **Date:** 2026-11-19
- **Rule:** Mențiunile legale se adaugă automat din setările firmei (de exemplu art. 319 alin. 29, TVA la încasare), din cotele de pe linii (taxare inversă, scutire) și din cursul de rezervă (D-008). În formular apar doar ca text, sub totaluri, și numai când există; nu se editează.
- **Why:** D-006 cere mențiunile legale pentru fiecare regim; generarea automată evită o mențiune uitată sau greșită.
- **Rejected:** Mențiuni scrise sau editate de Operator în formular.
- **Evidence:** acceptat de user cu macheta DS-p21-invoice-form, 19.11.2026; SAMPLES.md (S1, mențiunea art. 319 alin. 29).
- **Source:** user · confidence high
- **Confirmed:** 2026-11-19

## D-016 · Termenul de plată implicit egal cu data emiterii
- **Status:** approved
- **Date:** 2026-11-19
- **Rule:** Termenul de plată e implicit egal cu data emiterii și poate fi modificat. Nu poate fi înaintea datei emiterii (eroare lângă câmp).
- **Why:** La client, termenul de plată este aceeași zi cu emiterea (CONTEXT.md „Termen de plată”; S1).
- **Rejected:** Termen implicit la un număr fix de zile după emitere.
- **Evidence:** acceptat de user cu macheta DS-p21-invoice-form, 19.11.2026; SAMPLES.md (S1, observația 5).
- **Source:** user · confidence high
- **Confirmed:** 2026-11-19
