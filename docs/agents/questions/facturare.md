# Open questions · Facturare: proforme, rotunjire, numerotare, curs, storno, linii

> Topic file. The index is in `../OPEN-QUESTIONS.md`.

## Q-006 · Proforme
- **Status:** open
- **Context:** Clientul a spus atât „nu folosesc proforme”, cât și că proforma devine factură după plată. Le folosește sau nu?
- **Blocks:** Fluxul proformă → factură.
- **Who answers:** Alexandru Cerghezan (răspunsul vine de la client, Aura Logistic SRL)
- **Source:** interviul din 16.11.2026 și factura Aa0060 (INTERVIEW.md, SAMPLES.md)

## Q-007 · Regula de rotunjire
- **Status:** open
- **Context:** Răspunsul a fost „nu rotunjesc TVA-ul”. Pe S1 netul pare calculat din prețul cu TVA (360 / 1.21). Se calculează TVA-ul pe fiecare linie sau pe total, și pornind de la prețul cu TVA sau fără?
- **Blocks:** Calculul TVA și testele de rotunjire.
- **Who answers:** Alexandru Cerghezan (răspunsul vine de la client, Aura Logistic SRL)
- **Source:** interviul din 16.11.2026 și factura Aa0060 (INTERVIEW.md, SAMPLES.md)

## Q-008 · Formatul numărului
- **Status:** open
- **Context:** Ce se întâmplă după factura 99 (`Aa0100`?) și după 9999? Seria `Aa` rămâne aceeași în fiecare an: cum se deosebește `Aa0001` din 2025 de `Aa0001` din 2026?
- **Blocks:** Formatul numărului de factură și resetarea anuală.
- **Who answers:** Alexandru Cerghezan (răspunsul vine de la client, Aura Logistic SRL)
- **Source:** interviul din 16.11.2026 și factura Aa0060 (INTERVIEW.md, SAMPLES.md)

## Q-009 · Cursul BNR
- **Status:** open
- **Context:** Clientul facturează doar în RON, dar a spus că folosește cursul BNR din ziua emiterii. La ce îl folosește?
- **Blocks:** Importul cursului BNR (cum îl folosește clientul).
- **Who answers:** Alexandru Cerghezan (răspunsul vine de la client, Aura Logistic SRL)
- **Source:** interviul din 16.11.2026 și factura Aa0060 (INTERVIEW.md, SAMPLES.md)

## Q-010 · Storno parțial
- **Status:** open
- **Context:** Clientul a făcut doar storno total. Cum ar arăta un storno parțial pentru el (fișa îl cere)?
- **Blocks:** Storno parțial.
- **Who answers:** Alexandru Cerghezan (răspunsul vine de la client, Aura Logistic SRL)
- **Source:** interviul din 16.11.2026 și factura Aa0060 (INTERVIEW.md, SAMPLES.md)

## Q-011 · Ce înseamnă o „bucată”
- **Status:** open
- **Context:** La închiriere / transport, ce reprezintă cantitatea 1 (o cursă, o închiriere de N zile)? Cum arată o linie tipică?
- **Blocks:** Liniile de factură și U.M. implicită.
- **Who answers:** Alexandru Cerghezan (răspunsul vine de la client, Aura Logistic SRL)
- **Source:** interviul din 16.11.2026 și factura Aa0060 (INTERVIEW.md, SAMPLES.md)

## Q-015 · U.M. „1” vs „buc”
- **Status:** open
- **Context:** Pe factura Aa0060 coloana U.M. conține „1”, iar la interviu clientul a spus „bucăți”. E o setare greșită în SmartBill?
- **Blocks:** U.M. implicită.
- **Who answers:** Alexandru Cerghezan (răspunsul vine de la client, Aura Logistic SRL)
- **Source:** interviul din 16.11.2026 și factura Aa0060 (INTERVIEW.md, SAMPLES.md)

## Q-023 · Forma discountului
- **Status:** answered
- **Answer:** Discount ca procent pe linie (Alexandru Cerghezan, 19.11.2026). Decizia: D-013.
- **Context:** Fișa cere „linii cu discount”. Discountul e procent sau valoare? Se aplică pe linie, pe total sau ca linie separată (cum face SmartBill)? Macheta DS-p21-invoice-form îl desenează ca procent pe linie.
- **Blocks:** Coloana „Discount” din formularul de factură, calculul valorii pe linie, XML-ul (AllowanceCharge).
- **Who answers:** Alexandru Cerghezan
- **Source:** macheta DS-p21-invoice-form, 19.11.2026; docs/scope.md

## Q-024 · Seria proformei
- **Status:** answered
- **Answer:** La „Salvează ca proformă” Operatorul alege seria dintr-o listă cu seriile de tip proformă; prima e preselectată (Alexandru Cerghezan, 19.11.2026). Decizia: D-014.
- **Context:** La „Salvează ca proformă”, ce serie primește proforma? Una implicită pe tipul „proformă”, sau Operatorul o alege în formular când există mai multe (D-005)?
- **Blocks:** Butonul „Salvează ca proformă” din formularul de factură.
- **Who answers:** Alexandru Cerghezan
- **Source:** macheta DS-p21-invoice-form, 19.11.2026; D-005

## Q-025 · Nicio serie de proformă
- **Status:** answered
- **Answer:** Butonul „Salvează ca proformă” e dezactivat și lângă el apare mesajul „Nu există nicio serie de proformă. Adaugă una în Serii.”; linkul spre Serii apare doar pentru Admin. Nu se adaugă serie de proformă în seed (Alexandru Cerghezan, 19.11.2026). Decizia: D-017 (mesajul la forma de politețe, D-011).
- **Context:** D-014: la „Salvează ca proformă” Operatorul alege dintr-o listă cu seriile de tip proformă. Ce se întâmplă când Admin-ul nu a configurat nicio serie de proformă (seed-ul are doar `Aa`)? Butonul e dezactivat, apare un mesaj cu link spre serii, sau seed-ul primește și o serie de proformă?
- **Blocks:** Butonul „Salvează ca proformă” fără serii de proformă; seed-ul seriilor.
- **Who answers:** Alexandru Cerghezan
- **Source:** macheta DS-p21-invoice-form, 19.11.2026; D-005, D-014

## Q-026 · Ciorne de factură
- **Status:** answered
- **Answer:** Ciornele nu intră în scope. Butonul „Salvează ciornă” dispare din macheta DS-p21-invoice-form; formularul are doar „Salvează ca proformă” și „Emite factura” (Alexandru Cerghezan, 19.11.2026). Decizia: D-018.
- **Context:** Macheta aprobată DS-p21-invoice-form are butonul „Salvează ciornă”, dar nicio decizie și nici docs/scope.md nu descriu ciornele (factură salvată fără număr, editabilă până la emitere, cu status propriu în listă).
- **Blocks:** Butoanele formularului de factură; statusurile din lista de facturi.
- **Who answers:** Alexandru Cerghezan
- **Source:** macheta DS-p21-invoice-form, 19.11.2026; docs/scope.md

## Q-028 · Seria de tip storno
- **Status:** answered
- **Answer:** Tipul de document „storno” dispare din serii; tipurile rămân factură, proformă și chitanță. Storno-ul ia mereu următorul număr din seria facturii originale (Alexandru Cerghezan, 19.11.2026). Decizia: D-005 (schimbată).
- **Context:** D-005 dă fiecărei serii un tip de document (factură, proformă, storno, chitanță), dar spune și că storno-ul folosește seria facturii, deci o serie de tip storno nu ar fi folosită.
- **Blocks:** Tipurile de document ale seriilor (DS-p21-series); numerotarea storno-ului.
- **Who answers:** Alexandru Cerghezan
- **Source:** D-005; docs/scope.md; INTERVIEW.md

## Q-029 · Lista cotelor de TVA
- **Status:** answered
- **Answer:** Cotele 21%, 11%, scutit (0%), taxare inversă (0%), fiecare cu codul de clasificare TVA pentru XML, stau într-un singur loc: un tabel în baza de date populat printr-o migrare, fără ecran sau setare. O cotă nouă înseamnă o migrare nouă (Alexandru Cerghezan, 19.11.2026; răspunsul inițial „fixă în cod” a fost precizat la aprobare). Decizia: D-020.
- **Context:** Macheta DS-p21-invoice-form oferă pe linie cotele 21%, 11%, Scutit, Taxare inversă, dar nicio decizie nu spunea de unde vine lista și nu există un ecran pentru cote.
- **Blocks:** Câmpul „Cotă TVA” din formularul de factură și de produs; codurile de clasificare TVA din XML.
- **Who answers:** Alexandru Cerghezan
- **Source:** macheta DS-p21-invoice-form, 19.11.2026; D-006

## Q-030 · Linia de factură: catalog sau text liber
- **Status:** answered
- **Answer:** Câmpul liniei caută în catalogul de produse/servicii; la alegere completează denumirea, U.M., prețul și cota, toate modificabile pe linie. Se poate scrie și o denumire care nu e în catalog, cu U.M., preț și cotă completate de mână (Alexandru Cerghezan, 19.11.2026). Decizia: D-021.
- **Context:** Registry-ul spune „linii (produs, …)” și scope-ul are catalog (DS-p21-products), dar macheta DS-p21-invoice-form arată doar o denumire; pe S1 denumirea pare scrisă pentru factură.
- **Blocks:** Câmpul „Produs / serviciu” din formularul de factură.
- **Who answers:** Alexandru Cerghezan
- **Source:** macheta DS-p21-invoice-form, 19.11.2026; docs/design/registry.yaml; SAMPLES.md (S1)

## Q-031 · Ce curs BNR folosește factura
- **Status:** answered
- **Answer:** Factura în EUR/USD folosește ultimul curs comunicat de BNR anterior datei emiterii (în practică, cursul din ziua bancară precedentă), conform art. 290 din Codul fiscal. „Curs de rezervă” și mențiunea apar doar dacă lipsește cursul din ziua bancară precedentă și se folosește unul mai vechi (Alexandru Cerghezan, 19.11.2026; formularea inițială „ultimul curs publicat în momentul emiterii” a fost schimbată la aprobare). Decizia: D-008 (schimbată). Confirmarea contabilului: Q-033.
- **Context:** BNR publică cursul zilei în jurul orei 13:00. D-008 spunea „cursul BNR din ziua emiterii”, iar macheta DS-p21-invoice-form trata cursul din ziua precedentă ca rezervă.
- **Blocks:** Alegerea cursului la emitere; marcajul „curs de rezervă” (D-008, D-015); starea EUR din macheta DS-p21-invoice-form.
- **Who answers:** Alexandru Cerghezan
- **Source:** D-008; macheta DS-p21-invoice-form, 19.11.2026; docs/brief.md

## Q-033 · Contabilul confirmă regula cursului BNR
- **Status:** open
- **Context:** D-008 folosește la facturile în EUR/USD ultimul curs comunicat de BNR anterior datei emiterii (în practică, cursul din ziua bancară precedentă), conform art. 290 din Codul fiscal, iar „curs de rezervă” doar când lipsește și cursul din ziua bancară precedentă. Contabilul confirmă că asta e regula pe care o aplică și că mențiunea pentru cursul de rezervă e potrivită?
- **Blocks:** Confirmarea regulii din D-008 (construcția merge înainte pe D-008 aprobat).
- **Who answers:** contabilul clientului, prin Alexandru Cerghezan (contactul: Q-005)
- **Source:** răspunsul la Q-031, 19.11.2026; D-008
