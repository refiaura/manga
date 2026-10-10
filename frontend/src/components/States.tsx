export function SkeletonGrid({ count = 12 }: { count?: number }) {
  return (
    <div className="manga-grid" aria-busy="true" aria-label="Memuat katalog">
      {Array.from({ length: count }, (_, i) => (
        <div className="manga-card skeleton" key={i}>
          <div className="cover-wrap shimmer" />
          <div className="card-body">
            <div className="line shimmer" style={{ width: '80%' }} />
            <div className="line shimmer" style={{ width: '50%' }} />
          </div>
        </div>
      ))}
    </div>
  );
}

export function EmptyState({ title, hint }: { title: string; hint?: string }) {
  return (
    <div className="state-block empty" role="status">
      <div className="state-icon">🔍</div>
      <h3>{title}</h3>
      {hint && <p>{hint}</p>}
    </div>
  );
}

export function ErrorState({ message, onRetry }: { message: string; onRetry?: () => void }) {
  return (
    <div className="state-block error" role="alert">
      <div className="state-icon">⚠️</div>
      <h3>Terjadi kesalahan</h3>
      <p>{message}</p>
      {onRetry && (
        <button className="btn btn-primary" onClick={onRetry}>Coba lagi</button>
      )}
    </div>
  );
}
