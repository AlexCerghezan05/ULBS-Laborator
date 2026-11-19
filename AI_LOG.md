# AI_LOG · P21 Facturare + e-Factura

## 2026-11-16 · Pregătirea interviului și anonimizarea facturii S1
Unealta: Claude (chat, claude.ai)
Ce am cerut: întrebări pentru interviul cu clientul pe baza fișei P21, un document Word cu ele, apoi să anonimizeze factura Aa0060 și să scrie INTERVIEW.md, SAMPLES.md, CONTEXT.md și întrebările Q- din notițele mele.
Ce am primit: 40 de întrebări pe 12 teme, documentul Word, factura cu datele furnizorului și ale clientului înlocuite (sumele, seria și datele păstrate), draft-urile fișierelor .md.
Ce am făcut cu ea: am completat interviul cu clientul. Când am cerut să completeze el răspunsuri în locul clientului și să schimbe U.M. din „1” în „buc” pe factură, a refuzat, pentru că ar fi fost date inventate; am păstrat datele reale și am trecut diferențele ca întrebări Q-. Am obținut separat acordul clientului pentru folosirea datelor. Am scos numărul de telefon al clientului din documentele din repo.

## 2026-11-19 · Review la PROPOSAL-urile D-002…D-010
Unealta: Claude (chat, claude.ai)
Ce am cerut: o a doua părere pe propunerile de decizii ale agentului din terminal.
Ce am primit: D-008 („doar RON”) contrazice cerința „must” din fișă și Definition of Done (factură în EUR cu echivalent corect); sugestia că React + TypeScript adaugă un al doilea limbaj de explicat la susținere, cu alternativa Thymeleaf.
Ce am făcut cu ea: am schimbat D-008 în RON/EUR/USD cu facturi de test. Am păstrat React + TypeScript (D-002).

## 2026-11-19 · Interviu de scope și documentarea deciziilor
Unealta: Claude Code (prompt liber)
Ce am cerut: Să salveze fișa P21 în `docs/brief.md`, să citească INTERVIEW.md, SAMPLES.md, CONTEXT.md și OPEN-QUESTIONS.md, apoi să mă intervieveze, câte o întrebare pe rând, despre regimul de TVA, serii, PDF, rotunjire, rapoarte, roluri, date, ecrane, stack și ce nu intră, fără să inventeze nimic despre client. Apoi să scrie ce am stabilit în README.md, CONTEXT.md și OPEN-QUESTIONS.md, să propună deciziile tehnice și să ruleze ledger-lint.
Ce am primit: 13 întrebări cu variante. A semnalat că scoaterea facturilor în EUR/USD încalcă Definition of Done și că trimiterea în SPV cere un certificat calificat. Au rezultat `docs/brief.md`, `docs/scope.md`, `README.md`, termeni noi în CONTEXT.md, întrebările Q-018..Q-021 și Q-001 marcată răspunsă. Deciziile tehnice au venit ca PROPOSAL-uri și au fost scrise în DECISIONS.md abia după aprobarea mea (D-002…D-010).
Ce am făcut cu ea: Am ales variantele, inclusiv stack-ul Java 21 + Spring Boot + PostgreSQL + React. Am aprobat așa cum erau D-002…D-007, D-009 și D-010. Am schimbat D-008: propunerea era „doar RON”, iar eu am cerut facturi în RON, EUR și USD, cum cere fișa. La EUR/USD se folosește cursul BNR din ziua emiterii, cu fallback marcat pe factură și echivalent în RON. Sunt facturi de test, pentru că clientul facturează doar în RON. Am cerut să nu se împartă OPEN-QUESTIONS.md, deși trece de 150 de linii.

## 2026-11-19 · Macheta pentru formularul de factură (DS-p21-invoice-form)
Unealta: Claude Code (prompt liber)
Ce am cerut: macheta formularului de factură/proformă, doar cu tokens din DESIGN.md, în ordinea din PDF-ul SmartBill, cu toate stările (gol, încărcare, eroare, succes, text lung, EUR cu curs de rezervă), light și dark, completată cu S1. Apoi o variantă mai simplă, ca fișier separat, ca să le compar.
Ce am primit: două machete HTML cu toate stările. Prima: casete separate, furnizorul în stânga, clientul în dreapta, toate coloanele în tabel. A doua: o singură coloană, fără casete, furnizorul scos din formular (D-003), clientul ca un câmp de căutare cu CIF-ul și adresa dedesubt, U.M. și discount la „Mai multe” pe fiecare linie, mențiunile legale ca un rând sub totaluri, „Emite factura” singurul buton principal. Agentul a deschis două întrebări noi: forma discountului (Q-023) și seria proformei (Q-024).
Ce am făcut cu ea: am comparat cele două variante în browser și am ales varianta simplă, care a devenit macheta oficială, confirmată. Am răspuns la Q-023 (discount ca procent pe linie) și la Q-024 (seria proformei se alege dintr-o listă cu seriile de tip proformă, prima preselectată); deciziile au venit ca PROPOSAL-uri. Am acceptat mențiunile legale automate și needitabile, termenul de plată egal implicit cu data emiterii și opacitatea pentru butoanele dezactivate.

## 2026-11-19 · Scheletul proiectului (/plan, /build)
Unealta: Claude Code (/plan, apoi /build)
Ce am cerut: prin /plan, scheletul proiectului: backend Java 21 + Spring Boot cu `GET /api/health`, frontend React + TypeScript care afișează răspunsul, PostgreSQL în Docker Compose, Flyway cu prima migrare (tabelul cotelor de TVA din D-020), un test pentru health și un test de integrare pentru migrare, CI pe GitHub Actions cu teste și ledger-lint la fiecare push, `.env.example` fără secrete, plus răspunsul la Q-002 (comenzile).
Ce am primit: trei întrebări înainte de plan (Maven sau Gradle, Postgres-ul pentru teste, cum rulează frontend-ul), apoi un plan cu fișierele, testele, riscurile și lista deciziilor folosite. Propunerea D-023 și o întrebare nouă, Q-034 (motivul de scutire pentru XML). La build: macheta DS-p21-health, testele scrise întâi și văzute picând (404, tabel lipsă), apoi codul până au trecut, Docker Compose, CI, documentele și pattern-ul candidat PATTERN-001.
Ce am făcut cu ea: am ales Maven, testele într-un profil `test` din Compose și frontend-ul servit de nginx. Am aprobat planul, am acceptat D-023 și Q-034 și am păstrat CI-ul `on: push`. Am confirmat macheta DS-p21-health fără butonul „Verifică din nou”. Am pornit aplicația cu `docker compose up -d --build` și am verificat pagina în browser, la http://localhost:8080: arată ca macheta.

## 2026-11-19 · Review la PR #1 cu trei sub-agenți
Unealta: Claude Code (trei sub-agenți în paralel, apoi /build)
Ce am cerut: review la diff-ul PR #1 (`chore/schelet` → `main`), comparat cu planul aprobat și cu `docs/brief.md`, de la trei sub-agenți care doar raportează: Performance, Security, SEO & GEO. Numai problemele de corectitudine sau siguranță, cu fișier și linie.
Ce am primit: 8 probleme minore, niciuna importantă. nginx: DNS-ul backend-ului rezolvat o singură dată; fișierele JS lipsă primeau index.html cu 200; versiunea serverului vizibilă; lipseau headerele de securitate. Aplicația se putea indexa. Backend-ul rula ca root. Porturile erau deschise pe toate interfețele. CI-ul nu avea `permissions:`, iar acțiunile nu erau fixate pe SHA.
Ce am făcut cu ea: am ales să le repar pe toate acum, pe `chore/schelet`. Am aprobat D-024 „Securitate de bază” pentru regulile noi.
