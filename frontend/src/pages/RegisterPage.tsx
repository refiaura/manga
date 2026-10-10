import { useState } from 'react';
import type { FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { ApiError } from '../api/types';
import { useAuth } from '../state/auth';

export default function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [displayName, setDisplayName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirm, setConfirm] = useState('');
  const [errors, setErrors] = useState<string[]>([]);
  const [busy, setBusy] = useState(false);

  function validate(): string[] {
    const e: string[] = [];
    if (displayName.trim().length === 0 || displayName.length > 100) e.push('Nama tampilan 1–100 karakter.');
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) e.push('Format email tidak valid.');
    if (password.length < 8) e.push('Password minimal 8 karakter.');
    if (password !== confirm) e.push('Konfirmasi password tidak cocok.');
    return e;
  }

  async function onSubmit(ev: FormEvent) {
    ev.preventDefault();
    const errs = validate();
    setErrors(errs);
    if (errs.length) return;
    setBusy(true);
    try {
      await register(email, displayName.trim(), password);
      navigate('/library');
    } catch (e) {
      setErrors([e instanceof ApiError ? e.message : 'Registrasi gagal. Coba lagi.']);
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className="page auth">
      <form className="auth-form" onSubmit={onSubmit} noValidate>
        <h1>Daftar Akun</h1>
        {errors.length > 0 && (
          <ul className="form-errors" role="alert">{errors.map((x, i) => <li key={i}>{x}</li>)}</ul>
        )}
        <label>Nama tampilan
          <input value={displayName} onChange={(e) => setDisplayName(e.target.value)} maxLength={100} required />
        </label>
        <label>Email
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} autoComplete="email" required />
        </label>
        <label>Password
          <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} autoComplete="new-password" minLength={8} required />
        </label>
        <label>Konfirmasi password
          <input type="password" value={confirm} onChange={(e) => setConfirm(e.target.value)} autoComplete="new-password" required />
        </label>
        <button className="btn btn-primary" type="submit" disabled={busy}>{busy ? 'Memproses…' : 'Daftar'}</button>
        <p className="hint">Sudah punya akun? <Link to="/login">Masuk</Link></p>
      </form>
    </section>
  );
}
