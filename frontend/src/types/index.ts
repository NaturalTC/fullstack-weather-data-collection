export interface WeatherDataDTO {
  cityName: string;
  country: string;
  temperature: number;
  feelsLike: number;
  humidity: number;
  pressure: number;
  windSpeed: number;
  description: string;
  fetchedAt: string;
}

export interface CityDTO {
  name: string;
  state: string;
  country: string;
  latitude: number;
  longitude: number;
}

export interface WeatherSummaryDTO {
  date: string;
  minTemperature: number;
  maxTemperature: number;
  avgTemperature: number;
}

export interface ForecastDayDTO {
  date: string;
  high: number;
  low: number;
  precipChance: number;
  description: string;
}

export interface AqiDTO {
  index: number;
  label: string;
}

export interface HeatmapEntryDTO {
  city: string;
  date: string;
  avgTemp: number;
}
