# Patterns · API: acces pe rute, erori

> Topic file. The index is in `../PATTERNS.md`.

## PATTERN-004 · Fiecare rută își declară accesul cu @PreAuthorize, verificat de un test
- **Status:** candidate
- **Date:** 2026-11-25
- **How:** Fiecare metodă de controller din `ro.ulbs.p21` are `@PreAuthorize` cu unul din nivelurile din `Access` (`ADMIN`, `WRITE`, `REPORTS`, `READ`, după D-012). `SecurityConfig` lasă publice doar rutele din lista lui; restul cer login. `RouteSecurityCoverageIT` parcurge toate rutele: pică dacă una n-are `@PreAuthorize`, dacă o rută care modifică date (POST, PUT, PATCH, DELETE) are doar `permitAll()` sau `isAuthenticated()`, sau dacă rutele lăsate publice de regulile din `SecurityConfig` (întrebate direct pentru un vizitator fără login) diferă de lista lui. O rută publică nouă se adaugă în ambele locuri, cu motiv. Testele de roluri folosesc rute de test într-un `@RestController` imbricat în test, cu `@Import`.
- **Why:** Rolurile se verifică pe server pe fiecare rută, nu doar în UI (D-012); o rută uitată fără regulă e prinsă de test, nu în producție.
- **Example:** `backend/src/main/java/ro/ulbs/p21/auth/Access.java`; `SecurityConfig.java`; `backend/src/test/java/ro/ulbs/p21/auth/RouteSecurityCoverageIT.java`; `AuthorizationIT.java`.
- **Source:** agent · confidence medium
- **Confirmed:** 2026-11-25

## PATTERN-005 · Erorile API au forma {"error":"COD"}; textul îl pune frontend-ul
- **Status:** candidate
- **Date:** 2026-11-25
- **How:** O eroare pe care frontend-ul o tratează are un status HTTP și un cod stabil în majuscule (`BAD_CREDENTIALS`, `SESSION_EXPIRED`), scris cu `ApiError.response` în controller sau cu `ApiError.write` în filtrele Spring Security. Mesajul pentru utilizator (din machetă, în română) îl alege frontend-ul după cod. Codurile sunt în contract (C-001).
- **Why:** Textele stau într-un singur loc, lângă machetă; backend-ul nu trimite texte care se pot schimba fără să rupă testele.
- **Example:** `backend/src/main/java/ro/ulbs/p21/auth/ApiError.java`; `SecurityConfig.java` (entry point, access denied).
- **Source:** agent · confidence medium
- **Confirmed:** 2026-11-25

## PATTERN-006 · Contor pe un rând: citire FOR UPDATE și actualizare în aceeași tranzacție
- **Status:** candidate
- **Date:** 2026-11-25
- **How:** Când o regulă depinde de o stare dintr-un rând care se schimbă la fiecare cerere (de ex. încercările greșite și blocarea contului, D-036–D-038), serviciul e `@Transactional` și citește rândul cu `SELECT ... FOR UPDATE`; verificarea și UPDATE-ul se fac sub același lock, ca cererile simultane pe același rând să se facă una după alta. Un test `*ConcurrencyIT`, fără `@Transactional`, pornește cererile deodată și verifică rezultatul final.
- **Why:** Review-ul de securitate PR #3: o citire fără lock urmată de UPDATE lăsa 20 de cereri simultane să verifice parola de 20 de ori în loc de 5.
- **Example:** `backend/src/main/java/ro/ulbs/p21/auth/LoginService.java` (login); `AppUserRepository.java` (findForLogin); `backend/src/test/java/ro/ulbs/p21/auth/LoginConcurrencyIT.java`.
- **Source:** agent · confidence medium
