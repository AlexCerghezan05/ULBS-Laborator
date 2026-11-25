# Scope · P21 Facturare + e-Factura

Sursă: fișa proiectului (`docs/brief.md`), interviul cu clientul (`INTERVIEW.md`),
factura reală S1 (`SAMPLES.md`) și interviul cu Alexandru Cerghezan din 19.11.2026.
Termenii sunt cei din `CONTEXT.md`. Ce nu e decis rămâne întrebare Q- și nu se
construiește până la răspuns.

> Status: **aprobat** (19.11.2026), prin deciziile D-002…D-022 din `docs/agents/DECISIONS.md`.

## 1. Pentru cine
O singură firmă furnizor pe instalare: Aura Logistic SRL (în repo, anonimizată ca
„FURNIZOR EXEMPLU S.R.L.”, vezi Q-017). Datele firmei sunt o singură înregistrare,
editată de Admin. (D-003)

## 2. Roluri (auth pe server, D-012)
| Rol | Poate |
|---|---|
| Admin | tot: setări firmă, serii, utilizatori, plus tot ce poate Operatorul. Clientul primește cont de Admin. |
| Operator | clienți, produse/servicii, facturi, proforme, storno, încasări, chitanțe, export XML, reminder-e. (Rolul „emitent” din fișă.) |
| Contabil | doar citire, rapoarte și export. Contabilul real nu e încă contactat (Q-005). |

## 3. Ce intră

### Date de bază
- Firma furnizor: CUI, nr. reg. com., adresă, IBAN, bancă, plătitor/neplătitor de TVA, TVA la încasare da/nu, regula de rotunjire.
- Clienți din România: persoane juridice, PFA și persoane fizice (D-010). Persoana fizică are CNP opțional; fără el, XML-ul primește 13 zerouri (D-019). Email opțional; fără email nu se trimit reminder-e (D-022).
- Produse/servicii cu U.M. și cotă TVA. U.M. implicită: deschisă (Q-011, Q-015).
- Cotele de TVA (21%, 11%, scutit, taxare inversă, cu codul de clasificare pentru XML) stau într-un tabel populat printr-o migrare, fără ecran; o cotă nouă = o migrare nouă (D-020).

### Serii și numerotare (D-005)
- Mai multe serii, configurabile de Admin; fiecare cu tipul de document (factură, proformă, chitanță) și formatul de număr. Seed: seria `Aa`.
- Număr secvențial, fără goluri și fără duplicate sub emiteri simultane (test).
- Resetare anuală pe fiecare serie, după anul datei emiterii (D-005). Comportamentul după 9999: după răspunsul la Q-008.
- Storno-ul nu are serie proprie: ia următorul număr din seria facturii originale (interviu, D-005).
- Proforma nu consumă număr din seria de facturi (test). Clientul nu folosește proforme; ele rămân în scope și se testează cu date de test (Q-006).

### Facturi
- Data emiterii și data scadentă; linii cu discount; cote de TVA multiple pe aceeași factură.
- Linia caută în catalogul de produse/servicii și precompletează denumirea, U.M., prețul și cota (toate editabile); se acceptă și o linie scrisă liber (D-021).
- Fără ciorne: factura se salvează ca proformă sau se emite (D-018).
- Toate regimurile de TVA din fișă, configurabile: plătitor/neplătitor, TVA la încasare, taxare inversă, scutire, cu mențiunile legale. Cazurile pe care clientul nu le folosește se testează cu **facturi de test construite de noi**, ținute separat de facturile reale din SAMPLES.md. (D-006)
- Moneda: RON, EUR sau USD (D-008). La EUR/USD se folosește ultimul curs comunicat de BNR anterior datei emiterii și se afișează echivalentul în RON. Clientul facturează doar în RON, deci facturile în EUR/USD sunt facturi de test.
- Factura emisă nu se mai modifică, doar se stornează (test).
- Storno total și parțial, cu legătură la factura originală, vizibil în soldul clientului. Forma storno-ului parțial la client: Q-010.

### Rotunjire (D-007)
- Toată aritmetica în decimal/întregi (bani).
- Trei variante configurabile în setările firmei, fiecare testată, inclusiv pe S1:
  TVA pe linie, TVA pe total, calcul pornind de la prețul cu TVA.
- Varianta implicită: după răspunsul la Q-007.

### Cursuri BNR (D-008)
- Job zilnic care importă XML-ul oficial BNR; ecran de cursuri.
- Factura în EUR/USD folosește ultimul curs comunicat de BNR anterior datei emiterii (în practică, cel din ziua bancară precedentă), conform art. 290 din Codul fiscal. Dacă lipsește și acesta și se folosește unul mai vechi, factura e marcată „curs de rezervă” și poartă mențiunea. Confirmarea contabilului: Q-033.
- Ce face clientul real cu cursul: Q-009.

### Încasări
- Încasări parțiale și multiple, numerar sau transfer; sold per client; facturi restante.
- Chitanțe pentru încasările în numerar, cu numerotare proprie (seria lor) (Q-016). Chitanțele pe tranșe: Q-012.
- Reminder-e de plată pe email (cerință „must” din fișă, intră în scope chiar cu Q-019 deschisă):
  - Admin-ul configurează șablonul de email și regula de trimitere: după câte zile de la scadență și la câte zile se repetă.
  - Istoricul reminder-elor trimise: Admin, Operator și Contabil îl pot citi.
  - Pe o factură restantă, Admin-ul și Operatorul pot trimite manual un reminder („Trimite reminder”).
  - Valorile implicite (zile, text, server de email): după răspunsul la Q-019.

### PDF
- După modelul PDF SmartBill al clientului (S1): aceleași câmpuri, în aceeași ordine și în aceleași zone ale paginii, aspect apropiat, fără logo.
- Diferențele față de PDF-ul real se notează în SAMPLES.md.

### e-Factura (D-009)
- Export XML UBL 2.1 conform RO_CIUS, per factură și în lot, generat cu o bibliotecă XML.
- Validare cu schematron-ul RO_CIUS în CI; raportul validatorului în `docs/validation/`.

### Rapoarte (export CSV și XLSX)
- Jurnal de vânzări și TVA colectat: coloanele din formularul legal standard (Q-020).
- Raport de încasări: coloane, perioadă și format după răspunsul la Q-014.

### Ecrane
Login · setări firmă · serii · utilizatori · clienți · produse/servicii · listă facturi
(filtre: status, client, perioadă, restante) · creare/vizualizare factură și proformă
(cu PDF, XML, storno) · încasări (și chitanțe) · sold per client · rapoarte · cursuri BNR ·
reminder-e de plată.

Lista completă, cu ID-uri DS- și rolurile care văd fiecare ecran:
`docs/design/registry.yaml` (aprobată 19.11.2026). După login se deschide lista de facturi.
Creare factură, vizualizare factură și storno sunt ecrane separate.

### Cerințe comune
API REST documentat (OpenAPI), PostgreSQL cu migrări și seed (min 6 tabele), Docker
Compose, auth pe server pentru cele 3 roluri, validare pe server, min 10 teste (4 pe
twist), CI pe GitHub Actions cu teste și ledger-lint, deploy live HTTPS, README,
SUBMISSION.md, AI_LOG.md.

## 4. Ce NU intră
| Ce | De ce |
|---|---|
| Clienți din străinătate | Clientul nu are. Codul de țară e mereu `RO` (D-010). |
| Trimiterea directă în SPV (API ANAF) | Fișa cere doar export XML; cere certificat digital calificat. Sugestie pentru mai târziu. (D-009) |

## 5. Stack (D-002)
Java 21 + Spring Boot, PostgreSQL, Flyway, OpenAPI, React + TypeScript; schematron
RO_CIUS validat în Java (ph-schematron sau Saxon). Totul în Docker (D-001).

## 6. Blocaje deschise
- Q-018 răspunsă (25.11.2026): o singură factură reală (S1); Definition of Done cere „factura reală S1 produce XML valid” (README.md).
- Q-005 contabil · Q-007 rotunjire implicită · Q-008 format număr · Q-009 BNR ·
  Q-010 storno parțial · Q-011/Q-015 U.M. · Q-012 tranșe · Q-013 XML din SmartBill ·
  Q-014 raport de încasări · Q-017 numele firmei · Q-019 reminder-e · Q-020 coloane rapoarte · Q-021 deploy · Q-033 regula cursului BNR (contabil).
