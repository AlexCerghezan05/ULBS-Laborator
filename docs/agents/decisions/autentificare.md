# Decisions · Autentificare: login, sesiune, blocare

> Topic file. The index is in `../DECISIONS.md`.

## D-034 · După login, revenire la pagina internă de unde a plecat utilizatorul
- **Status:** approved
- **Date:** 2026-11-25
- **Rule:** După autentificare, aplicația deschide pagina de unde a plecat utilizatorul (de exemplu după o sesiune expirată), doar dacă este o cale internă a aplicației: începe cu „/”, nu începe cu „//” și nu este un URL extern. Altfel, deschide lista de facturi (DS-p21-invoice-list).
- **Why:** Utilizatorul nu pierde locul unde lucra; verificarea căii împiedică redirectul spre alt site (open redirect).
- **Rejected:** Mereu lista de facturi; redirect spre orice URL primit.
- **Evidence:** răspunsul la Q-040 (Alexandru Cerghezan, 25.11.2026); macheta DS-p21-login.
- **Source:** user · confidence high
- **Confirmed:** 2026-11-25

## D-035 · Mesajul „Cont dezactivat” doar după date corecte
- **Status:** approved
- **Date:** 2026-11-25
- **Rule:** La login, mesajul „Contul este dezactivat” apare doar când numele de utilizator și parola sunt corecte și contul are `app_user.active = false`. În orice alt caz de eșec apare mesajul generic „Numele de utilizator sau parola nu sunt corecte.”, fără să spună care e greșit.
- **Why:** Mesajul nu dezvăluie ce nume de utilizator există.
- **Rejected:** Mesajul de cont dezactivat la orice parolă.
- **Evidence:** răspunsul la Q-041 (Alexandru Cerghezan, 25.11.2026); macheta DS-p21-login.
- **Source:** user · confidence high
- **Confirmed:** 2026-11-25

## D-036 · Blocarea autentificării după 5 încercări greșite, 15 minute
- **Status:** approved
- **Date:** 2026-11-25
- **Rule:** După 5 încercări de login greșite, autentificarea e blocată 15 minute. În perioada blocării, orice încercare (chiar cu parola corectă) primește mesajul „Prea multe încercări greșite. Autentificarea este blocată 15 minute.” Cum se numără încercările și cum expiră blocarea: D-037.
- **Why:** Limitează ghicirea parolelor; conturile nu au „am uitat parola” (D-012), deci blocarea trebuie să expire singură (D-037).
- **Rejected:** none recorded.
- **Evidence:** cererea lui Alexandru Cerghezan, 25.11.2026 („de ex. după 5 încercări greșite, blocare 15 minute”); valorile confirmate de Alexandru Cerghezan, 25.11.2026; macheta DS-p21-login.
- **Source:** user · confidence high
- **Confirmed:** 2026-11-25

## D-037 · Încercările greșite se numără pe cont; blocarea expiră singură
- **Status:** approved
- **Date:** 2026-11-25
- **Rule:** Încercările greșite de login (D-036) se numără pe numele de utilizator (`app_user.username`). Un login reușit resetează numărătoarea. Blocarea de 15 minute expiră singură; Admin-ul nu poate debloca manual un cont.
- **Why:** Răspunsul la Q-042: o regulă simplă, fără ecran sau acțiune nouă pentru Admin.
- **Rejected:** Numărarea pe adresa IP sau pe cont și IP; deblocarea manuală de către Admin.
- **Evidence:** răspunsul la Q-042 (Alexandru Cerghezan, 25.11.2026).
- **Source:** user · confidence high
- **Confirmed:** 2026-11-25

## D-038 · După blocare, numărătoarea începe de la zero; blocarea nu se prelungește
- **Status:** approved
- **Date:** 2026-11-25
- **Rule:** Când începe blocarea de 15 minute (D-036), numărătoarea încercărilor greșite a contului revine la 0: după expirare, utilizatorul are din nou 5 încercări. O încercare făcută în timpul blocării (cu orice parolă) nu prelungește blocarea și nu se numără.
- **Why:** Blocarea expiră singură (D-037); prelungirea ar permite ținerea contului blocat la nesfârșit cu o încercare la 15 minute.
- **Rejected:** Numărătoarea continuă după expirare; prelungirea blocării la fiecare încercare.
- **Evidence:** planul „Autentificare și autorizare”, răspunsul lui Alexandru Cerghezan, 25.11.2026.
- **Source:** user · confidence high
- **Confirmed:** 2026-11-25

## D-039 · Sesiune pe server, în memorie, 30 de minute; cookie HttpOnly, SameSite=Lax; CSRF cu cookie
- **Status:** approved
- **Date:** 2026-11-25
- **Rule:** Autentificarea folosește sesiunea servlet, ținută în memoria backend-ului (o singură instanță). Sesiunea expiră după 30 de minute de inactivitate. Cookie-ul de sesiune e `HttpOnly`, `SameSite=Lax` și `Secure` după `P21_COOKIE_SECURE` (implicit `true`; `false` doar local, în Compose). CSRF: cookie-ul `XSRF-TOKEN` citit de frontend și trimis în header-ul `X-XSRF-TOKEN` la fiecare cerere care modifică ceva. Frontend-ul și API-ul sunt pe aceeași origine, prin nginx.
- **Why:** Cea mai mică variantă pentru o singură instanță; la repornirea backend-ului utilizatorii se loghează din nou (acceptat).
- **Rejected:** Spring Session JDBC; token JWT în browser; SameSite=Strict (un link din afara aplicației ar arăta login deși sesiunea e validă).
- **Evidence:** planul „Autentificare și autorizare”, aprobat de Alexandru Cerghezan, 25.11.2026.
- **Source:** user · confidence high
- **Confirmed:** 2026-11-25

## D-041 · Contul dezactivat își pierde sesiunea la următoarea cerere
- **Status:** approved
- **Date:** 2026-11-25
- **Rule:** La fiecare cerere autentificată, serverul citește `app_user.active`. Dacă e `false` (sau utilizatorul nu mai există), sesiunea se invalidează și răspunsul e 401.
- **Why:** Răspunsul la Q-043: dezactivarea unui cont trebuie să-l scoată imediat din aplicație, nu după expirarea sesiunii.
- **Rejected:** Sesiunea rămâne validă până expiră.
- **Evidence:** răspunsul la Q-043 (Alexandru Cerghezan, 25.11.2026).
- **Source:** user · confidence high
- **Confirmed:** 2026-11-25

## D-042 · O schimbare de rol se aplică la următorul login
- **Status:** approved
- **Date:** 2026-11-25
- **Rule:** Rolul utilizatorului (`app_user.role`) se citește la login și rămâne în sesiune (D-039). Dacă Admin-ul schimbă rolul unui cont, sesiunile deschise păstrează rolul vechi până la logout sau expirare; rolul nou se aplică la următorul login. Dezactivarea contului e diferită: scoate imediat utilizatorul (D-041).
- **Why:** Cea mai mică variantă: sesiunea ține rolul de la login; recitirea rolului la fiecare cerere nu a fost cerută.
- **Rejected:** Rolul recitit din baza de date la fiecare cerere.
- **Evidence:** acceptat de Alexandru Cerghezan, 25.11.2026; `LoginService.java` (rolul pus în `AppPrincipal` la login), `LoginController.java`.
- **Source:** user · confidence high
- **Confirmed:** 2026-11-25
