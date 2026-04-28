import { useEffect, useState, useCallback } from 'react';
import { Link } from 'react-router-dom';

const API_BASE = import.meta.env.VITE_API_BASE ?? '';

interface HealthData {
  status: string;
  components?: Record<string, { status: string }>;
}

interface MetricData {
  measurements: { statistic: string; value: number }[];
}

interface CacheStat {
  name: string;
  hits: number;
  misses: number;
}

interface Metrics {
  health: HealthData | null;
  memUsed: number | null;
  memMax: number | null;
  cpuUsage: number | null;
  cpuCount: number | null;
  uptime: number | null;
  httpCount: number | null;
  caches: CacheStat[];
  cityCount: number | null;
  lastIngestion: string | null;
}

const CACHES = ['latestWeather', 'dailySummary', 'forecast', 'aqi', 'heatmap'];

async function getMetric(path: string): Promise<MetricData | null> {
  try {
    const r = await fetch(`${API_BASE}${path}`);
    return r.ok ? r.json() : null;
  } catch {
    return null;
  }
}

function fmtBytes(bytes: number): string {
  return bytes >= 1024 ** 2
    ? `${(bytes / 1024 ** 2).toFixed(0)} MB`
    : `${(bytes / 1024).toFixed(0)} KB`;
}

function fmtUptime(secs: number): string {
  const h = Math.floor(secs / 3600);
  const m = Math.floor((secs % 3600) / 60);
  if (h > 0) return `${h}h ${m}m`;
  return `${m}m ${Math.floor(secs % 60)}s`;
}

function toUtc(iso: string): Date {
  // MySQL datetimes have no timezone indicator — append Z so the browser treats them as UTC
  return new Date(/[Zz]|[+-]\d{2}:?\d{2}$/.test(iso) ? iso : iso + 'Z');
}

function timeAgo(iso: string): string {
  const diff = Math.round((Date.now() - toUtc(iso).getTime()) / 1000);
  if (diff < 0) return 'just now';
  if (diff < 60) return `${diff}s ago`;
  if (diff < 3600) return `${Math.floor(diff / 60)}m ago`;
  return `${Math.floor(diff / 3600)}h ago`;
}

function ingestionColor(iso: string): string {
  const mins = (Date.now() - toUtc(iso).getTime()) / 60000;
  if (mins < 15) return '#4ade80';
  if (mins < 30) return '#facc15';
  return '#f87171';
}

export default function MetricsDashboard() {
  const [metrics, setMetrics] = useState<Metrics>({
    health: null, memUsed: null, memMax: null,
    cpuUsage: null, cpuCount: null, uptime: null,
    httpCount: null, caches: [], cityCount: null, lastIngestion: null,
  });
  const [loading, setLoading] = useState(true);
  const [updated, setUpdated] = useState<Date | null>(null);

  const load = useCallback(async () => {
    const cacheReqs = CACHES.flatMap(n => [
      getMetric(`/actuator/metrics/cache.gets?tag=name:${n}&tag=result:hit`),
      getMetric(`/actuator/metrics/cache.gets?tag=name:${n}&tag=result:miss`),
    ]);

    const [health, memUsed, memMax, cpu, cpuCount, uptimeM, http, ...cacheRes] =
      await Promise.all([
        fetch(`${API_BASE}/actuator/health`).then(r => r.ok ? r.json() : null).catch(() => null),
        getMetric('/actuator/metrics/jvm.memory.used'),
        getMetric('/actuator/metrics/jvm.memory.max'),
        getMetric('/actuator/metrics/process.cpu.usage'),
        getMetric('/actuator/metrics/system.cpu.count'),
        getMetric('/actuator/metrics/process.uptime'),
        getMetric('/actuator/metrics/http.server.requests'),
        ...cacheReqs,
      ]);

    const caches: CacheStat[] = CACHES.map((name, i) => ({
      name,
      hits: cacheRes[i * 2]?.measurements[0]?.value ?? 0,
      misses: cacheRes[i * 2 + 1]?.measurements[0]?.value ?? 0,
    }));

    let cityCount = null, lastIngestion = null;
    try {
      const [cities, latest] = await Promise.all([
        fetch(`${API_BASE}/api/cities`).then(r => r.json()),
        fetch(`${API_BASE}/api/weather/latest`).then(r => r.json()),
      ]);
      cityCount = (cities as unknown[]).length;
      if ((latest as { fetchedAt: string }[]).length > 0) {
        lastIngestion = (latest as { fetchedAt: string }[]).reduce(
          (a, b) => (a > b.fetchedAt ? a : b.fetchedAt),
          (latest as { fetchedAt: string }[])[0].fetchedAt,
        );
      }
    } catch { /* non-blocking */ }

    setMetrics({
      health,
      memUsed: memUsed?.measurements[0]?.value ?? null,
      memMax: memMax?.measurements[0]?.value ?? null,
      cpuUsage: cpu?.measurements[0]?.value ?? null,
      cpuCount: cpuCount?.measurements[0]?.value ?? null,
      uptime: uptimeM?.measurements[0]?.value ?? null,
      httpCount: http?.measurements.find(x => x.statistic === 'COUNT')?.value ?? null,
      caches,
      cityCount,
      lastIngestion,
    });
    setUpdated(new Date());
    setLoading(false);
  }, []);

  useEffect(() => {
    load();
    const iv = setInterval(load, 30_000);
    return () => clearInterval(iv);
  }, [load]);

  if (loading) {
    return (
      <div style={s.page}>
        <p style={{ color: '#94a3b8', fontSize: '0.9rem' }}>Loading metrics…</p>
      </div>
    );
  }

  const isUp = metrics.health?.status === 'UP';
  const memPct = metrics.memUsed && metrics.memMax
    ? (metrics.memUsed / metrics.memMax) * 100
    : null;
  const cpuPct = metrics.cpuUsage != null ? metrics.cpuUsage * 100 : null;

  return (
    <div style={s.page}>

      {/* Header */}
      <div style={s.header}>
        <div>
          <Link to="/" style={s.navLink}>← Weather</Link>
          <h1 style={s.title}>System Metrics</h1>
        </div>
        <div style={{ textAlign: 'right' }}>
          {updated && (
            <p style={s.updatedAt}>Updated {updated.toLocaleTimeString()}</p>
          )}
          <button style={s.refreshBtn} onClick={load}>Refresh</button>
        </div>
      </div>

      {/* Stat tiles */}
      <div style={s.tileGrid}>
        <StatTile
          label="Status"
          value={isUp ? 'UP' : metrics.health ? 'DOWN' : 'Unknown'}
          color={isUp ? '#4ade80' : metrics.health ? '#f87171' : '#94a3b8'}
        />
        <StatTile
          label="Uptime"
          value={metrics.uptime != null ? fmtUptime(metrics.uptime) : '—'}
        />
        <StatTile
          label="CPU Usage"
          value={cpuPct != null ? `${cpuPct.toFixed(1)}%` : '—'}
          color={cpuPct != null && cpuPct > 80 ? '#f87171' : '#60a5fa'}
          sub={metrics.cpuCount != null ? `${metrics.cpuCount} cores` : undefined}
        />
        <StatTile
          label="HTTP Requests"
          value={metrics.httpCount != null ? metrics.httpCount.toLocaleString() : '—'}
          sub="since startup"
        />
        <StatTile
          label="Cities"
          value={metrics.cityCount != null ? String(metrics.cityCount) : '—'}
          sub="monitored"
        />
        <StatTile
          label="Last Ingestion"
          value={metrics.lastIngestion ? timeAgo(metrics.lastIngestion) : '—'}
          color={metrics.lastIngestion ? ingestionColor(metrics.lastIngestion) : '#94a3b8'}
          sub="scheduled every 10m"
        />
      </div>

      {/* JVM Memory */}
      <div style={s.card}>
        <p style={s.cardLabel}>JVM Memory</p>
        {memPct != null ? (
          <>
            <div style={s.barMeta}>
              <span style={s.subText}>
                {metrics.memUsed ? fmtBytes(metrics.memUsed) : '—'} used
              </span>
              <span style={s.subText}>
                {metrics.memMax ? fmtBytes(metrics.memMax) : '—'} max &nbsp;·&nbsp; {memPct.toFixed(1)}%
              </span>
            </div>
            <div style={s.barTrack}>
              <div style={{
                ...s.barFill,
                width: `${memPct}%`,
                background: memPct > 85 ? '#f87171' : memPct > 65 ? '#facc15' : '#60a5fa',
              }} />
            </div>
          </>
        ) : (
          <p style={s.subText}>Unavailable — ensure <code>/actuator/metrics</code> is exposed</p>
        )}
      </div>

      {/* Cache performance */}
      <div style={s.card}>
        <p style={s.cardLabel}>Cache Performance</p>
        <table style={s.table}>
          <thead>
            <tr>
              {['Cache', 'Hits', 'Misses', 'Hit Rate', ''].map(h => (
                <th key={h} style={s.th}>{h}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {metrics.caches.map(c => {
              const total = c.hits + c.misses;
              const rate = total === 0 ? null : (c.hits / total) * 100;
              return (
                <tr key={c.name}>
                  <td style={s.td}>
                    <code style={s.codeTag}>{c.name}</code>
                  </td>
                  <td style={s.td}>{c.hits.toLocaleString()}</td>
                  <td style={s.td}>{c.misses.toLocaleString()}</td>
                  <td style={{
                    ...s.td,
                    color: rate == null ? '#94a3b8'
                      : rate >= 70 ? '#4ade80'
                      : rate >= 40 ? '#facc15'
                      : '#f87171',
                    fontVariantNumeric: 'tabular-nums',
                  }}>
                    {rate == null ? '—' : `${rate.toFixed(0)}%`}
                  </td>
                  <td style={{ ...s.td, width: 120 }}>
                    {rate != null && (
                      <div style={s.miniTrack}>
                        <div style={{
                          ...s.miniFill,
                          width: `${rate}%`,
                          background: rate >= 70 ? '#4ade80' : rate >= 40 ? '#facc15' : '#f87171',
                        }} />
                      </div>
                    )}
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>

      {/* Component health */}
      {metrics.health?.components && Object.keys(metrics.health.components).length > 0 && (
        <div style={s.card}>
          <p style={s.cardLabel}>Component Health</p>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.625rem' }}>
            {Object.entries(metrics.health.components).map(([name, comp]) => (
              <div key={name} style={s.compChip}>
                <span style={{ color: comp.status === 'UP' ? '#4ade80' : '#f87171', marginRight: '0.4rem' }}>
                  ●
                </span>
                <span style={{ fontSize: '0.85rem', color: '#e2e8f0' }}>{name}</span>
                <span style={{ fontSize: '0.72rem', color: '#64748b', marginLeft: '0.4rem' }}>
                  {comp.status}
                </span>
              </div>
            ))}
          </div>
        </div>
      )}

      <footer style={s.footer}>
        <Link to="/admin" style={s.navLink}>admin panel</Link>
      </footer>
    </div>
  );
}

function StatTile({ label, value, sub, color }: {
  label: string;
  value: string;
  sub?: string;
  color?: string;
}) {
  return (
    <div style={s.tile}>
      <p style={s.tileLabel}>{label}</p>
      <p style={{ ...s.tileValue, color: color ?? '#f8fafc' }}>{value}</p>
      {sub && <p style={s.tileSub}>{sub}</p>}
    </div>
  );
}

const s: Record<string, React.CSSProperties> = {
  page: {
    minHeight: '100vh',
    background: '#0f1117',
    padding: '2rem 1rem',
    fontFamily: 'system-ui, sans-serif',
    color: '#e2e8f0',
    maxWidth: 900,
    margin: '0 auto',
  },
  header: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: '1.75rem',
  },
  navLink: { color: '#60a5fa', textDecoration: 'none', fontSize: '0.82rem' },
  title: { margin: '0.3rem 0 0', fontSize: '1.5rem', color: '#f8fafc', fontWeight: 600 },
  updatedAt: { margin: '0 0 0.4rem', fontSize: '0.72rem', color: '#475569' },
  refreshBtn: {
    padding: '0.35rem 0.9rem',
    borderRadius: '0.375rem',
    border: '1px solid #334155',
    background: 'transparent',
    color: '#94a3b8',
    cursor: 'pointer',
    fontSize: '0.8rem',
  },
  tileGrid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(3, 1fr)',
    gap: '0.75rem',
    marginBottom: '0.75rem',
  },
  tile: {
    background: '#1e2130',
    borderRadius: '0.625rem',
    padding: '1rem 1.1rem',
    border: '1px solid #1e293b',
  },
  tileLabel: {
    margin: '0 0 0.35rem',
    fontSize: '0.68rem',
    color: '#475569',
    textTransform: 'uppercase' as const,
    letterSpacing: '0.06em',
  },
  tileValue: {
    margin: 0,
    fontSize: '1.35rem',
    fontWeight: 700,
    fontVariantNumeric: 'tabular-nums',
    lineHeight: 1.1,
  },
  tileSub: { margin: '0.25rem 0 0', fontSize: '0.68rem', color: '#475569' },
  card: {
    background: '#1e2130',
    borderRadius: '0.625rem',
    padding: '1.25rem 1.5rem',
    border: '1px solid #1e293b',
    marginBottom: '0.75rem',
  },
  cardLabel: {
    margin: '0 0 1rem',
    fontSize: '0.7rem',
    color: '#475569',
    textTransform: 'uppercase' as const,
    letterSpacing: '0.07em',
  },
  subText: { margin: 0, fontSize: '0.78rem', color: '#64748b' },
  barMeta: {
    display: 'flex',
    justifyContent: 'space-between',
    marginBottom: '0.5rem',
  },
  barTrack: {
    height: 10,
    borderRadius: 5,
    background: '#0f172a',
    overflow: 'hidden',
  },
  barFill: {
    height: '100%',
    borderRadius: 5,
    transition: 'width 0.5s ease',
  },
  table: { width: '100%', borderCollapse: 'collapse' as const },
  th: {
    textAlign: 'left' as const,
    padding: '0.35rem 0.5rem',
    borderBottom: '1px solid #1e293b',
    fontSize: '0.72rem',
    color: '#475569',
    fontWeight: 500,
  },
  td: { padding: '0.5rem 0.5rem', borderBottom: '1px solid #0f172a', fontSize: '0.875rem' },
  codeTag: {
    fontFamily: 'monospace',
    fontSize: '0.78rem',
    color: '#93c5fd',
    background: '#0f172a',
    padding: '0.15rem 0.45rem',
    borderRadius: '0.25rem',
  },
  miniTrack: { height: 6, borderRadius: 3, background: '#0f172a', overflow: 'hidden' },
  miniFill: { height: '100%', borderRadius: 3, transition: 'width 0.5s ease' },
  compChip: {
    display: 'flex',
    alignItems: 'center',
    background: '#0f172a',
    border: '1px solid #1e293b',
    borderRadius: '0.375rem',
    padding: '0.4rem 0.8rem',
  },
  footer: { marginTop: '2rem', textAlign: 'center' as const, paddingBottom: '1rem' },
};
