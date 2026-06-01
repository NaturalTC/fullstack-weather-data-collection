import { Link } from 'react-router-dom';
import { LogoMark } from '../components/LogoMark';
import './AuthPages.css';

export default function StripeSuccessPage() {
  return (
    <div className="auth-page">
      <Link to="/" className="auth-logo">
        <LogoMark size={40} />
        <span className="auth-logo-name">NovaCast</span>
      </Link>
      <div className="auth-card" style={{ textAlign: 'center' }}>
        <div style={{ fontSize: '2.5rem', marginBottom: '1rem' }}>🎉</div>
        <h1 className="auth-title">You're upgraded!</h1>
        <p className="auth-subtitle" style={{ marginBottom: '1.5rem' }}>
          Your plan has been updated. It may take a moment to reflect in your dashboard.
        </p>
        <Link
          to="/developer"
          className="auth-submit"
          style={{ display: 'block', textAlign: 'center', textDecoration: 'none', paddingTop: '0.8rem', paddingBottom: '0.8rem' }}
        >
          Go to Dashboard
        </Link>
      </div>
    </div>
  );
}
