// D-034: după login, aplicația deschide pagina de unde a plecat utilizatorul, doar dacă e o
// cale internă; altfel lista de facturi (DS-p21-invoice-list).

/** Lista de facturi: pagina implicită după login. */
export const HOME = '/facturi';

const LOGIN = '/login';
const BASE = 'http://p21.invalid';

/**
 * Calea în care se poate intra după login, sau HOME. Acceptă doar ce începe cu un singur „/”,
 * fără „\” și fără caractere de control (browserele le ignoră și transformă „/\t/x” în „//x”),
 * și rămâne pe aceeași origine după parsare. Pagina de login nu e o destinație.
 */
export function safeRedirect(next: string | null): string {
  if (!next || !next.startsWith('/') || next.startsWith('//')) return HOME;
  if (/[\\\u0000-\u001f\u007f]/.test(next)) return HOME;
  let url: URL;
  try {
    url = new URL(next, BASE);
  } catch {
    return HOME;
  }
  if (url.origin !== BASE || url.pathname === LOGIN) return HOME;
  return url.pathname + url.search + url.hash;
}

/** Adresa paginii de login, cu pagina de revenire și, după caz, mesajul „Sesiunea a expirat”. */
export function loginPath(from: string, expired: boolean): string {
  const params = new URLSearchParams();
  if (from !== HOME) params.set('next', from);
  if (expired) params.set('expired', '1');
  const query = params.toString();
  return query ? `${LOGIN}?${query}` : LOGIN;
}
