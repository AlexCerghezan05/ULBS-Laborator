# Decisions · Autentificare: login, sesiune, blocare

> Topic file. The index is in `../DECISIONS.md`.

## D-034 · După login, revenire la pagina internă de unde a plecat utilizatorul
- **Status:** proposed
- **Date:** 2026-11-25
- **Rule:** După autentificare, aplicația deschide pagina de unde a plecat utilizatorul (de exemplu după o sesiune expirată), doar dacă este o cale internă a aplicației: începe cu „/”, nu începe cu „//” și nu este un URL extern. Altfel, deschide lista de facturi (DS-p21-invoice-list).
- **Why:** Utilizatorul nu pierde locul unde lucra; verificarea căii împiedică redirectul spre alt site (open redirect).
- **Rejected:** Mereu lista de facturi; redirect spre orice URL primit.
- **Evidence:** răspunsul la Q-040 (Alexandru Cerghezan, 25.11.2026); macheta DS-p21-login.
- **Source:** user · confidence high

## D-035 · Mesajul „Cont dezactivat” doar după date corecte
- **Status:** proposed
- **Date:** 2026-11-25
- **Rule:** La login, mesajul „Contul este dezactivat” apare doar când numele de utilizator și parola sunt corecte și contul are `app_user.active = false`. În orice alt caz de eșec apare mesajul generic „Numele de utilizator sau parola nu sunt corecte.”, fără să spună care e greșit.
- **Why:** Mesajul nu dezvăluie ce nume de utilizator există.
- **Rejected:** Mesajul de cont dezactivat la orice parolă.
- **Evidence:** răspunsul la Q-041 (Alexandru Cerghezan, 25.11.2026); macheta DS-p21-login.
- **Source:** user · confidence high

## D-036 · Blocarea autentificării după 5 încercări greșite, 15 minute
- **Status:** proposed
- **Date:** 2026-11-25
- **Rule:** După 5 încercări de login greșite, autentificarea e blocată 15 minute. În perioada blocării, orice încercare (chiar cu parola corectă) primește mesajul „Prea multe încercări greșite. Autentificarea este blocată 15 minute.” Pe ce se numără încercările (cont, adresă IP sau ambele): Q-042.
- **Why:** Limitează ghicirea parolelor; conturile nu au „am uitat parola” (D-012), deci deblocarea vine din trecerea timpului sau de la Admin.
- **Rejected:** none recorded.
- **Evidence:** cererea lui Alexandru Cerghezan, 25.11.2026 („de ex. după 5 încercări greșite, blocare 15 minute”); macheta DS-p21-login.
- **Source:** user · confidence medium (valorile au fost date ca exemplu)
