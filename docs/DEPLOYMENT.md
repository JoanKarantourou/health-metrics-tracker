# Health Metrics Tracker - Deployment Guide

## Table of Contents

- [Prerequisites](#prerequisites)
- [Local Development Setup](#local-development-setup)
- [Docker Deployment](#docker-deployment)
- [Environment Configuration](#environment-configuration)
- [Production Considerations](#production-considerations)
- [Troubleshooting](#troubleshooting)

---

## Prerequisites

### For Local Development

| Tool          | Version | Purpose                  |
|---------------|---------|--------------------------|
| Java JDK      | 21+     | Backend runtime          |
| Node.js        | 20 LTS  | Frontend build & dev     |
| PostgreSQL     | 16+     | Database                 |
| Maven          | 3.9+    | Backend build (or use included wrapper) |

### For Docker Deployment

| Tool            | Version | Purpose                  |
|-----------------|---------|--------------------------|
| Docker           | 24+     | Container runtime        |
| Docker Compose   | 2.20+   | Multi-container orchestration |

---

## Local Development Setup

### 1. Database

```bash
# Create the database
psql -U postgres -c "CREATE DATABASE health_metrics_db;"
```

The application uses Hibernate `ddl-auto: update`, so tables are created automatically on first startup.

### 2. Backend

```bash
cd backend

# Run with Maven wrapper (no Maven install needed)
./mvnw spring-boot:run

# Or build and run the JAR
./mvnw clean package -DskipTests
java -jar target/tracker-0.0.1-SNAPSHOT.jar
```

The backend starts on **http://localhost:8080**.

### 3. Frontend

```bash
cd frontend/client

# Install dependencies
npm install

# Start development server
npm start
```

The frontend starts on **http://localhost:3000** and proxies API requests to the backend.

### Verify Everything Works

| Service          | URL                                   |
|------------------|---------------------------------------|
| Frontend         | http://localhost:3000                  |
| Backend API      | http://localhost:8080/api/facilities   |
| Swagger UI       | http://localhost:8080/swagger-ui.html  |
| OpenAPI Spec     | http://localhost:8080/api-docs         |

---

## Docker Deployment

### Quick Start (Single Command)

From the project root:

```bash
docker compose up --build
```

This builds and starts all three services:
- **PostgreSQL** on port 5432
- **Backend** on port 8080
- **Frontend** on port 3000

### Step-by-Step

```bash
# 1. Build all images
docker compose build

# 2. Start services in detached mode
docker compose up -d

# 3. Check status
docker compose ps

# 4. View logs
docker compose logs -f           # All services
docker compose logs -f backend   # Backend only
docker compose logs -f frontend  # Frontend only
docker compose logs -f postgres  # Database only

# 5. Stop services
docker compose down

# 6. Stop and remove volumes (resets database)
docker compose down -v
```

### Docker Architecture

```
┌──────────────────────────────────────────────────────┐
│                  docker compose                       │
│                                                      │
│  ┌──────────────┐  ┌──────────────┐  ┌────────────┐ │
│  │   frontend   │  │   backend    │  │  postgres   │ │
│  │  (nginx)     │  │ (Spring Boot)│  │ (PostgreSQL)│ │
│  │  :3000       │──│  :8080       │──│  :5432      │ │
│  └──────────────┘  └──────────────┘  └────────────┘ │
│                                                      │
│  Volume: postgres_data (persistent)                  │
└──────────────────────────────────────────────────────┘
```

### Container Details

| Container       | Image Base              | Exposed Port | Description                        |
|-----------------|-------------------------|--------------|------------------------------------|
| `hmt-postgres`  | `postgres:16-alpine`    | 5432         | PostgreSQL database                |
| `hmt-backend`   | `eclipse-temurin:21-jre`| 8080         | Spring Boot API (multi-stage build)|
| `hmt-frontend`  | `nginx:alpine`          | 3000         | React app served by Nginx          |

### Multi-Stage Builds

Both the backend and frontend use multi-stage Docker builds to keep images small:

**Backend** (2 stages):
1. **Build stage** — Maven + JDK 21: compiles source and packages JAR
2. **Run stage** — JRE 21 only: runs the JAR (no build tools in final image)

**Frontend** (2 stages):
1. **Build stage** — Node 20: installs dependencies and creates optimized production build
2. **Run stage** — Nginx Alpine: serves static files and proxies API requests to backend

### Nginx Configuration

The frontend Nginx container handles:
- Serving the React build as static files
- Proxying `/api/*` requests to the backend container
- Proxying `/swagger-ui/*` and `/api-docs` to the backend
- React Router support (all routes fall back to `index.html`)
- Static asset caching headers (1 year for JS, CSS, images, fonts)

---

## Environment Configuration

### Backend Environment Variables

These can be set in `docker-compose.yml` or passed via a `.env` file:

| Variable                          | Default                                          | Description                |
|-----------------------------------|--------------------------------------------------|----------------------------|
| `SPRING_DATASOURCE_URL`          | `jdbc:postgresql://localhost:5432/health_metrics_db` | Database JDBC URL          |
| `SPRING_DATASOURCE_USERNAME`     | `postgres`                                       | Database username          |
| `SPRING_DATASOURCE_PASSWORD`     | `postgres`                                       | Database password          |
| `SPRING_JPA_HIBERNATE_DDL_AUTO`  | `update`                                         | Schema management strategy |
| `SERVER_PORT`                    | `8080`                                           | Backend server port        |

### Using a .env File

Create a `.env` file in the project root:

```env
# Database
POSTGRES_DB=health_metrics_db
POSTGRES_USER=postgres
POSTGRES_PASSWORD=your_secure_password

# Backend
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/health_metrics_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=your_secure_password
```

Then reference in `docker-compose.yml` with `env_file: .env` or `${VARIABLE}` syntax.

> **Important:** Never commit `.env` files with real credentials. The `.gitignore` already excludes `.env`.

### Frontend Environment Variables

For the React app, environment variables must be prefixed with `REACT_APP_`:

```env
REACT_APP_API_BASE_URL=http://localhost:8080/api
```

These are embedded at build time, so the frontend image must be rebuilt if they change.

In Docker, the Nginx proxy handles API routing, so the frontend calls `/api/*` relative paths that Nginx forwards to the backend.

---

## Production Considerations

### Security

- [ ] **Enable Spring Security** — Implement JWT authentication and role-based authorization
- [ ] **Enable CSRF protection** for browser-based clients
- [ ] **Use HTTPS** — Add TLS certificates (e.g., via Let's Encrypt + Certbot or a reverse proxy like Traefik)
- [ ] **Change default credentials** — Never use `postgres/postgres` in production
- [ ] **Externalize secrets** — Use Docker secrets, environment variables, or a vault (e.g., HashiCorp Vault)
- [ ] **Set secure headers** — Content-Security-Policy, X-Frame-Options, Strict-Transport-Security
- [ ] **Enable rate limiting** — Protect API endpoints from abuse

### Database

- [ ] **Change `ddl-auto` to `validate`** — Use Flyway or Liquibase for schema migrations in production
- [ ] **Set up backups** — Automated PostgreSQL backups with `pg_dump` or continuous archiving (WAL)
- [ ] **Connection pooling** — Configure HikariCP pool size based on expected load
- [ ] **Add database indexes** — On frequently queried columns (`region`, `category`, `period_start`)

### Performance

- [ ] **JVM tuning** — Set `-Xms` and `-Xmx` flags for the backend container
- [ ] **Enable gzip compression** in Nginx for API responses
- [ ] **Frontend caching** — Nginx already sets cache headers for static assets
- [ ] **Database query optimization** — Add indexes, review slow query logs
- [ ] **Container resource limits** — Set CPU and memory limits in docker-compose

```yaml
# Example resource limits in docker-compose.yml
backend:
  deploy:
    resources:
      limits:
        cpus: '1.0'
        memory: 512M
```

### Logging & Monitoring

- [ ] **Structured logging** — Use JSON log format for production (Logback configuration)
- [ ] **Disable `show-sql`** — Set `spring.jpa.show-sql: false` in production
- [ ] **Health checks** — Spring Boot Actuator endpoints (`/actuator/health`)
- [ ] **Log aggregation** — Forward container logs to ELK Stack, Loki, or CloudWatch
- [ ] **Application monitoring** — Integrate Prometheus + Grafana or Spring Boot Admin

### Scaling

- [ ] **Horizontal scaling** — Run multiple backend instances behind a load balancer
- [ ] **Database read replicas** — Separate read and write traffic for analytics queries
- [ ] **Redis caching** — Cache frequently accessed data (facility lists, indicator lists)
- [ ] **CDN** — Serve frontend static assets from a CDN (CloudFront, Cloudflare)

### CI/CD Pipeline (Example with GitHub Actions)

```yaml
# .github/workflows/deploy.yml
name: Build and Deploy

on:
  push:
    branches: [main]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Run backend tests
        run: cd backend && ./mvnw test

      - name: Set up Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '20'

      - name: Run frontend tests
        run: cd frontend/client && npm ci && npm test -- --watchAll=false

  build:
    needs: test
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Build Docker images
        run: docker compose build

      # Push to registry, deploy to server, etc.
```

---

## Troubleshooting

### Backend won't connect to database

```
Connection refused to localhost:5432
```

- **Local dev:** Ensure PostgreSQL is running: `pg_isready -U postgres`
- **Docker:** The backend connects to `postgres:5432` (container hostname), not `localhost`. Check that the `SPRING_DATASOURCE_URL` uses `postgres` as the host.

### Frontend shows blank page or API errors

- **CORS error in console:** Ensure the backend `SecurityConfig` allows the frontend origin
- **Docker:** The Nginx proxy handles API routing. Verify the `nginx.conf` proxies `/api/` to `http://backend:8080/api/`
- **Network tab shows 502:** The backend container may not be ready yet. Check `docker compose logs backend`

### Docker build fails

```bash
# Clean rebuild from scratch
docker compose build --no-cache

# Check individual service builds
docker build -t hmt-backend ./backend
docker build -t hmt-frontend ./frontend/client
```

### Database data persistence

- Data is stored in the `postgres_data` Docker volume
- `docker compose down` preserves the volume
- `docker compose down -v` **deletes** the volume and all data
- To back up: `docker exec hmt-postgres pg_dump -U postgres health_metrics_db > backup.sql`
- To restore: `docker exec -i hmt-postgres psql -U postgres health_metrics_db < backup.sql`

### Port conflicts

If ports 3000, 5432, or 8080 are already in use:

```yaml
# Change port mapping in docker-compose.yml (host:container)
ports:
  - "3001:3000"   # Access frontend on 3001 instead
  - "5433:5432"   # Access postgres on 5433 instead
  - "8081:8080"   # Access backend on 8081 instead
```

### View running containers and logs

```bash
docker compose ps                    # List containers and their status
docker compose logs -f backend       # Stream backend logs
docker exec -it hmt-postgres psql -U postgres health_metrics_db  # Connect to DB
docker exec -it hmt-backend sh       # Shell into backend container
```
