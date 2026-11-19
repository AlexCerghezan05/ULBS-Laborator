# Open questions · Date de la client: interviu, contacte, facturi reale, XML

> Topic file. The index is in `../OPEN-QUESTIONS.md`.

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

## Q-013 · XML-urile facturilor
- **Status:** open
- **Context:** Nu le poate descărca din SPV. Le poate exporta din SmartBill?
- **Blocks:** Dovada XML valid pentru facturile din SAMPLES.md.
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

## Q-027 · Clientul persoană fizică
- **Status:** answered
- **Answer:** Persoanele fizice rămân clienți (D-010). CNP-ul e opțional; fără el, XML-ul primește 13 zerouri (`0000000000000`). CONTEXT.md primește termenul „Persoană fizică”, distinct de PFA (Alexandru Cerghezan, 19.11.2026). Decizia: D-019.
- **Context:** D-010 și docs/scope.md includ persoanele fizice, dar CONTEXT.md definea Clientul ca „persoană juridică sau PFA, cu CIF” și punea „persoană fizică” pe lista de evitat. RO_CIUS cere un identificator pentru persoana fizică.
- **Blocks:** Formularul de client (DS-p21-clients); identificatorul cumpărătorului în XML.
- **Who answers:** Alexandru Cerghezan
- **Source:** CONTEXT.md; D-010; docs/scope.md
