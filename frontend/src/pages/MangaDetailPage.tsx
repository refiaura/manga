import { useMemo, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { api } from '../api/client';
import { ApiError, READING_STATUSES, READING_STATUS_LABELS } from '../api/types';
import type { MangaDetail, ReadingSourceView } from '../api/types';
import { useApiFetch, userMessage } from '../api/hooks';
import { useAuth } from '../state/auth';
import { getLibraryEntry, removeLibrary, saveProgress, upsertLibrary, useGuestState } from '../state/guestStore';
import { ErrorState } from '../components/States';

const FALLBACK_COVER =
  'data:image/svg+xml;utf8,' +
  encodeURIComponent(
    '<svg xmlns="http://www.w3.org/2000/svg" width="320" height="450"><rect width="100%" height="100%" fill="#2a2a3a"/><text x="50%" y="50%" fill="#888" font-size="16" text-anchor="middle">No cover</text></svg>',
  );

export default function MangaDetailPage() {
  const { id } = useParams();
  const malId = Number(id);
  const valid = Number.isInteger(malId) && malId > 0;
  const { data: manga, loading, error, reload } = useApiFetch<MangaDetail>(valid ? `/manga/${malId}` : null);
  const sources = useApiFetch<ReadingSourceView[]>(valid ? `/reading-sources/JIKAN/${malId}` : null);
  const { user } = useAuth();
  const guest = useGuestState();
  const [feedback, setFeedback] = useState<string | null>(null);
  const [chapterLabel, setChapterLabel] = useState('');

  const entry = useMemo(() => (valid ? getLibraryEntry(malId) : undefined), [guest, malId, valid]);

  if (!valid) return <ErrorState message="ID manga tidak valid." />;
  if (loading) return <div className="detail-loading skeleton-detail" aria-busy="true">Memuat detail…</div>;
  if (error) return <ErrorState message={userMessage(error)} onRetry={reload} />;
  if (!manga) return <ErrorState message="Data manga tidak tersedia." onRetry={reload} />;

  async function setStatus(status: (typeof READING_STATUSES)[number]) {
    const payload = { provider: 'JIKAN', malId: manga!.malId, title: manga!.title, imageUrl: manga!.imageUrl, status };
    if (user) {
      try {
        await api.post('/library', { provider: 'JIKAN', externalMangaId: manga!.malId, status });
        upsertLibrary({ malId: manga!.malId, title: manga!.title, imageUrl: manga!.imageUrl, status });
        setFeedback(`Tersimpan ke perpustakaan akun: ${READING_STATUS_LABELS[status]}`);
      } catch (e) {
        setFeedback(e instanceof ApiError ? e.message : 'Gagal menyimpan ke server.');
      }
    } else {
      upsertLibrary({ malId: manga!.malId, title: manga!.title, imageUrl: manga!.imageUrl, status });
      setFeedback(`Disimpan lokal (${READING_STATUS_LABELS[status]}). Login untuk sinkronisasi antar perangkat.`);
    }
    void payload;
  }

  function removeFromLibrary() {
    if (user) {
      api.del(`/library/JIKAN/${manga!.malId}`)
        .then(() => {
          removeLibrary(manga!.malId);
          setFeedback('Dihapus dari perpustakaan.');
        })
        .catch((e) => setFeedback(e instanceof ApiError ? e.message : 'Gagal menghapus.'));
    } else {
      removeLibrary(manga!.malId);
      setFeedback('Dihapus dari daftar lokal.');
    }
  }

  function persistProgress() {
    const label = chapterLabel.trim();
    if (!label) {
      setFeedback('Isi nomor/judul bab terlebih dahulu.');
      return;
    }
    // Jikan TIDAK menyediakan bab/halaman — progres dicatat manual berbasis label bab.
    if (user) {
      api
        .post('/progress', { provider: 'JIKAN', externalMangaId: manga!.malId, chapterLabel: label, lastReadAt: new Date().toISOString() })
        .then(() => {
          saveProgress({ malId: manga!.malId, chapterLabel: label });
          setFeedback('Progres tersimpan di server.');
        })
        .catch((e) => setFeedback(e instanceof ApiError ? e.message : 'Gagal menyimpan progres.'));
    } else {
      saveProgress({ malId: manga!.malId, chapterLabel: label });
      setFeedback('Progres tersimpan lokal.');
    }
  }

  const titles = manga.titles?.filter((t) => t.title && t.title !== manga.title) ?? [];

  return (
    <section className="page detail">
      <nav className="crumbs"><Link to="/catalog">← Kembali ke katalog</Link></nav>
      <div className="detail-grid">
        <div className="detail-cover">
          <img
            src={manga.imageUrl || manga.smallImageUrl || FALLBACK_COVER}
            alt={manga.title}
            onError={(e) => { (e.currentTarget as HTMLImageElement).src = FALLBACK_COVER; }}
          />
        </div>
        <div className="detail-info">
          <h1>{manga.title}</h1>
          {manga.titleEnglish && <p className="subtitle">{manga.titleEnglish}</p>}
          <ul className="facts">
            <li><strong>Tipe:</strong> {manga.type ?? '—'}</li>
            <li><strong>Status:</strong> {manga.status ?? '—'}</li>
            <li><strong>Skor:</strong> {manga.score != null ? `${manga.score} (${manga.scoredBy ?? 0} suara)` : '—'}</li>
            <li><strong>Bab terdaftar (MAL):</strong> {manga.chapters ?? '—'} <span className="note">(metadata saja, bukan daftar bab baca)</span></li>
            <li><strong>Volume:</strong> {manga.volumes ?? '—'}</li>
            <li><strong>Tahun:</strong> {manga.year ?? '—'}</li>
          </ul>
          {manga.genres && manga.genres.length > 0 && (
            <div className="genre-chips">
              {manga.genres.map((g) => <Link key={g.malId} to={`/catalog?genres=${g.malId}`} className="chip">{g.name}</Link>)}
            </div>
          )}
          {titles.length > 0 && (
            <div className="alt-titles">
              <strong>Judul lain:</strong>
              <ul>{titles.slice(0, 5).map((t, i) => <li key={i}>{t.type}: {t.title}</li>)}</ul>
            </div>
          )}
          <p className="synopsis">{manga.synopsis || 'Sinopsis tidak tersedia.'}</p>
          {manga.url && <a className="external-link" href={manga.url} target="_blank" rel="noopener noreferrer">Lihat di MyAnimeList ↗</a>}

          <div className="library-box">
            <h2>Daftar Baca {entry ? `— ${READING_STATUS_LABELS[entry.status]}` : ''}</h2>
            <div className="status-buttons">
              {READING_STATUSES.map((s) => (
                <button key={s} className={`btn ${entry?.status === s ? 'btn-primary' : ''}`} onClick={() => setStatus(s)}>
                  {READING_STATUS_LABELS[s]}
                </button>
              ))}
            </div>
            {entry && (
              <button className="btn btn-danger" onClick={removeFromLibrary}>Hapus dari daftar</button>
            )}
            {!user && <p className="hint">Tanpa login, data disimpan di perangkat ini (localStorage).</p>}
          </div>

          <div className="progress-box">
            <h2>Catatan Progres Membaca</h2>
            <p className="hint">
              MVP ini tidak menampilkan isi bab (tidak ada sumber gambar berizin). Catat kemajuan Anda secara manual.
            </p>
            <div className="progress-form">
              <input
                value={chapterLabel}
                onChange={(e) => setChapterLabel(e.target.value)}
                placeholder="cth. Bab 12 / Chapter 12"
                maxLength={100}
              />
              <button className="btn btn-primary" onClick={persistProgress}>Simpan progres</button>
            </div>
          </div>

          <div className="sources-box">
            <h2>Sumber Bacaan</h2>
            {sources.loading && <p className="hint">Memuat sumber…</p>}
            {!sources.loading && (sources.data?.length ? (
              <ul className="source-list">
                {sources.data.filter((s) => s.active).map((s) => (
                  <li key={s.id}>
                    <a href={s.sourceUrl} target="_blank" rel="noopener noreferrer">{s.sourceName} ↗</a>
                    <span className={`badge ${s.verificationStatus}`}>{s.verificationStatus}</span>
                  </li>
                ))}
              </ul>
            ) : (
              <p className="hint">
                Belum ada sumber bacaan terverifikasi untuk manga ini. Kami hanya menautkan penyedia resmi/berizin —
                tidak ada scraping atau hosting ulang konten.
              </p>
            ))}
          </div>

          {feedback && <div className="toast" role="status">{feedback}</div>}
        </div>
      </div>
    </section>
  );
}
