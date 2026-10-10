import { useState } from 'react';
import type { FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { ApiError } from '../api/types';
import { useAuth } from '../state/auth';

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [errors, setErrors] = useState<string[]>([]);
  const [busy, setBusy] = useState(false);

  function validate(): string[] {
    const e: string[] = [];
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) e.push('Format email tidak valid.');
    if (password.length < 1) e.push('Password wajib diisi.');
    return e;
  }

  async function onSubmit(ev: FormEvent) {
    ev.preventDefault();
    const errs = validate();
    setErrors(errs);
    if (errs.length) return;
    setBusy(true);
    try {
      await login(email, password);
      navigate('/library');
    } catch (e) {
      setErrors([e instanceof ApiError ? e.message : 'Login gagal. Coba lagi.']);
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className="page auth">
      <form className="auth-form" onSubmit={onSubmit} noValidate>
        <h1>Login</h1>
        {errors.length > 0 && (
          <ul className="form-errors" role="alert">{errors.map((x, i) => <li key={i}>{x}</li>)}</ul>
        )}
        <label>Email
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} autoComplete="email" required />
        </label>
        <label>Password
          <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} autoComplete="current-password" required />
        </label>
        <button className="btn btn-primary" type="submit" disabled={busy}>{busy ? 'Memproses…' : 'Masuk'}</button>
        <p className="hint">
          Belum punya akun? <Link to="/register">Daftar</Link> — data baca lokal Anda akan otomatis disinkronkan.
        </p>
      </form>
    </section>
  );
}
