import { MapContainer, TileLayer, CircleMarker, Popup } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import type { CityDTO, WeatherDataDTO } from '../types';

const RADAR_URL = 'https://mesonet.agron.iastate.edu/cache/tile.py/1.0.0/nexrad-n0q-900913/{z}/{x}/{y}.png';

const TEMP_SCALE = [
  { max: 20,  color: '#60a5fa', label: '< 20°F' },
  { max: 40,  color: '#818cf8', label: '20–40°F' },
  { max: 60,  color: '#34d399', label: '40–60°F' },
  { max: 75,  color: '#fbbf24', label: '60–75°F' },
  { max: Infinity, color: '#f87171', label: '> 75°F' },
];

function tempColor(temp: number): string {
  return TEMP_SCALE.find(s => temp < s.max)!.color;
}

interface Props {
  cities: CityDTO[];
  latestWeather: WeatherDataDTO[];
}

export default function WeatherMap({ cities, latestWeather }: Props) {
  return (
    <div style={{ position: 'relative' }}>
      <MapContainer
        center={[43.8, -71.5]}
        zoom={7}
        style={{ height: '440px', width: '100%', borderRadius: '14px' }}
        scrollWheelZoom={false}
      >
        <TileLayer
          url="https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png"
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> &copy; <a href="https://carto.com/">CARTO</a>'
        />
        <TileLayer
          url={RADAR_URL}
          opacity={0.85}
          attribution='Radar: &copy; <a href="https://mesonet.agron.iastate.edu/">Iowa State Mesonet</a>'
        />
        {cities.map(city => {
          const weather = latestWeather.find(w => w.cityName === city.name);
          if (!weather) return null;
          const color = tempColor(weather.temperature);
          return (
            <CircleMarker
              key={city.name}
              center={[city.latitude, city.longitude]}
              radius={22}
              pathOptions={{ color, fillColor: color, fillOpacity: 0.85, weight: 2 }}
            >
              <Popup>
                <strong>{city.name}, {city.state}</strong><br />
                {Math.round(weather.temperature)}°F — {weather.description}
              </Popup>
            </CircleMarker>
          );
        })}
      </MapContainer>

      {/* Legend */}
      <div style={{
        position: 'absolute', bottom: '1.5rem', right: '0.75rem', zIndex: 1000,
        background: 'rgba(15,17,23,0.82)', backdropFilter: 'blur(6px)',
        borderRadius: '0.5rem', padding: '0.6rem 0.8rem',
        display: 'flex', flexDirection: 'column', gap: '0.3rem',
        fontSize: '0.72rem', color: '#e2e8f0', pointerEvents: 'none',
      }}>
        <span style={{ fontWeight: 600, marginBottom: '0.1rem', color: '#94a3b8' }}>Temperature</span>
        {TEMP_SCALE.map(s => (
          <div key={s.label} style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
            <span style={{
              width: 10, height: 10, borderRadius: '50%',
              background: s.color, display: 'inline-block', flexShrink: 0,
            }} />
            {s.label}
          </div>
        ))}
      </div>
    </div>
  );
}
