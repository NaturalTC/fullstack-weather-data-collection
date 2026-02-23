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
}

export default function WeatherChart({ data }: Props) {
  const chartData = [...data]
    .reverse()
    .map((d) => ({
      time: new Date(d.fetchedAt + 'Z').toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      temperature: Math.round(d.temperature),
      feelsLike: Math.round(d.feelsLike),
    }));

  return (
    <ResponsiveContainer width="100%" height={260}>
      <LineChart data={chartData} margin={{ top: 5, right: 10, left: -10, bottom: 0 }}>
        <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.06)" />
        <XAxis
          dataKey="time"
          tick={{ fill: 'rgba(255,255,255,0.3)', fontSize: 11 }}
          axisLine={{ stroke: 'rgba(255,255,255,0.06)' }}
          tickLine={false}
        />
        <YAxis
          unit="°"
          tick={{ fill: 'rgba(255,255,255,0.3)', fontSize: 11 }}
          axisLine={false}
          tickLine={false}
        />
        <Tooltip
          contentStyle={{ background: '#1e293b', border: '1px solid rgba(255,255,255,0.1)', borderRadius: 10, fontSize: 13 }}
          labelStyle={{ color: 'rgba(255,255,255,0.5)', marginBottom: 4 }}
          formatter={(value) => [`${value}°F`]}
        />
        <Line type="monotone" dataKey="temperature" stroke="#60a5fa" name="Temperature" dot={false} strokeWidth={2} />
        <Line type="monotone" dataKey="feelsLike" stroke="#f97316" name="Feels Like" dot={false} strokeWidth={2} strokeDasharray="4 4" />
      </LineChart>
    </ResponsiveContainer>
  );
}
