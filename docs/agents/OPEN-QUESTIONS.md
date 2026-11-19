# Open questions — ULBS-Laborator

<!-- graffino-agentic:begin v1 -->
> Unknowns. Work that depends on an open question is out of scope until it is
> answered. Only the user answers; an agent never answers by guessing. An agent may
> write a `Proposed answer` the evidence supports; the question stays
> `investigating` until the user confirms it.
> Answered entries stay, with the answer and date, and usually become a D- or PATTERN-.
<!-- graffino-agentic:end -->

## Index
- Q-001 · What will this repo hold, and in which stack? · _answered_
- Q-002 · Which build, run and test commands does the project use?
- Q-003 · How is work released or handed in?
- Q-004 · Cine a răspuns la interviu · _answered_
- Q-005 · Contactul contabilului
- Q-006 · Proforme
- Q-007 · Regula de rotunjire
- Q-008 · Formatul numărului
- Q-009 · Cursul BNR
- Q-010 · Storno parțial
- Q-011 · Ce înseamnă o „bucată”
- Q-012 · Încasări în tranșe
- Q-013 · XML-urile facturilor
- Q-014 · Raportul de încasări
- Q-015 · U.M. „1” vs „buc”
- Q-016 · Chitanțele în aplicație
- Q-017 · Numele firmei în repo-ul public
- Q-018 · Facturile reale S2–S5
- Q-019 · Reminder-e de plată
- Q-020 · Coloanele jurnalului de vânzări și ale raportului de TVA colectat
- Q-021 · Unde rulează deploy-ul live HTTPS
- Q-022 · Cum facem screenshot-urile aplicației

---

## Q-001 · What will this repo hold, and in which stack?
- **Status:** answered
- **Answer:** Proiectul P21 din catalogul Web Avansat (docs/brief.md): facturare + export e-Factura (UBL 2.1, RO_CIUS) pentru clientul real Aura Logistic SRL. Stack: Java 21 + Spring Boot, PostgreSQL, Flyway, OpenAPI, React + TypeScript (Alexandru Cerghezan, 19.11.2026; D-002).
- **Blocks:** AGENTS.md sections 1–3, CONTEXT.md terms, ARCHITECTURE.md.
- **Who answers:** Alexandru Cerghezan

## Q-002 · Which build, run and test commands does the project use?
- **Status:** open
- **Blocks:** AGENTS.md sections 2 and 6 (definition of done).
- **Who answers:** Alexandru Cerghezan (after Q-001)

## Q-003 · How is work released or handed in?
- **Status:** open
- **Blocks:** AGENTS.md section 5 (branches, tags, delivery).
- **Who answers:** Alexandru Cerghezan

## Q-004 · Cine a răspuns la interviu
- **Status:** answered
- **Answer:** Oprescu Gabriel, Administrator al Aura Logistic SRL (16.11.2026).
- **Context:** Numele și rolul persoanei din Aura Logistic SRL care a răspuns (administrator?). Necesar pentru INTERVIEW.md.
- **Blocks:** INTERVIEW.md (persoana intervievată).
- **Who answers:** Alexandru Cerghezan (răspunsul vine de la client, Aura Logistic SRL)
- **Source:** interviul din 16.11.2026 și factura Aa0060 (INTERVIEW.md, SAMPLES.md)

## Q-005 · Contactul contabilului
- **Status:** open
- **Context:** Contabilul nu a participat. Cum se numește și îl putem contacta direct? Definition of Done cere feedback-ul lui pe PDF și pe rapoarte.
- **Blocks:** Definition of Done: feedback-ul contabilului pe PDF și rapoarte.
- **Who answers:** Alexandru Cerghezan (răspunsul vine de la client, Aura Logistic SRL)
- **Source:** interviul din 16.11.2026 și factura Aa0060 (INTERVIEW.md, SAMPLES.md)

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

## Q-012 · Încasări în tranșe
- **Status:** open
- **Context:** Cum înregistrează în SmartBill o factură plătită în mai multe tranșe? Emite câte o chitanță pentru fiecare tranșă în numerar?
- **Blocks:** Încasările parțiale și chitanțele.
- **Who answers:** Alexandru Cerghezan (răspunsul vine de la client, Aura Logistic SRL)
- **Source:** interviul din 16.11.2026 și factura Aa0060 (INTERVIEW.md, SAMPLES.md)

## Q-013 · XML-urile facturilor
- **Status:** open
- **Context:** Nu le poate descărca din SPV. Le poate exporta din SmartBill?
- **Blocks:** Dovada XML valid pentru facturile din SAMPLES.md.
- **Who answers:** Alexandru Cerghezan (răspunsul vine de la client, Aura Logistic SRL)
- **Source:** interviul din 16.11.2026 și factura Aa0060 (INTERVIEW.md, SAMPLES.md)

## Q-014 · Raportul de încasări
- **Status:** open
- **Context:** Ce coloane, pe ce perioadă (lunar?) și în ce format vrea raportul de încasări?
- **Blocks:** Raportul de încasări.
- **Who answers:** Alexandru Cerghezan (răspunsul vine de la client, Aura Logistic SRL)
- **Source:** interviul din 16.11.2026 și factura Aa0060 (INTERVIEW.md, SAMPLES.md)

## Q-015 · U.M. „1” vs „buc”
- **Status:** open
- **Context:** Pe factura Aa0060 coloana U.M. conține „1”, iar la interviu clientul a spus „bucăți”. E o setare greșită în SmartBill?
- **Blocks:** U.M. implicită.
- **Who answers:** Alexandru Cerghezan (răspunsul vine de la client, Aura Logistic SRL)
- **Source:** interviul din 16.11.2026 și factura Aa0060 (INTERVIEW.md, SAMPLES.md)

## Q-016 · Chitanțele în aplicație
- **Status:** open
- **Context:** Chitanțele au numerotare proprie (`Aa 0021`). Vrea ca aplicația să emită și chitanțe? (Nu e cerință din fișă.)
- **Blocks:** Emiterea de chitanțe în aplicație.
- **Who answers:** Alexandru Cerghezan (răspunsul vine de la client, Aura Logistic SRL)
- **Source:** interviul din 16.11.2026 și factura Aa0060 (INTERVIEW.md, SAMPLES.md)

## Q-017 · Numele firmei în repo-ul public
- **Status:** open
- **Context:** E de acord ca „Aura Logistic SRL” să apară în INTERVIEW.md, într-un repo public pe GitHub?
- **Blocks:** Numele clientului în INTERVIEW.md (repo public).
- **Who answers:** Alexandru Cerghezan (răspunsul vine de la client, Aura Logistic SRL)
- **Source:** interviul din 16.11.2026 și factura Aa0060 (INTERVIEW.md, SAMPLES.md)

## Q-018 · Facturile reale S2–S5
- **Status:** open
- **Context:** Fișa cere minim 5 facturi reale anonimizate; în SAMPLES.md este doar S1 (Aa0060). Ideal S2–S5 acoperă un storno, o factură cu mai multe linii, una plătită în tranșe și una din alt an. Clientul a refuzat să dea un storno ca exemplu.
- **Blocks:** Definition of Done: „cele 5 facturi reale produc XML valid”; testele de rotunjire pe facturi reale (Q-007).
- **Who answers:** Alexandru Cerghezan (facturile vin de la client, Aura Logistic SRL)
- **Source:** interviul cu Alexandru Cerghezan, 19.11.2026; SAMPLES.md

## Q-019 · Reminder-e de plată
- **Status:** open
- **Context:** Fișa cere reminder-e de plată pe email și intră în scope, dar clientul nu trimite reminder-e și n-a dat exemplu de email. Când se trimit (câte zile după scadență, de câte ori), către ce adresă, cu ce text, și prin ce server de email?
- **Blocks:** Reminder-ele de plată.
- **Who answers:** Alexandru Cerghezan (cu clientul, Aura Logistic SRL)
- **Source:** interviul cu Alexandru Cerghezan, 19.11.2026; INTERVIEW.md

## Q-020 · Coloanele jurnalului de vânzări și ale raportului de TVA colectat
- **Status:** open
- **Context:** Coloanele jurnalului de vânzări nu s-au discutat cu clientul, iar contabilul nu a fost contactat (Q-005). S-a ales formularul legal standard, dar coloanele exacte și actul normativ după care se fac nu sunt încă verificate. Ce coloane și ce totaluri are fiecare raport?
- **Blocks:** Rapoartele „jurnal de vânzări” și „TVA colectat”.
- **Who answers:** Alexandru Cerghezan (din legislație; ideal confirmat de contabil)
- **Source:** interviul cu Alexandru Cerghezan, 19.11.2026; INTERVIEW.md

## Q-021 · Unde rulează deploy-ul live HTTPS
- **Status:** open
- **Context:** Cerințele comune cer deploy live cu HTTPS. Nu s-a stabilit pe ce server sau serviciu, cu ce domeniu și cum se obține certificatul HTTPS.
- **Blocks:** Deploy-ul live; AGENTS.md §5.
- **Who answers:** Alexandru Cerghezan
- **Source:** docs/brief.md (cerințe comune); interviul cu Alexandru Cerghezan, 19.11.2026

## Q-022 · Cum facem screenshot-urile aplicației
- **Status:** open
- **Context:** Procedura de design cere screenshot-uri light și dark pentru fiecare ecran schimbat (DESIGN.md → Procedure). Încă nu există cod de UI și nici o comandă de screenshot. Pe host nu se instalează unelte native (D-001), deci comanda va rula probabil într-un container (de ex. Playwright în Docker), dar nu e stabilită.
- **Blocks:** AGENTS.md §2 (comanda de screenshot) și §6 (Definition of Done pentru UI).
- **Who answers:** Alexandru Cerghezan
- **Source:** setup-ul de design, 19.11.2026
