# Decisions superseded — ULBS-Laborator

<!-- graffino-agentic:begin v1 -->
> Decisions that no longer apply. **Never obeyed**; read only to explain old code or a
> past choice. Each points to the entry that replaced it. `ledger-lint.py --fix`
> keeps this file in sync: superseded entries move here from `DECISIONS.md`, and
> their index lines move here from its index.
<!-- graffino-agentic:end -->

## Index
- D-004 · Roluri: Admin, Operator, Contabil · _superseded-by D-012_

### [Design](decisions/design.md)
- D-011 · Design tokens și stil UI din DESIGN.md · _superseded-by D-025_

### [Platforma](decisions/platforma.md)
- D-023 · Build și rulare: Maven, testele în profilul `test` din Compose, frontend în nginx · _superseded-by D-040_

---

## D-004 · Roluri: Admin, Operator, Contabil
- **Status:** superseded-by D-012
- **Date:** 2026-11-19
- **Rule:** Trei roluri, verificate pe server: Admin (tot: setări firmă, serii, utilizatori și tot ce poate Operatorul), Operator (facturi, proforme, storno, încasări, chitanțe, export XML, reminder-e), Contabil (doar citire, rapoarte, export). Clientul are cont de Admin.
- **Why:** Fișa cere emitent / contabil / admin; clientul vrea acces la toate operațiunile admin-ului; „emitent” este termen de evitat în CONTEXT.md, deci rolul se numește Operator.
- **Rejected:** Păstrarea numelui „emitent” pentru rol.
- **Evidence:** interviul de scope, 19.11.2026 (întrebarea 6); INTERVIEW.md; CONTEXT.md.
- **Source:** user · confidence high
