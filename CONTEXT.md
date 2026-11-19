# CONTEXT — ULBS-Laborator vocabulary

<!-- graffino-agentic:begin v1 -->
> The approved terms for this project. Use each term exactly as defined, in the part
> of speech given. Never swap in a synonym; never use a word on an _Avoid_ line.
<!-- graffino-agentic:end -->

Termenii clientului (Aura Logistic SRL), din interviul din 16.11.2026 și din factura Aa0060.

| Termen | Parte de vorbire | Sens | _Avoid_ |
|---|---|---|---|
| Furnizor | substantiv | Firma care emite factura (clientul nostru). Plătitor de TVA, fără TVA la încasare. | emitent, vânzător |
| Client | substantiv | Firma care primește factura: persoană juridică sau PFA din România, cu CIF. | cumpărător, beneficiar |
| PFA | substantiv | Persoană fizică autorizată. Are CIF fără prefix RO și Reg. com. care începe cu F. Se facturează ca B2B. | persoană fizică |
| Serie | substantiv | Literele din fața numărului facturii. Clientul folosește o singură serie: `Aa`. | prefix, cod |
| Număr factură | substantiv | 4 cifre cu zerouri în față (`0060`). Secvențial, fără goluri, resetat la 1 în fiecare an (Q-008). | id factură |
| Factură emisă | substantiv | Factură cu număr alocat. Nu se mai modifică, doar se stornează. | factură finalizată |
| Storno | substantiv | Factură cu cantități negative, aceeași serie, care anulează o factură emisă. | anulare, credit note |
| Proformă | substantiv | Nu consumă număr din seria de facturi. Folosirea ei la client e neclară (Q-006). | ofertă |
| Chitanță | substantiv | Document pentru încasarea în numerar, cu numerotare proprie (`Aa 0021`) (Q-016). | bon |
| Încasare | substantiv | Plată primită pe o factură: numerar sau transfer, uneori în mai multe tranșe. | plată primită |
| Termen de plată | substantiv | Data scadentă. La client: o zi, în practică aceeași dată cu emiterea. | deadline |
| U.M. | substantiv | Unitate de măsură. Declarat „buc”; pe factura reală apare „1” (Q-015). | unitate |
| Cotă TVA | substantiv | Procentul de TVA. Clientul folosește doar 21%. | rată TVA |
| SPV | substantiv | Spațiul Privat Virtual ANAF, unde ajung facturile e-Factura. | portal ANAF |
| Index încărcare SPV | substantiv | Numărul primit de la ANAF la încărcarea facturii, trecut pe PDF. | id ANAF |
| Raport de încasări | substantiv | Raportul pe care clientul vrea să-l vadă: încasările pe o perioadă (Q-014). | raport plăți |
| Admin | substantiv | Rol de utilizator: poate tot (setări firmă, serii, utilizatori și tot ce poate Operatorul). Clientul are cont de Admin. | administrator de sistem |
| Operator | substantiv | Rol de utilizator: emite facturi, proforme, storno, înregistrează încasări și chitanțe, exportă XML. Rolul „emitent” din fișă. | emitent, utilizator |
| Contabil | substantiv | Rol de utilizator: doar citire, rapoarte și export. | auditor |
| Factură de test | substantiv | Factură construită de noi (nu de client) ca să testăm cazuri pe care clientul nu le folosește (taxare inversă, scutire, TVA la încasare, cote multiple). Se ține separat de facturile reale din SAMPLES.md. | factură reală, sample |
| Factură reală | substantiv | Factură emisă de client în SmartBill, anonimizată în SAMPLES.md (S1–S5). | exemplu, factură de test |
| Mențiune legală | substantiv | Textul obligatoriu tipărit pe factură (ex. art. 319 alin. 29 pe S1; mențiunile pentru TVA la încasare, taxare inversă, scutire). | notă, observație |
| Regulă de rotunjire | substantiv | Felul în care se calculează și se rotunjește TVA-ul: pe linie, pe total sau pornind de la prețul cu TVA. Configurabilă în setările firmei; cea implicită e deschisă (Q-007). | precizie |
| Sold client | substantiv | Suma rămasă de încasat de la un client: facturi emise minus storno minus încasări. | datorie, balanță |
| Factură restantă | substantiv | Factură emisă cu termenul de plată depășit și sold neîncasat. | factură întârziată |
| Reminder de plată | substantiv | Email trimis clientului pentru o factură restantă. Clientul nu trimite acum reminder-e (Q-019). | notificare, somație |
| Curs BNR | substantiv | Cursul oficial publicat zilnic de BNR (XML), importat automat. Se folosește la facturile în EUR/USD, din ziua emiterii (D-008). Clientul real facturează doar în RON (Q-009). | curs valutar, rată |
| Jurnal de vânzări | substantiv | Raportul lunar cu facturile emise, pentru contabil (coloane: Q-020). | registru vânzări |
| TVA colectat | substantiv | TVA-ul de pe facturile emise într-o perioadă, raportat contabilului (coloane: Q-020). | TVA de plată |
| SmartBill | substantiv | Programul de facturare pe care clientul îl folosește acum; modelul de PDF vine de aici. | — |
