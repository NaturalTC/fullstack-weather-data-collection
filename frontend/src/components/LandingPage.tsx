import { Link } from 'react-router-dom';
import { LogoMark } from './LogoMark';
import './LandingPage.css';

const FEATURES = [
  { icon: '⚡', title: 'Live Weather API',      desc: 'Current conditions refreshed every 10 minutes. Temperature, humidity, wind, pressure, and AQI — all in one request.' },
  { icon: '📊', title: 'Historical Data',        desc: 'Query archived records by city and date range. Built for ML training, analytics, and long-range trend research.' },
  { icon: '🤖', title: 'AI Anomaly Detection',   desc: 'GPT-4o mini flags statistically unusual conditions and generates natural language severity scores automatically.' },
  { icon: '🔔', title: 'Email Alerts',           desc: 'Set metric thresholds on any city. We send an email the moment conditions are triggered — temperature, wind, humidity, pressure.' },
  { icon: '📅', title: '5-Day Forecasts',        desc: 'Daily high/low temperature, precipitation probability, and conditions. Live from OpenWeatherMap, cached for speed.' },
  { icon: '🗺️', title: 'Multi-City Coverage',    desc: '74 US cities with full lat/lon coordinates on every response. More cities added continuously.' },
  { icon: '🔑', title: 'API Key Auth',           desc: 'Generate and manage API keys from your developer dashboard. Authenticate every request with a single header.' },
];

const TRUST = [
  'REST API', 'JSON Responses', 'API Key Auth',
  'Email Alerts', 'Historical Data', 'AI Insights',
  '99.9% Uptime',
];

const ENDPOINTS = [
  { method: 'GET', path: '/api/weather/latest',          desc: 'Current conditions for all cities' },
  { method: 'GET', path: '/api/weather?city=Boston',     desc: 'Full history for a city' },
  { method: 'GET', path: '/api/weather/summary',         desc: 'Daily min / max / avg temperatures' },
  { method: 'GET', path: '/api/forecast?city=Boston',    desc: '5-day forecast' },
  { method: 'GET', path: '/api/aqi?city=Boston',         desc: 'Air quality index' },
  { method: 'GET', path: '/api/weather/insights',        desc: 'AI-powered weather insight ⚡' },
];

const PRICING = [
  {
    name: 'Free', price: '$0', period: 'forever',
    desc: 'For side projects and exploration',
    features: ['1,000 requests / day', '7-day history', 'Current conditions', 'Community support'],
    cta: 'Get API Key', highlight: false,
  },
  {
    name: 'Pro', price: '$19', period: 'per month',
    desc: 'For production applications',
    features: ['50,000 requests / day', 'Full 2-year history', '5-day forecasts', 'AQI data', 'Email alerts', 'Email support'],
    cta: 'Start Free Trial', highlight: true,
  },
  {
    name: 'Enterprise', price: 'Custom', period: '',
    desc: 'For large-scale platforms',
    features: ['Unlimited requests', 'Custom city coverage', 'Dedicated infrastructure', '99.9% SLA', 'On-call support'],
    cta: 'Contact Us', highlight: false,
  },
];

const CITY_BLIPS = [
  { cx: 62, cy: 30 }, { cx: 52, cy: 42 }, { cx: 68, cy: 48 },
  { cx: 44, cy: 35 }, { cx: 58, cy: 22 }, { cx: 72, cy: 38 },
  { cx: 48, cy: 55 }, { cx: 65, cy: 58 }, { cx: 38, cy: 48 },
];

function WeatherRadar() {
  return (
    <svg viewBox="0 0 100 100" className="lp-radar" xmlns="http://www.w3.org/2000/svg">
      <circle cx="50" cy="50" r="12"  fill="none" stroke="rgba(194,255,80,0.35)" strokeWidth="0.3" />
      <circle cx="50" cy="50" r="24"  fill="none" stroke="rgba(194,255,80,0.28)" strokeWidth="0.3" />
      <circle cx="50" cy="50" r="36"  fill="none" stroke="rgba(194,255,80,0.20)" strokeWidth="0.3" />
      <circle cx="50" cy="50" r="48"  fill="none" stroke="rgba(194,255,80,0.12)" strokeWidth="0.3" />

      <line x1="50" y1="1"  x2="50" y2="99" stroke="rgba(194,255,80,0.12)" strokeWidth="0.25" />
      <line x1="1"  y1="50" x2="99" y2="50" stroke="rgba(194,255,80,0.12)" strokeWidth="0.25" />
      <line x1="15" y1="15" x2="85" y2="85" stroke="rgba(194,255,80,0.06)" strokeWidth="0.25" />
      <line x1="85" y1="15" x2="15" y2="85" stroke="rgba(194,255,80,0.06)" strokeWidth="0.25" />

      <g className="lp-radar-sweep" style={{ transformOrigin: '50px 50px' }}>
        <line x1="50" y1="50" x2="50" y2="2" stroke="rgba(194,255,80,0.9)" strokeWidth="0.6" strokeLinecap="round" />
        <path d="M50,50 L50,2 A48,48 0 0,1 98,50 Z" fill="rgba(194,255,80,0.06)" />
      </g>

      {CITY_BLIPS.map((b, i) => (
        <circle key={i} cx={b.cx} cy={b.cy} r="0.9" fill="#c2ff50" opacity="0.75" />
      ))}

      <circle cx="50" cy="50" r="1.5" fill="#c2ff50" />
      <circle cx="50" cy="50" r="3.5" fill="none" stroke="#c2ff50" strokeWidth="0.35" opacity="0.5" />
    </svg>
  );
}

export default function LandingPage() {
  return (
    <div className="lp">

      {/* ── Nav ── */}
      <nav className="lp-nav">
        <div className="lp-nav-inner">
          <div className="lp-logo">
            <LogoMark size={36} />
            <span className="lp-logo-name">WeatherConnect</span>
          </div>
          <div className="lp-nav-links">
            <a href="#features">Platform</a>
            <a href="#pricing">Pricing</a>
            <a href="#api">API</a>
            <Link to="/metrics"><span className="lp-status-dot" /> Status</Link>
          </div>
          <div className="lp-nav-actions">
            <Link to="/login"  className="lp-btn-login">Log in</Link>
            <Link to="/signup" className="lp-btn-lime">Try for free</Link>
          </div>
        </div>
      </nav>

      {/* ── Hero ── */}
      <section className="lp-hero">
        {/* Aurora glow */}
        <div className="lp-aurora" />
        <div className="lp-aurora lp-aurora--2" />

        {/* Radar */}
        <WeatherRadar />

        <div className="lp-hero-content">
          <div className="lp-badge">
            <span className="lp-badge-dot" />
            New England Weather API · Now with AI Insights
          </div>

          <h1 className="lp-headline">
            <span className="lp-headline-white">Weather Data.</span>
            <span className="lp-headline-muted">Reimagined.</span>
          </h1>

          <p className="lp-subtext">
            High-frequency weather observations, anomaly detection, and forecasting APIs
            built for modern developers. Free to start, production-ready at scale.
          </p>

          <div className="lp-hero-ctas">
            <Link to="/signup"    className="lp-btn-lime lp-btn-lg">Get your API key</Link>
            <Link to="/dashboard" className="lp-btn-outline-hero lp-btn-lg">View live data →</Link>
          </div>

          <div className="lp-hero-stats">
            {[
              { label: 'Cities monitored',  value: '74',   unit: '' },
              { label: 'Data refresh rate', value: '10',   unit: 'min' },
              { label: 'Uptime',            value: '99.9', unit: '%' },
              { label: 'Data points',       value: '2.1M', unit: '+' },
            ].map(s => (
              <div className="lp-stat-card" key={s.label}>
                <p className="lp-stat-card-label">{s.label}</p>
                <p className="lp-stat-card-value">
                  {s.value}
                  {s.unit && <span className="lp-stat-card-unit">{s.unit}</span>}
                </p>
              </div>
            ))}
          </div>
        </div>
      </section>

      <hr className="lp-divider" />

      {/* ── Trust strip ── */}
      <div className="lp-trust">
        {TRUST.map(t => (
          <div className="lp-trust-item" key={t}>
            <span className="lp-trust-check">✓</span>
            <span>{t}</span>
          </div>
        ))}
      </div>

      <hr className="lp-divider" />

      {/* ── Features ── */}
      <section className="lp-features-section" id="features">
        <p className="lp-eyebrow">Platform</p>
        <h2 className="lp-features-title">
          <span className="white">One API, </span>
          <span className="muted">endless</span>
          <br />
          <span className="muted">possibilities.</span>
        </h2>
        <div className="lp-features-grid">
          {FEATURES.map(f => (
            <div className="lp-feature-card" key={f.title}>
              <div className="lp-feature-icon">{f.icon}</div>
              <h3 className="lp-feature-title">{f.title}</h3>
              <p className="lp-feature-desc">{f.desc}</p>
            </div>
          ))}
        </div>
      </section>

      {/* ── API Reference ── */}
      <section className="lp-api-section" id="api">
        <p className="lp-eyebrow">API Reference</p>
        <h2 className="lp-section-title">Simple, predictable endpoints.</h2>
        <p className="lp-section-sub">RESTful JSON. One header. All the data.</p>
        <div className="lp-endpoints">
          {ENDPOINTS.map(e => (
            <div className="lp-endpoint" key={e.path}>
              <span className="lp-method">{e.method}</span>
              <span className="lp-path">{e.path}</span>
              <span className="lp-endpoint-desc">{e.desc}</span>
            </div>
          ))}
        </div>
        <div className="lp-api-note">
          Base URL: <code>http://localhost:8080</code> · Authenticate via <code>X-API-Key</code> header
        </div>
      </section>

      {/* ── Pricing ── */}
      <section className="lp-pricing-section" id="pricing">
        <p className="lp-eyebrow">Pricing</p>
        <h2 className="lp-pricing-title">Start free, scale when ready.</h2>
        <div className="lp-pricing-grid">
          {PRICING.map(p => (
            <div className={`lp-pricing-card${p.highlight ? ' lp-pricing-card--highlight' : ''}`} key={p.name}>
              {p.highlight && <div className="lp-pricing-badge">Popular</div>}
              <div className="lp-pricing-name">{p.name}</div>
              <div>
                <span className="lp-pricing-amount">{p.price}</span>
                {p.period && <span className="lp-pricing-period"> / {p.period}</span>}
              </div>
              <p className="lp-pricing-desc">{p.desc}</p>
              <ul className="lp-pricing-features">
                {p.features.map(f => (
                  <li key={f}><span className="lp-check">✓</span> {f}</li>
                ))}
              </ul>
              <button className={`lp-pricing-cta ${p.highlight ? 'lp-pricing-cta--lime' : 'lp-pricing-cta--outline'}`}>
                {p.cta}
              </button>
            </div>
          ))}
        </div>
      </section>

      {/* ── Footer ── */}
      <footer className="lp-footer">
        <div className="lp-footer-inner">
          <div className="lp-footer-logo">
            <LogoMark size={36} />
            <span className="lp-logo-name">WeatherConnect</span>
          </div>
          <div className="lp-footer-links">
            <Link to="/dashboard">Live Dashboard</Link>
            <Link to="/metrics">System Status</Link>
            <a href="#api">API Docs</a>
            <a href="#pricing">Pricing</a>
          </div>
          <p className="lp-footer-copy">© 2026 WeatherConnect. Weather data for developers.</p>
        </div>
      </footer>

    </div>
  );
}
