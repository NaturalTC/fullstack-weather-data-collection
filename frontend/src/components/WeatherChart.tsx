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
  // Show last 24 hours only (144 points at 10-min intervals)
  const chartData = [...data]
    .slice(0, 144)
    .reverse()
    .map((d) => ({
      time: new Date(d.fetchedAt + 'Z').toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      temperature: Math.round(d.temperature),
      feelsLike: Math.round(d.feelsLike),
    }));

  const tickInterval = Math.max(1, Math.floor(chartData.length / 6));

  return (
    <div>
      <ResponsiveContainer width="100%" height={260}>
        <LineChart data={chartData} margin={{ top: 5, right: 10, left: -15, bottom: 0 }}>
          <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
          <XAxis
            dataKey="time"
            interval={tickInterval}
            tick={{ fill: '#94a3b8', fontSize: 11 }}
            axisLine={{ stroke: '#e2e8f0' }}
            tickLine={false}
          />
          <YAxis
            unit="°"
            tick={{ fill: '#94a3b8', fontSize: 11 }}
            axisLine={false}
            tickLine={false}
          />
          <Tooltip
            contentStyle={{ background: 'white', border: 'none', borderRadius: 12, boxShadow: '0 4px 24px rgba(0,0,0,0.12)', fontSize: 13 }}
            labelStyle={{ color: '#64748b', marginBottom: 4, fontWeight: 600 }}
            formatter={(value, name) => [`${value}°F`, name]}
          />
          <Line type="monotone" dataKey="temperature" stroke="#2563eb" name="Temperature" dot={false} strokeWidth={2.5} isAnimationActive={false} />
          <Line type="monotone" dataKey="feelsLike" stroke="#f97316" name="Feels Like" dot={false} strokeWidth={2} strokeDasharray="5 4" isAnimationActive={false} />
        </LineChart>
      </ResponsiveContainer>
      <div className="chart-legend">
        <span className="legend-item"><span className="legend-dot" style={{ background: '#2563eb' }} />Temperature</span>
        <span className="legend-item"><span className="legend-dot" style={{ background: '#f97316' }} />Feels Like</span>
      </div>
    </div>
  );
}
