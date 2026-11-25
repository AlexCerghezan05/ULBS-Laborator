import { useCallback, useEffect, useState, type ReactNode } from 'react';
import { Navigate, useLocation } from 'react-router';
import { ApiError, getMe, type Me } from './api';
import { loginPath } from './safeRedirect';
import AppShell from './components/AppShell';

type State =
  | { kind: 'loading' }
  | { kind: 'ready'; me: Me }
  | { kind: 'error' }
  | { kind: 'login'; expired: boolean };

/**
 * Rută protejată: citește GET /api/me (C-001). Fără sesiune trimite la login cu pagina de unde a
 * plecat utilizatorul (D-034), cu „Sesiunea a expirat” pentru SESSION_EXPIRED. Rolul îl verifică
 * serverul pe fiecare rută (D-012); aici doar se afișează.
 */
export function RequireAuth({ children }: { children: ReactNode }) {
  const location = useLocation();
  const [state, setState] = useState<State>({ kind: 'loading' });

  const load = useCallback(() => {
    setState({ kind: 'loading' });
    getMe()
      .then((me) => setState({ kind: 'ready', me }))
      .catch((e) => {
        if (e instanceof ApiError && e.status === 401) {
          setState({ kind: 'login', expired: e.code === 'SESSION_EXPIRED' });
        } else {
          setState({ kind: 'error' });
        }
      });
  }, []);

  useEffect(load, [load]);

  if (state.kind === 'login') {
    const from = location.pathname + location.search + location.hash;
    return <Navigate to={loginPath(from, state.expired)} replace />;
  }
  if (state.kind === 'loading') return <AppShell state="loading" />;
  if (state.kind === 'error') return <AppShell state="error" onRetry={load} />;
  return <AppShell state="ready" me={state.me}>{children}</AppShell>;
}
