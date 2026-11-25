# Deploy pe Render (planul gratuit)

Decizia: D-044, cu datele de pornire din D-045 și conturile din D-046. Blueprint-ul e `render.yaml`, cu trei resurse:
- `p21-db`: Postgres 18 Free;
- `p21-backend`: Spring Boot în Docker;
- `p21-frontend`: nginx în Docker.

Toate stau în regiunea `frankfurt`. Browserul vede doar adresa lui `p21-frontend`; nginx trimite `/api/` la adresa publică HTTPS a lui `p21-backend`. Deploy-ul se face doar la go-ul owner-ului.

Local nu se schimbă nimic: `docker compose up --build` merge ca înainte (AGENTS.md §2).

## Pașii, în ordine

1. **Merge pe `main`.** Render citește `render.yaml` din GitHub.
2. **Local, parola și hash-ul ei** (prin Docker, D-001). Parola o păstrați într-un manager de parole; nu intră nicăieri altundeva.
   ```bash
   # o parolă puternică (32 de caractere aleatoare)
   docker run --rm alpine sh -c "head -c 24 /dev/urandom | base64"
   # hash-ul BCrypt al parolei (îl puneți în Render, nu parola)
   docker run --rm httpd:2.4-alpine htpasswd -nbBC 10 "" 'PAROLA' | tr -d ':\n'
   ```
   Pentru Operator și Contabil faceți la fel, dacă vreți conturile lor.
3. **Render → New → Blueprint.** Alegeți repo-ul `ULBS-Laborator` și branch-ul `main`, apoi dați un nume Blueprint-ului. Render afișează `p21-db`, `p21-backend` și `p21-frontend` și cere valorile secrete (`sync: false`):

   | Serviciu | Variabilă | Valoare |
   |---|---|---|
   | p21-backend | `P21_ADMIN_USERNAME` | numele contului Admin (litere, cifre, `.`, `-`, `_`) |
   | p21-backend | `P21_ADMIN_PASSWORD_HASH` | hash-ul de la pasul 2, fără ghilimele |
   | p21-backend | `P21_OPERATOR_USERNAME` și `P21_OPERATOR_PASSWORD_HASH` | opționale: ambele sau niciuna |
   | p21-backend | `P21_CONTABIL_USERNAME` și `P21_CONTABIL_PASSWORD_HASH` | opționale: ambele sau niciuna |
   | p21-frontend | `P21_BACKEND_URL` | `https://p21-backend.onrender.com` (se corectează la pasul 5) |

   Apoi **Apply**. Dacă lipsește o variabilă a Admin-ului sau dacă puneți parola în locul hash-ului, migrarea se oprește cu un mesaj clar în log-ul lui `p21-backend`, iar deploy-ul pică. Ca să corectați: **Environment** → corectați valoarea → **Save, rebuild and deploy**.
4. **Așteptați** ca `p21-backend` să ajungă *Live*. Prima pornire durează câteva minute: build-ul Maven, apoi ~2,5 minute de pornire pe 0,1 CPU.
5. **Copiați adresa lui `p21-backend`** din dashboard. Dacă diferă de cea de la pasul 3 (de ex. `https://p21-backend-ab12.onrender.com`): `p21-frontend` → **Environment** → `P21_BACKEND_URL` → **Save, rebuild and deploy**. Adresa nu are `/` la final.
6. **Verificați:**
   - deschideți adresa lui `p21-frontend` și vă logați ca Admin;
   - în DevTools → Application → Cookies, `JSESSIONID` are `HttpOnly`, `Secure`, `SameSite=Lax`, iar `XSRF-TOKEN` are `Secure`; niciunul nu are `Domain`;
   - `/api/docs/ui` se deschide pentru Admin.
7. **Verificarea bazei (Q-038).** `p21-db` acceptă doar conexiuni din Render (`ipAllowList: []`). Pentru verificare:
   - `p21-db` → **Networking** → **Access Control** → adăugați IP-ul vostru;
   - copiați **Connect → External Database URL**;
   - rulați comanda de mai jos;
   - la final scoateți IP-ul din listă.

   ```bash
   docker run --rm -i postgres:18.7-alpine psql "EXTERNAL_DATABASE_URL" < docs/deploy-check.sql
   ```
   Rezultatul (fără URL, care conține parola) îl trimiteți agentului: el răspunde la Q-038 și arată dacă se poate face rolul restrâns `p21_app`.
8. **Notați data creării bazei.** Planul gratuit o face inaccesibilă după 30 de zile și o șterge după încă 14 (vezi Riscuri).

## Ce verifică `docs/deploy-check.sql`
- Drepturile utilizatorului dat de Render: superuser, CREATEROLE, CREATEDB, proprietarul bazei și al tabelelor.
- Dacă `set session_replication_role = replica` e refuzat. Rezultatul așteptat: eroare. Eroarea arată că trigger-ele nu pot fi ocolite așa.
- Că niciun trigger nu e oprit (după D-045 trebuie să fie toate pornite).
- Numerele: S1 = Aa0060, chitanța = 21, contoarele 60 și 21.

**Ce înseamnă pentru trigger-e** (D-033, D-005):
- Fără superuser, `session_replication_role` nu le ocolește.
- Proprietarul tabelelor, adică utilizatorul cu care rulează aplicația, le poate totuși opri cu `ALTER TABLE … DISABLE TRIGGER`. Asta poate face oricine are parola bazei sau controlează aplicația.

Bariera completă e un rol `p21_app` doar cu SELECT/INSERT/UPDATE/DELETE, cu migrările rulate de proprietar, plus `REVOKE TEMP`. E un plan separat, după verificare.

## Memoria (măsurată 26.11.2026)
Backend-ul a rulat local cu limitele planului gratuit și setările din `render.yaml`: `docker run --memory=512m --cpus=0.1`, `JAVA_TOOL_OPTIONS=-Xmx200m -Xss512k -XX:MaxMetaspaceSize=128m -XX:ReservedCodeCacheSize=48m -XX:MaxDirectMemorySize=32m -XX:+UseSerialGC -XX:TieredStopAtLevel=1`, Hikari 5, Tomcat 20. Baza era nouă, cu `db/migration` și `db/prod`.

| Măsurătoare | Valoare |
|---|---|
| Memoria după pornire (`docker stats`) | 141 MiB din 512 MiB |
| Memoria după 50 de login-uri | 144 MiB |
| RSS maxim (VmHWM) | 164 MB |
| OOM | nu |
| Pornire (Spring) / până la `/api/health` 200 | 123 s / 144 s |
| 50 de login-uri (CSRF + login, BCrypt cost 10) | 121 s, toate 200 (~2,4 s fiecare pe 0,1 CPU) |

## Riscuri
- **Adormirea după 15 minute fără trafic.** Fiecare serviciu adoarme separat. Prima cerere după o pauză așteaptă trezirea, iar la backend se adaugă pornirea Spring, ~2,5 minute pe 0,1 CPU. nginx așteaptă până la 300 s (`proxy_read_timeout`). Sesiunile sunt în memorie (D-039), deci după fiecare adormire sau repornire a backend-ului toată lumea se loghează din nou. Practic, după 15 minute fără trafic, nu după 30.
- **750 de ore gratuite pe lună, pentru tot contul.** Două servicii treze tot timpul ar consuma ~1.488 de ore. Dacă orele se termină, Render suspendă serviciile până la luna următoare.
- **Baza expiră la 30 de zile și nu are backup.** După 30 de zile devine inaccesibilă, iar după încă 14 e ștearsă cu tot cu facturile emise. Înainte de date reale ale clientului, baza trebuie trecută pe un plan plătit sau mutată. Backup de mână (IP-ul adăugat ca la pasul 7):
  ```bash
  docker run --rm -v "$(pwd -W):/out" postgres:18.7-alpine pg_dump --format=custom --file=/out/p21-AAAA-LL-ZZ.dump "EXTERNAL_DATABASE_URL"
  ```
  Fișierul `.dump` conține datele: nu intră în git.
- **Backend-ul are și adresă publică proprie.** Pe planul gratuit nu se poate evita. Se poate ajunge la backend ocolind nginx, deci fără headerele din D-024. Sesiunea, CSRF-ul și rolurile funcționează la fel. Cine trimite direct un `X-Forwarded-*` fals schimbă doar ce vede backend-ul despre propria lui cerere.
- **Nedocumentat de Render:** drepturile utilizatorului Postgres (pasul 7) și headerele `X-Forwarded-*` ale proxy-ului (pasul 6).
- **Greu de inversat:** regiunea nu se mai schimbă după creare. Numerele 61 și 22, odată emise, nu se mai refolosesc. S1 și chitanța ei nu se mai pot șterge (D-033).
