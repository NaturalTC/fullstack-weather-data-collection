import { MapContainer, TileLayer } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';

// Free NEXRAD composite radar tiles from Iowa State Mesonet — no API key needed
const RADAR_URL = 'https://mesonet.agron.iastate.edu/cache/tile.py/1.0.0/nexrad-n0q-900913/{z}/{x}/{y}.png';

export default function WeatherMap() {
  return (
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
    </MapContainer>
  );
}
