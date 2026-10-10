// Simpanan lokal guest (PRD §6 Guest Mode) — skema terversi, validasi aman.
import { useSyncExternalStore } from 'react';
import type { ReadingStatus, SyncResult } from '../api/types';

export const GUEST_SCHEMA_VERSION = 1;
const STORAGE_KEY = 'manga-reader.guest.v1';

export interface GuestLibraryEntry {
  malId: number;
  title: string;
  imageUrl?: string | null;
  status: ReadingStatus;
  updatedAt: string; // ISO-8601
}

export interface GuestProgressEntry {
  malId: number;
  chapterExternalId?: string | null;
  chapterLabel?: string | null;
  pageIndex?: number | null;
  lastReadAt: string; // ISO-8601
}

export interface GuestState {
  schemaVersion: number;
  library: GuestLibraryEntry[];
  progress: GuestProgressEntry[];
  /** true bila data lokal belum disinkronkan ke akun */
  pendingSync: boolean;
}

export const EMPTY_STATE: GuestState = {
  schemaVersion: GUEST_SCHEMA_VERSION,
  library: [],
  progress: [],
  pendingSync: false,
};

const STATUSES: ReadingStatus[] = ['READING', 'PLAN_TO_READ', 'COMPLETED', 'ON_HOLD', 'DROPPED'];

function isValidIso(s: unknown): s is string {
  return typeof s === 'string' && !Number.isNaN(Date.parse(s));
}

/** Validasi data localStorage sebelum dipakai; data rusak → state kosong (aman). */
export function parseGuestState(raw: string | null): GuestState {
  if (!raw) return { ...EMPTY_STATE };
  try {
    const obj = JSON.parse(raw);
    if (typeof obj !== 'object' || obj === null) return { ...EMPTY_STATE };
    if (obj.schemaVersion !== GUEST_SCHEMA_VERSION) return { ...EMPTY_STATE };
    const library: GuestLibraryEntry[] = Array.isArray(obj.library)
      ? obj.library.filter(
          (e: any) =>
            Number.isInteger(e?.malId) && e.malId > 0 &&
            typeof e?.title === 'string' && e.title.length <= 300 &&
            STATUSES.includes(e?.status),
        )
      : [];
    const progress: GuestProgressEntry[] = Array.isArray(obj.progress)
      ? obj.progress.filter(
          (e: any) => Number.isInteger(e?.malId) && e.malId > 0 && isValidIso(e?.lastReadAt),
        )
      : [];
    return {
      schemaVersion: GUEST_SCHEMA_VERSION,
      library,
      progress,
      pendingSync: Boolean(obj.pendingSync),
    };
  } catch {
    return { ...EMPTY_STATE };
  }
}

type Listener = () => void;
const listeners = new Set<Listener>();

let state: GuestState = parseGuestState(
  typeof localStorage !== 'undefined' ? localStorage.getItem(STORAGE_KEY) : null,
);

function persist() {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
  } catch {
    /* storage penuh / mode privat — abaikan, data tetap di memori */
  }
  listeners.forEach((l) => l());
}

export function getState(): GuestState {
  return state;
}

export function subscribe(l: Listener): () => void {
  listeners.add(l);
  return () => listeners.delete(l);
}

/** Tambah/ubah entri perpustakaan. Upsert berdasarkan malId. */
export function upsertLibrary(entry: Omit<GuestLibraryEntry, 'updatedAt'>): void {
  const now = new Date().toISOString();
  const idx = state.library.findIndex((e) => e.malId === entry.malId);
  const next = [...state.library];
  if (idx >= 0) next[idx] = { ...next[idx], ...entry, updatedAt: now };
  else next.unshift({ ...entry, updatedAt: now });
  state = { ...state, library: next, pendingSync: true };
  persist();
}

export function removeLibrary(malId: number): void {
  state = {
    ...state,
    library: state.library.filter((e) => e.malId !== malId),
    pendingSync: true,
  };
  persist();
}

export function getLibraryEntry(malId: number): GuestLibraryEntry | undefined {
  return state.library.find((e) => e.malId === malId);
}

/** Simpan progres; hanya simpan bila informasi progres valid (bab/halaman). */
export function saveProgress(entry: Omit<GuestProgressEntry, 'lastReadAt'> & { lastReadAt?: string }): void {
  const hasValidInfo = Boolean(entry.chapterExternalId || entry.chapterLabel || (entry.pageIndex != null && entry.pageIndex >= 0));
  if (!hasValidInfo) return;
  const now = entry.lastReadAt ?? new Date().toISOString();
  const next = [...state.progress];
  const idx = next.findIndex((e) => e.malId === entry.malId);
  const item: GuestProgressEntry = {
    malId: entry.malId,
    chapterExternalId: entry.chapterExternalId ?? null,
    chapterLabel: entry.chapterLabel ?? null,
    pageIndex: entry.pageIndex ?? null,
    lastReadAt: now,
  };
  if (idx >= 0) next[idx] = item;
  else next.unshift(item);
  state = { ...state, progress: next.slice(0, 200), pendingSync: true };
  persist();
}

export function getProgress(malId: number): GuestProgressEntry | undefined {
  return state.progress.find((e) => e.malId === malId);
}

/** Setelah sinkron server berhasil: tandai selesai sync (jangan hapus data — riwayat lokal berguna). */
export function markSynced(_result: SyncResult): void {
  state = { ...state, pendingSync: false };
  persist();
}

/** Payload untuk POST /sync/guest sesuai kontrak backend. */
export function buildSyncPayload() {
  return {
    schemaVersion: GUEST_SCHEMA_VERSION,
    library: state.library.map((e) => ({ provider: 'JIKAN', externalMangaId: e.malId, status: e.status })),
    progress: state.progress.map((e) => ({
      provider: 'JIKAN',
      externalMangaId: e.malId,
      chapterExternalId: e.chapterExternalId ?? null,
      chapterLabel: e.chapterLabel ?? null,
      pageIndex: e.pageIndex ?? null,
      lastReadAt: e.lastReadAt,
    })),
  };
}

export function clearGuestData(): void {
  state = { ...EMPTY_STATE };
  persist();
}

/** Hook React: re-render setiap state guest berubah. */
export function useGuestState(): GuestState {
  return useSyncExternalStore(subscribe, getState, () => EMPTY_STATE);
}
