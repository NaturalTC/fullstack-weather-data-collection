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

// Free NEXRAD composite radar tiles from Iowa State Mesonet — no API key needed
const RADAR_URL = 'https://mesonet.agron.iastate.edu/cache/tile.py/1.0.0/nexrad-n0q-900913/{z}/{x}/{y}.png';

function createMarker(temp: number, isSelected: boolean) {
  const bg = isSelected
    ? 'background: rgba(0,122,255,0.92);'
    : 'background: rgba(255,255,255,0.88);';
  const color = isSelected ? 'color: #fff;' : 'color: #1c1c1e;';
  const shadow = isSelected
    ? 'box-shadow: 0 2px 12px rgba(0,122,255,0.5);'
    : 'box-shadow: 0 1px 6px rgba(0,0,0,0.18);';

  const html = `
    <div style="
      transform: translate(-50%,-50%);
      ${bg}
      ${color}
      ${shadow}
      padding: 4px 10px;
      border-radius: 999px;
      font-size: 13px;
      font-weight: 600;
      font-family: -apple-system, BlinkMacSystemFont, sans-serif;
      letter-spacing: -0.3px;
      white-space: nowrap;
      cursor: pointer;
      backdrop-filter: blur(12px);
      -webkit-backdrop-filter: blur(12px);
      border: 1px solid rgba(255,255,255,0.3);
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
      style={{ height: '440px', width: '100%', borderRadius: '14px' }}
      scrollWheelZoom={false}
    >
      {/* Dark basemap so radar colors pop */}
      <TileLayer
        url="https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png"
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> &copy; <a href="https://carto.com/">CARTO</a>'
      />

      {/* NEXRAD Doppler radar overlay — real precipitation data, updates every ~5min */}
      <TileLayer
        url={RADAR_URL}
        opacity={0.85}
        attribution='Radar: &copy; <a href="https://mesonet.agron.iastate.edu/">Iowa State Mesonet</a>'
      />

      {/* City markers */}
      {cities.map((city) => {
        const w = weatherByCity.get(city.name);
        if (!w) return null;
        return (
          <Marker
            key={city.name}
            position={[city.latitude, city.longitude]}
            icon={createMarker(w.temperature, city.name === selectedCity)}
            eventHandlers={{ click: () => onCitySelect(city.name) }}
            zIndexOffset={city.name === selectedCity ? 1000 : 0}
          >
            <Popup offset={[0, -4]}>
              <div style={{ textAlign: 'center', minWidth: 120, fontFamily: 'system-ui, sans-serif' }}>
                <div style={{ fontWeight: 700, fontSize: '0.9rem', marginBottom: 4 }}>
                  {city.name}, {city.state}
                </div>
                <div style={{ fontSize: '1.8rem', lineHeight: 1, margin: '4px 0' }}>
                  {getWeatherIcon(w.description)}
                </div>
                <div style={{ fontSize: '1.4rem', fontWeight: 700, color: '#2563eb' }}>
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
