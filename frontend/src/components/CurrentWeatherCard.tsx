import type { WeatherDataDTO } from '../types';
import { getWeatherIcon } from '../utils/weatherIcon';

interface Props {
  data: WeatherDataDTO;
  selected: boolean;
  onClick: () => void;
}

export default function CityChip({ data, selected, onClick }: Props) {
  return (
    <button className={`city-chip ${selected ? 'active' : ''}`} onClick={onClick}>
      <span className="chip-icon">{getWeatherIcon(data.description)}</span>
      <p className="chip-name">{data.cityName}</p>
      <p className="chip-temp">{Math.round(data.temperature)}°</p>
    </button>
  );
}
