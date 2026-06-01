import { useState } from 'react';
import { Link } from 'react-router-dom';
import { forgotPassword } from '../api/authApi';
import { LogoMark } from '../components/LogoMark';
import './AuthPages.css';

export default function ForgotPasswordPage() {
  const [email,   setEmail]   = useState('');
  const [sent,    setSent]    = useState(false);
  const [loading, setLoading] = useState(false);
  const [error,   setError]   = useState('');

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await forgotPassword(email);
      setSent(true);
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Something went wrong');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="auth-page">
      <Link to="/" className="auth-logo">
        <LogoMark size={40} />
        <span className="auth-logo-name">NovaCast</span>
      </Link>

      <div className="auth-card">
        {sent ? (
          <>
            <h1 className="auth-title">Check your inbox</h1>
            <p className="auth-subtitle" style={{ marginBottom: '1.5rem' }}>
              If <strong style={{ color: 'white' }}>{email}</strong> is registered, a reset link is on its way. Check your spam folder too.
            </p>
            <Link to="/login" className="auth-submit" style={{ textAlign: 'center', textDecoration: 'none', display: 'block', paddingTop: '0.8rem', paddingBottom: '0.8rem' }}>
              Back to Sign In
            </Link>
          </>
        ) : (
          <>
            <h1 className="auth-title">Forgot password?</h1>
            <p className="auth-subtitle">Enter your email and we'll send you a reset link.</p>

            <form className="auth-form" onSubmit={handleSubmit}>
              <div className="auth-field">
                <label className="auth-label">Email</label>
                <input
                  className="auth-input"
                  type="email"
                  placeholder="you@example.com"
                  value={email}
                  onChange={e => setEmail(e.target.value)}
                  required
                />
              </div>

              {error && <div className="auth-error">{error}</div>}

              <button className="auth-submit" type="submit" disabled={loading}>
                {loading ? 'Sending…' : 'Send Reset Link'}
              </button>
            </form>

            <div className="auth-footer">
              Remembered it? <Link to="/login">Sign in</Link>
            </div>
          </>
        )}
      </div>
    </div>
  );
}
