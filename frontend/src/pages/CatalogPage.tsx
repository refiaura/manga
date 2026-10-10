import { useEffect, useMemo, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useApiFetch, userMessage } from '../api/hooks';
import type { MangaSummary } from '../api/types';
import MangaCard from '../components/MangaCard';
import FilterBar, { buildCatalogQuery, type CatalogFilters } from '../components/FilterBar';
import PaginationBar from '../components/PaginationBar';
import { EmptyState, ErrorState, SkeletonGrid } from '../components/States';

const EMPTY_FILTERS: CatalogFilters = { genres: '', type: '', status: '', orderBy: '', sort: '' };

export default function CatalogPage() {
  const [params, setParams] = useSearchParams();
  const q = params.get('q') ?? '';
  const page = Math.max(1, Number(params.get('page') ?? '1') || 1);
  const [filters, setFilters] = useState<CatalogFilters>(() => ({
    genres: params.get('genres') ?? '',
    type: params.get('type') ?? '',
    status: params.get('status') ?? '',
    orderBy: params.get('orderBy') ?? '',
    sort: params.get('sort') ?? '',
  }));

  // Sinkronkan filter ke URL agar state dapat dibagikan/di-refresh.
  useEffect(() => {
    const next = new URLSearchParams();
    if (q) next.set('q', q);
    Object.entries(filters).forEach(([k, v]) => { if (v) next.set(k, v); });
    if (page > 1) next.set('page', String(page));
    setParams(next, { replace: true });
  }, [q, filters, page, setParams]);

  const path = useMemo(() => buildCatalogQuery(q, filters, page), [q, filters, page]);
  const { data, pagination, loading, error, reload } = useApiFetch<MangaSummary[]>(path);

  const goPage = (p: number) => {
    const next = new URLSearchParams(params);
    next.set('page', String(p));
    setParams(next);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  return (
    <section className="page catalog">
      <h1>{q ? `Hasil pencarian: “${q}”` : 'Katalog Manga'}</h1>
      <FilterBar value={filters} onChange={(f) => setFilters(f)} />
      {loading && <SkeletonGrid />}
      {!loading && error && <ErrorState message={userMessage(error)} onRetry={reload} />}
      {!loading && !error && (data?.length ? (
        <>
          <div className="manga-grid">
            {data.map((m) => <MangaCard key={m.malId} manga={m} />)}
          </div>
          <PaginationBar pagination={pagination} page={page} onPage={goPage} />
        </>
      ) : (
        <EmptyState
          title="Tidak ada hasil"
          hint={q ? 'Coba kata kunci lain atau ubah filter.' : 'Ubah filter untuk melihat hasil.'}
        />
      ))}
    </section>
  );
}
