import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from 'recharts';
import type { WeatherDataDTO } from '../types';

interface Props {
  data: WeatherDataDTO[];
  city: string;
}

export default function WeatherChart({ data, city }: Props) {
  const chartData = [...data]
    .reverse()
    .map((d) => ({
      time: new Date(d.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      temperature: Math.round(d.temperature),
      feelsLike: Math.round(d.feelsLike),
    }));

  return (
    <div className="chart-container">
      <h2>{city} — Temperature History</h2>
      <ResponsiveContainer width="100%" height={300}>
        <LineChart data={chartData}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="time" />
          <YAxis unit="°F" />
          <Tooltip formatter={(value) => `${value}°F`} />
          <Line type="monotone" dataKey="temperature" stroke="#4f9cf9" name="Temperature" dot={false} />
          <Line type="monotone" dataKey="feelsLike" stroke="#f97316" name="Feels Like" dot={false} strokeDasharray="4 4" />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
}
