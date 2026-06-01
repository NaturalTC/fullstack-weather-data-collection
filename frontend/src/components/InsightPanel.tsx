import type { WeatherInsightDTO } from '../types';

const TREND_ICONS: Record<string, string> = {
  warming: '📈',
  cooling: '📉',
  stable:  '➡️',
};

const SEVERITY_COLOR = (score: number) => {
  if (score >= 70) return '#ef4444';
  if (score >= 40) return '#f97316';
  if (score >= 20) return '#eab308';
  return '#22c55e';
};

interface Props {
  insight: WeatherInsightDTO;
}

export default function InsightPanel({ insight }: Props) {
  const sevColor = SEVERITY_COLOR(insight.severityScore);

  return (
    <div className="insight-panel">
      <div className="insight-header">
        <span className="insight-label">AI INSIGHTS</span>
        <span className="insight-badge insight-badge--ai">⚡ Powered by GPT-4o mini</span>
      </div>

      {/* Summary */}
      <p className="insight-summary">{insight.summary}</p>

      {/* Metrics row */}
      <div className="insight-metrics">

        {/* Severity */}
        <div className="insight-metric">
          <span className="insight-metric-label">Severity</span>
          <div className="insight-severity-bar">
            <div
              className="insight-severity-fill"
              style={{ width: `${insight.severityScore}%`, background: sevColor }}
            />
          </div>
          <span className="insight-metric-value" style={{ color: sevColor }}>
            {insight.severityScore}/100
          </span>
        </div>

        {/* Trend */}
        <div className="insight-metric">
          <span className="insight-metric-label">Trend</span>
          <span className="insight-trend">
            {TREND_ICONS[insight.trend] ?? '➡️'} {insight.trend}
          </span>
        </div>

        {/* Deviation */}
        <div className="insight-metric">
          <span className="insight-metric-label">vs 7-day avg</span>
          <span
            className="insight-metric-value"
            style={{ color: insight.tempDeviation > 0 ? '#f97316' : insight.tempDeviation < 0 ? '#60a5fa' : '#94a3b8' }}
          >
            {insight.tempDeviation > 0 ? '+' : ''}{insight.tempDeviation.toFixed(1)}°F
          </span>
        </div>
      </div>

      {/* Anomaly */}
      {insight.anomalyFlag && insight.anomalyDescription && (
        <div className="insight-anomaly">
          <span className="insight-anomaly-icon">⚠️</span>
          <span>{insight.anomalyDescription}</span>
        </div>
      )}
    </div>
  );
}
