import type { CityDTO, WeatherDataDTO, WeatherSummaryDTO, ForecastDayDTO, AqiDTO, HeatmapEntryDTO, WeatherAlertDTO } from '../types';

const API_BASE = import.meta.env.VITE_API_BASE ?? '';

export async function fetchCities(): Promise<CityDTO[]> {
  const res = await fetch(`${API_BASE}/api/cities`);
  if (!res.ok) throw new Error('Failed to fetch cities');
  return res.json();
}

export async function fetchLatestWeather(): Promise<WeatherDataDTO[]> {
  const res = await fetch(`${API_BASE}/api/weather/latest`);
  if (!res.ok) throw new Error('Failed to fetch latest weather');
  return res.json();
}

export async function fetchWeatherHistory(city: string): Promise<WeatherDataDTO[]> {
  const res = await fetch(`${API_BASE}/api/weather?city=${encodeURIComponent(city)}`);
  if (!res.ok) throw new Error('Failed to fetch weather history');
  return res.json();
}

export async function fetchDailySummary(city: string): Promise<WeatherSummaryDTO[]> {
  const res = await fetch(`${API_BASE}/api/weather/summary?city=${encodeURIComponent(city)}`);
  if (!res.ok) throw new Error('Failed to fetch daily summary');
  return res.json();
}

export async function fetchForecast(city: string): Promise<ForecastDayDTO[]> {
  const res = await fetch(`${API_BASE}/api/forecast?city=${encodeURIComponent(city)}`);
  if (!res.ok) throw new Error('Failed to fetch forecast');
  return res.json();
}

export async function fetchAqi(city: string): Promise<AqiDTO> {
  const res = await fetch(`${API_BASE}/api/aqi?city=${encodeURIComponent(city)}`);
  if (!res.ok) throw new Error('Failed to fetch AQI');
  return res.json();
}

export async function fetchHeatmap(): Promise<HeatmapEntryDTO[]> {
  const res = await fetch(`${API_BASE}/api/weather/heatmap`);
  if (!res.ok) throw new Error('Failed to fetch heatmap');
  return res.json();
}

export async function fetchAlerts(): Promise<WeatherAlertDTO[]> {
  const res = await fetch(`${API_BASE}/api/alerts`);
  if (!res.ok) throw new Error('Failed to fetch alerts');
  return res.json();
}

export async function createAlert(dto: Omit<WeatherAlertDTO, 'id' | 'triggered' | 'triggeredAt'>): Promise<WeatherAlertDTO> {
  const res = await fetch(`${API_BASE}/api/alerts`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(dto),
  });
  if (!res.ok) throw new Error('Failed to create alert');
  return res.json();
}

export async function deleteAlert(id: number): Promise<void> {
  const res = await fetch(`${API_BASE}/api/alerts/${id}`, { method: 'DELETE' });
  if (!res.ok) throw new Error('Failed to delete alert');
}

export async function dismissAlert(id: number): Promise<WeatherAlertDTO> {
  const res = await fetch(`${API_BASE}/api/alerts/${id}/dismiss`, { method: 'POST' });
  if (!res.ok) throw new Error('Failed to dismiss alert');
  return res.json();
}
