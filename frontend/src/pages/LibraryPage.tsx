import { useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/client';
import { ApiError, READING_STATUS_LABELS, READING_STATUSES } from '../api/types';
import type { LibraryItem, ReadingStatus } from '../api/types';
import { useApiFetch, userMessage } from '../api/hooks';
import { useAuth } from '../state/auth';
import { removeLibrary, upsertLibrary, useGuestState } from '../state/guestStore';
import { ErrorState } from '../components/States';

export default function LibraryPage() {
  const { user, syncGuestData, syncing } = useAuth();
  const guest = useGuestState();
  const [filter, setFilter] = useState<ReadingStatus | ''>('');
  const server = useApiFetch<LibraryItem[]>(user ? '/library' : null);
  const [feedback, setFeedback] = useState<string | null>(null);

  // Sumber data: server bila login; lokal untuk guest.
  const items = useMemo(() => {
    const raw: Array<{ malId: number; status: ReadingStatus }> = user
      ? (server.data ?? []).map((i) => ({ malId: i.externalMangaId, status: i.status }))
      : guest.library.map((e) => ({ malId: e.malId, status: e.status }));
    const filtered = filter ? raw.filter((r) => r.status === filter) : raw;
    return filtered;
  }, [user, server.data, guest.library, filter]);

  async function changeStatus(malId: number, status: ReadingStatus) {
    if (user) {
      try {
        await api.put(`/library/JIKAN/${malId}?status=${status}`);
        upsertLibrary({ malId, title: `#${malId}`, status });
        server.reload();
        setFeedback('Status diperbarui.');
      } catch (e) {
        setFeedback(e instanceof ApiError ? e.message : 'Gagal memperbarui status.');
      }
    } else {
      const existing = guest.library.find((e) => e.malId === malId);
      upsertLibrary({ malId, title: existing?.title ?? `#${malId}`, imageUrl: existing?.imageUrl, status });
    }
  }

  function remove(malId: number) {
    if (!window.confirm('Hapus manga ini dari perpustakaan?')) return;
    if (user) {
      api.del(`/library/JIKAN/${malId}`).then(() => { removeLibrary(malId); server.reload(); }).catch((e) =>
        setFeedback(e instanceof ApiError ? e.message : 'Gagal menghapus.'));
    } else {
      removeLibrary(malId);
    }
  }

  return (
    <section className="page library">
      <h1>My Library {!user && <span className="tag-local">(lokal / guest)</span>}</h1>
      {user && guest.pendingSync && (
        <div className="sync-prompt">
          Ada data lokal yang belum tersinkron.{' '}
          <button className="btn btn-primary" onClick={() => void syncGuestData()} disabled={syncing}>
            {syncing ? 'Menyinkronkan…' : 'Sinkronkan sekarang'}
          </button>
        </div>
      )}
      <div className="filter-bar">
        <label>
          Status
          <select value={filter} onChange={(e) => setFilter(e.target.value as ReadingStatus | '')}>
            <option value="">Semua</option>
            {READING_STATUSES.map((s) => <option key={s} value={s}>{READING_STATUS_LABELS[s]}</option>)}
          </select>
        </label>
      </div>

      {user && server.loading && <p className="hint">Memuat perpustakaan…</p>}
      {user && !server.loading && server.error && <ErrorState message={userMessage(server.error)} onRetry={server.reload} />}

      {!server.loading && (items.length === 0 ? (
        <div className="state-block empty" role="status">
          <div className="state-icon">📚</div>
          <h3>Perpustakaan kosong</h3>
          <p><Link to="/catalog">Jelajahi katalog</Link> untuk menambah manga.</p>
        </div>
      ) : (
        <ul className="library-list">
          {items.map((it) => {
            const meta = guest.library.find((e) => e.malId === it.malId);
            return (
              <li key={it.malId} className="library-row">
                <div className="lib-info">
                  <Link to={`/manga/${it.malId}`} className="lib-title">{meta?.title ?? `Manga #${it.malId}`}</Link>
                  <span className="badge">{READING_STATUS_LABELS[it.status]}</span>
                </div>
                <div className="lib-actions">
                  <select
                    value={it.status}
                    onChange={(e) => void changeStatus(it.malId, e.target.value as ReadingStatus)}
                    aria-label={`Ubah status ${meta?.title ?? it.malId}`}
                  >
                    {READING_STATUSES.map((s) => <option key={s} value={s}>{READING_STATUS_LABELS[s]}</option>)}
                  </select>
                  <button className="btn btn-danger" onClick={() => remove(it.malId)}>Hapus</button>
                </div>
              </li>
            );
          })}
        </ul>
      ))}
      {feedback && <div className="toast" role="status">{feedback}</div>}
    </section>
  );
}
