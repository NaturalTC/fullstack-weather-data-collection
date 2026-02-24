import { useEffect, useState } from 'react';
import { createPortal } from 'react-dom';
import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet';
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

interface HeatPoint {
  x: number;
  y: number;
  color: string;
}

// Absolute scale: 10°F = deep blue, 100°F = deep red
// So winter in New England is always blue, summer always red
function tempToHeatColor(temp: number): string {
  const t = Math.max(0, Math.min(1, (temp - 10) / 90));
  const hue = Math.round((1 - t) * 240);
  return `hsl(${hue}, 85%, 55%)`;
}

function markerLabelColor(temp: number): string {
  const t = Math.max(0, Math.min(1, (temp - 10) / 90));
  const hue = Math.round((1 - t) * 240);
  return `hsl(${hue}, 75%, 38%)`;
}

function createMarker(temp: number, isSelected: boolean) {
  const color = markerLabelColor(temp);
  const ring = isSelected
    ? `border: 2.5px solid white; box-shadow: 0 0 0 3px ${color}66, 0 3px 12px rgba(0,0,0,0.25);`
    : 'border: 2px solid rgba(255,255,255,0.85); box-shadow: 0 2px 8px rgba(0,0,0,0.2);';
  const scale = isSelected ? 'scale(1.18)' : 'scale(1)';

  const html = `
    <div style="
      transform: translate(-50%,-50%) ${scale};
      background: white;
      color: ${color};
      padding: 4px 11px;
      border-radius: 20px;
      font-size: 14px;
      font-weight: 800;
      font-family: system-ui, -apple-system, sans-serif;
      white-space: nowrap;
      cursor: pointer;
      ${ring}
    ">${Math.round(temp)}°</div>
  `;
  return divIcon({ html, className: '', iconSize: [0, 0], iconAnchor: [0, 0] });
}

// Heat layer — SVG blurred circles portaled into the map container
function HeatLayer({ points }: { points: Array<{ lat: number; lon: number; temp: number }> }) {
  const map = useMap();
  const [heat, setHeat] = useState<HeatPoint[]>([]);
  const [size, setSize] = useState({ x: 0, y: 0 });

  useEffect(() => {
    const recalc = () => {
      const s = map.getSize();
      setSize({ x: s.x, y: s.y });

      setHeat(
        points.map((p) => {
          const px = map.latLngToContainerPoint([p.lat, p.lon]);
          return { x: px.x, y: px.y, color: tempToHeatColor(p.temp) };
        })
      );
    };

    map.on('zoom move resize', recalc);
    recalc();
    return () => { map.off('zoom move resize', recalc); };
  }, [map, points]);

  if (!size.x || heat.length === 0) return null;

  return createPortal(
    <svg
      style={{
        position: 'absolute', top: 0, left: 0, zIndex: 400,
        width: size.x, height: size.y,
        pointerEvents: 'none',
        filter: 'blur(48px)',
        opacity: 0.35,
      }}
    >
      {heat.map((p, i) => (
        <circle key={i} cx={p.x} cy={p.y} r={90} fill={p.color} />
      ))}
    </svg>,
    map.getContainer()
  );
}

export default function WeatherMap({ cities, latestWeather, selectedCity, onCitySelect }: Props) {
  const weatherByCity = new Map(latestWeather.map((w) => [w.cityName, w]));

  const heatPoints = cities
    .map((c) => {
      const w = weatherByCity.get(c.name);
      return w ? { lat: c.latitude, lon: c.longitude, temp: w.temperature } : null;
    })
    .filter(Boolean) as Array<{ lat: number; lon: number; temp: number }>;

  return (
    <MapContainer
      center={[43.8, -71.5]}
      zoom={7}
      style={{ height: '420px', width: '100%', borderRadius: '14px' }}
      scrollWheelZoom={false}
    >
      <TileLayer
        url="https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png"
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> &copy; <a href="https://carto.com/">CARTO</a>'
      />
      <HeatLayer points={heatPoints} />
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
                <div style={{ fontSize: '1.4rem', fontWeight: 700, color: markerLabelColor(w.temperature) }}>
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
