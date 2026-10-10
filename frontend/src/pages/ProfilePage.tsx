import { useState } from 'react';
import { useAuth } from '../state/auth';
import { useGuestState, clearGuestData } from '../state/guestStore';

export default function ProfilePage() {
  const { user, logout, syncGuestData, syncing } = useAuth();
  const guest = useGuestState();
  const [feedback, setFeedback] = useState<string | null>(null);

  if (!user) {
    return (
      <section className="page profile">
        <h1>Profil</h1>
        <p className="hint">Anda sedang dalam mode guest. Data baca tersimpan lokal di perangkat ini.</p>
        <p>
          Jumlah entri lokal: {guest.library.length} perpustakaan, {guest.progress.length} progres.
          {guest.pendingSync ? ' (belum tersinkron)' : ''}
        </p>
        <button className="btn btn-danger" onClick={() => { if (window.confirm('Hapus semua data lokal?')) clearGuestData(); }}>
          Hapus data lokal
        </button>
      </section>
    );
  }

  return (
    <section className="page profile">
      <h1>Profil</h1>
      <ul className="profile-facts">
        <li><strong>Nama:</strong> {user.displayName || '—'}</li>
        <li><strong>Email:</strong> {user.email}</li>
        <li><strong>Role:</strong> {user.role}</li>
      </ul>

      <div className="sync-box">
        <h2>Sinkronisasi data lokal</h2>
        <p className="hint">
          Entri lokal: {guest.library.length} · Progres lokal: {guest.progress.length} ·{' '}
          {guest.pendingSync ? 'Menunggu sinkronisasi' : 'Tersinkron'}
        </p>
        <button className="btn btn-primary" disabled={syncing || !guest.pendingSync} onClick={() => void syncGuestData()}>
          {syncing ? 'Menyinkronkan…' : 'Sinkronkan sekarang'}
        </button>
      </div>

      <button className="btn btn-ghost" onClick={logout}>Logout</button>
      {feedback && <div className="toast" role="status">{feedback}</div>}
    </section>
  );
}
