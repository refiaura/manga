export interface ApiResponse<T> {
  success: boolean;
  data?: T;
  error?: { code: string; message: string };
  pagination?: Pagination;
}
export interface Pagination { page: number; limit: number; total?: number; totalPages?: number; hasNext: boolean; hasPrevious: boolean; }
export interface GenreRef { malId: number; name: string; }
export interface MangaSummary {
  malId: number; title: string; titleEnglish?: string | null;
  imageUrl?: string | null; smallImageUrl?: string | null;
  synopsis?: string | null; score?: number | null; scoredBy?: number | null;
  rank?: number | null; popularity?: number | null;
  type?: string | null; status?: string | null; chapters?: number | null; volumes?: number | null;
  genres?: GenreRef[]; year?: number | null; updatedAt?: string | null; url?: string | null;
}
export interface MangaDetail extends MangaSummary {
  titles?: { type: string; title: string }[];
  authors?: { malId: number; name: string }[];
  serializations?: { name: string; url: string }[];
  relatedManga?: { relation: string; entry: { malId: number; title: string; type: string } }[];
  recommendations?: { manga: MangaSummary; votes: number }[];
}
export interface LibraryItem { id: string; provider: string; externalMangaId: number; status: ReadingStatus; addedAt: string; updatedAt: string; }
export type ReadingStatus = 'READING' | 'PLAN_TO_READ' | 'COMPLETED' | 'ON_HOLD' | 'DROPPED';
export const READING_STATUSES: ReadingStatus[] = ['READING', 'PLAN_TO_READ', 'COMPLETED', 'ON_HOLD', 'DROPPED'];
export const READING_STATUS_LABELS: Record<ReadingStatus, string> = {
  READING: 'Sedang dibaca',
  PLAN_TO_READ: 'Akan dibaca',
  COMPLETED: 'Selesai',
  ON_HOLD: 'Ditunda',
  DROPPED: 'Dihentikan',
};
export interface ReadingProgress {
  id: string; provider: string; externalMangaId: number; readingSourceId?: string | null;
  chapterExternalId?: string | null; chapterLabel?: string | null; pageIndex?: number | null;
  scrollPosition?: number | null; lastReadAt: string; updatedAt: string;
}
export interface UserMe { userId: string; email: string; displayName: string; role: string; }
export interface LoginResult { userId: string; email: string; displayName: string; role: string; token: string; }
export interface ReadingSourceView {
  id: string; provider: string; externalMangaId: number; sourceName: string; sourceUrl: string;
  sourceType: string; verificationStatus: string; active: boolean;
}
export interface SyncResult {
  librarySynced: number; progressSynced: number; skippedCount: number;
  library?: LibraryItem[]; progress?: ReadingProgress[]; skipped?: string[];
}
export class ApiError extends Error {
  code: string; status: number;
  constructor(code: string, message: string, status: number) { super(message); this.code = code; this.status = status; }
}
