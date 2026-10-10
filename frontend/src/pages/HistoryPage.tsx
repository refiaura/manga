import { Link } from 'react-router-dom';
import { useApiFetch, userMessage } from '../api/hooks';
import type { ReadingProgress } from '../api/types';
import { useAuth } from '../state/auth';
import { useGuestState } from '../state/guestStore';
import { ErrorState } from '../components/States';

function fmt(iso: string): string {
  const d = new Date(iso);
  return Number.isNaN(d.getTime()) ? '—' : d.toLocaleString('id-ID');
}

export default function HistoryPage() {
  const { user } = useAuth();
  const guest = useGuestState();
  const server = useApiFetch<ReadingProgress[]>(user ? '/progress/history?limit=50' : null);

  const rows = user
    ? (server.data ?? []).map((p) => ({
        malId: p.externalMangaId,
        label: p.chapterLabel ?? p.chapterExternalId ?? (p.pageIndex != null ? `Halaman ${p.pageIndex}` : '—'),
        at: p.lastReadAt,
      }))
    : guest.progress.map((p) => ({ malId: p.malId, label: p.chapterLabel ?? p.chapterExternalId ?? (p.pageIndex != null ? `Halaman ${p.pageIndex}` : '—'), at: p.lastReadAt }));

  return (
    <section className="page history">
      <h1>Riwayat / Lanjutkan Membaca {!user && <span className="tag-local">(lokal)</span>}</h1>
      {user && server.loading && <p className="hint">Memuat riwayat…</p>}
      {user && !server.loading && server.error && <ErrorState message={userMessage(server.error)} onRetry={server.reload} />}
      {!user && rows.length === 0 && (
        <div className="state-block empty" role="status">
          <div className="state-icon">🕐</div>
          <h3>Belum ada riwayat</h3>
          <p>Simpan progres membaca dari halaman detail manga. <Link to="/catalog">Buka katalog</Link>.</p>
        </div>
      )}
      {rows.length > 0 && (
        <ul className="history-list">
          {rows.map((r, i) => (
            <li key={`${r.malId}-${i}`} className="history-row">
              <Link to={`/manga/${r.malId}`}>Manga #{r.malId}</Link>
              <span className="hist-label">{r.label}</span>
              <span className="hist-time">{fmt(r.at)}</span>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
