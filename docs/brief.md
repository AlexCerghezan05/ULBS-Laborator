P21 · Facturare + e-Factura pentru un PFA real. Categoria E · Integrări & fișiere · ★★★★★ · ~45h

Pe scurt: aplicație de facturare pentru un freelancer, PFA sau SRL mic, real: clienți, servicii, facturi cu serii și numerotare, TVA configurabil, cursuri BNR, PDF, și export e-Factura (UBL XML RO_CIUS) care trece validatorul ANAF.

Input uman: un freelancer/PFA/SRL mic real; minim 5 facturi reale anonimizate; discuție cu el și ideal cu contabilul (serii, numerotare, storno, ce cere contabilul la final de lună, cum trimite în SPV). INTERVIEW.md + SAMPLES.md.

Cerințe funcționale (must):
- Date de firmă (CUI, nr. reg. com., adresă, IBAN, plătitor/neplătitor de TVA), clienți (persoane juridice și fizice), produse/servicii cu unitate de măsură și cotă de TVA.
- Facturi cu serie și număr secvențial fără goluri, proformă → factură, data emiterii/scadenței, linii cu discount, cote de TVA multiple pe aceeași factură, mențiuni legale (TVA la încasare, taxare inversă, scutire).
- Facturi în EUR/USD cu curs BNR din ziua emiterii (importat automat, zilnic) și echivalent în RON.
- Storno (factură cu valori negative care anulează parțial sau total o factură emisă), cu legătură la originală.
- Încasări (parțiale, multiple), sold per client, facturi restante, reminder-e de plată pe email.
- PDF al facturii după modelul real al clientului (verificat cu contabilul).
- Export e-Factura: XML UBL 2.1 conform RO_CIUS, per factură și în lot, validat cu validatorul ANAF sau cu schematron-ul RO_CIUS în CI.
- Rapoarte lunare pentru contabil (jurnal de vânzări, încasări, TVA colectat), export CSV/XLSX. Roluri: emitent, contabil (read-only + export), admin.

Cerințe tehnice specifice:
- Toată aritmetica financiară în decimal/întregi (bani); regulile de rotunjire (pe linie vs pe total) explicite și testate pe facturile reale.
- Numerotarea secvențială și fără duplicate sub concurență (test cu emiteri simultane).
- Job zilnic care importă cursurile BNR (XML oficial), cu fallback la ultimul curs disponibil și marcare în factură.
- Generarea XML cu o bibliotecă de XML (nu string concatenation), cu teste de validare schemă/schematron în CI.

Twist: fiecare factură reală din SAMPLES.md produce un XML care trece validarea RO_CIUS (coduri de țară și județ ANAF, codul de clasificare TVA pe linie și pe total, formatul sumelor, mențiuni la scutiri, storno cu cantități negative, identificatori pentru persoane fizice); diferențele dintre PDF-ul real și al meu sunt explicate. Două emiteri simultane pe aceeași serie nu pot sări sau dubla un număr; o factură emisă nu se mai modifică, doar se stornează; o proformă nu consumă număr din seria de facturi. Toate cu teste.

Variabilele mele: clientul real (regim TVA, serii, model PDF); regula de rotunjire cerută de contabil; ce rapoarte lunare îi trebuie contabilului.

Definition of Done:
- Cele 5 facturi reale reproduse în aplicație produc XML valid (raportul validatorului în repo).
- Numerotarea rezistă la emiteri simultane (test) și facturile emise sunt imutabile (test).
- Cursul BNR se importă automat; o factură în EUR are echivalentul corect.
- Storno-ul parțial funcționează și se vede în soldul clientului.
- Contabilul a văzut PDF-ul și rapoartele; feedback-ul e în INTERVIEW.md.

Cerințe comune din catalog: API REST/GraphQL documentat, PostgreSQL/MySQL cu migrări și seed real (min 6 tabele), Docker Compose, min 2 roluri cu auth pe server, validare pe server, min 10 teste (4 pe twist), CI pe GitHub Actions cu teste + ledger-lint, deploy live HTTPS, README, SUBMISSION.md, AI_LOG.md.
