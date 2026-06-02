import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  getUser, getToken, logout,
  listApiKeys, generateApiKey, revokeApiKey,
  type ApiKeyDTO,
} from '../api/authApi';
import { LogoMark } from '../components/LogoMark';
import './DeveloperPage.css';

function maskKey(key: string) {
  return key.slice(0, 8) + '••••••••••••••••••••••••••••••••';
}

function CodeBlock({ apiKey }: { apiKey: string }) {
  const [tab, setTab] = useState<'curl' | 'js' | 'python'>('curl');

  const BASE = 'http://localhost:8080';
  const samples = {
    curl: `curl -X GET \\
  "${BASE}/api/weather/latest" \\
  -H "X-API-Key: ${apiKey}"`,
    js: `const res = await fetch(
  '${BASE}/api/weather/latest',
  { headers: { 'X-API-Key': '${apiKey}' } }
);
const data = await res.json();
console.log(data);`,
    python: `import requests

res = requests.get(
    '${BASE}/api/weather/latest',
    headers={'X-API-Key': '${apiKey}'}
)
print(res.json())`,
  };

  return (
    <div className="dev-code-block">
      <div className="dev-code-tabs">
        {(['curl', 'js', 'python'] as const).map(t => (
          <button
            key={t}
            className={`dev-code-tab${tab === t ? ' active' : ''}`}
            onClick={() => setTab(t)}
          >
            {t === 'js' ? 'JavaScript' : t === 'python' ? 'Python' : 'cURL'}
          </button>
        ))}
      </div>
      <pre className="dev-code-pre"><code>{samples[tab]}</code></pre>
    </div>
  );
}

export default function DeveloperPage() {
  const navigate   = useNavigate();
  const user       = getUser();
  const [keys,       setKeys]       = useState<ApiKeyDTO[]>([]);
  const [loading,    setLoading]    = useState(true);
  const [genName,    setGenName]    = useState('');
  const [genError,   setGenError]   = useState('');
  const [genLoading, setGenLoading] = useState(false);
  const [newKey,     setNewKey]     = useState<ApiKeyDTO | null>(null);
  const [copied,     setCopied]     = useState<number | null>(null);

  useEffect(() => {
    if (!getToken() || !user) { navigate('/login'); return; }
    listApiKeys()
      .then(setKeys)
      .catch(() => setKeys([]))
      .finally(() => setLoading(false));
  }, []);

  async function handleGenerate() {
    setGenError('');
    setGenLoading(true);
    try {
      const key = await generateApiKey(genName || 'Default');
      setNewKey(key);
      setKeys(prev => [{ ...key, keyValue: maskKey(key.keyValue) }, ...prev]);
      setGenName('');
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to generate key.';
      setGenError(msg.includes('Maximum') ? msg : 'Failed to generate key. Please try again.');
    } finally {
      setGenLoading(false);
    }
  }

  async function handleRevoke(id: number) {
    if (!confirm('Revoke this key? Apps using it will stop working immediately.')) return;
    try {
      await revokeApiKey(id);
      setKeys(prev => prev.filter(k => k.id !== id));
    } catch {
      alert('Failed to revoke key.');
    }
  }

  function handleNewKeyCopy() {
    if (!newKey) return;
    navigator.clipboard.writeText(newKey.keyValue);
    setCopied(newKey.id);
    setTimeout(() => setCopied(null), 2000);
  }

  function handleLogout() {
    logout();
    navigate('/');
  }

  const activeKey = newKey ?? keys[0];

  return (
    <div className="dev-page">

      {/* ── Nav ── */}
      <nav className="dev-nav">
        <Link to="/" className="dev-logo">
          <LogoMark size={34} />
          <span className="dev-logo-name">WeatherConnect</span>
        </Link>
        <div className="dev-nav-links">
          <Link to="/dashboard">Live Data</Link>
          <Link to="/metrics">Status</Link>
        </div>
        <div className="dev-nav-right">
          <span className="dev-user-email">{user?.email}</span>
          <button className="dev-signout" onClick={handleLogout}>Sign Out</button>
        </div>
      </nav>

      <div className="dev-body">

        {/* ── Sidebar ── */}
        <aside className="dev-sidebar">
          <div className="dev-sidebar-section">
            <p className="dev-sidebar-label">Account</p>
            <p className="dev-sidebar-name">{user?.name}</p>
            <p className="dev-sidebar-email">{user?.email}</p>
          </div>
          <div className="dev-sidebar-section">
            <p className="dev-sidebar-label">Plan</p>
            <span className="dev-plan-badge">Free</span>
            <p className="dev-plan-limit">1,000 req / day</p>
          </div>
          <div className="dev-sidebar-links">
            <a href="#keys">API Keys</a>
            <a href="#quickstart">Quick Start</a>
            <a href="#endpoints">Endpoints</a>
            <Link to="/profile" style={{ color: 'inherit', textDecoration: 'none', fontSize: '0.875rem' }}>Profile</Link>
          </div>
        </aside>

        {/* ── Main ── */}
        <main className="dev-main">

          {/* Welcome */}
          <div className="dev-welcome">
            <h1 className="dev-title">Developer Dashboard</h1>
            <p className="dev-subtitle">Manage your API keys and explore the WeatherConnect API.</p>
          </div>

          {/* ── API Keys section ── */}
          <section className="dev-section" id="keys">
            <div className="dev-section-header">
              <div>
                <h2 className="dev-section-title">API Keys</h2>
                <p className="dev-section-sub">Your key authenticates every request via the <code>X-API-Key</code> header.</p>
              </div>
            </div>

            {/* Generate row — disabled when 2 keys exist */}
            {keys.length < 2 && (
              <div className="dev-generate-row">
                <input
                  className="dev-gen-input"
                  placeholder="Key name (e.g. Production)"
                  value={genName}
                  onChange={e => setGenName(e.target.value)}
                  onKeyDown={e => e.key === 'Enter' && handleGenerate()}
                />
                <button
                  className="dev-btn-primary"
                  onClick={handleGenerate}
                  disabled={genLoading}
                >
                  {genLoading ? 'Generating…' : '+ Generate Key'}
                </button>
              </div>
            )}
            {keys.length >= 2 && (
              <p className="dev-key-limit-note">Maximum of 2 API keys reached. Revoke one to generate a new key.</p>
            )}
            {genError && <p className="dev-error">{genError}</p>}

            {/* One-time new key banner */}
            {newKey && (
              <div className="dev-new-key-banner">
                <div className="dev-new-key-header">
                  <span className="dev-new-key-title">🔑 Save your API key — it won't be shown again</span>
                  <button className="dev-new-key-dismiss" onClick={() => setNewKey(null)}>✕</button>
                </div>
                <div className="dev-new-key-value">
                  <code>{newKey.keyValue}</code>
                </div>
                <button
                  className={`dev-btn-copy${copied === newKey.id ? ' copied' : ''}`}
                  onClick={handleNewKeyCopy}
                  style={{ marginTop: '0.5rem' }}
                >
                  {copied === newKey.id ? '✓ Copied' : 'Copy Key'}
                </button>
              </div>
            )}

            {/* Keys list */}
            {loading ? (
              <p className="dev-loading">Loading keys…</p>
            ) : keys.length === 0 ? (
              <div className="dev-empty">
                <p>No API keys yet. Generate one above to get started.</p>
              </div>
            ) : (
              <div className="dev-keys-list">
                {keys.map(key => (
                  <div className="dev-key-card" key={key.id}>
                    <div className="dev-key-top">
                      <div className="dev-key-meta">
                        <span className="dev-key-name">{key.name}</span>
                        <span className="dev-key-date">
                          Created {new Date(key.createdAt).toLocaleDateString()}
                        </span>
                      </div>
                      <div className="dev-key-actions">
                        <button
                          className="dev-btn-revoke"
                          onClick={() => handleRevoke(key.id)}
                        >
                          Revoke
                        </button>
                      </div>
                    </div>
                    <div className="dev-key-value">
                      <code>{key.keyValue}</code>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </section>

          {/* ── Quick Start ── */}
          <section className="dev-section" id="quickstart">
            <h2 className="dev-section-title">Quick Start</h2>
            <p className="dev-section-sub">
              Pass your API key in the <code>X-API-Key</code> header on every request.
            </p>
            <CodeBlock apiKey={activeKey?.keyValue ?? 'nvc_your_key_here'} />
          </section>

          {/* ── Endpoints ── */}
          <section className="dev-section" id="endpoints">
            <h2 className="dev-section-title">Endpoints</h2>
            <div className="dev-endpoints">
              {[
                { method: 'GET', path: '/api/weather/latest',         desc: 'Current conditions for all cities' },
                { method: 'GET', path: '/api/weather?city=Boston',   desc: 'Full history for a city' },
                { method: 'GET', path: '/api/weather/summary',        desc: 'Daily min / max / avg temps' },
                { method: 'GET', path: '/api/forecast?city=Boston',   desc: '5-day forecast' },
                { method: 'GET', path: '/api/aqi?city=Boston',        desc: 'Air quality index' },
                { method: 'GET', path: '/api/weather/insights',       desc: 'AI-powered weather insight ⚡' },
              ].map(e => (
                <div className="dev-endpoint" key={e.path}>
                  <span className="dev-method">{e.method}</span>
                  <code className="dev-path">{e.path}</code>
                  <span className="dev-endpoint-desc">{e.desc}</span>
                </div>
              ))}
            </div>
            <p className="dev-base-url">Base URL: <code>http://localhost:8080</code> · Authenticate via <code>X-API-Key</code> header</p>
          </section>

        </main>
      </div>
    </div>
  );
}
