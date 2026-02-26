# Full-Stack Weather Data Collection Platform

A production-style weather monitoring system that collects data from [OpenWeatherMap](https://openweathermap.org/) on a scheduled basis, stores historical records in MySQL, and exposes REST APIs consumed by a React frontend.

**Live demo:** [Frontend](http://weather-app-frontend-njc.s3-website.us-east-2.amazonaws.com) · [API](http://13.59.3.49:8080/swagger-ui/index.html)

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot 4, Spring Data JPA, Spring Security |
| Database | MySQL |
| Frontend | React 19, TypeScript, Recharts, Leaflet, Vite |
| Infrastructure | Docker, AWS EC2 (backend), AWS S3 (frontend) |
| CI/CD | GitHub Actions |

---

## Features

- **Scheduled ingestion** — fetches current weather every 10 minutes for all monitored cities
- **Historical storage** — time-series weather records (temperature, humidity, pressure, wind)
- **Daily summaries** — min/max/avg aggregations per city per day
- **5-day forecast** — live from OpenWeatherMap
- **Air quality index** — per city AQI with color-coded labels
- **NEXRAD radar map** — live Doppler radar overlay with city temperature markers
- **Admin panel** — password-protected dashboard to manage cities, trigger manual fetches, and view system stats
- **API docs** — auto-generated Swagger UI at `/swagger-ui/index.html`

---

## Architecture

```
┌──────────────────────────────┐
│  React Frontend (S3)         │
│  App.tsx · AdminPage.tsx     │
│  WeatherChart · WeatherMap   │
└──────────────┬───────────────┘
               │ HTTP/REST
               ▼
┌──────────────────────────────┐
│  Spring Boot Backend (EC2)   │
│                              │
│  Controllers                 │
│  ├─ WeatherController        │  ← public /api/**
│  └─ AdminController          │  ← protected /admin/**
│                              │
│  Services                    │
│  ├─ WeatherQueryService      │
│  ├─ WeatherIngestionService  │  ← @Scheduled every 10 min
│  └─ AdminService             │
│                              │
│  Repositories (JPA)          │
│  └─ WeatherData · City       │
└──────────────┬───────────────┘
               │ JDBC
               ▼
┌──────────────────────────────┐
│  MySQL (Docker)              │
│  weather_data · city         │
│  weather_condition           │
└──────────────────────────────┘
               ▲
               │ REST
┌──────────────────────────────┐
│  OpenWeatherMap API          │
│  Current · Forecast · AQI    │
└──────────────────────────────┘
```

---

## API Endpoints

### Public — `/api/**`

| Method | Endpoint | Params | Description |
|---|---|---|---|
| GET | `/api/cities` | — | All monitored cities |
| GET | `/api/weather` | `city` (optional) | Weather history |
| GET | `/api/weather/latest` | — | Latest record per city |
| GET | `/api/weather/summary` | `city` | Daily min/max/avg |
| GET | `/api/forecast` | `city` | 5-day forecast |
| GET | `/api/aqi` | `city` | Air quality index |
| GET | `/api/weather/heatmap` | — | Daily avg temps per city (last 7 days) |

### Admin — `/admin/**` (HTTP Basic Auth required)

| Method | Endpoint | Body | Description |
|---|---|---|---|
| GET | `/admin/stats` | — | Total records, last fetch, records per city |
| POST | `/admin/cities` | `{ name, state, country }` | Add a city (coords auto-resolved) |
| DELETE | `/admin/cities/{name}` | — | Remove city and its weather data |
| POST | `/admin/fetch` | — | Trigger manual ingestion |

Full interactive docs at `/swagger-ui/index.html`.

---

## Running Locally

### Prerequisites
- Docker & Docker Compose
- [OpenWeatherMap API key](https://openweathermap.org/api) (free tier)

### 1. Clone the repo
```bash
git clone https://github.com/NaturalTC/fullstack-weather-data-collection.git
cd fullstack-weather-data-collection
```

### 2. Create a `.env` file
```env
WEATHER_API_KEY=your_key_here
DB_USERNAME=root
DB_PASSWORD=root
ADMIN_USERNAME=admin
ADMIN_PASSWORD=changeme
```

### 3. Start the stack
```bash
docker compose up
```

This starts MySQL and the Spring Boot app. On first run, 9 default New England cities are seeded automatically. The scheduler fires immediately and then every 10 minutes.

| URL | Description |
|---|---|
| `http://localhost:8080/api/cities` | API |
| `http://localhost:8080/swagger-ui/index.html` | API docs |

### 4. Run the frontend locally
```bash
cd frontend
npm install
npm run dev   # http://localhost:5173
```

---

## Deployment

### Backend → AWS EC2
On push to `main` (changes to `src/**`, `pom.xml`, `Dockerfile`, `docker-compose.yaml`), GitHub Actions SSHes into EC2 and runs:
```bash
git pull && docker compose down app && docker compose build app && docker compose up -d app
```

**Required GitHub Secrets:** `EC2_HOST`, `EC2_SSH_KEY`

### Frontend → AWS S3
On push to `main` (changes to `frontend/**`), GitHub Actions builds the React app with `VITE_API_BASE` injected and syncs the output to S3.

**Required GitHub Secrets:** `EC2_HOST`, `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`

### Environment Variables on EC2
Set these in `~/.bashrc` (or equivalent) on the EC2 instance:
```bash
export WEATHER_API_KEY=...
export DB_PASSWORD=...
export ADMIN_USERNAME=...
export ADMIN_PASSWORD=...
```

---

## Database Schema

```sql
city (id, name, state, country, latitude, longitude)

weather_condition (id, description)

weather_data (
  id, city_id, condition_id,
  temperature, feels_like, humidity, pressure, wind_speed,
  fetched_at
)
```

`weather_data` grows at ~9 rows per 10-minute cycle (one per city). At that rate: ~1,300 rows/day, ~40,000/month.

---

## Project Structure

```
├── src/main/java/.../
│   ├── controller/       # REST endpoints
│   ├── service/          # Business logic
│   ├── repository/       # JPA data access
│   ├── model/            # JPA entities
│   ├── dto/              # API response shapes
│   ├── client/           # OpenWeatherMap HTTP client
│   └── config/           # Security, CORS
├── frontend/src/
│   ├── components/       # React components
│   ├── api/              # fetch wrappers
│   └── types/            # TypeScript interfaces
├── Dockerfile
├── docker-compose.yaml
└── .github/workflows/    # CI/CD pipelines
```

Thanks for reading! :)
