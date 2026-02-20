import { useEffect, useState } from 'react';
import { fetchCities, fetchLatestWeather, fetchWeatherHistory, fetchDailySummary } from './api/weatherApi';
import type { CityDTO, WeatherDataDTO, WeatherSummaryDTO } from './types';
import CurrentWeatherCard from './components/CurrentWeatherCard';
import WeatherChart from './components/WeatherChart';
import SummaryChart from './components/SummaryChart';
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
      .then(([cities, latest]) => {
        setCities(cities);
        setLatestWeather(latest);
        if (cities.length > 0) setSelectedCity(cities[0].name);
      })
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    if (!selectedCity) return;
    fetchWeatherHistory(selectedCity).then(setHistory);
    fetchDailySummary(selectedCity).then(setSummary);
  }, [selectedCity]);

  if (loading) return <div className="loading">Loading...</div>;

  return (
    <div className="app">
      <header>
        <h1>New England Weather</h1>
      </header>

      <section className="current-weather">
        <h2>Current Conditions</h2>
        <div className="card-grid">
          {latestWeather.map((w) => (
            <CurrentWeatherCard key={w.cityName} data={w} />
          ))}
        </div>
      </section>

      <section className="history">
        <div className="section-controls">
          <div className="city-selector">
            <label htmlFor="city-select">City</label>
            <select
              id="city-select"
              value={selectedCity}
              onChange={(e) => setSelectedCity(e.target.value)}
            >
              {cities.map((c) => (
                <option key={c.name} value={c.name}>
                  {c.name}, {c.state}
                </option>
              ))}
            </select>
          </div>
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
            ? <WeatherChart data={history} city={selectedCity} />
            : <p className="no-data">No history yet for {selectedCity}</p>
        )}
        {activeTab === 'summary' && (
          summary.length > 0
            ? <SummaryChart data={summary} city={selectedCity} />
            : <p className="no-data">No summary yet for {selectedCity}</p>
        )}
      </section>
    </div>
  );
}
