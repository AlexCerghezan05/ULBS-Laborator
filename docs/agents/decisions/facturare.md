# Decisions · Facturare: serii, TVA, rotunjire, valută, linii, proforme

> Topic file. The index is in `../DECISIONS.md`.

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
