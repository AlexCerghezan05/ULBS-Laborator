import { useState, type ReactNode } from 'react';
import { NavLink, useNavigate } from 'react-router';
import { ApiError, logout, type Me, type Role } from '../api';
import { DocIcon, ErrorIcon, LogoutIcon } from '../icons';

// DS-p21-app-shell (mocks/DS-p21-app-shell.html): antetul cu „Facturare” și firma, meniul (doar
// ecranele care există), utilizatorul cu rolul și „Ieșire”.

const ROLE_LABEL: Record<Role, string> = { ADMIN: 'Admin', OPERATOR: 'Operator', CONTABIL: 'Contabil' };

type Props =
  | { state: 'loading' }
  | { state: 'error'; onRetry: () => void }
  | { state: 'ready'; me: Me; children: ReactNode };

export default function AppShell(props: Props) {
  return (
    <div className="app">
      <a className="skip" href="#main">Sari la conținut</a>
      {props.state === 'ready' ? <ReadyShell me={props.me}>{props.children}</ReadyShell> : <PendingShell {...props} />}
    </div>
  );
}

function Brand({ companyName }: { companyName: string | null }) {
  return (
    <div className="brand">
      <span className="app-name">Facturare</span>
      {companyName && <span className="company" title={companyName}>{companyName}</span>}
    </div>
  );
}

function Nav() {
  return (
    <nav className="nav" aria-label="Meniu principal">
      <NavLink to="/facturi">Facturi</NavLink>
    </nav>
  );
}

function PendingShell(props: { state: 'loading' } | { state: 'error'; onRetry: () => void }) {
  return (
    <>
      <header className="header">
        <Brand companyName={null} />
        <Nav />
        <div className="user" aria-hidden="true">
          <div className="who"><span className="sk name" /><span className="sk role" /></div>
          <span className="sk btn" />
        </div>
      </header>
      {props.state === 'loading' ? (
        <main className="content" id="main" aria-busy="true">
          <p className="muted" role="status">Se încarcă…</p>
          <div className="sk-stack" aria-hidden="true"><span className="sk title" /><span className="sk block" /></div>
        </main>
      ) : (
        <main className="content" id="main">
          <div className="banner danger" role="alert">
            <ErrorIcon />
            <div>
              <p><strong>Nu am putut încărca datele contului.</strong> Serverul nu răspunde. Verificați conexiunea, apoi încercați din nou.</p>
              <div className="actions"><button type="button" className="btn-secondary" onClick={props.onRetry}>Reîncercați</button></div>
            </div>
          </div>
        </main>
      )}
    </>
  );
}

function ReadyShell({ me, children }: { me: Me; children: ReactNode }) {
  const navigate = useNavigate();
  const [logoutState, setLogoutState] = useState<'idle' | 'busy' | 'failed'>('idle');

  async function onLogout() {
    setLogoutState('busy');
    try {
      await logout();
      navigate('/login', { replace: true });
    } catch (e) {
      // Fără sesiune pe server (expirată între timp): utilizatorul e deja ieșit.
      if (e instanceof ApiError && e.status === 401) navigate('/login', { replace: true });
      else setLogoutState('failed');
    }
  }

  const busy = logoutState === 'busy';
  return (
    <>
      <header className="header">
        <Brand companyName={me.companyName} />
        <Nav />
        <div className="user">
          <div className="who">
            <span className="name" title={me.fullName}>{me.fullName}</span>
            <span className="role">{ROLE_LABEL[me.role]}</span>
          </div>
          <button type="button" className="btn-secondary" onClick={onLogout} disabled={busy} aria-busy={busy || undefined}>
            <LogoutIcon />
            {busy ? 'Se închide sesiunea…' : 'Ieșire'}
          </button>
        </div>
      </header>
      <main className="content" id="main">
        {logoutState === 'failed' && (
          <div className="banner danger" role="alert">
            <ErrorIcon />
            <div>
              <p><strong>Sesiunea nu s-a putut închide.</strong> Serverul nu a răspuns, așa că sunteți încă autentificat. Încercați din nou.</p>
              <div className="actions"><button type="button" className="btn-secondary" onClick={onLogout}>Încercați din nou</button></div>
            </div>
          </div>
        )}
        {children}
      </main>
    </>
  );
}

/** Empty state-ul listei de facturi provizorii (DS-p21-app-shell). */
export function EmptyState({ title, text }: { title: string; text: string }) {
  return (
    <section className="empty" aria-labelledby="empty-h">
      <DocIcon />
      <h2 id="empty-h">{title}</h2>
      <p className="muted">{text}</p>
    </section>
  );
}
