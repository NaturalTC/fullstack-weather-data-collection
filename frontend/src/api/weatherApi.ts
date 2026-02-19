import type { CityDTO, WeatherDataDTO } from '../types';

export async function fetchCities(): Promise<CityDTO[]> {
  const res = await fetch('/api/cities');
  if (!res.ok) throw new Error('Failed to fetch cities');
  return res.json();
}

export async function fetchLatestWeather(): Promise<WeatherDataDTO[]> {
  const res = await fetch('/api/weather/latest');
  if (!res.ok) throw new Error('Failed to fetch latest weather');
  return res.json();
}

export async function fetchWeatherHistory(city: string): Promise<WeatherDataDTO[]> {
  const res = await fetch(`/api/weather?city=${encodeURIComponent(city)}`);
  if (!res.ok) throw new Error('Failed to fetch weather history');
  return res.json();
}
