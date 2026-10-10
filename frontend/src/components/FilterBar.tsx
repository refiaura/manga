import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/client';
import type { GenreRef, MangaSummary } from '../api/types';

/** Ambil genre dari katalog; fallback ke daftar umum Jikan bila endpoint gagal. */
const FALLBACK_GENRES: GenreRef[] = [
  { malId: 4, name: 'Action' }, { malId: 2, name: 'Adventure' }, { malId: 8, name: 'Comedy' },
  { malId: 10, name: 'Fantasy' }, { malId: 37, name: 'Supernatural' }, { malId: 41, name: 'Thriller' },
  { malId: 24, name: 'Gore' }, { malId: 62, name: 'Psychological' }, { malId: 79, name: 'Romance' },
  { malId: 42, name: 'Mystery' }, { malId: 14, name: 'Horror' }, { malId: 7, name: 'Martial Arts' },
];

interface Props {
  value: { genres: string; type: string; status: string; orderBy: string; sort: string };
  onChange(v: Props['value']): void;
}

export default function FilterBar({ value, onChange }: Props) {
  const [genres, setGenres] = useState<GenreRef[]>(FALLBACK_GENRES);
  useEffect(() => {
    let cancelled = false;
    api.get<GenreRef[]>('/genres').then((r) => {
      if (!cancelled && r.data && r.data.length > 0) setGenres(r.data);
    }).catch(() => undefined); // fallback tetap dipakai
    return () => { cancelled = true; };
  }, []);

  const set = (k: keyof Props['value'], v: string) => onChange({ ...value, [k]: v });

  return (
    <div className="filter-bar">
      <label>
        Genre
        <select value={value.genres} onChange={(e) => set('genres', e.target.value)}>
          <option value="">Semua</option>
          {genres.map((g) => <option key={g.malId} value={g.malId}>{g.name}</option>)}
        </select>
      </label>
      <label>
        Tipe
        <select value={value.type} onChange={(e) => set('type', e.target.value)}>
          <option value="">Semua</option>
          <option value="manga">Manga</option>
          <option value="manhwa">Manhwa</option>
          <option value="manhua">Manhua</option>
          <option value="one_shot">One Shot</option>
          <option value="doujin">Doujin</option>
        </select>
      </label>
      <label>
        Status
        <select value={value.status} onChange={(e) => set('status', e.target.value)}>
          <option value="">Semua</option>
          <option value="publishing">Sedang terbit</option>
          <option value="finished">Selesai</option>
          <option value="upcoming">Akan datang</option>
        </select>
      </label>
      <label>
        Urutkan
        <select value={value.orderBy} onChange={(e) => set('orderBy', e.target.value)}>
          <option value="">Skor (default)</option>
          <option value="members">Jumlah anggota</option>
          <option value="favorites">Favorit</option>
          <option value="title">Judul</option>
          <option value="score">Skor</option>
        </select>
      </label>
      <label>
        Arah
        <select value={value.sort} onChange={(e) => set('sort', e.target.value)}>
          <option value="">Desc</option>
          <option value="asc">Asc</option>
        </select>
      </label>
      <Link to="/catalog" className="btn btn-ghost" onClick={() => onChange({ genres: '', type: '', status: '', orderBy: '', sort: '' })}>
        Reset
      </Link>
    </div>
  );
}

export function buildCatalogQuery(
  q: string, filters: Props['value'], page: number, limit = 12,
): string {
  const p = new URLSearchParams();
  if (q) p.set('q', q);
  if (filters.genres) p.set('genres', filters.genres);
  if (filters.type) p.set('type', filters.type);
  if (filters.status) p.set('status', filters.status);
  if (filters.orderBy) p.set('orderBy', filters.orderBy);
  if (filters.sort) p.set('sort', filters.sort);
  p.set('page', String(page));
  p.set('limit', String(limit));
  return `/manga?${p.toString()}`;
}

export type CatalogFilters = Props['value'];
export type CatalogManga = MangaSummary;
