# Patterns — ULBS-Laborator

<!-- graffino-agentic:begin v1 -->
> How we build things here. Copy only `approved` entries. `candidate` = seen once or
> not yet confirmed; agents add these freely. Promotion needs the user's OK.
<!-- graffino-agentic:end -->

## Index
### [Api](patterns/api.md)
- PATTERN-004 · Fiecare rută își declară accesul cu @PreAuthorize, verificat de un test · candidate
- PATTERN-005 · Erorile API au forma {"error":"COD"}; textul îl pune frontend-ul · candidate
- PATTERN-006 · Contor pe un rând: citire FOR UPDATE și actualizare în aceeași tranzacție · candidate

### [Teste](patterns/teste.md)
- PATTERN-001 · Testele de integrare se numesc *IT și rulează pe db-test · approved
- PATTERN-002 · Regulile de integritate stau în baza de date și se testează direct cu SQL · candidate
- PATTERN-003 · Tranzacția de emitere: documentul inserat la final, fără I/O, fără savepoint-uri · candidate

---
