import { Link } from 'react-router-dom';
import type { MangaSummary } from '../api/types';

const FALLBACK_COVER =
  'data:image/svg+xml;utf8,' +
  encodeURIComponent(
    '<svg xmlns="http://www.w3.org/2000/svg" width="160" height="224"><rect width="100%" height="100%" fill="#2a2a3a"/><text x="50%" y="50%" fill="#888" font-size="14" text-anchor="middle" dominant-baseline="middle">No cover</text></svg>',
  );

export default function MangaCard({ manga }: { manga: MangaSummary }) {
  const title = manga.titleEnglish || manga.title;
  return (
    <Link to={`/manga/${manga.malId}`} className="manga-card">
      <div className="cover-wrap">
        <img
          src={manga.imageUrl || manga.smallImageUrl || FALLBACK_COVER}
          alt={title}
          loading="lazy"
          onError={(e) => {
            (e.currentTarget as HTMLImageElement).src = FALLBACK_COVER;
          }}
        />
        {manga.score != null && <span className="badge-score">★ {manga.score.toFixed(2)}</span>}
      </div>
      <div className="card-body">
        <h3 className="card-title" title={title}>{title}</h3>
        <p className="card-meta">
          {[manga.type, manga.status, manga.year != null ? String(manga.year) : null]
            .filter(Boolean)
            .join(' · ') || '—'}
        </p>
      </div>
    </Link>
  );
}
