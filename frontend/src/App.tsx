import { useEffect, useState } from 'react';
import { fetchCities, fetchLatestWeather, fetchWeatherHistory, fetchDailySummary } from './api/weatherApi';
import type { CityDTO, WeatherDataDTO, WeatherSummaryDTO } from './types';
import CityChip from './components/CurrentWeatherCard';
import WeatherChart from './components/WeatherChart';
import SummaryChart from './components/SummaryChart';
import { getWeatherIcon } from './utils/weatherIcon';
import './App.css';

type Tab = 'history' | 'summary';

export default function App() {
  const [cities, setCities] = useState<CityDTO[]>([]);
  const [latestWeather, setLatestWeather] = useState<WeatherDataDTO[]>([]);
  const [selectedCity, setSelectedCity] = useState<string>('');
  const [history, setHistory] = useState<WeatherDataDTO[]>([]);
  const [summary, setSummary] = useState<WeatherSummaryDTO[]>([]);
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
            <button
              className={activeTab === 'history' ? 'active' : ''}
              onClick={() => setActiveTab('history')}
            >
              History
            </button>
            <button
              className={activeTab === 'summary' ? 'active' : ''}
              onClick={() => setActiveTab('summary')}
            >
              Daily Summary
            </button>
          </div>
        </div>

        {activeTab === 'history' && (
          history.length > 0
            ? <WeatherChart data={history} />
            : <p className="no-data">No history yet for {selectedCity}</p>
        )}
        {activeTab === 'summary' && (
          summary.length > 0
            ? <SummaryChart data={summary} />
            : <p className="no-data">No summary yet for {selectedCity}</p>
        )}
      </div>
    </div>
  );
}
