# Decisions · Design: tokens și stil UI

> Topic file. The index is in `../DECISIONS.md`.

## D-011 · Design tokens și stil UI din DESIGN.md
- **Status:** approved
- **Date:** 2026-11-19
- **Rule:** UI-ul folosește doar tokens-urile din `docs/design/DESIGN.md` → Tokens: accent albastru-ardezie `#3E6A87` (dark `#7FA9C7`) pe bază neutră gri, font Inter cu cifre tabulare (PDF-ul în Helvetica/Arial), stil sobru și compact (14 px în tabele), spațiere pe pași de 4 px, colțuri de 4–6 px, borduri subțiri, umbră doar la meniuri și dialoguri, fără gradienți și fără animații decorative. Întâi pentru calculator, utilizabil pe telefon de la 360 px.
- **Why:** Clientul nu are logo sau alt material de brand; accentul vine din PDF-ul SmartBill pe care îl folosește deja (S1), ca aplicația și factura să arate unitar. Utilizatorii lucrează zilnic cu liste dense de facturi.
- **Rejected:** Altă culoare de accent; UI complet neutru; fonturile sistemului; un font ales separat; stil aerisit cu text de 15–16 px; ton prietenos cu „tu”; font monospace pentru coduri.
- **Evidence:** interviul de stil, 19.11.2026 (DESIGN.md → Style interview); `samples/factura_Aa0060_anonimizata.pdf`; contrast verificat pentru fiecare pereche de culori, în ambele teme.
- **Source:** user · confidence high
- **Confirmed:** 2026-11-19
