import { ApiResponse, ApiError } from './types';

const BASE = (import.meta as any).env?.VITE_API_BASE ?? '/api/v1';
let authToken: string | null = null;
export function setAuthToken(t: string | null) { authToken = t; }
export function getAuthToken() { return authToken; }

async function request<T>(path: string, init: RequestInit = {}): Promise<ApiResponse<T>> {
  const headers: Record<string, string> = { 'Content-Type': 'application/json', ...(init.headers as any) };
  if (authToken) headers['Authorization'] = `Bearer ${authToken}`;
  let res: Response;
  try {
    res = await fetch(`${BASE}${path}`, { ...init, headers });
  } catch {
    throw new ApiError('NETWORK_ERROR', 'Tidak dapat menghubungi server. Periksa koneksi Anda.', 0);
  }
  if (res.status === 204) return { success: true } as ApiResponse<T>;
  let body: ApiResponse<T>;
  try { body = await res.json(); } catch { throw new ApiError('INVALID_RESPONSE', 'Respons server tidak valid.', res.status); }
  if (!res.ok || body.success === false) {
    const code = body.error?.code ?? `HTTP_${res.status}`;
    const msg = body.error?.message ?? 'Terjadi kesalahan pada server.';
    throw new ApiError(code, msg, res.status);
  }
  return body;
}

export const api = {
  get: <T>(p: string) => request<T>(p),
  post: <T>(p: string, body?: unknown) => request<T>(p, { method: 'POST', body: body === undefined ? undefined : JSON.stringify(body) }),
  put: <T>(p: string, body?: unknown) => request<T>(p, { method: 'PUT', body: body === undefined ? undefined : JSON.stringify(body) }),
  patch: <T>(p: string, body?: unknown) => request<T>(p, { method: 'PATCH', body: body === undefined ? undefined : JSON.stringify(body) }),
  del: <T>(p: string) => request<T>(p, { method: 'DELETE' }),
};
