import type { Pagination } from '../api/types';

interface Props {
  pagination?: Pagination;
  page: number;
  onPage(p: number): void;
}

/** Pagination sederhana dengan info halaman — backend membatasi limit ≤ 25. */
export default function PaginationBar({ pagination, page, onPage }: Props) {
  if (!pagination) return null;
  const total = pagination.totalPages ?? 0;
  return (
    <nav className="pagination" aria-label="Navigasi halaman">
      <button className="btn" disabled={!pagination.hasPrevious} onClick={() => onPage(page - 1)}>
        ← Sebelumnya
      </button>
      <span className="page-info">
        Halaman {pagination.page}
        {total > 0 ? ` dari ${Math.min(total, 1000)}` : ''}
        {pagination.total != null && pagination.total > 0 ? ` (${pagination.total} hasil)` : ''}
      </span>
      <button className="btn" disabled={!pagination.hasNext} onClick={() => onPage(page + 1)}>
        Berikutnya →
      </button>
    </nav>
  );
}
