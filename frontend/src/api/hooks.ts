import { useCallback, useEffect, useState } from 'react';
import { api } from './client';
import { ApiError } from './types';
import type { ApiResponse } from './types';

export interface FetchState<T> {
  data: T | undefined;
  pagination: ApiResponse['pagination'];
  loading: boolean;
  error: ApiError | null;
  reload(): void;
}

/**
 * Hook fetch generik dengan guard pembatalan (StrictMode-safe) dan retry manual.
 * path === null → tidak melakukan request sama sekali.
 */
export function useApiFetch<T>(path: string | null): FetchState<T> {
  const [data, setData] = useState<T | undefined>(undefined);
  const [pagination, setPagination] = useState<ApiResponse['pagination']>(undefined);
  const [loading, setLoading] = useState(path !== null);
  const [error, setError] = useState<ApiError | null>(null);
  const [tick, setTick] = useState(0);

  useEffect(() => {
    if (path === null) {
      setLoading(false);
      return;
    }
    let cancelled = false;
    setLoading(true);
    setError(null);
    api
      .get<T>(path)
      .then((res: ApiResponse<T>) => {
        if (!cancelled) {
          setData(res.data);
          setPagination(res.pagination);
        }
      })
      .catch((e: unknown) => {
        if (!cancelled && e instanceof ApiError) setError(e);
        else if (!cancelled) setError(new ApiError('UNKNOWN', 'Kesalahan tak terduga', 0));
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [path, tick]);

  const reload = useCallback(() => setTick((t) => t + 1), []);
  return { data, pagination, loading, error, reload };
}

/** Error message ramah pengguna dari ApiError. */
export function userMessage(e: ApiError | null | undefined): string {
  if (!e) return '';
  switch (e.code) {
    case 'RATE_LIMITED':
    case 'UPSTREAM_RATE_LIMIT':
      return 'Terlalu banyak permintaan ke penyedia data. Silakan tunggu sebentar lalu coba lagi.';
    case 'UPSTREAM_ERROR':
    case 'PROVIDER_UNAVAILABLE':
      return 'Sumber data manga sedang tidak tersedia. Coba lagi beberapa saat.';
    case 'NETWORK_ERROR':
      return 'Tidak dapat menghubungi server. Pastikan backend berjalan.';
    case 'NOT_FOUND':
      return 'Data tidak ditemukan.';
    default:
      return e.message || 'Terjadi kesalahan.';
  }
}
