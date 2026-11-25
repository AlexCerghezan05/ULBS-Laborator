import { useEffect, useRef, useState, type FormEvent } from 'react';
import { useNavigate, useSearchParams } from 'react-router';
import { ApiError, login } from '../api';
import { safeRedirect } from '../safeRedirect';
import { ClockIcon, ErrorIcon, EyeIcon, EyeOffIcon, LockIcon } from '../icons';

// DS-p21-login (mocks/DS-p21-login.html). Mesajele vin după codul de eroare din C-001 (PATTERN-005):
// date greșite (generic), blocare (D-036), cont dezactivat (D-035), sesiune expirată (D-034).

type Message = 'error' | 'locked' | 'disabled' | 'expired' | 'network';

function Banner({ message }: { message: Message }) {
  switch (message) {
    case 'error':
      return (
        <div id="login-msg" className="banner danger" role="alert"><ErrorIcon />
          <p><strong>Numele de utilizator sau parola nu sunt corecte.</strong> Verificați-le și încercați din nou.</p></div>
      );
    case 'locked':
      return (
        <div id="login-msg" className="banner danger" role="alert"><ClockIcon />
          <p><strong>Prea multe încercări greșite.</strong> Autentificarea este blocată 15 minute. Încercați din nou după aceea.</p></div>
      );
    case 'disabled':
      return (
        <div id="login-msg" className="banner danger" role="alert"><LockIcon />
          <p><strong>Contul este dezactivat.</strong> Pentru reactivare, contactați administratorul aplicației.</p></div>
      );
    case 'expired':
      return (
        <div id="login-msg" className="banner warning" role="status"><ClockIcon />
          <p><strong>Sesiunea a expirat.</strong> Autentificați-vă din nou pentru a continua.</p></div>
      );
    case 'network':
      return (
        <div id="login-msg" className="banner danger" role="alert"><ErrorIcon />
          <p><strong>Serverul nu răspunde.</strong> Verificați conexiunea, apoi încercați din nou.</p></div>
      );
  }
}

function messageFor(e: unknown): Message {
  if (e instanceof ApiError) {
    if (e.code === 'BAD_CREDENTIALS') return 'error';
    if (e.code === 'LOGIN_LOCKED') return 'locked';
    if (e.code === 'ACCOUNT_DISABLED') return 'disabled';
  }
  return 'network';
}

export default function LoginPage() {
  const navigate = useNavigate();
  const [params] = useSearchParams();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPw, setShowPw] = useState(false);
  const [busy, setBusy] = useState(false);
  const [missing, setMissing] = useState({ username: false, password: false });
  const [message, setMessage] = useState<Message | null>(params.get('expired') === '1' ? 'expired' : null);
  const userRef = useRef<HTMLInputElement>(null);
  const pwRef = useRef<HTMLInputElement>(null);
  const submitRef = useRef<HTMLButtonElement>(null);
  const [focusAfter, setFocusAfter] = useState<'username' | 'password' | 'submit' | null>('username');

  useEffect(() => {
    document.title = 'Autentificare';
  }, []);

  // Focus-ul se mută după ce câmpurile nu mai sunt dezactivate.
  useEffect(() => {
    if (!focusAfter || busy) return;
    ({ username: userRef, password: pwRef, submit: submitRef })[focusAfter].current?.focus();
    setFocusAfter(null);
  }, [focusAfter, busy]);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    const empty = { username: username.trim() === '', password: password === '' };
    setMissing(empty);
    if (empty.username || empty.password) {
      setFocusAfter(empty.username ? 'username' : 'password');
      return;
    }
    setMessage(null);
    setBusy(true);
    try {
      await login(username, password);
      navigate(safeRedirect(params.get('next')), { replace: true });
    } catch (err) {
      const m = messageFor(err);
      setMessage(m);
      if (m === 'error') {
        setPassword('');
        setFocusAfter('password');
      } else {
        setFocusAfter('submit');
      }
      setBusy(false);
    }
  }

  const credentialsError = message === 'error';
  const describedBy = (field: 'username' | 'password') =>
    [missing[field] ? `${field}-err` : null, message ? 'login-msg' : null].filter(Boolean).join(' ') || undefined;

  return (
    <main className="login-screen">
      <div className="card">
        <h1>Autentificare</h1>
        {message && <Banner message={message} />}
        <form noValidate onSubmit={onSubmit}>
          <div className="field">
            <label htmlFor="username">Nume de utilizator</label>
            <input
              ref={userRef}
              className={`input${missing.username || credentialsError ? ' has-error' : ''}`}
              id="username" name="username" type="text" autoComplete="username" autoCapitalize="none" spellCheck={false}
              required value={username} disabled={busy}
              aria-invalid={missing.username || credentialsError || undefined}
              aria-describedby={describedBy('username')}
              onChange={(e) => setUsername(e.target.value)}
            />
            {missing.username && <p className="err" id="username-err"><ErrorIcon size={16} />Introduceți numele de utilizator.</p>}
          </div>
          <div className="field">
            <label htmlFor="password">Parolă</label>
            <div className="pw">
              <input
                ref={pwRef}
                className={`input${missing.password || credentialsError ? ' has-error' : ''}`}
                id="password" name="password" type={showPw ? 'text' : 'password'} autoComplete="current-password"
                required value={password} disabled={busy}
                aria-invalid={missing.password || credentialsError || undefined}
                aria-describedby={describedBy('password')}
                onChange={(e) => setPassword(e.target.value)}
              />
              <button
                type="button" className="toggle" aria-controls="password" aria-pressed={showPw}
                aria-label={showPw ? 'Ascunde parola' : 'Afișează parola'} disabled={busy}
                onClick={() => setShowPw((v) => !v)}
              >
                {showPw ? <EyeOffIcon /> : <EyeIcon />}
              </button>
            </div>
            {missing.password && <p className="err" id="password-err"><ErrorIcon size={16} />Introduceți parola.</p>}
          </div>
          <button ref={submitRef} type="submit" className="btn" disabled={busy} aria-busy={busy || undefined}>
            {busy ? 'Se autentifică…' : 'Autentificare'}
          </button>
        </form>
        <p className="foot">Contul îl creează administratorul aplicației. Dacă nu aveți cont sau ați uitat parola, contactați-l.</p>
      </div>
    </main>
  );
}
