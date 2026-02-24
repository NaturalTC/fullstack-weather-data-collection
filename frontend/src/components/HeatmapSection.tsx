import type { HeatmapEntryDTO } from '../types';

interface Props {
  data: HeatmapEntryDTO[];
}

function tempToColor(temp: number, min: number, max: number): string {
  const ratio = Math.max(0, Math.min(1, (temp - min) / (max - min)));
  const hue = Math.round(220 - ratio * 220); // 220=blue → 0=red
  return `hsl(${hue}, 75%, 52%)`;
}

function formatShortDate(dateStr: string): string {
  const date = new Date(dateStr + 'T12:00:00');
  return date.toLocaleDateString([], { month: 'short', day: 'numeric' });
}

export default function HeatmapSection({ data }: Props) {
  if (data.length === 0) return <p className="no-data">Not enough data yet for heatmap</p>;

  const cities = [...new Set(data.map((d) => d.city))].sort();
  const dates = [...new Set(data.map((d) => d.date))].sort();
  const lookup = new Map(data.map((d) => [`${d.city}|${d.date}`, d.avgTemp]));

  const temps = data.map((d) => d.avgTemp);
  const minTemp = Math.min(...temps);
  const maxTemp = Math.max(...temps);

  return (
    <div className="heatmap-wrapper">
      <div
        className="heatmap-grid"
        style={{ gridTemplateColumns: `110px repeat(${dates.length}, 1fr)` }}
      >
        {/* Header */}
        <div className="heatmap-cell heatmap-corner" />
        {dates.map((date) => (
          <div key={date} className="heatmap-cell heatmap-date-label">
            {formatShortDate(date)}
          </div>
        ))}

        {/* Rows */}
        {cities.map((city) => (
          <>
            <div key={`${city}-label`} className="heatmap-cell heatmap-city-label">
              {city}
            </div>
            {dates.map((date) => {
              const temp = lookup.get(`${city}|${date}`);
              return (
                <div
                  key={`${city}-${date}`}
                  className="heatmap-cell heatmap-data"
                  style={{ background: temp != null ? tempToColor(temp, minTemp, maxTemp) : '#f1f5f9' }}
                  title={temp != null ? `${city} · ${date} · ${Math.round(temp)}°F avg` : 'No data'}
                >
                  {temp != null ? `${Math.round(temp)}°` : '—'}
                </div>
              );
            })}
          </>
        ))}
      </div>

      <div className="heatmap-legend">
        <span className="legend-cold">{Math.round(minTemp)}°F</span>
        <div className="heatmap-legend-bar" />
        <span className="legend-hot">{Math.round(maxTemp)}°F</span>
      </div>
    </div>
  );
}
