import { useCallback, useEffect, useRef, useState } from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../state/auth';
import { useGuestState } from '../state/guestStore';

export default function Header() {
  const { user, ready, logout, syncing, syncMessage } = useAuth();
  const guest = useGuestState();
  const navigate = useNavigate();
  const [q, setQ] = useState('');
  const [menuOpen, setMenuOpen] = useState(false);
  const searchTimer = useRef<number>();

  // Debounce submit via Enter saja; form tidak auto-search saat mengetik (PRD §8).
  useEffect(() => () => window.clearTimeout(searchTimer.current), []);

  const onSearch = useCallback(
    (e: React.FormEvent) => {
      e.preventDefault();
      const term = q.trim();
      if (term) navigate(`/catalog?q=${encodeURIComponent(term)}`);
    },
    [q, navigate],
  );

  return (
    <header className="site-header">
      <div className="header-inner">
        <Link to="/" className="brand">📚 Manga Reader</Link>
        <form className="search-form" onSubmit={onSearch} role="search">
          <input
            type="search"
            value={q}
            onChange={(e) => setQ(e.target.value)}
            placeholder="Cari manga…"
            aria-label="Cari manga"
            maxLength={200}
          />
          <button type="submit">Cari</button>
        </form>
        <nav className={`main-nav ${menuOpen ? 'open' : ''}`}>
          <NavLink to="/catalog" onClick={() => setMenuOpen(false)}>Katalog</NavLink>
          <NavLink to="/library" onClick={() => setMenuOpen(false)}>My Library</NavLink>
          <NavLink to="/history" onClick={() => setMenuOpen(false)}>Riwayat</NavLink>
          {ready && user ? (
            <>
              <span className="user-chip" title={user.email}>{user.displayName || user.email}</span>
              <button className="btn btn-ghost" onClick={() => { logout(); setMenuOpen(false); }}>Logout</button>
            </>
          ) : (
            <NavLink to="/login" onClick={() => setMenuOpen(false)} className="btn btn-primary">Login</NavLink>
          )}
        </nav>
        <button className="nav-toggle" aria-label="Buka menu" onClick={() => setMenuOpen((v) => !v)}>☰</button>
      </div>
      {(syncing || syncMessage) && (
        <div className={`sync-banner ${syncing ? 'busy' : 'done'}`} role="status">
          {syncing ? '⏳ ' : 'ℹ️ '}
          {syncMessage ?? 'Menyinkronkan…'}
          {!syncing && guest.pendingSync && ' (masih ada data lokal belum tersinkron)'}
        </div>
      )}
    </header>
  );
}
