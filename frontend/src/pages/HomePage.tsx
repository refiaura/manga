import { useApiFetch, userMessage } from '../api/hooks';
import type { MangaSummary } from '../api/types';
import MangaCard from '../components/MangaCard';
import { EmptyState, ErrorState, SkeletonGrid } from '../components/States';
import { Link } from 'react-router-dom';

export default function HomePage() {
  const { data, loading, error, reload } = useApiFetch<MangaSummary[]>('/manga/top?limit=12');

  return (
    <section className="page home">
      <div className="hero">
        <h1>Jelajahi Manga</h1>
        <p>Katalog metadata manga dari MyAnimeList via Jikan API. Simpan daftar baca Anda — tanpa maupun dengan akun.</p>
        <Link to="/catalog" className="btn btn-primary">Buka Katalog</Link>
      </div>
      <h2 className="section-title">Manga Terpopuler</h2>
      {loading && <SkeletonGrid />}
      {!loading && error && <ErrorState message={userMessage(error)} onRetry={reload} />}
      {!loading && !error && (data?.length ? (
        <div className="manga-grid">
          {data.map((m) => <MangaCard key={m.malId} manga={m} />)}
        </div>
      ) : (
        <EmptyState title="Belum ada data" hint="Sumber data sedang tidak tersedia. Coba lagi nanti." />
      ))}
    </section>
  );
}
