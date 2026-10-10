import { Link } from 'react-router-dom';

export default function NotFoundPage() {
  return (
    <section className="page notfound">
      <div className="state-block empty" role="status">
        <div className="state-icon">🧭</div>
        <h1>404 — Halaman tidak ditemukan</h1>
        <p>Halaman yang Anda cari tidak ada atau telah dipindahkan.</p>
        <p><Link to="/" className="btn btn-primary">Kembali ke Beranda</Link></p>
      </div>
    </section>
  );
}
