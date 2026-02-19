export interface WeatherDataDTO {
  cityName: string;
  country: string;
  temperature: number;
  feelsLike: number;
  humidity: number;
  pressure: number;
  windSpeed: number;
  description: string;
  timestamp: string;
  fetchedAt: string;
}

export interface CityDTO {
  name: string;
  state: string;
  country: string;
  latitude: number;
  longitude: number;
}
