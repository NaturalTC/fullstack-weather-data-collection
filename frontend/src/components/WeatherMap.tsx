import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
import { divIcon } from 'leaflet';
import 'leaflet/dist/leaflet.css';
import type { CityDTO, WeatherDataDTO } from '../types';
import { getWeatherIcon } from '../utils/weatherIcon';

interface Props {
  cities: CityDTO[];
  latestWeather: WeatherDataDTO[];
  selectedCity: string;
  onCitySelect: (city: string) => void;
}

function tempToColor(temp: number): string {
  const clamped = Math.max(0, Math.min(80, temp));
  const hue = Math.round(220 - (clamped / 80) * 220);
  return `hsl(${hue}, 78%, 46%)`;
}

function createMarker(temp: number, isSelected: boolean) {
  const color = tempToColor(temp);
  const border = isSelected
    ? 'border: 2.5px solid white; box-shadow: 0 0 0 3px rgba(29,78,216,0.55), 0 4px 16px rgba(0,0,0,0.3);'
    : 'border: 2px solid rgba(255,255,255,0.7); box-shadow: 0 2px 10px rgba(0,0,0,0.22);';
  const scale = isSelected ? 'scale(1.18)' : 'scale(1)';

  const html = `
    <div style="
      transform: translate(-50%, -50%) ${scale};
      background: ${color};
      color: white;
      padding: 5px 13px;
      border-radius: 20px;
      font-size: 15px;
      font-weight: 700;
      font-family: system-ui, -apple-system, sans-serif;
      letter-spacing: -0.3px;
      white-space: nowrap;
      cursor: pointer;
      transition: transform 0.15s ease;
      ${border}
    ">${Math.round(temp)}°</div>
  `;

  return divIcon({ html, className: '', iconSize: [0, 0], iconAnchor: [0, 0] });
}

export default function WeatherMap({ cities, latestWeather, selectedCity, onCitySelect }: Props) {
  const weatherByCity = new Map(latestWeather.map((w) => [w.cityName, w]));

  return (
    <MapContainer
      center={[43.8, -71.5]}
      zoom={7}
      style={{ height: '420px', width: '100%', borderRadius: '14px' }}
      scrollWheelZoom={false}
      zoomControl={true}
    >
      <TileLayer
        url="https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png"
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> &copy; <a href="https://carto.com/">CARTO</a>'
      />
      {cities.map((city) => {
        const w = weatherByCity.get(city.name);
        if (!w) return null;
        return (
          <Marker
            key={city.name}
            position={[city.latitude, city.longitude]}
            icon={createMarker(w.temperature, city.name === selectedCity)}
            eventHandlers={{ click: () => onCitySelect(city.name) }}
          >
            <Popup offset={[0, -6]}>
              <div style={{ textAlign: 'center', minWidth: 120, fontFamily: 'system-ui, sans-serif' }}>
                <div style={{ fontWeight: 700, fontSize: '0.9rem', marginBottom: 4 }}>
                  {city.name}, {city.state}
                </div>
                <div style={{ fontSize: '1.8rem', lineHeight: 1, marginBottom: 4 }}>
                  {getWeatherIcon(w.description)}
                </div>
                <div style={{ fontSize: '1.4rem', fontWeight: 700, color: tempToColor(w.temperature) }}>
                  {Math.round(w.temperature)}°F
                </div>
                <div style={{ fontSize: '0.75rem', color: '#64748b', textTransform: 'capitalize', marginTop: 2 }}>
                  {w.description}
                </div>
                <div style={{ fontSize: '0.72rem', color: '#94a3b8', marginTop: 6, display: 'flex', justifyContent: 'center', gap: 8 }}>
                  <span>💧 {w.humidity}%</span>
                  <span>💨 {w.windSpeed} mph</span>
                </div>
              </div>
            </Popup>
          </Marker>
        );
      })}
    </MapContainer>
  );
}
