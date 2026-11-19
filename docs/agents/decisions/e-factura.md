# Decisions · e-Factura și clienți: export XML, clienți din România

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
