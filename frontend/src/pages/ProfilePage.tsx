import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  getToken, getUser, logout,
  getProfile, updateProfile, createCheckoutSession,
  type UserProfileDTO,
} from '../api/authApi';
import { LogoMark } from '../components/LogoMark';
import './ProfilePage.css';

type Tab = 'general' | 'billing';

const PLANS: Record<string, { label: string; limit: string; color: string }> = {
  USER:  { label: 'Free',       limit: '1,000 req / day',  color: '#c2ff50' },
  PRO:   { label: 'Pro',        limit: '50,000 req / day', color: '#60a5fa' },
  SCALE: { label: 'Scale',      limit: 'Unlimited',        color: '#f97316' },
};

export default function ProfilePage() {
  const navigate = useNavigate();
  const user     = getUser();

  const [tab,          setTab]          = useState<Tab>('general');
  const [upgrading,    setUpgrading]    = useState(false);
  const [upgradeError, setUpgradeError] = useState('');
  const [profile,      setProfile]      = useState<UserProfileDTO | null>(null);
  const [loading,      setLoading]      = useState(true);

  // general form
  const [name,         setName]         = useState('');
  const [nameSuccess,  setNameSuccess]  = useState('');
  const [nameError,    setNameError]    = useState('');
  const [nameSaving,   setNameSaving]   = useState(false);

  // password form
  const [currentPw,    setCurrentPw]    = useState('');
  const [newPw,        setNewPw]        = useState('');
  const [confirmPw,    setConfirmPw]    = useState('');
  const [pwSuccess,    setPwSuccess]    = useState('');
  const [pwError,      setPwError]      = useState('');
  const [pwSaving,     setPwSaving]     = useState(false);

  useEffect(() => {
    if (!getToken() || !user) { navigate('/login'); return; }
    getProfile()
      .then(p => { setProfile(p); setName(p.name); })
      .catch(() => navigate('/login'))
      .finally(() => setLoading(false));
  }, []);

  async function handleNameSave(e: React.FormEvent) {
    e.preventDefault();
    setNameError(''); setNameSuccess('');
    if (!name.trim()) { setNameError('Name cannot be empty'); return; }
    setNameSaving(true);
    try {
      const updated = await updateProfile({ name: name.trim() });
      setProfile(updated);
      localStorage.setItem('nc_name', updated.name);
      setNameSuccess('Name updated.');
    } catch (err: unknown) {
      setNameError(err instanceof Error ? err.message : 'Update failed');
    } finally {
      setNameSaving(false);
    }
  }

  async function handlePasswordSave(e: React.FormEvent) {
    e.preventDefault();
    setPwError(''); setPwSuccess('');
    if (newPw !== confirmPw) { setPwError('Passwords do not match'); return; }
    if (newPw.length < 8)    { setPwError('New password must be at least 8 characters'); return; }
    setPwSaving(true);
    try {
      await updateProfile({ currentPassword: currentPw, newPassword: newPw });
      setPwSuccess('Password updated.');
      setCurrentPw(''); setNewPw(''); setConfirmPw('');
    } catch (err: unknown) {
      setPwError(err instanceof Error ? err.message : 'Update failed');
    } finally {
      setPwSaving(false);
    }
  }

  async function handleUpgrade(targetPlan: 'PRO' | 'SCALE') {
    setUpgradeError('');
    setUpgrading(true);
    try {
      const url = await createCheckoutSession(targetPlan);
      window.location.href = url;
    } catch (err: unknown) {
      setUpgradeError(err instanceof Error ? err.message : 'Upgrade failed');
      setUpgrading(false);
    }
  }

  function handleLogout() { logout(); navigate('/'); }

  const plan = profile ? (PLANS[profile.plan] ?? PLANS.USER) : PLANS.USER;

  if (loading) return (
    <div className="prof-loading">
      <div className="loading-pulse" />
    </div>
  );

  return (
    <div className="prof-page">

      {/* Nav */}
      <nav className="dev-nav">
        <Link to="/" className="dev-logo">
          <LogoMark size={34} />
          <span className="dev-logo-name">WeatherConnect</span>
        </Link>
        <div className="dev-nav-links">
          <Link to="/developer">Dashboard</Link>
          <Link to="/metrics">Status</Link>
        </div>
        <div className="dev-nav-right">
          <span className="dev-user-email">{user?.email}</span>
          <button className="dev-signout" onClick={handleLogout}>Sign Out</button>
        </div>
      </nav>

      <div className="prof-body">

        {/* Sidebar */}
        <aside className="prof-sidebar">
          <div className="prof-avatar">{profile?.name?.[0]?.toUpperCase() ?? '?'}</div>
          <p className="prof-sidebar-name">{profile?.name}</p>
          <p className="prof-sidebar-email">{profile?.email}</p>
          <span className="prof-plan-badge" style={{ background: `${plan.color}18`, color: plan.color, borderColor: `${plan.color}30` }}>
            {plan.label}
          </span>
          <nav className="prof-sidebar-nav">
            <button className={tab === 'general' ? 'active' : ''} onClick={() => setTab('general')}>General</button>
            <button className={tab === 'billing' ? 'active' : ''} onClick={() => setTab('billing')}>Billing</button>
          </nav>
        </aside>

        {/* Main */}
        <main className="prof-main">

          {tab === 'general' && (
            <>
              <h1 className="prof-title">General</h1>

              {/* Name */}
              <section className="prof-section">
                <h2 className="prof-section-title">Display Name</h2>
                <form className="prof-form" onSubmit={handleNameSave}>
                  <input
                    className="prof-input"
                    value={name}
                    onChange={e => setName(e.target.value)}
                    placeholder="Your name"
                  />
                  {nameError   && <p className="prof-msg prof-msg--error">{nameError}</p>}
                  {nameSuccess && <p className="prof-msg prof-msg--success">{nameSuccess}</p>}
                  <button className="prof-btn-primary" type="submit" disabled={nameSaving}>
                    {nameSaving ? 'Saving…' : 'Save Name'}
                  </button>
                </form>
              </section>

              {/* Email (read-only) */}
              <section className="prof-section">
                <h2 className="prof-section-title">Email Address</h2>
                <p className="prof-section-sub">Email changes are not supported yet.</p>
                <div className="prof-input prof-input--readonly">{profile?.email}</div>
              </section>

              {/* Password */}
              <section className="prof-section">
                <h2 className="prof-section-title">Change Password</h2>
                <form className="prof-form" onSubmit={handlePasswordSave}>
                  <div className="prof-field">
                    <label className="prof-label">Current Password</label>
                    <input className="prof-input" type="password" value={currentPw} onChange={e => setCurrentPw(e.target.value)} required />
                  </div>
                  <div className="prof-field">
                    <label className="prof-label">New Password</label>
                    <input className="prof-input" type="password" placeholder="Minimum 8 characters" value={newPw} onChange={e => setNewPw(e.target.value)} required />
                  </div>
                  <div className="prof-field">
                    <label className="prof-label">Confirm New Password</label>
                    <input className="prof-input" type="password" value={confirmPw} onChange={e => setConfirmPw(e.target.value)} required />
                  </div>
                  {pwError   && <p className="prof-msg prof-msg--error">{pwError}</p>}
                  {pwSuccess && <p className="prof-msg prof-msg--success">{pwSuccess}</p>}
                  <button className="prof-btn-primary" type="submit" disabled={pwSaving}>
                    {pwSaving ? 'Updating…' : 'Update Password'}
                  </button>
                </form>
              </section>
            </>
          )}

          {tab === 'billing' && (
            <>
              <h1 className="prof-title">Billing</h1>

              <section className="prof-section">
                <h2 className="prof-section-title">Current Plan</h2>
                <div className="prof-plan-card">
                  <div className="prof-plan-left">
                    <span className="prof-plan-name" style={{ color: plan.color }}>{plan.label}</span>
                    <p className="prof-plan-limit">{plan.limit}</p>
                  </div>
                </div>
              </section>

              {profile?.plan === 'FREE' && (
                <section className="prof-section">
                  <h2 className="prof-section-title">Upgrade your plan</h2>
                  <p className="prof-section-sub">More requests, more history, more power.</p>
                  {upgradeError && <p className="prof-msg prof-msg--error" style={{ marginBottom: '1rem' }}>{upgradeError}</p>}
                  <div className="prof-upgrade-grid">
                    <div className="prof-upgrade-card">
                      <p className="prof-upgrade-name" style={{ color: '#60a5fa' }}>Pro</p>
                      <p className="prof-upgrade-price">$19 <span>/mo</span></p>
                      <ul className="prof-upgrade-features">
                        <li>50,000 requests / day</li>
                        <li>Full 2-year history</li>
                        <li>AI insights included</li>
                        <li>Email support</li>
                      </ul>
                      <button
                        className="prof-btn-upgrade"
                        onClick={() => handleUpgrade('PRO')}
                        disabled={upgrading}
                      >
                        {upgrading ? 'Redirecting…' : 'Upgrade to Pro'}
                      </button>
                    </div>
                    <div className="prof-upgrade-card prof-upgrade-card--featured">
                      <p className="prof-upgrade-name" style={{ color: '#f97316' }}>Scale</p>
                      <p className="prof-upgrade-price">$49 <span>/mo</span></p>
                      <ul className="prof-upgrade-features">
                        <li>Unlimited requests</li>
                        <li>Full data archive</li>
                        <li>Priority AI insights</li>
                        <li>Priority support</li>
                      </ul>
                      <button
                        className="prof-btn-upgrade"
                        style={{ borderColor: 'rgba(249,115,22,0.4)', color: '#f97316' }}
                        onClick={() => handleUpgrade('SCALE')}
                        disabled={upgrading}
                      >
                        {upgrading ? 'Redirecting…' : 'Upgrade to Scale'}
                      </button>
                    </div>
                  </div>
                </section>
              )}

              <section className="prof-section">
                <h2 className="prof-section-title">Membership</h2>
                <div className="prof-info-row">
                  <span className="prof-info-label">Member since</span>
                  <span className="prof-info-value">
                    {profile ? new Date(profile.createdAt).toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' }) : '—'}
                  </span>
                </div>
                <div className="prof-info-row">
                  <span className="prof-info-label">Account email</span>
                  <span className="prof-info-value">{profile?.email}</span>
                </div>
                <div className="prof-info-row">
                  <span className="prof-info-label">Billing</span>
                  <span className="prof-info-value" style={{ color: 'rgba(255,255,255,0.35)' }}>
                    {profile?.plan === 'FREE' ? 'Free — no payment method' : 'Managed via Stripe'}
                  </span>
                </div>
              </section>
            </>
          )}

        </main>
      </div>
    </div>
  );
}
