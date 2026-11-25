# SAMPLES · Facturi reale (anonimizate)

Fișa cere minim 5 facturi reale. Clientul a refuzat să dea alte facturi în afară de S1, iar
profesorul a confirmat pe 25.11.2026 că o singură factură reală e suficientă (Q-018).
**S1 este singura factură reală**: trebuie reprodusă în aplicație și trebuie să producă un
XML RO_CIUS valid.

## Cum am anonimizat
Am înlocuit doar datele care identifică persoane și firme:
- furnizorul: denumire, CIF, Reg. com., adresă → „FURNIZOR EXEMPLU S.R.L.”, CIF RO12345674
- clientul: denumire, CIF, Reg. com., adresă → „CLIENT EXEMPLU PFA”, CIF 76543210
- persoana care a întocmit factura, indexul de încărcare SPV, codul documentului

CIF-urile fictive au cifra de control validă (algoritmul cu cheia 753217532), ca să treacă validarea.

**Păstrate exact ca în original:** seria și numărul, datele, cantitățile, prețurile, cota de TVA, totalurile, descrierea, U.M., mențiunile legale.
Originalele nu sunt în repo.

## Lista facturilor

| # | Factură | Data | Tip | Linii | Cote TVA | Monedă | Total | Fișier | XML valid |
|---|---|---|---|---|---|---|---|---|---|
| S1 | Aa0060 | 10.11.2026 | Factură + chitanță (numerar) | 1 | 21% | RON | 360.00 | `samples/factura_Aa0060_anonimizata.pdf` | ☐ |
S2–S5 nu există: clientul nu a dat alte facturi (Q-018, 25.11.2026).

## Facturi de test
Cazurile pe care S1 nu le acoperă se testează cu **facturi de test construite de noi**,
marcate clar ca test și ținute separat de factura reală (D-006, D-008): storno, factură
în EUR, cote de TVA multiple pe aceeași factură, client persoană fizică și proformă
(clientul nu folosește proforme, Q-006). Nu sunt facturi reale și nu apar în tabelul de mai sus.

## S1 · Aa0060

| Câmp | Valoare |
|---|---|
| Client | PFA (CIF fără RO, Reg. com. cu F) |
| Linie | „Inchiriere masina 7 zile”, U.M. „1”, cant. 1 |
| Preț unitar fără TVA | 297.52 |
| Valoare | 297.52 |
| TVA 21% | 62.48 |
| Total plată | 360.00 RON |
| Data emiterii = termen de plată | 10.11.2026 |
| Încărcat în SPV | 11.11.2026 |
| Încasare | Chitanță seria Aa nr. 0021, 10.11.2026, 360.00 RON |
| Mențiune legală | „Factura este valabila fara semnatura si stampila, conform art. 319 alin. 29 din legea 227/2015.” |

### Ce am observat
1. **Rotunjirea pornește de la prețul cu TVA.** 360.00 / 1.21 = 297.5206 → 297.52. TVA = 360.00 − 297.52 = 62.48. Dacă TVA-ul s-ar fi calculat din net, 297.52 × 0.21 = 62.4792 → 62.48, deci pe factura asta ambele metode dau același rezultat. Trebuie verificat pe celelalte facturi (Q-007).
2. **U.M. este „1”, nu „buc”** cum a spus clientul la interviu (Q-015).
3. **Serviciul este închiriere de mașină**, nu transport de persoane.
4. **Chitanța are numerotare proprie** (Aa 0021), separată de facturi, deși are aceeași literă de serie (Q-016).
5. **Termenul de plată este aceeași zi cu emiterea**, deci „o zi” înseamnă scadență = data emiterii.

### Diferențe între PDF-ul clientului și PDF-ul aplicației
_De completat după ce aplicația generează PDF-ul._

### XML
Clientul nu poate descărca XML-ul din SPV. Dovada validării: _raportul validatorului ANAF pentru XML-ul generat de aplicație, în `docs/validation/S1.txt`._
