import {
  ComposedChart,
  Area,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from 'recharts';
import type { WeatherSummaryDTO } from '../types';

interface Props {
  data: WeatherSummaryDTO[];
  city: string;
}

export default function SummaryChart({ data, city }: Props) {
  const chartData = [...data].reverse().map((d) => ({
    date: d.date,
    range: [Math.round(d.minTemperature), Math.round(d.maxTemperature)],
    avg: Math.round(d.avgTemperature),
  }));

  return (
    <div className="chart-container">
      <h2>{city} — Daily Summary</h2>
      <ResponsiveContainer width="100%" height={300}>
        <ComposedChart data={chartData}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="date" />
          <YAxis unit="°F" />
          <Tooltip formatter={(value) => Array.isArray(value) ? `${value[0]}°F – ${value[1]}°F` : `${value}°F`} />
          <Legend />
          <Area
            type="monotone"
            dataKey="range"
            fill="#4f9cf933"
            stroke="#4f9cf9"
            name="Min / Max Range"
          />
          <Line
            type="monotone"
            dataKey="avg"
            stroke="#f97316"
            name="Avg Temp"
            dot={false}
            strokeWidth={2}
          />
        </ComposedChart>
      </ResponsiveContainer>
    </div>
  );
}
