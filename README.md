# Manga Reader MVP

Aplikasi web katalog & daftar baca manga. Metadata bersumber dari **Jikan API v4**
yang diambil **melalui backend** (frontend tidak pernah memanggil Jikan langsung),
dengan dukungan mode tamu (localStorage) dan akun pengguna (PostgreSQL + JWT).

## Arsitektur Singkat

```
Browser (React SPA)
   │  fetch /api/v1/*  (JWT untuk endpoint privat)
   ▼
Spring Boot Backend ──► PostgreSQL (Flyway migrations V1–V4)
   │  abstraksi MangaMetadataProvider
   ▼
Jikan API v4  (Caffeine cache + rate limiter + timeout + penanganan 429/5xx)
```

- **Frontend**: React 18 + TypeScript + Vite. State: context autentikasi (JWT)
  dan guest store berversi di `localStorage`. Data fetching via hook `useApiFetch`.
- **Backend**: Java 17 + Spring Boot (Web, Data JPA, Security, Validation),
  Flyway, Caffeine cache, JWT (jjwt), springdoc OpenAPI.
- **Reader penuh tidak tersedia di MVP** — sumber bacaan hanya tautan resmi/terverifikasi
  (modul `reading_sources`). Tidak ada scraping/proxy konten.

## Prasyarat

- JDK 17+ dan Maven 3.8+
- Node.js 18+ dan npm
- PostgreSQL 14+ (atau Docker)

## 1. Menyiapkan Database

Menggunakan Docker (paling mudah):

```bash
docker run -d --name manga-pg -e POSTGRES_PASSWORD=devpass \
  -e POSTGRES_DB=manga_reader -p 5432:5432 postgres:16
```

Atau buat database manual lalu sesuaikan variabel env:

```sql
CREATE DATABASE manga_reader;
```

Migration dijalankan **otomatis oleh Flyway** saat backend start
(`backend/src/main/resources/db/migration/V1__create_users.sql` … `V4__create_reading_progress.sql`).
Tidak perlu perintah terpisah; untuk reset total: drop schema publik lalu restart backend.

## 2. Menjalankan Backend

```bash
cd backend
export DB_PASSWORD=<password-anda>
export APP_JWT_SECRET=$(openssl rand -base64 48)   # wajib; lihat .env.example
mvn spring-boot:run
```

- API: `http://localhost:8080/api/v1/...`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Health check: `GET /actuator/health`

## 3. Menjalankan Frontend

```bash
cd frontend
npm install
echo "VITE_API_BASE_URL=http://localhost:8080" > .env
npm run dev            # http://localhost:5173 (proxy /api sudah dikonfigurasi di vite.config.ts)
npm run build          # build produksi ke dist/
```

## 4. Menjalankan Test

```bash
# Backend (unit tests: mapper Jikan, validasi query, merge sync, dsb.)
cd backend && mvn test

# Frontend (type-check + build sebagai gate kualitas)
cd frontend && npx tsc --noEmit && npm run build
```

## Konfigurasi Environment

Lihat **`.env.example`** di root (backend) dan `frontend/.env` (Vite). Variabel utama:

| Variabel | Default | Keterangan |
|---|---|---|
| `SERVER_PORT` | 8080 | Port backend |
| `DB_HOST/DB_PORT/DB_NAME/DB_USERNAME/DB_PASSWORD` | localhost/5432/manga_reader/postgres/— | Koneksi PostgreSQL |
| `APP_JWT_SECRET` | (kosong, wajib diisi) | Secret HMAC JWT, min. 256 bit |
| `APP_JWT_EXPIRATION_MINUTES` | 720 | Umur token |
| `JIKAN_BASE_URL` | https://api.jikan.moe/v4 | Provider metadata |
| `JIKAN_TIMEOUT_SECONDS` | 10 | Timeout request eksternal |
| `VITE_API_BASE_URL` | http://localhost:8080 | Base URL API dari frontend |

Jangan pernah meng-commit `.env` asli — sudah masuk `.gitignore`.

## Endpoint API Utama (`/api/v1`)

| Method & Path | Auth | Fungsi |
|---|---|---|
| `GET /manga?q=&genres=&type=&status=&order_by=&page=&limit=` | Publik | Katalog + pencarian (via cache → Jikan) |
| `GET /manga/top?filter=` | Publik | Top manga |
| `GET /manga/{id}` | Publik | Detail manga |
| `GET /genres` | Publik | Daftar genre |
| `POST /auth/register`, `POST /auth/login`, `POST /auth/logout` | Publik | Autentikasi JWT |
| `GET /users/me`, `DELETE /users/me` | JWT | Profil pengguna |
| `GET /library`, `POST /library`, `PUT /library/{provider}/{externalMangaId}`, `DELETE /library/{provider}/{externalMangaId}` | JWT | My Library (status: READING, PLAN_TO_READ, COMPLETED, ON_HOLD, DROPPED) |
| `PATCH /library/{id}`, `GET /library?status=` | JWT | Ubah status / filter per status |
| `GET/PUT /reading-progress/{provider}/{externalMangaId}` | JWT | Simpan/ambil progres bab terakhir |
| `GET /progress/history` | JWT | Riwayat membaca |
| `POST /sync` (+ `/guest`, `/guest-library`) | JWT | Sinkronisasi data guest → akun (merge newer-wins, idempotent) |
| `GET /reading-sources/{provider}/{externalMangaId}` | Publik | Sumber baca (tautan resmi saja) |
| `POST/PATCH /admin/reading-sources...` | Admin | Kelola sumber baca |

Format respons konsisten: `{ data, pagination?, error? }` dengan HTTP status yang tepat.

## Penanganan Error Umum

- `400 VALIDATION_ERROR` — parameter query/body tidak valid (mis. `page < 1`, `limit > 50`).
- `401 UNAUTHENTICATED` — token hilang/kedaluwarsa → frontend mengarahkan ke login.
- `409` — duplikat (email terdaftar, entri library ganda).
- `502 PROVIDER_ERROR` — Jikan gagal (timeout/5xx). Frontend menampilkan state error
  dengan tombol retry; cache dipakai bila masih layak (TTL Caffeine).
- `429` dari Jikan → diretraksi terbatas oleh backend; permintaan berlebih dari klien
  dibatasi rate limiter internal.

## Batasan Jikan & Sumber Bacaan

- Jikan **tidak menyediakan halaman/gambar bab** — karena itu reader internal tidak dibuat;
  aplikasi hanya menampilkan metadata, cover, dan tautan penyedia resmi.
- Hormati rate limit Jikan (±3 req/detik, 60/menit): semua request lewat backend + cache.
- Field Jikan bisa null/kosong; mapper menormalkannya dan UI punya fallback cover.

## Fitur yang Belum Tersedia (MVP)

- Reader internal (butuh sumber gambar berizin).
- Test E2E otomatis (Playwright/Cypress) — alur diverifikasi manual.
- Refresh token / rotasi sesi (logout saat ini client-side + expiry).
- Rekomendasi personal & notifikasi rilis bab baru.
