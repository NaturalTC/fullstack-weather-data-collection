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
