import { useState } from 'react';

const API_BASE = import.meta.env.VITE_API_BASE ?? 'http://13.59.3.49:8080';

interface AdminStats {
  totalRecords: number;
  lastFetch: string | null;
  recordsPerCity: Record<string, number>;
}

interface CityForm {
  name: string;
  state: string;
  country: string;
  latitude: string;
  longitude: string;
}

export default function AdminPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [authHeader, setAuthHeader] = useState('');
  const [stats, setStats] = useState<AdminStats | null>(null);
  const [loginError, setLoginError] = useState('');
  const [fetchMsg, setFetchMsg] = useState('');
  const [cityForm, setCityForm] = useState<CityForm>({
    name: '', state: '', country: 'US', latitude: '', longitude: '',
  });
  const [cityMsg, setCityMsg] = useState('');

  function makeAuth(u: string, p: string) {
    return 'Basic ' + btoa(`${u}:${p}`);
  }

  async function handleLogin(e: React.FormEvent) {
    e.preventDefault();
    setLoginError('');
    const header = makeAuth(username, password);
    const res = await fetch(`${API_BASE}/admin/stats`, {
      headers: { Authorization: header },
    });
    if (res.ok) {
      setAuthHeader(header);
      setStats(await res.json());
    } else {
      setLoginError('Invalid credentials');
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
        latitude: parseFloat(cityForm.latitude),
        longitude: parseFloat(cityForm.longitude),
      }),
    });
    if (res.ok) {
      setCityMsg(`Added ${cityForm.name}.`);
      setCityForm({ name: '', state: '', country: 'US', latitude: '', longitude: '' });
      refreshStats();
    } else {
      setCityMsg('Failed to add city.');
    }
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
        <h2 style={styles.title}>Admin Panel</h2>

        {/* Stats */}
        <section style={styles.section}>
          <h3 style={styles.sectionTitle}>System Stats</h3>
          {stats && (
            <>
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

        {/* Add city */}
        <section style={styles.section}>
          <h3 style={styles.sectionTitle}>Add City</h3>
          <form onSubmit={handleAddCity} style={{ ...styles.form, flexDirection: 'row', flexWrap: 'wrap', gap: '0.5rem' }}>
            {(['name', 'state', 'country', 'latitude', 'longitude'] as const).map(field => (
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
