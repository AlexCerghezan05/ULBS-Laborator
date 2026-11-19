# Open questions · Încasări: tranșe, chitanțe, reminder-e

> Topic file. The index is in `../OPEN-QUESTIONS.md`.

## Q-012 · Încasări în tranșe
- **Status:** open
- **Context:** Cum înregistrează în SmartBill o factură plătită în mai multe tranșe? Emite câte o chitanță pentru fiecare tranșă în numerar?
- **Blocks:** Încasările parțiale și chitanțele.
- **Who answers:** Alexandru Cerghezan (răspunsul vine de la client, Aura Logistic SRL)
- **Source:** interviul din 16.11.2026 și factura Aa0060 (INTERVIEW.md, SAMPLES.md)

## Q-016 · Chitanțele în aplicație
- **Status:** answered
- **Answer:** Chitanțele intră în scope, ca în D-005 (tipul de serie „chitanță”) și D-012 (Operatorul emite chitanțe), pentru încasările în numerar. Câte o chitanță pe fiecare tranșă rămâne la Q-012 (Alexandru Cerghezan, 19.11.2026).
- **Context:** Chitanțele au numerotare proprie (`Aa 0021`). Vrea ca aplicația să emită și chitanțe? (Nu e cerință din fișă.)
- **Blocks:** Emiterea de chitanțe în aplicație.
- **Who answers:** Alexandru Cerghezan (răspunsul vine de la client, Aura Logistic SRL)
- **Source:** interviul din 16.11.2026 și factura Aa0060 (INTERVIEW.md, SAMPLES.md)

## Q-019 · Reminder-e de plată
- **Status:** open
- **Context:** Fișa cere reminder-e de plată pe email și intră în scope, dar clientul nu trimite reminder-e și n-a dat exemplu de email. Când se trimit (câte zile după scadență, de câte ori), către ce adresă, cu ce text, și prin ce server de email?
- **Blocks:** Valorile implicite ale reminder-elor: după câte zile de la scadență, la câte zile se repetă, textul șablonului, adresa destinatarului, serverul de email. Ecranul și funcția intră în scope (DS-p21-reminders, docs/scope.md).
- **Who answers:** Alexandru Cerghezan (cu clientul, Aura Logistic SRL)
- **Source:** interviul cu Alexandru Cerghezan, 19.11.2026; INTERVIEW.md

## Q-032 · Emailul clientului pentru reminder-e
- **Status:** answered
- **Answer:** Clientul are un câmp email, opțional. Fără email, reminder-ul automat nu se trimite pentru facturile acelui client, iar „Trimite reminder” e dezactivat, cu un mesaj care explică de ce (Alexandru Cerghezan, 19.11.2026). Decizia: D-022. Textul mesajului se stabilește la macheta DS-p21-invoice-view.
- **Context:** Reminder-ele de plată se trimit pe email (DS-p21-reminders), dar datele de client din docs/scope.md nu aveau email.
- **Blocks:** Formularul de client (DS-p21-clients); trimiterea reminder-elor; butonul „Trimite reminder” (DS-p21-invoice-view).
- **Who answers:** Alexandru Cerghezan
- **Source:** docs/scope.md; docs/design/registry.yaml; Q-019
