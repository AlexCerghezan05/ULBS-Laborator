import { useEffect, useState } from 'react';

// DS-p21-health (mocks/DS-p21-health.html): o singură cerere la deschidere, fără buton.
type Health =
  | { state: 'loading' }
  | { state: 'success'; body: Record<string, unknown> }
  | { state: 'error' };

export default function App() {
  const [health, setHealth] = useState<Health>({ state: 'loading' });

  useEffect(() => {
    const ctrl = new AbortController();
    fetch('/api/health', { signal: ctrl.signal })
      .then(async (res) => {
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        setHealth({ state: 'success', body: await res.json() });
      })
      .catch(() => {
        if (!ctrl.signal.aborted) setHealth({ state: 'error' });
      });
    return () => ctrl.abort();
  }, []);

  return (
    <main className="screen">
      <div className="col">
        <div className="head">
          <h1>Starea serverului</h1>
          <p className="muted">Pagină tehnică: arată răspunsul serverului la verificarea de stare.</p>
        </div>
        <Result health={health} />
      </div>
    </main>
  );
}

function Result({ health }: { health: Health }) {
  if (health.state === 'loading') {
    return (
      <div className="result" role="status" aria-live="polite">
        <p className="muted">Se verifică serverul…</p>
        <div className="sk-stack" aria-hidden="true">
          <div className="sk w40" />
          <div className="sk h64" />
        </div>
      </div>
    );
  }

  if (health.state === 'error') {
    return (
      <div className="banner danger" role="alert">
        <svg width="20" height="20" viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" aria-hidden="true">
          <circle cx="8" cy="8" r="6.5" />
          <path d="M8 4.5v4" />
          <path d="M8 11.2v.1" />
        </svg>
        <div>
          <h2>Serverul nu răspunde</h2>
          <p>Verificați că aplicația rulează cu „docker compose up”, apoi reîncărcați pagina.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="result" role="status" aria-live="polite">
      <div>
        <span className="label-tag success">
          <svg width="12" height="12" viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
            <path d="M3 8.5l3 3 7-7" />
          </svg>
          Serverul răspunde
        </span>
      </div>
      <dl className="resp">
        {Object.entries(health.body).map(([key, value]) => (
          <div key={key} className="resp-row">
            <dt>{key}</dt>
            <dd>{String(value)}</dd>
          </div>
        ))}
      </dl>
    </div>
  );
}
