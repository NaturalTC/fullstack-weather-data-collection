# Project Context

You are assisting me on a full-stack Weather Data Collection Platform.

This project is designed as a production-style system to demonstrate backend, cloud, and full-stack engineering skills suitable for a junior backend or full-stack engineer role.

The system collects weather data from an external API on a scheduled basis, stores historical records in PostgreSQL, and exposes REST APIs for querying and analytics. A React frontend consumes these APIs to visualize time-series data. The system is containerized and deployed to AWS.

---

# Tech Stack

## Backend
- Java 17
- Maven
- Spring Boot
- Spring Web
- Spring Data JPA
- MySQL
- Scheduled jobs (`@Scheduled`)
- RESTful APIs
- Docker

## Frontend
- React
- TypeScript
- Fetch or Axios
- Basic charting (e.g., Recharts or Chart.js)

## Cloud / DevOps
- AWS EC2 (backend)
- AWS S3 (data backups & frontend hosting)
- Docker & Docker Compose
- Environment-based configuration

---

# Architecture Principles

- Clean separation of concerns (Controller → Service → Repository)
- DTOs are used for all API responses (never expose entities directly)
- Configuration via environment variables and Spring profiles
- External API integrations are isolated in client/service classes
- Time-series data is indexed and query-efficient
- Logging and error handling are production-minded

---

# Coding Guidelines

- Favor clarity and maintainability over cleverness
- Avoid overengineering or unnecessary abstractions
- Explain tradeoffs and design decisions when proposing solutions
- Use best practices, but keep implementations realistic for a solo project
- Assume this project may be reviewed by hiring managers

---

# How to Help Me

When responding:
- Give step-by-step guidance when implementing features
- Provide code snippets when useful, but explain *why* they work
- Flag potential pitfalls (performance, scaling, API limits)
- Suggest improvements only when they add real value
- Keep suggestions aligned with the current project phase

---

# Current Project Goals

- Build a reliable scheduled weather ingestion service
- Store and query historical weather data efficiently
- Expose clean REST APIs for frontend consumption
- Visualize data meaningfully in the frontend
- Deploy the system using Docker and AWS
- Prepare the project for resume and interview discussion

---

# Out of Scope (Unless Explicitly Requested)

- Overly complex microservice architectures
- Premature optimization
- Enterprise-only tooling
- Unnecessary frontend frameworks

---

# Tone & Style Preference

- Be direct, practical, and engineering-focused
- Treat me like a developer, not a beginner
- Use clear explanations without excessive theory