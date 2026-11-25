# Decisions · Facturare: serii, TVA, rotunjire, valută, linii, proforme

> Topic file. The index is in `../DECISIONS.md`.

## D-005 · Serii multiple configurabile, numerotare fără goluri
- **Status:** approved
- **Date:** 2026-11-19
- **Rule:** Admin-ul configurează serii; fiecare are tipul de document (factură, proformă, chitanță) și formatul de număr. Seed: seria `Aa`. Storno-ul nu are serie proprie: ia mereu următorul număr din seria facturii originale. Numărul este secvențial, fără goluri și fără duplicate sub emiteri simultane. Proforma nu consumă număr din seria de facturi. Numărul se resetează la 1 la începutul fiecărui an, pe fiecare serie; anul vine din data emiterii, iar unicitatea e pe (serie, an, număr). Formatul după 9999 așteaptă Q-008.
- **Why:** Fișa (twist) cere numerotare sigură sub concurență și proforme fără număr de factură; clientul folosește seria `Aa` și stornează pe aceeași serie, deci un tip de serie „storno” nu ar fi folosit (Q-028). Clientul resetează numerele anual (INTERVIEW.md); userul a confirmat resetarea la planul schemei, 25.11.2026.
- **Rejected:** Doar seria `Aa` fixă, cu o serie de proforme separată și fixă; tipul de serie „storno”, obligatoriu sau opțional; un singur contor per serie, fără an.
- **Evidence:** interviul de scope, 19.11.2026 (întrebarea 2); INTERVIEW.md; docs/brief.md; răspunsul la Q-028, 19.11.2026; planul „Schema bazei de date”, aprobat 25.11.2026; `backend/src/main/resources/db/migration/V4__series_numbering.sql`.
- **Source:** user · confidence high
- **Confirmed:** 2026-11-25

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

## D-008 · Facturi în RON, EUR și USD cu ultimul curs BNR comunicat înaintea datei emiterii
- **Status:** approved
- **Date:** 2026-11-19
- **Rule:** Facturile pot fi în RON, EUR sau USD. La EUR/USD se folosește ultimul curs comunicat de BNR anterior datei emiterii (în practică, cursul publicat în ziua bancară precedentă), conform art. 290 din Codul fiscal. Cursurile vin dintr-un import zilnic al XML-ului oficial BNR. „Curs de rezervă” și mențiunea pe factură apar doar dacă lipsește cursul din ziua bancară precedentă și se folosește unul mai vechi. Factura afișează echivalentul în RON. Clientul real facturează doar în RON, deci facturile în EUR/USD sunt facturi de test, ca la D-006. Confirmarea regulii de contabil: Q-033.
- **Why:** Fișa și Definition of Done cer facturi în EUR/USD cu echivalent corect în RON. BNR publică cursul zilei în jurul orei 13:00, iar art. 290 cere ultimul curs comunicat, deci cursul din ziua bancară precedentă e cazul normal, nu unul de rezervă (Q-031).
- **Rejected:** Doar RON, cu importul BNR păstrat fără folosire pe facturi (propunerea inițială, schimbată de user); cursul publicat chiar în ziua emiterii, cu orice alt curs marcat ca rezervă; ultimul curs publicat în momentul emiterii, inclusiv cel din aceeași zi.
- **Evidence:** decizia userului, 19.11.2026; docs/brief.md; INTERVIEW.md (valută); Q-009; răspunsul la Q-031, 19.11.2026.
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
- **Rule:** La „Salvează ca proformă”, Operatorul alege seria dintr-o listă cu seriile de tip proformă (D-005); prima din listă e preselectată. Cazul fără nicio serie de proformă: D-017.
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

## D-017 · Fără serie de proformă, butonul de proformă e dezactivat
- **Status:** approved
- **Date:** 2026-11-19
- **Rule:** Dacă nu există nicio serie de tip proformă, butonul „Salvează ca proformă” e dezactivat și lângă el apare mesajul „Nu există nicio serie de proformă. Adăugați una în Serii.”. „Serii” e link spre DS-p21-series doar pentru Admin; Operatorul vede același text, fără link. Seed-ul nu conține o serie de proformă (doar `Aa`, D-005).
- **Why:** D-014 cere alegerea dintr-o listă de serii de proformă; răspunsul la Q-025 acoperă lista goală. Doar Admin-ul configurează seriile (D-012).
- **Rejected:** O serie de proformă în seed; butonul ascuns.
- **Evidence:** răspunsul la Q-025, 19.11.2026; `docs/design/mocks/DS-p21-invoice-form.html` (stările „Fără serie de proformă”).
- **Source:** user · confidence high
- **Confirmed:** 2026-11-19

## D-018 · Fără ciorne de factură
- **Status:** approved
- **Date:** 2026-11-19
- **Rule:** Nu există ciorne. Formularul de factură are doar „Salvează ca proformă” și „Emite factura”; lista de facturi nu are statusul „Ciornă”.
- **Why:** Macheta aprobată avea „Salvează ciornă” fără nicio decizie sau cerință în spate; răspunsul la Q-026.
- **Rejected:** Ciorne editabile, fără număr, cu status propriu în listă.
- **Evidence:** răspunsul la Q-026, 19.11.2026; `docs/design/mocks/DS-p21-invoice-form.html`.
- **Source:** user · confidence high
- **Confirmed:** 2026-11-19

## D-020 · Cotele de TVA într-un tabel populat prin migrare
- **Status:** approved
- **Date:** 2026-11-19
- **Rule:** Cotele de TVA stau într-un singur loc: un tabel în baza de date, populat printr-o migrare Flyway: 21%, 11%, scutit (0%), taxare inversă (0%), fiecare cu codul de clasificare TVA pentru XML. Nu există ecran sau setare pentru cote. O cotă nouă înseamnă o migrare nouă.
- **Why:** Macheta oferea pe linie cote fără o sursă stabilită; o schimbare de lege (ca 19% → 21%) se face printr-o versiune nouă (Q-029).
- **Rejected:** Cote configurabile de Admin în setările firmei; ecran separat de cote; listă fixă scrisă direct în cod.
- **Evidence:** răspunsul la Q-029, 19.11.2026; `docs/design/mocks/DS-p21-invoice-form.html`; D-006.
- **Source:** user · confidence high
- **Confirmed:** 2026-11-19

## D-021 · Linia de factură din catalog, cu text liber permis
- **Status:** approved
- **Date:** 2026-11-19
- **Rule:** Câmpul „Produs / serviciu” al liniei caută în catalogul de produse/servicii; la alegere completează denumirea, U.M., prețul și cota, toate modificabile pe linie. Se poate scrie și o denumire care nu e în catalog, cu U.M., preț și cotă completate de mână.
- **Why:** Scope-ul are catalog (DS-p21-products), iar pe S1 denumirea e scrisă pentru factura respectivă; răspunsul la Q-030.
- **Rejected:** Linie obligatoriu din catalog (editabilă sau nu).
- **Evidence:** răspunsul la Q-030, 19.11.2026; `docs/design/registry.yaml`; SAMPLES.md (S1).
- **Source:** user · confidence high
- **Confirmed:** 2026-11-19

## D-026 · O singură factură reală (S1); restul cazurilor cu facturi de test
- **Status:** approved
- **Date:** 2026-11-25
- **Rule:** S1 (Aa0060) este singura factură reală și trebuie să producă XML RO_CIUS valid (raportul validatorului în repo). Storno-ul, facturile în EUR/USD, cotele de TVA multiple, clientul persoană fizică și proforma se testează cu facturi de test, marcate clar ca test și ținute separat de S1. D-006 și D-008 rămân.
- **Why:** Clientul a refuzat să dea alte facturi în afară de S1 și nu folosește proforme; profesorul a confirmat pe 25.11.2026 că o singură factură reală e suficientă (Q-018, Q-006).
- **Rejected:** Cinci facturi reale, cum cere fișa.
- **Evidence:** răspunsurile la Q-018 și Q-006, 25.11.2026; INTERVIEW.md (follow-up 25.11.2026); SAMPLES.md.
- **Source:** user · confidence high
- **Confirmed:** 2026-11-25

## D-027 · Factura copiază la emitere datele clientului, ale furnizorului, totalurile și mențiunile
- **Status:** approved
- **Date:** 2026-11-25
- **Rule:** La emitere, factura (și storno-ul, proforma) primește o copie a datelor clientului (tip, denumire, CIF, Reg. com., CNP, adresă, oraș, județ) și ale furnizorului (denumire, CIF, Reg. com., adresă, oraș, județ, IBAN, bancă), totalurile în monedă și în RON, TVA-ul pe cote (`invoice_vat_summary`) și mențiunile legale (`invoice_legal_note`). Liniile copiază denumirea, U.M., prețul și cota. O editare ulterioară a clientului, a firmei sau a catalogului nu schimbă factura.
- **Why:** Factura emisă nu se mai modifică (CONTEXT.md „Factură emisă”); PDF-ul și XML-ul trebuie să arate datele de la emitere.
- **Rejected:** Factura citește datele curente ale clientului și ale firmei prin FK.
- **Evidence:** planul „Schema bazei de date”, aprobat 25.11.2026 (A1); `V5__invoices.sql`.
- **Source:** user · confidence high
- **Confirmed:** 2026-11-25

## D-028 · Încasarea e în moneda facturii
- **Status:** approved
- **Date:** 2026-11-25
- **Rule:** Suma unei încasări (`payment.amount`) e în moneda facturii pe care o încasează. Nu există curs pe încasare.
- **Why:** Clientul real facturează doar în RON (Q-009); facturile în EUR/USD sunt facturi de test (D-008).
- **Rejected:** Încasare într-o altă monedă decât factura, cu curs propriu.
- **Evidence:** planul „Schema bazei de date”, aprobat 25.11.2026 (A2); `V6__payments_receipts.sql`.
- **Source:** user · confidence high
- **Confirmed:** 2026-11-25

## D-030 · Factura reține utilizatorul care a emis-o
- **Status:** approved
- **Date:** 2026-11-25
- **Rule:** Fiecare factură, storno sau proformă are `issued_by`, utilizatorul care a emis-o (obligatoriu). Pe S1 (seed) este Operatorul de test „Ion Ionescu”, numele anonimizat de la „Întocmit de”.
- **Why:** PDF-ul SmartBill al clientului (S1) are „Întocmit de”.
- **Rejected:** none recorded.
- **Evidence:** planul „Schema bazei de date”, aprobat 25.11.2026 (A4); `samples/factura_Aa0060_anonimizata.pdf`.
- **Source:** user · confidence high
- **Confirmed:** 2026-11-25

## D-031 · Precizia valorilor, fără rotunjire tăcută în baza de date
- **Status:** approved
- **Date:** 2026-11-25
- **Rule:** Valorile zecimale sunt `numeric` fără scale fix, cu `CHECK (scale(x) <= n)`: sumele de bani cel mult 2 zecimale, cantitatea și prețul unitar cel mult 4, cursul BNR cel mult 4, discountul cel mult 2. Baza respinge o valoare cu mai multe zecimale în loc s-o rotunjească; rotunjirea se face explicit în cod (D-007). Nicio coloană `real` sau `double precision`. Excepție: `vat_rate.percent` (V1, `numeric(5,2)`).
- **Why:** `numeric(15,2)` ar rotunji în tăcere o valoare cu 3 zecimale și ar ascunde o rotunjire uitată în cod.
- **Rejected:** `numeric(p,s)` cu scale fix.
- **Evidence:** planul „Schema bazei de date”, aprobat 25.11.2026 (A5 și ajustarea 2); `MoneyColumnsIT`.
- **Source:** user · confidence high
- **Confirmed:** 2026-11-25

## D-033 · Chitanța emisă nu se modifică și nu se șterge
- **Status:** approved
- **Date:** 2026-11-25
- **Rule:** Ca factura emisă, chitanța nu se mai modifică și nu se șterge (trigger pe `receipt`), ca numerotarea ei să rămână fără goluri (D-005).
- **Why:** Chitanța are numerotare proprie, cu aceleași reguli ca facturile.
- **Rejected:** Chitanță editabilă sau ștearsă după emitere.
- **Evidence:** planul „Schema bazei de date”, aprobat 25.11.2026 (A7); `V6__payments_receipts.sql`; `ReceiptImmutableIT`.
- **Source:** user · confidence high
- **Confirmed:** 2026-11-25
