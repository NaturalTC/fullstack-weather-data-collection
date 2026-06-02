import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  fetchCities, fetchLatestWeather, fetchWeatherHistory,
  fetchDailySummary, fetchForecast, fetchAqi, fetchAlerts, fetchInsight,
} from './api/weatherApi';
import { LogoMark } from './components/LogoMark';
import type { CityDTO, WeatherDataDTO, WeatherSummaryDTO, ForecastDayDTO, AqiDTO, WeatherAlertDTO, WeatherInsightDTO } from './types';
import CityChip from './components/CurrentWeatherCard';
import WeatherChart from './components/WeatherChart';
import SummaryChart from './components/SummaryChart';
import ForecastSection from './components/ForecastSection';
import WeatherMap from './components/WeatherMap';
import AlertsPanel from './components/AlertsPanel';
import InsightPanel from './components/InsightPanel';
import { getWeatherIcon } from './utils/weatherIcon';
import './App.css';

type Tab = 'history' | 'forecast' | 'summary';

const AQI_COLORS: Record<number, string> = {
  1: '#4ade80',
  2: '#a3e635',
  3: '#facc15',
  4: '#fb923c',
  5: '#f87171',
};

const AQI_LABELS: Record<number, string> = {
  1: 'Good', 2: 'Fair', 3: 'Moderate', 4: 'Poor', 5: 'Very Poor',
};

export default function App() {
  const [cities, setCities]               = useState<CityDTO[]>([]);
  const [latestWeather, setLatestWeather] = useState<WeatherDataDTO[]>([]);
  const [selectedCity, setSelectedCity]   = useState<string>('');
  const [history, setHistory]             = useState<WeatherDataDTO[]>([]);
  const [summary, setSummary]             = useState<WeatherSummaryDTO[]>([]);
  const [forecast, setForecast]           = useState<ForecastDayDTO[]>([]);
  const [aqi, setAqi]                     = useState<AqiDTO | null>(null);
  const [activeTab, setActiveTab]         = useState<Tab>('history');
  const [loading, setLoading]             = useState(true);
  const [alerts,  setAlerts]              = useState<WeatherAlertDTO[]>([]);
  const [insight, setInsight]             = useState<WeatherInsightDTO | null>(null);
  const [unit, setUnit]                   = useState<'imperial' | 'metric'>('imperial');

  useEffect(() => {
    fetchAlerts().then(setAlerts).catch(() => setAlerts([]));

    Promise.all([fetchCities(), fetchLatestWeather(unit)])
      .then(([c, latest]) => {
        setCities(c);
        setLatestWeather(latest);
        setSelectedCity(prev => prev || (latest.length > 0 ? latest[0].cityName : ''));
      })
      .finally(() => setLoading(false));
  }, [unit]);

  useEffect(() => {
    if (!selectedCity) return;
    fetchWeatherHistory(selectedCity, unit).then(setHistory);
    fetchDailySummary(selectedCity, unit).then(setSummary);
    fetchForecast(selectedCity, unit).then(setForecast);
    fetchAqi(selectedCity).then(setAqi).catch(() => setAqi(null));
    fetchInsight(selectedCity).then(setInsight).catch(() => setInsight(null));
  }, [selectedCity, unit]);

  if (loading) {
    return (
      <div className="loading-screen">
        <div className="loading-pulse" />
        <p className="loading-text">reading the atmosphere</p>
      </div>
    );
  }

  const hero     = latestWeather.find(w => w.cityName === selectedCity);
  const heroCity = cities.find(c => c.name === selectedCity);

  return (
    <div className="app">

      {/* ── Nav ── */}
      <nav className="app-nav">
        <Link to="/" className="app-nav-logo">
          <LogoMark size={28} />
          <span className="app-nav-logo-name">WeatherConnect</span>
        </Link>
        <div className="app-nav-links">
          <Link to="/developer">My Keys</Link>
          <Link to="/metrics">Status</Link>
        </div>
        <button
          className="app-unit-toggle"
          onClick={() => setUnit(u => u === 'imperial' ? 'metric' : 'imperial')}
        >
          {unit === 'imperial' ? '°F' : '°C'}
        </button>
      </nav>

      {/* ── Header ── */}
      <header className="app-header">
        <div className="header-eyebrow">NOVACAST · NEW ENGLAND</div>
        <h1 className="header-title">Weather Dashboard</h1>
      </header>

      {/* ── Hero ── */}
      {hero && (
        <div className="hero-card">
          <div className="hero-glow" />
          <div className="hero-top">
            <div className="hero-left">
              <p className="hero-city">
                {hero.cityName}{heroCity ? `, ${heroCity.state}` : ''}
              </p>
              <p className="hero-temp">{Math.round(hero.temperature)}{unit === 'imperial' ? '°F' : '°C'}</p>
              <p className="hero-desc">{hero.description}</p>
              {aqi && (
                <div className="aqi-badge" style={{ '--aqi-color': AQI_COLORS[aqi.index] } as React.CSSProperties}>
                  <span className="aqi-dot" />
                  <span>AQI · {AQI_LABELS[aqi.index]}</span>
                </div>
              )}
            </div>
            <div className="hero-icon">{getWeatherIcon(hero.description)}</div>
          </div>
          <div className="hero-details">
            {[
              { label: 'Feels like', value: `${Math.round(hero.feelsLike)}${unit === 'imperial' ? '°F' : '°C'}` },
              { label: 'Humidity',   value: `${hero.humidity}%` },
              { label: 'Wind',       value: `${hero.windSpeed} mph` },
              { label: 'Pressure',   value: `${hero.pressure} hPa` },
            ].map(({ label, value }) => (
              <div className="detail-item" key={label}>
                <span className="detail-label">{label}</span>
                <span className="detail-value">{value}</span>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* ── City strip ── */}
      <div className="city-strip">
        {latestWeather.map(w => (
          <CityChip
            key={w.cityName}
            data={w}
            selected={w.cityName === selectedCity}
            onClick={() => setSelectedCity(w.cityName)}
          />
        ))}
      </div>

      {/* ── Chart section ── */}
      <div className="panel">
        <div className="panel-header">
          <span className="panel-title">{selectedCity}</span>
          <div className="tabs">
            {(['history', 'forecast', 'summary'] as Tab[]).map(t => (
              <button
                key={t}
                className={activeTab === t ? 'active' : ''}
                onClick={() => setActiveTab(t)}
              >
                {t === 'history' ? 'History' : t === 'forecast' ? 'Forecast' : 'Summary'}
              </button>
            ))}
          </div>
        </div>

        <div key={activeTab} className="tab-content">
          {activeTab === 'history' && (
            history.length > 0
              ? <WeatherChart data={history} />
              : <p className="no-data">No history yet for {selectedCity}</p>
          )}
          {activeTab === 'forecast' && (
            forecast.length > 0
              ? <ForecastSection data={forecast} />
              : <p className="no-data">Loading forecast…</p>
          )}
          {activeTab === 'summary' && (
            summary.length > 0
              ? <SummaryChart data={summary} />
              : <p className="no-data">No summary yet for {selectedCity}</p>
          )}
        </div>
      </div>

      {/* ── AI Insights ── */}
      {insight && (
        <div className="panel" style={{ marginTop: '1rem' }}>
          <InsightPanel insight={insight} />
        </div>
      )}

      {/* ── Alerts ── */}
      <div className="panel" style={{ marginTop: '1rem' }}>
        <AlertsPanel alerts={alerts} cities={cities} onAlertsChange={setAlerts} />
      </div>

      {/* ── Map ── */}
      <div className="panel" style={{ marginTop: '1rem' }}>
        <p className="panel-title" style={{ marginBottom: '1.25rem' }}>Weather Map</p>
        <WeatherMap cities={cities} latestWeather={latestWeather} />
      </div>

      <footer className="app-footer">
        <Link to="/">home</Link>
        <span style={{ color: '#94a3b8', margin: '0 0.5rem' }}>·</span>
        <Link to="/metrics">metrics</Link>
        <span style={{ color: '#94a3b8', margin: '0 0.5rem' }}>·</span>
        <Link to="/admin">admin</Link>
      </footer>
    </div>
  );
}
