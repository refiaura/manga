import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter, Route, Routes } from 'react-router-dom';
import Header from './components/Header';
import ErrorBoundary from './components/ErrorBoundary';
import { AuthProvider } from './state/auth';
import HomePage from './pages/HomePage';
import CatalogPage from './pages/CatalogPage';
import MangaDetailPage from './pages/MangaDetailPage';
import LibraryPage from './pages/LibraryPage';
import HistoryPage from './pages/HistoryPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import ProfilePage from './pages/ProfilePage';
import NotFoundPage from './pages/NotFoundPage';
import './styles.css';

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <ErrorBoundary>
      <BrowserRouter>
        <AuthProvider>
          <Header />
          <main className="container">
            <Routes>
              <Route path="/" element={<HomePage />} />
              <Route path="/catalog" element={<CatalogPage />} />
              <Route path="/manga/:id" element={<MangaDetailPage />} />
              <Route path="/library" element={<LibraryPage />} />
              <Route path="/history" element={<HistoryPage />} />
              <Route path="/login" element={<LoginPage />} />
              <Route path="/register" element={<RegisterPage />} />
              <Route path="/profile" element={<ProfilePage />} />
              <Route path="*" element={<NotFoundPage />} />
            </Routes>
          </main>
          <footer className="site-footer">
            <p>
              Data metadata oleh{' '}
              <a href="https://jikan.moe" target="_blank" rel="noopener noreferrer">Jikan API</a>{' '}
              (tidak berafiliasi dengan MyAnimeList). MVP ini tidak meng-host konten bab.
            </p>
          </footer>
        </AuthProvider>
      </BrowserRouter>
    </ErrorBoundary>
  </StrictMode>,
);
