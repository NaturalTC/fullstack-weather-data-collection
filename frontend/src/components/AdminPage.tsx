import { useState } from 'react';

const API_BASE = import.meta.env.VITE_API_BASE ?? '';

interface AdminStats {
  totalRecords: number;
  lastFetch: string | null;
  recordsPerCity: Record<string, number>;
}

interface CityForm {
  name: string;
  state: string;
  country: string;
}

interface CacheStat {
  name: string;
  hits: number;
  misses: number;
}

// The web pages memory, where data is stored during a live session
// Each line is a piece of memory the page holds. The pattern is always [value, setValue]:
export default function AdminPage() {
  const [username, setUsername] = useState(''); // whatevers typed
  const [password, setPassword] = useState('');
  const [authHeader, setAuthHeader] = useState('');
  const [stats, setStats] = useState<AdminStats | null>(null); // null until log in
  const [loginError, setLoginError] = useState('');
  const [fetchMsg, setFetchMsg] = useState('');
  const [health, setHealth] = useState<'UP' | 'DOWN' | 'unknown'>('unknown'); // 3 states it can be in
  const [cityForm, setCityForm] = useState<CityForm>({
    name: '', state: '', country: 'US',
  });
  const [cityMsg, setCityMsg] = useState('');
  const [cacheStats, setCacheStats] = useState<CacheStat[] | null>(null);
  const [cacheLoading, setCacheLoading] = useState(false);
  const [seedMsg, setSeedMsg] = useState('');
  const [seedLoading, setSeedLoading] = useState(false);
  const [importCity, setImportCity] = useState('');
  const [importFrom, setImportFrom] = useState(() => {
    const d = new Date(); d.setFullYear(d.getFullYear() - 1); return d.toISOString().slice(0, 10);
  });
  const [importTo, setImportTo] = useState(() => {
    const d = new Date(); d.setDate(d.getDate() - 1); return d.toISOString().slice(0, 10);
  });
  const [importMsg, setImportMsg] = useState('');
  const [importLoading, setImportLoading] = useState(false);


  function makeAuth(u: string, p: string) {
    return 'Basic ' + btoa(`${u}:${p}`);
  }

  async function handleLogin(e: React.FormEvent) {
    e.preventDefault();
    setLoginError('');
    const header = makeAuth(username, password);
    try {
      const res = await fetch(`${API_BASE}/admin/stats`, {
        headers: { Authorization: header },
      });
      if (res.ok) {
        setAuthHeader(header);
        setStats(await res.json());
        fetch(`${API_BASE}/actuator/health`)
          .then(r => r.json())
          .then(d => setHealth(d.status === 'UP' ? 'UP' : 'DOWN'))
          .catch(() => setHealth('DOWN'));
      } else {
        setLoginError(`Invalid credentials (${res.status})`);
      }
    } catch (err) {
      setLoginError(`Network error: ${err}`);
    }
  }

  async function refreshStats() {
    const res = await fetch(`${API_BASE}/admin/stats`, {
      headers: { Authorization: authHeader },
    });
    if (res.ok) setStats(await res.json());
  }

  async function handleTriggerFetch() {
    setFetchMsg('Fetching…');
    const res = await fetch(`${API_BASE}/admin/fetch`, {
      method: 'POST',
      headers: { Authorization: authHeader },
    });
    setFetchMsg(res.ok ? 'Done! Data refreshed.' : 'Fetch failed.');
    if (res.ok) refreshStats();
  }

  async function handleAddCity(e: React.FormEvent) {
    e.preventDefault();
    setCityMsg('');
    const res = await fetch(`${API_BASE}/admin/cities`, {
      method: 'POST',
      headers: { Authorization: authHeader, 'Content-Type': 'application/json' },
      body: JSON.stringify({
        name: cityForm.name,
        state: cityForm.state,
        country: cityForm.country,
      }),
    });
    if (res.ok) {
      setCityMsg(`Added ${cityForm.name}.`);
      setCityForm({ name: '', state: '', country: 'US' });
      refreshStats();
    } else {
      const body = await res.json().catch(() => null);
      setCityMsg(body?.detail ?? body?.message ?? `Failed to add city (${res.status}).`);
    }
  }

  async function loadCacheStats() {
    setCacheLoading(true);
    const cacheNames = ['latestWeather', 'dailySummary', 'forecast', 'aqi', 'heatmap'];
    try {
      const stats = await Promise.all(
        cacheNames.map(async name => {
          const [hitsRes, missesRes] = await Promise.all([
            fetch(`${API_BASE}/actuator/metrics/cache.gets?tag=name:${name}&tag=result:hit`),
            fetch(`${API_BASE}/actuator/metrics/cache.gets?tag=name:${name}&tag=result:miss`),
          ]);
          const hits = hitsRes.ok ? (await hitsRes.json()).measurements[0]?.value ?? 0 : 0;
          const misses = missesRes.ok ? (await missesRes.json()).measurements[0]?.value ?? 0 : 0;
          return { name, hits, misses };
        })
      );
      setCacheStats(stats);
    } catch {
      setCacheStats([]);
    } finally {
      setCacheLoading(false);
    }
  }

  async function handleSeedCities() {
    setSeedLoading(true); setSeedMsg('');
    const res = await fetch(`${API_BASE}/admin/import-cities`, {
      method: 'POST', headers: { Authorization: authHeader },
    });
    const data = await res.json().catch(() => ({}));
    setSeedMsg(res.ok
      ? `Done — added ${data.added}, skipped ${data.skipped} already-existing cities.`
      : `Failed: ${data.error ?? res.status}`);
    if (res.ok) refreshStats();
    setSeedLoading(false);
  }

  async function handleImportHistorical(e: React.FormEvent) {
    e.preventDefault();
    if (!importCity.trim()) { setImportMsg('Enter a city name.'); return; }
    setImportLoading(true); setImportMsg('Importing — this may take 10–30 seconds…');
    const res = await fetch(`${API_BASE}/admin/import-historical`, {
      method: 'POST',
      headers: { Authorization: authHeader, 'Content-Type': 'application/json' },
      body: JSON.stringify({ city: importCity.trim(), from: importFrom, to: importTo }),
    });
    const data = await res.json().catch(() => ({}));
    setImportMsg(res.ok
      ? `Imported ${data.imported.toLocaleString()} records for ${data.city} (${data.skipped.toLocaleString()} days skipped — already had data).`
      : `Failed: ${data.error ?? res.status}`);
    if (res.ok) refreshStats();
    setImportLoading(false);
  }

  async function handleRemoveCity(name: string) {
    const res = await fetch(`${API_BASE}/admin/cities/${encodeURIComponent(name)}`, {
      method: 'DELETE',
      headers: { Authorization: authHeader },
    });
    if (res.ok) refreshStats();
  }

  if (!authHeader) {
    return (
      <div style={styles.page}>
        <div style={styles.card}>
          <h2 style={styles.title}>Admin Login</h2>
          <form onSubmit={handleLogin} style={styles.form}>
            <input
              style={styles.input}
              placeholder="Username"
              value={username}
              onChange={e => setUsername(e.target.value)}
              autoComplete="username"
            />
            <input
              style={styles.input}
              type="password"
              placeholder="Password"
              value={password}
              onChange={e => setPassword(e.target.value)}
              autoComplete="current-password"
            />
            {loginError && <p style={styles.error}>{loginError}</p>}
            <button style={styles.btn} type="submit">Login</button>
          </form>
        </div>
      </div>
    );
  }

  return (
    <div style={styles.page}>
      <div style={{ ...styles.card, maxWidth: 720 }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
          <h2 style={{ ...styles.title, margin: 0 }}>Admin Panel</h2>
          <a
            href={`${API_BASE}/swagger-ui/index.html`}
            target="_blank"
            rel="noreferrer"
            style={{ fontSize: '0.8rem', color: '#60a5fa', textDecoration: 'none' }}
          >
            API Docs ↗
          </a>
        </div>

        {/* Stats */}
        <section style={styles.section}>
          <h3 style={styles.sectionTitle}>System Stats</h3>
          {stats && (
            <>
              <p style={styles.stat}>
            Server status:{' '}
            <strong style={{ color: health === 'UP' ? '#4ade80' : health === 'DOWN' ? '#f87171' : '#94a3b8' }}>
              {health}
            </strong>
          </p>
          <p style={styles.stat}>Total records: <strong>{stats.totalRecords.toLocaleString()}</strong></p>
              <p style={styles.stat}>
                Last fetch: <strong>{stats.lastFetch ? new Date(stats.lastFetch).toLocaleString() : '—'}</strong>
              </p>
              <table style={styles.table}>
                <thead>
                  <tr>
                    <th style={styles.th}>City</th>
                    <th style={styles.th}>Records</th>
                    <th style={styles.th}></th>
                  </tr>
                </thead>
                <tbody>
                  {Object.entries(stats.recordsPerCity).map(([city, count]) => (
                    <tr key={city}>
                      <td style={styles.td}>{city}</td>
                      <td style={styles.td}>{count.toLocaleString()}</td>
                      <td style={styles.td}>
                        <button
                          style={styles.removeBtn}
                          onClick={() => handleRemoveCity(city)}
                        >
                          Remove
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </>
          )}
        </section>

        {/* Manual fetch */}
        <section style={styles.section}>
          <h3 style={styles.sectionTitle}>Manual Fetch</h3>
          <button style={styles.btn} onClick={handleTriggerFetch}>Trigger Fetch Now</button>
          {fetchMsg && <p style={styles.msg}>{fetchMsg}</p>}
        </section>

        {/* Cache Stats */}
        <section style={styles.section}>
          <h3 style={styles.sectionTitle}>Cache Stats</h3>
          <button style={styles.btn} onClick={loadCacheStats} disabled={cacheLoading}>
            {cacheLoading ? 'Loading…' : 'Refresh Cache Stats'}
          </button>
          {cacheStats && (
            <table style={{ ...styles.table, marginTop: '0.75rem' }}>
              <thead>
                <tr>
                  <th style={styles.th}>Cache</th>
                  <th style={styles.th}>Hits</th>
                  <th style={styles.th}>Misses</th>
                  <th style={styles.th}>Hit Rate</th>
                </tr>
              </thead>
              <tbody>
                {cacheStats.map(c => {
                  const total = c.hits + c.misses;
                  const rate = total === 0 ? '—' : `${Math.round((c.hits / total) * 100)}%`;
                  return (
                    <tr key={c.name}>
                      <td style={styles.td}>{c.name}</td>
                      <td style={styles.td}>{c.hits}</td>
                      <td style={styles.td}>{c.misses}</td>
                      <td style={{ ...styles.td, color: total === 0 ? '#94a3b8' : c.hits / total > 0.5 ? '#4ade80' : '#f87171' }}>
                        {rate}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          )}
        </section>

        {/* Add city */}
        <section style={styles.section}>
          <h3 style={styles.sectionTitle}>Add City</h3>
          <form onSubmit={handleAddCity} style={{ ...styles.form, flexDirection: 'row', flexWrap: 'wrap', gap: '0.5rem' }}>
            {(['name', 'state', 'country'] as const).map(field => (
              <input
                key={field}
                style={{ ...styles.input, width: field === 'name' ? '8rem' : '5rem' }}
                placeholder={field.charAt(0).toUpperCase() + field.slice(1)}
                value={cityForm[field]}
                onChange={e => setCityForm(prev => ({ ...prev, [field]: e.target.value }))}
              />
            ))}
            <button style={styles.btn} type="submit">Add</button>
          </form>
          {cityMsg && <p style={styles.msg}>{cityMsg}</p>}
        </section>

        {/* Bulk seed cities */}
        <section style={styles.section}>
          <h3 style={styles.sectionTitle}>Bulk Add US Cities</h3>
          <p style={{ fontSize: '0.8rem', color: '#94a3b8', marginBottom: '0.75rem' }}>
            Seeds 51 major US cities with known coordinates. Skips any already in the database.
          </p>
          <button style={styles.btn} onClick={handleSeedCities} disabled={seedLoading}>
            {seedLoading ? 'Seeding…' : 'Seed 51 US Cities'}
          </button>
          {seedMsg && <p style={styles.msg}>{seedMsg}</p>}
        </section>

        {/* Historical import */}
        <section style={styles.section}>
          <h3 style={styles.sectionTitle}>Import Historical Data</h3>
          <p style={{ fontSize: '0.8rem', color: '#94a3b8', marginBottom: '0.75rem' }}>
            Pulls hourly data from Open-Meteo's free archive (no API key needed, goes back to 1940).
            Skips days that already have records.
          </p>
          <form onSubmit={handleImportHistorical} style={{ ...styles.form, gap: '0.5rem' }}>
            <input
              style={styles.input}
              placeholder="City name (must exist in DB)"
              value={importCity}
              onChange={e => setImportCity(e.target.value)}
            />
            <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem' }}>
                <label style={{ fontSize: '0.72rem', color: '#94a3b8' }}>From</label>
                <input style={styles.input} type="date" value={importFrom} onChange={e => setImportFrom(e.target.value)} />
              </div>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem' }}>
                <label style={{ fontSize: '0.72rem', color: '#94a3b8' }}>To</label>
                <input style={styles.input} type="date" value={importTo} onChange={e => setImportTo(e.target.value)} />
              </div>
            </div>
            <button style={styles.btn} type="submit" disabled={importLoading}>
              {importLoading ? 'Importing…' : 'Import Historical Data'}
            </button>
          </form>
          {importMsg && (
            <p style={{ ...styles.msg, color: importMsg.startsWith('Failed') ? '#f87171' : '#86efac' }}>
              {importMsg}
            </p>
          )}
        </section>
      </div>
    </div>
  );
}

const styles: Record<string, React.CSSProperties> = {
  page: {
    minHeight: '100vh',
    background: '#0f1117',
    display: 'flex',
    alignItems: 'flex-start',
    justifyContent: 'center',
    padding: '3rem 1rem',
    fontFamily: 'system-ui, sans-serif',
    color: '#e2e8f0',
  },
  card: {
    background: '#1e2130',
    borderRadius: '0.75rem',
    padding: '2rem',
    width: '100%',
    maxWidth: 400,
  },
  title: { margin: '0 0 1.5rem', fontSize: '1.25rem', color: '#f8fafc' },
  form: { display: 'flex', flexDirection: 'column', gap: '0.75rem' },
  input: {
    padding: '0.5rem 0.75rem',
    borderRadius: '0.375rem',
    border: '1px solid #334155',
    background: '#0f1117',
    color: '#e2e8f0',
    fontSize: '0.875rem',
  },
  btn: {
    padding: '0.5rem 1.25rem',
    borderRadius: '0.375rem',
    border: 'none',
    background: '#3b82f6',
    color: '#fff',
    cursor: 'pointer',
    fontSize: '0.875rem',
    alignSelf: 'flex-start',
  },
  removeBtn: {
    padding: '0.25rem 0.625rem',
    borderRadius: '0.25rem',
    border: 'none',
    background: '#ef4444',
    color: '#fff',
    cursor: 'pointer',
    fontSize: '0.75rem',
  },
  error: { color: '#f87171', margin: 0, fontSize: '0.875rem' },
  msg: { color: '#86efac', margin: '0.5rem 0 0', fontSize: '0.875rem' },
  section: { marginBottom: '2rem' },
  sectionTitle: { margin: '0 0 0.75rem', fontSize: '1rem', color: '#94a3b8' },
  stat: { margin: '0 0 0.25rem', fontSize: '0.9rem' },
  table: { width: '100%', borderCollapse: 'collapse', marginTop: '0.75rem' },
  th: {
    textAlign: 'left', padding: '0.375rem 0.5rem',
    borderBottom: '1px solid #334155', fontSize: '0.8rem', color: '#94a3b8',
  },
  td: {
    padding: '0.375rem 0.5rem',
    borderBottom: '1px solid #1e293b', fontSize: '0.875rem',
  },
};
