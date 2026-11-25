// Clientul API-ului de autentificare (C-001). Aceeași origine, prin nginx; sesiunea e în cookie.
// CSRF (D-039): orice cerere care modifică ceva trimite X-XSRF-TOKEN cu valoarea cookie-ului XSRF-TOKEN.

export type Role = 'ADMIN' | 'OPERATOR' | 'CONTABIL';

export type Me = {
  username: string;
  fullName: string;
  role: Role;
  companyName: string | null;
};

/** Răspuns de eroare de la server: statusul și codul din {"error":"COD"} (PATTERN-005). */
export class ApiError extends Error {
  constructor(readonly status: number, readonly code: string | null) {
    super(`HTTP ${status}${code ? ` ${code}` : ''}`);
  }
}

function xsrfToken(): string | null {
  const match = document.cookie.split('; ').find((c) => c.startsWith('XSRF-TOKEN='));
  return match ? decodeURIComponent(match.slice('XSRF-TOKEN='.length)) : null;
}

async function ensureXsrfToken(): Promise<string | null> {
  if (!xsrfToken()) await fetch('/api/csrf', { credentials: 'same-origin' });
  return xsrfToken();
}

async function errorCode(res: Response): Promise<string | null> {
  try {
    const body = await res.json();
    return typeof body?.error === 'string' ? body.error : null;
  } catch {
    return null;
  }
}

/**
 * O cerere către /api. Erorile HTTP devin ApiError; o eroare de rețea rămâne TypeError.
 * Un 403 CSRF (cookie vechi sau lipsă) se reîncearcă o dată, cu un token nou.
 */
async function request(method: string, path: string, body?: unknown, retried = false): Promise<Response> {
  const headers: Record<string, string> = { Accept: 'application/json' };
  if (method !== 'GET') {
    const token = await ensureXsrfToken();
    if (token) headers['X-XSRF-TOKEN'] = token;
  }
  if (body !== undefined) headers['Content-Type'] = 'application/json';
  const res = await fetch(path, {
    method,
    headers,
    credentials: 'same-origin',
    body: body === undefined ? undefined : JSON.stringify(body),
  });
  if (res.ok) return res;
  const code = await errorCode(res);
  if (res.status === 403 && code === 'CSRF' && !retried) {
    await fetch('/api/csrf', { credentials: 'same-origin' });
    return request(method, path, body, true);
  }
  throw new ApiError(res.status, code);
}

export async function getMe(): Promise<Me> {
  return (await request('GET', '/api/me')).json();
}

export async function login(username: string, password: string): Promise<Me> {
  return (await request('POST', '/api/login', { username, password })).json();
}

export async function logout(): Promise<void> {
  await request('POST', '/api/logout');
}
