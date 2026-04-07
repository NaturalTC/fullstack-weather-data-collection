import { useState } from 'react';
import type { WeatherAlertDTO, CityDTO } from '../types';
import { createAlert, deleteAlert } from '../api/weatherApi';

const METRIC_LABELS: Record<WeatherAlertDTO['metric'], string> = {
  TEMPERATURE: 'Temperature (°F)',
  FEELS_LIKE:  'Feels Like (°F)',
  HUMIDITY:    'Humidity (%)',
  WIND_SPEED:  'Wind Speed (mph)',
  PRESSURE:    'Pressure (hPa)',
};

interface Props {
  alerts: WeatherAlertDTO[];
  cities: CityDTO[];
  onAlertsChange: (updated: WeatherAlertDTO[]) => void;
}

export default function AlertsPanel({ alerts, cities, onAlertsChange }: Props) {
  const [cityName, setCityName]     = useState(cities[0]?.name ?? '');
  const [metric, setMetric]         = useState<WeatherAlertDTO['metric']>('TEMPERATURE');
  const [operator, setOperator]     = useState<'ABOVE' | 'BELOW'>('ABOVE');
  const [threshold, setThreshold]   = useState('');
  const [label, setLabel]           = useState('');
  const [recipientEmail, setEmail]  = useState('');
  const [saving, setSaving]         = useState(false);

  async function handleCreate(e: React.FormEvent) {
    e.preventDefault();
    if (!label.trim() || !threshold || !recipientEmail.trim()) return;
    setSaving(true);
    try {
      const created = await createAlert({
        cityName, metric, operator,
        threshold: Number(threshold),
        label: label.trim(),
        recipientEmail: recipientEmail.trim(),
      });
      onAlertsChange([...alerts, created]);
      setLabel('');
      setThreshold('');
      setEmail('');
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete(id: number) {
    await deleteAlert(id);
    onAlertsChange(alerts.filter(a => a.id !== id));
  }

  return (
    <div className="alerts-panel">
      <p className="panel-title" style={{ marginBottom: '1.25rem' }}>Email Alerts</p>

      {/* existing alert rules */}
      {alerts.length > 0 && (
        <div className="alert-rules-list">
          {alerts.map(a => (
            <div key={a.id} className="alert-rule">
              <div className="alert-rule-info">
                <span className="alert-rule-label">{a.label}</span>
                <span className="alert-rule-sub">
                  {a.cityName} · {METRIC_LABELS[a.metric]} {a.operator.toLowerCase()} {a.threshold} · {a.recipientEmail}
                </span>
              </div>
              <button className="alert-delete-btn" onClick={() => handleDelete(a.id!)}>✕</button>
            </div>
          ))}
        </div>
      )}

      {/* new alert form */}
      <form className="alert-form" onSubmit={handleCreate}>
        <p className="alert-form-title">Add Alert Rule</p>
        <div className="alert-form-row">
          <select value={cityName} onChange={e => setCityName(e.target.value)}>
            {cities.map(c => <option key={c.name} value={c.name}>{c.name}</option>)}
          </select>
          <select value={metric} onChange={e => setMetric(e.target.value as WeatherAlertDTO['metric'])}>
            {(Object.keys(METRIC_LABELS) as WeatherAlertDTO['metric'][]).map(m => (
              <option key={m} value={m}>{METRIC_LABELS[m]}</option>
            ))}
          </select>
          <select value={operator} onChange={e => setOperator(e.target.value as 'ABOVE' | 'BELOW')}>
            <option value="ABOVE">is above</option>
            <option value="BELOW">is below</option>
          </select>
          <input
            type="number"
            placeholder="Value"
            value={threshold}
            onChange={e => setThreshold(e.target.value)}
            required
          />
        </div>
        <div className="alert-form-row">
          <input
            className="alert-label-input"
            type="text"
            placeholder="Alert name  e.g. Boston heat alert"
            value={label}
            onChange={e => setLabel(e.target.value)}
            required
          />
          <input
            className="alert-label-input"
            type="email"
            placeholder="Email to notify  e.g. you@gmail.com"
            value={recipientEmail}
            onChange={e => setEmail(e.target.value)}
            required
          />
          <button type="submit" className="alert-add-btn" disabled={saving}>
            {saving ? 'Adding…' : 'Add Alert'}
          </button>
        </div>
      </form>
    </div>
  );
}
