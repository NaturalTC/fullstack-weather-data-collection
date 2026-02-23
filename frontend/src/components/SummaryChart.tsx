import {
  ComposedChart,
  Area,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from 'recharts';
import type { WeatherSummaryDTO } from '../types';

interface Props {
  data: WeatherSummaryDTO[];
}

export default function SummaryChart({ data }: Props) {
  const chartData = [...data].reverse().map((d) => ({
    date: d.date,
    range: [Math.round(d.minTemperature), Math.round(d.maxTemperature)],
    avg: Math.round(d.avgTemperature),
  }));

  return (
    <ResponsiveContainer width="100%" height={260}>
      <ComposedChart data={chartData} margin={{ top: 5, right: 10, left: -10, bottom: 0 }}>
        <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.06)" />
        <XAxis
          dataKey="date"
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
          formatter={(value) => Array.isArray(value) ? `${value[0]}° – ${value[1]}°F` : `${value}°F`}
        />
        <Area type="monotone" dataKey="range" fill="rgba(96,165,250,0.12)" stroke="#60a5fa" name="Min / Max" />
        <Line type="monotone" dataKey="avg" stroke="#f97316" name="Avg" dot={false} strokeWidth={2} />
      </ComposedChart>
    </ResponsiveContainer>
  );
}
