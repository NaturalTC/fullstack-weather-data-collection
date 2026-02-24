import {
  LineChart,
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
    high: Math.round(d.maxTemperature),
    avg: Math.round(d.avgTemperature),
    low: Math.round(d.minTemperature),
  }));

  return (
    <div>
      <ResponsiveContainer width="100%" height={260}>
        <LineChart data={chartData} margin={{ top: 5, right: 10, left: -15, bottom: 0 }}>
          <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
          <XAxis
            dataKey="date"
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
          <Line type="monotone" dataKey="high" stroke="#ef4444" name="High" dot={{ r: 4, fill: '#ef4444' }} strokeWidth={2} />
          <Line type="monotone" dataKey="avg" stroke="#2563eb" name="Avg" dot={{ r: 4, fill: '#2563eb' }} strokeWidth={2.5} />
          <Line type="monotone" dataKey="low" stroke="#0ea5e9" name="Low" dot={{ r: 4, fill: '#0ea5e9' }} strokeWidth={2} />
        </LineChart>
      </ResponsiveContainer>
      <div className="chart-legend">
        <span className="legend-item"><span className="legend-dot" style={{ background: '#ef4444' }} />High</span>
        <span className="legend-item"><span className="legend-dot" style={{ background: '#2563eb' }} />Avg</span>
        <span className="legend-item"><span className="legend-dot" style={{ background: '#0ea5e9' }} />Low</span>
      </div>
    </div>
  );
}
