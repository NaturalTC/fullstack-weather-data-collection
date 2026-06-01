import { useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { resetPassword } from '../api/authApi';
import { LogoMark } from '../components/LogoMark';
import './AuthPages.css';

export default function ResetPasswordPage() {
  const navigate     = useNavigate();
  const [params]     = useSearchParams();
  const token        = params.get('token') ?? '';
  const [password,   setPassword]   = useState('');
  const [confirm,    setConfirm]    = useState('');
  const [loading,    setLoading]    = useState(false);
  const [error,      setError]      = useState('');

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError('');
    if (password !== confirm) { setError('Passwords do not match'); return; }
    if (password.length < 8)  { setError('Password must be at least 8 characters'); return; }
    if (!token)               { setError('Missing reset token. Use the link from your email.'); return; }

    setLoading(true);
    try {
      await resetPassword(token, password);
      navigate('/login?reset=true');
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
        <h1 className="auth-title">Set new password</h1>
        <p className="auth-subtitle">Choose a strong password for your account.</p>

        <form className="auth-form" onSubmit={handleSubmit}>
          <div className="auth-field">
            <label className="auth-label">New Password</label>
            <input
              className="auth-input"
              type="password"
              placeholder="Minimum 8 characters"
              value={password}
              onChange={e => setPassword(e.target.value)}
              required
            />
          </div>
          <div className="auth-field">
            <label className="auth-label">Confirm Password</label>
            <input
              className="auth-input"
              type="password"
              placeholder="Repeat your new password"
              value={confirm}
              onChange={e => setConfirm(e.target.value)}
              required
            />
          </div>

          {error && <div className="auth-error">{error}</div>}

          <button className="auth-submit" type="submit" disabled={loading || !token}>
            {loading ? 'Updating…' : 'Update Password'}
          </button>
        </form>

        {!token && (
          <div className="auth-footer">
            Invalid link. <Link to="/forgot-password">Request a new one</Link>
          </div>
        )}
      </div>
    </div>
  );
}
