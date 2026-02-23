export function getWeatherIcon(description: string): string {
  const d = description.toLowerCase();
  if (d.includes('thunderstorm')) return '⛈️';
  if (d.includes('snow') || d.includes('sleet') || d.includes('blizzard')) return '🌨️';
  if (d.includes('drizzle') || d.includes('rain')) return '🌧️';
  if (d.includes('mist') || d.includes('fog') || d.includes('haze')) return '🌫️';
  if (d.includes('clear')) return '☀️';
  if (d.includes('few clouds')) return '🌤️';
  if (d.includes('scattered')) return '⛅';
  if (d.includes('broken')) return '🌥️';
  if (d.includes('overcast')) return '☁️';
  return '🌡️';
}
