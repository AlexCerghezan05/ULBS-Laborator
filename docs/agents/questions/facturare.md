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
- **Status:** open
- **Context:** D-014: la „Salvează ca proformă” Operatorul alege dintr-o listă cu seriile de tip proformă. Ce se întâmplă când Admin-ul nu a configurat nicio serie de proformă (seed-ul are doar `Aa`)? Butonul e dezactivat, apare un mesaj cu link spre serii, sau seed-ul primește și o serie de proformă?
- **Blocks:** Butonul „Salvează ca proformă” fără serii de proformă; seed-ul seriilor.
- **Who answers:** Alexandru Cerghezan
- **Source:** macheta DS-p21-invoice-form, 19.11.2026; D-005, D-014
