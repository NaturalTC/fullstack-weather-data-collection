import type { WeatherDataDTO } from '../types';

interface Props {
  data: WeatherDataDTO;
}

export default function CurrentWeatherCard({ data }: Props) {
  return (
    <div className="weather-card">
      <h3>{data.cityName}</h3>
      <p className="temp">{Math.round(data.temperature)}°F</p>
      <p className="description">{data.description}</p>
      <div className="details">
        <span>Feels like {Math.round(data.feelsLike)}°F</span>
        <span>Humidity {data.humidity}%</span>
        <span>Wind {data.windSpeed} mph</span>
      </div>
    </div>
  );
}
