# Decisions — ULBS-Laborator

<!-- graffino-agentic:begin v1 -->
> Constraints the code must obey. Status: `approved` (obey) · `proposed` (do not
> build on it) · `superseded-by <ID>` (kept to explain old code).
> Adding or changing an entry needs the user's OK (AGENTS.md → The gate).
<!-- graffino-agentic:end -->

## Index
### [Design](decisions/design.md)
- D-011 · Design tokens și stil UI din DESIGN.md

### [E factura](decisions/e-factura.md)
- D-009 · e-Factura: doar export XML, fără trimitere în SPV
- D-010 · Doar clienți din România
- D-019 · Persoana fizică se identifică prin CNP, opțional
- D-022 · Email opțional la client; fără email, fără reminder

### [Facturare](decisions/facturare.md)
- D-005 · Serii multiple configurabile, numerotare fără goluri
- D-006 · Toate regimurile de TVA, configurabile; facturi de test separate
- D-007 · Aritmetică decimală, rotunjire în trei variante configurabile
- D-008 · Facturi în RON, EUR și USD cu ultimul curs BNR comunicat înaintea datei emiterii
- D-013 · Discount ca procent pe linie
- D-014 · Seria proformei aleasă la salvare
- D-015 · Mențiunile legale sunt automate și needitabile
- D-016 · Termenul de plată implicit egal cu data emiterii
- D-017 · Fără serie de proformă, butonul de proformă e dezactivat
- D-018 · Fără ciorne de factură
- D-020 · Cotele de TVA într-un tabel populat prin migrare
- D-021 · Linia de factură din catalog, cu text liber permis

### [Platforma](decisions/platforma.md)
- D-001 · Servers and databases run in Docker
- D-002 · Stack: Java 21 + Spring Boot, PostgreSQL, React + TypeScript
- D-003 · O singură firmă furnizor pe instalare
- D-012 · Roluri: Admin, Operator, Contabil, cu drepturile pe ecrane

