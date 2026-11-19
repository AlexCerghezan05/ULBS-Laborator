# Decisions · e-Factura și clienți: export XML, clienți din România, identificare, email

> Topic file. The index is in `../DECISIONS.md`.

## D-009 · e-Factura: doar export XML, fără trimitere în SPV
- **Status:** approved
- **Date:** 2026-11-19
- **Rule:** e-Factura înseamnă export XML UBL 2.1 conform RO_CIUS, per factură și în lot, generat cu o bibliotecă XML (nu prin concatenare de stringuri). Aplicația nu trimite direct în SPV.
- **Why:** Fișa cere doar exportul; trimiterea în SPV ar cere un certificat digital calificat.
- **Rejected:** Trimitere directă în SPV prin API-ul ANAF.
- **Evidence:** interviul de scope, 19.11.2026 (întrebarea 13); docs/brief.md.
- **Source:** user · confidence high
- **Confirmed:** 2026-11-19

## D-010 · Doar clienți din România
- **Status:** approved
- **Date:** 2026-11-19
- **Rule:** Clienții sunt din România: persoane juridice, PFA și persoane fizice. Codul de țară este mereu RO.
- **Why:** Clientul real nu are clienți din străinătate.
- **Rejected:** Clienți din alte țări.
- **Evidence:** interviul de scope, 19.11.2026 (întrebarea 11); INTERVIEW.md.
- **Source:** user · confidence high
- **Confirmed:** 2026-11-19

## D-019 · Persoana fizică se identifică prin CNP, opțional
- **Status:** approved
- **Date:** 2026-11-19
- **Rule:** Clientul persoană fizică (D-010) se identifică prin CNP. CNP-ul e opțional; fără el, identificatorul din XML-ul e-Factura este `0000000000000` (13 zerouri). Persoana fizică e un tip de client distinct de PFA (CONTEXT.md).
- **Why:** RO_CIUS cere un identificator pentru cumpărător; CONTEXT.md contrazicea D-010 (Q-027).
- **Rejected:** CNP obligatoriu; scoaterea persoanelor fizice din scope.
- **Evidence:** răspunsul la Q-027, 19.11.2026; CONTEXT.md; D-010.
- **Source:** user · confidence high
- **Confirmed:** 2026-11-19

## D-022 · Email opțional la client; fără email, fără reminder
- **Status:** approved
- **Date:** 2026-11-19
- **Rule:** Clientul are un câmp email, opțional. Fără email, reminder-ul automat nu se trimite pentru facturile acelui client, iar „Trimite reminder” e dezactivat, cu un mesaj care explică de ce. Textul mesajului se stabilește la macheta DS-p21-invoice-view.
- **Why:** Reminder-ele de plată se trimit pe email, dar datele clientului nu aveau email (Q-032).
- **Rejected:** Email obligatoriu; mai multe adrese de email pe client.
- **Evidence:** răspunsul la Q-032, 19.11.2026; docs/scope.md; Q-019.
- **Source:** user · confidence high
- **Confirmed:** 2026-11-19
