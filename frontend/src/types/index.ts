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

export interface WeatherInsightDTO {
  city: string;
  summary: string;
  trend: 'warming' | 'cooling' | 'stable';
  anomalyDescription: string | null;
  severityScore: number;
  anomalyFlag: boolean;
  currentTemp: number;
  weeklyAvgTemp: number;
  tempDeviation: number;
}

export interface WeatherAlertDTO {
  id: number | null;
  cityName: string;
  metric: 'TEMPERATURE' | 'FEELS_LIKE' | 'HUMIDITY' | 'WIND_SPEED' | 'PRESSURE';
  operator: 'ABOVE' | 'BELOW';
  threshold: number;
  label: string;
  recipientEmail: string;
  triggered: boolean;
  triggeredAt: string | null;
}
