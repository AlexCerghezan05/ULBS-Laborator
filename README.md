# ULBS-Laborator · P21 Facturare + e-Factura

Aplicație de facturare cu export e-Factura (UBL 2.1, RO_CIUS) pentru un client real,
o firmă mică din Sibiu (Aura Logistic SRL, transport alternativ de persoane).
Proiectul P21 din catalogul Web Avansat. Fișa completă: [`docs/brief.md`](docs/brief.md).

> Stadiu (25.11.2026): scope și stack aprobate (deciziile în
> [`docs/agents/DECISIONS.md`](docs/agents/DECISIONS.md)). Există scheletul (`GET /api/health`,
> Docker Compose, CI), schema bazei de date (migrările Flyway V1–V8), autentificarea cu roluri
> (C-001) și documentația API OpenAPI la `/api/docs/ui`, doar pentru Admin (D-043).

## Documente
| Ce | Unde |
|---|---|
| Fișa proiectului | [`docs/brief.md`](docs/brief.md) |
| Scope-ul detaliat | [`docs/scope.md`](docs/scope.md) |
| Interviul cu clientul | [`INTERVIEW.md`](INTERVIEW.md) |
| Facturile reale (anonimizate) | [`SAMPLES.md`](SAMPLES.md) |
| Vocabular | [`CONTEXT.md`](CONTEXT.md) |
| Întrebări deschise | [`docs/agents/OPEN-QUESTIONS.md`](docs/agents/OPEN-QUESTIONS.md) |
| Jurnal de lucru cu AI | [`AI_LOG.md`](AI_LOG.md) |
| Documentația API (OpenAPI) | cu aplicația pornită și login ca Admin: Swagger UI la http://localhost:8090/api/docs/ui, specificația JSON la http://localhost:8090/api/docs (D-043) |

## Scope

### Intră
- **O singură firmă furnizor** pe instalare; datele ei le editează Admin-ul.
- **Roluri** (auth pe server): Admin (tot), Operator (facturare, încasări, export), Contabil (doar citire, rapoarte, export).
- **Clienți** din România: persoane juridice, PFA, persoane fizice.
- **Serii multiple**, configurabile (tip document + format număr); seed: `Aa`. Număr secvențial fără goluri sau duplicate sub emiteri simultane; proforma nu consumă număr din seria de facturi.
- **Toate regimurile de TVA** din fișă, configurabile: plătitor/neplătitor, TVA la încasare, taxare inversă, scutire, cote multiple pe aceeași factură. Cazurile pe care clientul nu le folosește se testează cu facturi de test, separat de facturile reale.
- Facturi cu discount pe linie, proformă → factură, factură emisă imutabilă, **storno total și parțial** legat de originală.
- **Încasări** parțiale și multiple, sold client, facturi restante, chitanțe.
- **Reminder-e de plată pe email**: Admin-ul configurează șablonul și regula de trimitere (după câte zile de la scadență, la câte zile se repetă); istoric al reminder-elor trimise; buton „Trimite reminder” pe facturile restante. Valorile implicite: după Q-019.
- **Ecrane**: 15, cu ID-uri DS- și roluri, în [`docs/design/registry.yaml`](docs/design/registry.yaml). Machetă aprobată: DS-p21-invoice-form.
- **Facturi în RON, EUR și USD**: la EUR/USD, ultimul curs comunicat de BNR anterior datei emiterii (import zilnic automat; un curs mai vechi decât ziua bancară precedentă e marcat pe factură ca „curs de rezervă”) și echivalentul în RON. Clientul facturează doar în RON, deci facturile în EUR/USD sunt facturi de test (D-008).
- **PDF** după modelul SmartBill al clientului.
- **Export e-Factura** XML UBL 2.1 RO_CIUS, per factură și în lot, validat cu schematron în CI.
- **Rapoarte**: jurnal de vânzări, TVA colectat, raport de încasări; export CSV și XLSX.

### Nu intră
| Ce | De ce |
|---|---|
| Clienți din străinătate | Clientul nu are. |
| Trimiterea directă în SPV | Fișa cere doar export XML; ar cere certificat digital calificat. Rămâne sugestie. |

## Definition of Done (ajustat 25.11.2026)
Față de fișă, un singur punct se schimbă: clientul a dat o singură factură reală (S1), iar
profesorul a confirmat pe 25.11.2026 că e suficientă (Q-018).
- **Factura reală S1 produce XML valid** (raportul validatorului în repo), în locul celor 5 facturi reale.
- Storno-ul, EUR, cotele multiple și persoana fizică se acoperă cu **facturi de test**, marcate clar ca test ([`SAMPLES.md`](SAMPLES.md) → Facturi de test).
- Restul punctelor rămân ca în [`docs/brief.md`](docs/brief.md).

## Stack (D-002)
| Strat | Alegere |
|---|---|
| Backend | Java 21 + Spring Boot |
| Bază de date | PostgreSQL, migrări cu Flyway |
| API | REST, documentat cu OpenAPI |
| Frontend | React + TypeScript |
| Validare RO_CIUS | schematron în Java (ph-schematron sau Saxon), în CI |
| Rulare | Docker / Docker Compose (D-001) |
| CI | GitHub Actions: teste + ledger-lint |

## Variabilele mele
| Variabilă | Valoare | Sursă |
|---|---|---|
| Clientul real | Aura Logistic SRL, Sibiu; plătitor de TVA, fără TVA la încasare | INTERVIEW.md |
| Regim TVA | o singură cotă, 21%; fără taxare inversă, fără scutire | INTERVIEW.md |
| Serii | una singură, `Aa`; număr din 4 cifre (`Aa0060`), resetat anual | INTERVIEW.md; format după 9999: Q-008 |
| Model PDF | PDF-ul SmartBill: aceleași câmpuri, aceeași ordine și zone, fără logo | INTERVIEW.md, S1 |
| Regula de rotunjire | **deschisă** (Q-007). Aplicația oferă trei variante configurabile: pe linie, pe total, pornind de la prețul cu TVA | SAMPLES.md (S1) |
| Rapoartele lunare | jurnal de vânzări și TVA colectat (formular legal standard, Q-020); raport de încasări (coloane: Q-014) | INTERVIEW.md |

## Comenzi
```bash
# Ledger lint (Python rulează în Docker):
MSYS_NO_PATHCONV=1 docker run --rm -v "$(pwd -W):/repo" -w /repo python:3.12-slim python3 docs/agents/tools/ledger-lint.py --fix
# O dată: copiați fișierul de mediu și completați parola
cp .env.example .env
# Rulare (frontend http://localhost:8090, backend http://localhost:8081):
docker compose up --build
# Build:
docker compose build
# Teste (mvn -B verify pe Postgres-ul db-test, D-023):
docker compose --profile test run --rm backend-test
```
CI (GitHub Actions, `.github/workflows/ci.yml`) rulează testele și ledger-lint la fiecare push.
