import { Component } from 'react';
import type { ErrorInfo, ReactNode } from 'react';

interface Props { children: ReactNode }
interface State { error: Error | null }

/** Fallback global agar satu komponen rusak tidak mematikan seluruh aplikasi. */
export default class ErrorBoundary extends Component<Props, State> {
  state: State = { error: null };

  static getDerivedStateFromError(error: Error): State {
    return { error };
  }

  componentDidCatch(error: Error, info: ErrorInfo) {
    // Logging aman: tanpa data pengguna/token.
    console.error('Unhandled UI error:', error.message, info.componentStack);
  }

  render() {
    if (this.state.error) {
      return (
        <div className="state-block error" role="alert" style={{ margin: '3rem auto', maxWidth: 520 }}>
          <div className="state-icon">💥</div>
          <h2>Terjadi kesalahan tampilan</h2>
          <p>{this.state.error.message || 'Kesalahan tak terduga.'}</p>
          <button className="btn btn-primary" onClick={() => window.location.assign('/')}>
            Muat ulang beranda
          </button>
        </div>
      );
    }
    return this.props.children;
  }
}
