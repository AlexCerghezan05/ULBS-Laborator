# Open questions · Proiect: stack, comenzi, livrare, deploy, screenshot

> Topic file. The index is in `../OPEN-QUESTIONS.md`.

## Q-001 · What will this repo hold, and in which stack?
- **Status:** answered
- **Answer:** Proiectul P21 din catalogul Web Avansat (docs/brief.md): facturare + export e-Factura (UBL 2.1, RO_CIUS) pentru clientul real Aura Logistic SRL. Stack: Java 21 + Spring Boot, PostgreSQL, Flyway, OpenAPI, React + TypeScript (Alexandru Cerghezan, 19.11.2026; D-002).
- **Blocks:** AGENTS.md sections 1–3, CONTEXT.md terms, ARCHITECTURE.md.
- **Who answers:** Alexandru Cerghezan

## Q-002 · Which build, run and test commands does the project use?
- **Status:** answered
- **Answer:** `cp .env.example .env` (o dată, cu parola completată); rulare: `docker compose up --build` (frontend http://localhost:8080, backend http://localhost:8081); build: `docker compose build`; teste: `docker compose --profile test run --rm backend-test` (`mvn -B verify` pe `db-test`); frontend-ul nu are încă teste (Alexandru Cerghezan, 19.11.2026, planul „Scheletul proiectului”; D-023).
- **Blocks:** AGENTS.md sections 2 and 6 (definition of done).
- **Who answers:** Alexandru Cerghezan (after Q-001)

## Q-003 · How is work released or handed in?
- **Status:** open
- **Blocks:** AGENTS.md section 5 (branches, tags, delivery).
- **Who answers:** Alexandru Cerghezan
- **Confirmed:** 2026-11-19

## Q-021 · Unde rulează deploy-ul live HTTPS
- **Status:** open
- **Context:** Cerințele comune cer deploy live cu HTTPS. Nu s-a stabilit pe ce server sau serviciu, cu ce domeniu și cum se obține certificatul HTTPS.
- **Blocks:** Deploy-ul live; AGENTS.md §5.
- **Who answers:** Alexandru Cerghezan
- **Source:** docs/brief.md (cerințe comune); interviul cu Alexandru Cerghezan, 19.11.2026
- **Confirmed:** 2026-11-19

## Q-022 · Cum facem screenshot-urile aplicației
- **Status:** open
- **Context:** Procedura de design cere screenshot-uri light și dark pentru fiecare ecran schimbat (DESIGN.md → Procedure). Încă nu există cod de UI și nici o comandă de screenshot. Pe host nu se instalează unelte native (D-001), deci comanda va rula probabil într-un container (de ex. Playwright în Docker), dar nu e stabilită.
- **Blocks:** AGENTS.md §2 (comanda de screenshot) și §6 (Definition of Done pentru UI).
- **Who answers:** Alexandru Cerghezan
- **Source:** setup-ul de design, 19.11.2026
- **Confirmed:** 2026-11-19
