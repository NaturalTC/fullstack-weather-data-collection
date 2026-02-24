import type { ForecastDayDTO } from '../types';
import { getWeatherIcon } from '../utils/weatherIcon';

interface Props {
  data: ForecastDayDTO[];
}

function formatDate(dateStr: string): string {
  const date = new Date(dateStr + 'T12:00:00');
  const today = new Date();
  const tomorrow = new Date();
  tomorrow.setDate(today.getDate() + 1);
  if (date.toDateString() === today.toDateString()) return 'Today';
  if (date.toDateString() === tomorrow.toDateString()) return 'Tomorrow';
  return date.toLocaleDateString([], { weekday: 'short', month: 'short', day: 'numeric' });
}

export default function ForecastSection({ data }: Props) {
  return (
    <div className="forecast-strip">
      {data.map((day) => (
        <div key={day.date} className="forecast-card">
          <p className="forecast-date">{formatDate(day.date)}</p>
          <span className="forecast-icon">{getWeatherIcon(day.description)}</span>
          <p className="forecast-desc">{day.description}</p>
          <div className="forecast-temps">
            <span className="forecast-high">{Math.round(day.high)}°</span>
            <span className="forecast-low">{Math.round(day.low)}°</span>
          </div>
          {day.precipChance > 0 && (
            <p className="forecast-pop">💧 {day.precipChance}%</p>
          )}
        </div>
      ))}
    </div>
  );
}
