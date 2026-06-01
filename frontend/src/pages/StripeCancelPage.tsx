import { Link } from 'react-router-dom';
import { LogoMark } from '../components/LogoMark';
import './AuthPages.css';

export default function StripeCancelPage() {
  return (
    <div className="auth-page">
      <Link to="/" className="auth-logo">
        <LogoMark size={40} />
        <span className="auth-logo-name">NovaCast</span>
      </Link>
      <div className="auth-card" style={{ textAlign: 'center' }}>
        <h1 className="auth-title">Checkout cancelled</h1>
        <p className="auth-subtitle" style={{ marginBottom: '1.5rem' }}>
          No charge was made. You can upgrade anytime from your profile.
        </p>
        <Link
          to="/profile"
          className="auth-submit"
          style={{ display: 'block', textAlign: 'center', textDecoration: 'none', paddingTop: '0.8rem', paddingBottom: '0.8rem' }}
        >
          Back to Profile
        </Link>
      </div>
    </div>
  );
}
