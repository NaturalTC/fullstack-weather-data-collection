import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  fetchCities, fetchLatestWeather, fetchWeatherHistory,
  fetchDailySummary, fetchForecast, fetchAqi,
} from './api/weatherApi';
import type { CityDTO, WeatherDataDTO, WeatherSummaryDTO, ForecastDayDTO, AqiDTO } from './types';
import CityChip from './components/CurrentWeatherCard';
import WeatherChart from './components/WeatherChart';
import SummaryChart from './components/SummaryChart';
import ForecastSection from './components/ForecastSection';
import WeatherMap from './components/WeatherMap';
import { getWeatherIcon } from './utils/weatherIcon';
import './App.css';

type Tab = 'history' | 'forecast' | 'summary';

const AQI_COLORS: Record<number, string> = {
  1: '#22c55e',
  2: '#84cc16',
  3: '#eab308',
  4: '#f97316',
  5: '#ef4444',
};

export default function App() {
  const [cities, setCities] = useState<CityDTO[]>([]);
  const [latestWeather, setLatestWeather] = useState<WeatherDataDTO[]>([]);
  const [selectedCity, setSelectedCity] = useState<string>('');
  const [history, setHistory] = useState<WeatherDataDTO[]>([]);
  const [summary, setSummary] = useState<WeatherSummaryDTO[]>([]);
  const [forecast, setForecast] = useState<ForecastDayDTO[]>([]);
  const [aqi, setAqi] = useState<AqiDTO | null>(null);
  const [activeTab, setActiveTab] = useState<Tab>('history');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([fetchCities(), fetchLatestWeather()])
      .then(([c, latest]) => {
        setCities(c);
        setLatestWeather(latest);
        if (latest.length > 0) setSelectedCity(latest[0].cityName);
      })
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    if (!selectedCity) return;
    fetchWeatherHistory(selectedCity).then(setHistory);
    fetchDailySummary(selectedCity).then(setSummary);
    fetchForecast(selectedCity).then(setForecast);
    fetchAqi(selectedCity).then(setAqi).catch(() => setAqi(null));
  }, [selectedCity]);

  if (loading) return <div className="loading">Loading</div>;

  const hero = latestWeather.find((w) => w.cityName === selectedCity);
  const heroCity = cities.find((c) => c.name === selectedCity);

  return (
    <div className="app">
      <header>
        <p className="app-title">New England Weather</p>
      </header>

      {hero && (
        <div className="hero-card">
          <div className="hero-top">
            <div>
              <p className="hero-city">{hero.cityName}{heroCity ? `, ${heroCity.state}` : ''}</p>
              <p className="hero-temp">{Math.round(hero.temperature)}°</p>
              <p className="hero-desc">{hero.description}</p>
              {aqi && (
                <div className="aqi-badge" style={{ borderColor: AQI_COLORS[aqi.index] }}>
                  <span className="aqi-dot" style={{ background: AQI_COLORS[aqi.index] }} />
                  <span>AQI · {aqi.label}</span>
                </div>
              )}
            </div>
            <div className="hero-icon">{getWeatherIcon(hero.description)}</div>
          </div>
          <div className="hero-details">
            <div className="detail-item">
              <span className="detail-label">Feels like</span>
              <span className="detail-value">{Math.round(hero.feelsLike)}°F</span>
            </div>
            <div className="detail-item">
              <span className="detail-label">Humidity</span>
              <span className="detail-value">{hero.humidity}%</span>
            </div>
            <div className="detail-item">
              <span className="detail-label">Wind</span>
              <span className="detail-value">{hero.windSpeed} mph</span>
            </div>
            <div className="detail-item">
              <span className="detail-label">Pressure</span>
              <span className="detail-value">{hero.pressure} hPa</span>
            </div>
          </div>
        </div>
      )}

      <div className="city-strip">
        {latestWeather.map((w) => (
          <CityChip
            key={w.cityName}
            data={w}
            selected={w.cityName === selectedCity}
            onClick={() => setSelectedCity(w.cityName)}
          />
        ))}
      </div>

      <div className="chart-section">
        <div className="chart-header">
          <span className="chart-title">{selectedCity}</span>
          <div className="tabs">
            <button className={activeTab === 'history' ? 'active' : ''} onClick={() => setActiveTab('history')}>
              History
            </button>
            <button className={activeTab === 'forecast' ? 'active' : ''} onClick={() => setActiveTab('forecast')}>
              Forecast
            </button>
            <button className={activeTab === 'summary' ? 'active' : ''} onClick={() => setActiveTab('summary')}>
              Daily Summary
            </button>
          </div>
        </div>

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

      <div className="heatmap-section">
        <p className="chart-title" style={{ marginBottom: '1.25rem' }}>Weather Map</p>
        <WeatherMap cities={cities} latestWeather={latestWeather} />
      </div>

      <footer style={{ textAlign: 'center', padding: '1.5rem 0 2rem', opacity: 0.3, fontSize: '0.75rem' }}>
        <Link to="/admin" style={{ color: 'inherit', textDecoration: 'none' }}>admin</Link>
      </footer>
    </div>
  );
}
