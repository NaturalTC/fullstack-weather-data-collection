import { MapContainer, TileLayer, CircleMarker, Popup } from 'react-leaflet';
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
  // Map temperature range ~0°F–70°F to hue 220 (blue) → 0 (red)
  const clamped = Math.max(0, Math.min(80, temp));
  const hue = Math.round(220 - (clamped / 80) * 220);
  return `hsl(${hue}, 85%, 50%)`;
}

export default function WeatherMap({ cities, latestWeather, selectedCity, onCitySelect }: Props) {
  const weatherByCity = new Map(latestWeather.map((w) => [w.cityName, w]));

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
      {cities.map((city) => {
        const w = weatherByCity.get(city.name);
        if (!w) return null;
        const isSelected = city.name === selectedCity;
        return (
          <CircleMarker
            key={city.name}
            center={[city.latitude, city.longitude]}
            radius={isSelected ? 36 : 28}
            pathOptions={{
              fillColor: tempToColor(w.temperature),
              fillOpacity: 0.92,
              color: isSelected ? '#1d4ed8' : 'white',
              weight: isSelected ? 3 : 2,
            }}
            eventHandlers={{ click: () => onCitySelect(city.name) }}
          >
            <Popup>
              <div style={{ textAlign: 'center', minWidth: 110 }}>
                <strong>{city.name}, {city.state}</strong>
                <div style={{ fontSize: '1.5rem', margin: '4px 0' }}>{getWeatherIcon(w.description)}</div>
                <div style={{ fontSize: '1.3rem', fontWeight: 700 }}>{Math.round(w.temperature)}°F</div>
                <div style={{ fontSize: '0.8rem', color: '#64748b', textTransform: 'capitalize' }}>{w.description}</div>
                <div style={{ fontSize: '0.75rem', color: '#94a3b8', marginTop: 4 }}>
                  Humidity {w.humidity}% · Wind {w.windSpeed} mph
                </div>
              </div>
            </Popup>
          </CircleMarker>
        );
      })}
    </MapContainer>
  );
}
