# INTERVIEW · P21 Facturare + e-Factura

## Interviul 1 · 16.11.2026

| Câmp | Valoare |
|---|---|
| Client | Aura Logistic SRL (Sibiu) |
| Domeniu | Transport alternativ de persoane |
| Persoana intervievată | Oprescu Gabriel, Administrator |
| Data | 16.11.2026 |
| Locul | Față în față, la biroul clientului |
| Contabil | Nu a participat |
| Înregistrare | Nu (notițe scrise în timpul interviului) |
| Acord pentru folosirea informațiilor și a facturilor anonimizate | La interviu: nu. **Acord dat ulterior, pe 16.11.2026.** |
| Acord să fie contactat de comisie | Da, telefonic (numărul e trecut în formularul de predare, nu în repo-ul public) |

Notițele complete (cu întrebări și răspunsuri) sunt în `docs/interview/Interviu_P21_completat.docx` (fără datele de contact ale clientului).

## Ce am aflat

### Regim de TVA și clienți
- Firma este plătitoare de TVA. **Nu** aplică TVA la încasare.
- Folosește o singură cotă: **21%**. Nu a avut niciodată mai multe cote pe aceeași factură.
- Nu a facturat cu taxare inversă sau cu scutire.
- Nu are clienți persoane fizice și nici clienți din străinătate. (Pe factura Aa0060 clientul este un PFA, cu CIF.)

### Serii și numerotare
- O singură serie: **Aa**.
- Numărul are 4 cifre, completat cu zerouri în față (ex. `Aa0060`). Se resetează la 1 în fiecare an.
- O factură greșită, deja emisă, se **stornează**.
- Nu i s-a întâmplat să sară un număr sau să aibă două facturi cu același număr.
- Proforme: răspuns contradictoriu („nu folosesc proforme” / „după ce proforma e plătită se transformă în factură”). Vezi Q-006.

### Conținutul facturii
- Unitate de măsură declarată: bucăți. (Pe factura Aa0060 coloana U.M. conține „1”. Vezi Q-015.)
- Nu dă discounturi.
- Termen de plată: **o zi**, același pentru toți clienții.
- Pe factură scrie în plus IBAN-ul și banca.

### Valută
- Facturează **doar în RON**. A menționat cursul BNR din ziua emiterii (vezi Q-009).
- Nu facturează în weekend.

### Storno
- A făcut doar storno **total**. Factura de storno are cantități negative și aceeași serie ca originalele.
- Nu a vrut să dea o factură de storno ca exemplu.

### Încasări
- Clienții plătesc numerar sau prin transfer. A primit și facturi plătite în mai multe tranșe.
- Evidența datoriilor o ține în programul de facturare.
- Nu trimite reminder-e de plată. Nu a vrut să dea exemplu de email.

### Cum lucrează acum (e-Factura / SPV)
- Emite cu **SmartBill** (aprox. 80 RON/lună).
- Trimite în SPV direct din SmartBill, cu un buton.
- Nu a primit niciodată erori de validare de la ANAF.
- Nu poate descărca XML-urile din SPV (vezi Q-013).
- Cel mai mult îl deranjează că **nu poate modifica o factură deja trimisă în SPV**.

### Contabilul și rapoarte (răspunsurile sunt ale clientului; contabilul nu a fost prezent)
- Rotunjire: a răspuns „nu rotunjesc TVA-ul” (vezi Q-007 și observația din SAMPLES.md).
- Contabilul nu cere nimic la final de lună, își ia datele direct din SPV.
- Coloanele jurnalului de vânzări: nediscutat.
- Clientul vrea în aplicație acces la toate operațiunile pe care le poate face admin-ul.
- Acceptă să vadă PDF-ul și **raportul de încasări** după 25 noiembrie. Contact telefonic, PDF-ul îl vrea pe email.

### Modelul de PDF
- Model: PDF-ul generat de SmartBill.
- Obligatoriu pe PDF: firma, CIF, suma, cota de TVA, data emiterii, data scadentă, datele clientului (plus IBAN și banca).
- Nu are logo.

## Follow-up · 25.11.2026 (telefonic)

| Câmp | Valoare |
|---|---|
| Cu cine | Clientul (Aura Logistic SRL); separat, profesorul |
| Data | 25.11.2026 |
| Cum | Telefonic |

- **Proforme (Q-006):** clientul nu folosește proforme. Proformele rămân în aplicație pentru că le cere fișa și se testează cu date de test.
- **Alte facturi reale (Q-018):** clientul a refuzat să dea alte facturi în afară de S1. Profesorul a confirmat, pe 25.11.2026, că o singură factură reală e suficientă. Punctul din Definition of Done devine „factura reală S1 produce XML valid”. Storno-ul, EUR, cotele multiple și persoana fizică se acoperă cu facturi de test, marcate clar ca test.

## Ce am schimbat în proiect pe baza interviului
_Se completează pe măsură ce iau decizii (link către D- din `docs/agents/DECISIONS.md`)._

- **D-003 · O singură firmă furnizor:** clientul este o singură firmă (Aura Logistic SRL) și emite facturi doar în numele ei → aplicația servește o singură firmă furnizor pe instalare, fără multi-tenant.
- **D-004 · Roluri Admin, Operator, Contabil:** clientul vrea acces la toate operațiunile pe care le poate face admin-ul, iar contabilul își ia datele din SPV și nu cere nimic la final de lună → clientul primește cont de Admin; Contabilul are un rol separat, doar de citire, rapoarte și export.
- **D-005 · Serii configurabile:** clientul folosește o singură serie, `Aa`, cu număr din 4 cifre resetat anual, și stornează pe aceeași serie → serii configurabile de Admin, cu `Aa` în seed; storno-ul folosește seria facturii.
- **D-006 · Toate regimurile de TVA:** clientul e plătitor de TVA, folosește doar 21% și n-a facturat cu TVA la încasare, taxare inversă sau scutire → aplicația le suportă pe toate, cum cere fișa, iar cazurile pe care clientul nu le folosește se testează cu facturi de test.
- **D-007 · Rotunjire în trei variante:** clientul a spus „nu rotunjesc TVA-ul”, iar pe S1 netul pare calculat din prețul cu TVA → trei variante de rotunjire configurabile și testate; varianta implicită așteaptă Q-007.
- **D-008 · Facturi în RON, EUR și USD:** clientul facturează doar în RON, dar a menționat cursul BNR din ziua emiterii → aplicația acceptă și EUR/USD, cu curs BNR din ziua emiterii, cum cere fișa, ca facturi de test (Q-009 rămâne deschisă).
- **D-009 · Doar export XML:** clientul trimite în SPV direct din SmartBill, cu un buton, și n-a primit erori de validare → aplicația exportă XML-ul RO_CIUS, dar nu trimite în SPV.
- **D-010 · Doar clienți din România:** clientul nu are clienți din străinătate → codul de țară al clientului este mereu RO.

## Feedback pe aplicație (după 25.11.2026)
_De completat: data, cine, ce a văzut (PDF, raport de încasări), ce a spus, ce am schimbat._
