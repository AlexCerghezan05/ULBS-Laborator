# Open questions · Autentificare: login, sesiune, conturi

> Topic file. The index is in `../OPEN-QUESTIONS.md`.

## Q-040 · Unde duce login-ul după o sesiune expirată
- **Status:** answered
- **Answer:** Utilizatorul revine la pagina de unde a plecat, doar dacă e o cale internă a aplicației (începe cu „/”, nu cu „//” și nu e un URL extern); altfel merge la lista de facturi (Alexandru Cerghezan, 25.11.2026). Decizia: D-034 (proposed).
- **Context:** Macheta DS-p21-login arată mesajul „Sesiunea a expirat” după redirect. După autentificare, aplicația deschide lista de facturi (docs/scope.md) sau pagina de pe care a plecat utilizatorul (de exemplu factura deschisă)?
- **Blocks:** Planul de autentificare; comportamentul stării „Sesiune expirată” din DS-p21-login.
- **Who answers:** Alexandru Cerghezan
- **Source:** macheta DS-p21-login, 25.11.2026

## Q-041 · Când apare mesajul „Contul este dezactivat”
- **Status:** answered
- **Answer:** Doar după nume de utilizator și parolă corecte; propunerea acceptată (Alexandru Cerghezan, 25.11.2026). Decizia: D-035 (proposed).
- **Context:** Mesajul pentru date greșite e generic, ca să nu spună dacă există utilizatorul. Dacă „Contul este dezactivat” apare la orice parolă, el spune că numele de utilizator există. Apare doar după o parolă corectă, sau la orice parolă?
- **Proposed answer:** Doar după nume de utilizator și parolă corecte; altfel, mesajul generic. Așa mesajul nu dezvăluie ce conturi există.
- **Blocks:** Planul de autentificare; starea „Cont dezactivat” din DS-p21-login.
- **Who answers:** Alexandru Cerghezan
- **Source:** macheta DS-p21-login, 25.11.2026

## Q-042 · Pe ce se numără încercările greșite de login
- **Status:** open
- **Context:** D-036 (propusă) blochează autentificarea 15 minute după 5 încercări greșite. Încercările se numără pe numele de utilizator, pe adresa IP sau pe amândouă? Pe cont, un atacator poate bloca intenționat contul altcuiva și mesajul arată că numele există; pe IP, nu protejează contul de încercări din mai multe adrese. Admin-ul poate debloca un cont înainte de 15 minute? Cele 5 încercări se numără într-o fereastră de timp sau până la primul login reușit?
- **Blocks:** Planul de autentificare (backend).
- **Who answers:** Alexandru Cerghezan
- **Source:** macheta DS-p21-login, 25.11.2026
