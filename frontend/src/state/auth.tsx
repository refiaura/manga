// Konteks autentikasi: token JWT di memori + sessionStorage (model keamanan SPA),
// sinkronisasi guest→akun setelah login/register (PRD §6, §7).
import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import type { ReactNode } from 'react';
import { api, getAuthToken, setAuthToken } from '../api/client';
import type { LoginResult, SyncResult, UserMe } from '../api/types';
import { buildSyncPayload, getState, markSynced } from './guestStore';

const SESSION_KEY = 'manga-reader.session.v1';

interface AuthContextValue {
  user: UserMe | null;
  ready: boolean;
  syncing: boolean;
  syncMessage: string | null;
  login(email: string, password: string): Promise<void>;
  register(email: string, displayName: string, password: string): Promise<void>;
  logout(): void;
  syncGuestData(): Promise<void>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

function loadSession(): UserMe | null {
  try {
    const raw = sessionStorage.getItem(SESSION_KEY);
    if (!raw) return null;
    const u = JSON.parse(raw);
    if (typeof u?.userId === 'string' && typeof u?.email === 'string') return u as UserMe;
    return null;
  } catch {
    return null;
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserMe | null>(() => loadSession());
  const [ready, setReady] = useState(false);
  const [syncing, setSyncing] = useState(false);
  const [syncMessage, setSyncMessage] = useState<string | null>(null);

  // Pulihkan sesi: validasi token ke server saat mount.
  useEffect(() => {
    let cancelled = false;
    async function restore() {
      const token = getAuthToken();
      if (!token) {
        setReady(true);
        return;
      }
      try {
        const res = await api.get<UserMe>('/users/me');
        if (!cancelled && res.data) {
          setUser(res.data);
          sessionStorage.setItem(SESSION_KEY, JSON.stringify(res.data));
        }
      } catch {
        setAuthToken(null);
        sessionStorage.removeItem(SESSION_KEY);
        if (!cancelled) setUser(null);
      } finally {
        if (!cancelled) setReady(true);
      }
    }
    restore();
    return () => {
      cancelled = true;
    };
  }, []);

  const runSession = useCallback(async (result: LoginResult) => {
    const me: UserMe = {
      userId: result.userId,
      email: result.email,
      displayName: result.displayName,
      role: result.role,
    };
    setAuthToken(result.token);
    sessionStorage.setItem(SESSION_KEY, JSON.stringify(me));
    setUser(me);
    // Sinkronkan data guest segera setelah sesi aktif (PRD §7).
    void syncGuest();
    async function syncGuest() {
      const state = getState();
      if (!state.pendingSync || (state.library.length === 0 && state.progress.length === 0)) return;
      setSyncing(true);
      setSyncMessage('Menyinkronkan daftar baca lokal…');
      try {
        const payload = buildSyncPayload();
        const res = await api.post<SyncResult>('/sync/guest', payload);
        if (res.data) {
          markSynced(res.data);
          const skipped = res.data.skippedCount ?? 0;
          setSyncMessage(
            `Sinkronisasi selesai: ${res.data.librarySynced} entri perpustakaan` +
              `, ${res.data.progressSynced} progres` +
              (skipped > 0 ? `, ${skipped} item dilewati` : '') +
              '.',
          );
        }
      } catch (e: any) {
        // Jangan hapus data lokal bila server gagal — pendingSync tetap true (retry otomatis pada login berikutnya).
        setSyncMessage(`Sinkronisasi gagal (${e?.message ?? 'error'}). Data lokal disimpan, coba lagi nanti.`);
      } finally {
        setSyncing(false);
      }
    }
  }, []);

  const login = useCallback(
    async (email: string, password: string) => {
      const res = await api.post<LoginResult>('/auth/login', { email, password });
      if (!res.data) throw new Error('Respons login tidak valid');
      await runSession(res.data);
    },
    [runSession],
  );

  const register = useCallback(
    async (email: string, displayName: string, password: string) => {
      await api.post('/auth/register', { email, displayName, password });
      const res = await api.post<LoginResult>('/auth/login', { email, password });
      if (!res.data) throw new Error('Registrasi berhasil tetapi sesi gagal dibuat');
      await runSession(res.data);
    },
    [runSession],
  );

  const logout = useCallback(() => {
    // Best-effort panggilan /auth/logout, lalu bersihkan sesi lokal.
    api.post('/auth/logout').catch(() => undefined);
    setAuthToken(null);
    sessionStorage.removeItem(SESSION_KEY);
    setUser(null);
    setSyncMessage(null);
  }, []);

  const syncGuestData = useCallback(async () => {
    if (!getAuthToken()) return;
    setSyncing(true);
    try {
      const res = await api.post<SyncResult>('/sync/guest', buildSyncPayload());
      if (res.data) {
        markSynced(res.data);
        setSyncMessage(`Sinkronisasi manual selesai (${res.data.librarySynced} entri).`);
      }
    } catch (e: any) {
      setSyncMessage(`Sinkronisasi gagal: ${e?.message ?? 'error'}`);
    } finally {
      setSyncing(false);
    }
  }, []);

  const value = useMemo(
    () => ({ user, ready, syncing, syncMessage, login, register, logout, syncGuestData }),
    [user, ready, syncing, syncMessage, login, register, logout, syncGuestData],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth harus dipakai dalam AuthProvider');
  return ctx;
}
