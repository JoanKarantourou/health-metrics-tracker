# Health Metrics Tracker (HMT)

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.2-green?logo=springboot)
![React](https://img.shields.io/badge/React-19-blue?logo=react)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?logo=postgresql)
![License](https://img.shields.io/badge/License-MIT-yellow)

A full-stack application for tracking, aggregating, and visualizing health indicators across healthcare facilities.

## Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Features](#features)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Setup Instructions](#setup-instructions)
- [Docker Deployment](#docker-deployment)
- [API Endpoints](#api-endpoints)
- [API Documentation (Swagger)](#api-documentation-swagger)
- [Database Schema](#database-schema)
- [Testing](#testing)
- [Future Enhancements](#future-enhancements)
- [Contributing](#contributing)
- [Author](#author)
- [License](#license)

## Overview

Health Metrics Tracker is a complete health information system that enables healthcare organizations to:

- Manage healthcare facilities across regions and districts
- Define and categorize health indicators (e.g., Malaria Cases, Vaccination Coverage)
- Submit periodic health data values linked to facilities and indicators
- Aggregate and visualize data by region with interactive dashboards
- Search, filter, and paginate through large datasets

The system follows a layered architecture with a Spring Boot REST API backend and a React single-page application frontend.

## Tech Stack

**Backend:**
- Java 21
- Spring Boot 4.0.2
- Spring Data JPA (Hibernate)
- Spring Security
- Spring Cache
- PostgreSQL
- Maven
- Lombok
- SpringDoc OpenAPI (Swagger UI)

**Frontend:**
- React 19
- Material-UI (MUI) 7
- Recharts 3 (data visualization)
- React Router 7
- Axios (HTTP client)
- TanStack React Query 5
- date-fns 4

**Testing:**
- JUnit 5 & Mockito (backend unit + integration tests)
- React Testing Library (frontend component tests)

**DevOps:**
- Docker & Docker Compose
- Nginx (frontend serving & reverse proxy)

## Architecture

```
┌─────────────────────┐         ┌─────────────────────────────────────────┐
│                     │  HTTP   │              Backend (8080)              │
│   React Frontend    │◄───────►│                                         │
│   (port 3000)       │  REST   │  Controller ──► Service ──► Repository  │
│                     │         │                                  │      │
│  - Dashboard        │         │  - FacilityController            │      │
│  - Facilities Page  │         │  - HealthIndicatorController     │      │
│  - Data Entry Page  │         │  - DataValueController           ▼      │
│                     │         │                            ┌──────────┐ │
└─────────────────────┘         │                            │PostgreSQL│ │
                                │                            └──────────┘ │
                                └─────────────────────────────────────────┘
```

## Features

**Facility Management**
- CRUD operations for healthcare facilities
- Facility types: Hospital, Clinic, Health Center
- Geographic data (region, district, coordinates)
- Soft delete (deactivate/reactivate) support
- Advanced search and filtering by name, code, region, type, and active status
- Paginated results with configurable page size and sorting

**Health Indicator Tracking**
- Define health indicators with categories (Disease Control, Maternal Health, Child Health, etc.)
- Support for multiple data types: NUMBER, PERCENTAGE, BOOLEAN
- Category-based filtering and data type grouping

**Data Entry & Validation**
- Submit health data values tied to a facility, indicator, and reporting period
- Period types: DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY
- Type-specific validation (e.g., percentages must be 0-100)
- Duplicate prevention via unique constraint on (facility, indicator, period)
- Future-date prevention

**Dashboard & Visualization**
- Summary statistic cards (total facilities, active facilities, indicators, data points)
- Interactive line chart for indicator trends over time
- Regional comparison bar chart
- Indicator selector for dynamic chart updates

**API & Error Handling**
- RESTful API with consistent error responses
- Global exception handling with proper HTTP status codes (400, 404, 409, 500)
- Field-level validation error messages
- Request/response logging

**Security**
- Centralized CORS configuration
- Security headers (X-Content-Type-Options, X-Frame-Options, Referrer-Policy, Permissions-Policy)
- Rate limiting (100 requests/minute per IP)
- Input validation with Jakarta Bean Validation annotations

**Performance**
- Database indexes on frequently queried columns
- Spring Cache for frequently accessed data
- Read-only transactions for query operations
- HikariCP connection pool tuning
- Frontend lazy loading with React.lazy() and Suspense
- Response compression (gzip)

## Project Structure

```
health-metrics-tracker/
├── backend/
│   ├── src/main/java/com/healthmetrics/tracker/
│   │   ├── config/          # SecurityConfig, CacheConfig, DataSeeder, RateLimitFilter
│   │   ├── controller/      # REST API controllers
│   │   ├── dto/             # Data Transfer Objects
│   │   ├── entity/          # JPA entities (Facility, HealthIndicator, DataValue)
│   │   ├── exception/       # Custom exceptions & global handler
│   │   ├── repository/      # Spring Data JPA repositories
│   │   ├── service/         # Business logic layer
│   │   └── util/            # Utility classes
│   ├── src/test/java/       # Unit & integration tests
│   ├── src/main/resources/
│   │   └── application.yml  # Application configuration
│   ├── Dockerfile           # Backend container image
│   └── pom.xml              # Maven dependencies
├── frontend/
│   └── client/
│       ├── src/
│       │   ├── pages/       # Dashboard, FacilitiesPage, DataEntryPage
│       │   ├── components/  # Reusable UI components
│       │   │   ├── common/      # Navigation, ErrorBoundary
│       │   │   ├── facilities/  # FacilityList
│       │   │   ├── dataentry/   # DataEntryForm
│       │   │   └── indicators/  # Indicator components
│       │   ├── services/    # API service layer (Axios)
│       │   ├── hooks/       # Custom React hooks
│       │   ├── utils/       # Utility functions
│       │   └── App.js       # Root component with routing
│       ├── Dockerfile       # Frontend container image
│       └── package.json     # npm dependencies
├── docs/
│   ├── API.md               # Comprehensive API documentation
│   ├── API_TESTING.md       # API testing guide with cURL examples
│   ├── ARCHITECTURE.md      # System architecture documentation
│   ├── DEPLOYMENT.md        # Deployment and Docker instructions
│   └── Health-Metrics-Tracker.postman_collection.json
├── docker-compose.yml       # Multi-container orchestration
├── CONTRIBUTING.md          # Contribution guidelines
├── LICENSE                  # MIT License
└── README.md
```

## Prerequisites

- **Java JDK 21** (Amazon Corretto or OpenJDK)
- **Node.js 20 LTS** (or later)
- **PostgreSQL 16+**
- **Maven** (or use the included Maven wrapper)
- **Docker & Docker Compose** (optional, for containerized deployment)

## Setup Instructions

### 1. Database Setup

Create the PostgreSQL database:

```sql
CREATE DATABASE health_metrics_db;
```

### 2. Backend Setup

```bash
cd backend

# Configure database credentials in src/main/resources/application.yml
# Default: username=postgres, password=postgres, port=5432

# Run the application (uses Maven wrapper)
./mvnw spring-boot:run
```

The backend starts on **http://localhost:8080**. On first run, the `DataSeeder` automatically populates the database with sample facilities, indicators, and data values.

### 3. Frontend Setup

```bash
cd frontend/client

# Install dependencies
npm install

# Start the development server
npm start
```

The frontend starts on **http://localhost:3000**.

## Docker Deployment

Run the entire stack with Docker Compose:

```bash
# Build and start all services
docker-compose up --build

# Or run in detached mode
docker-compose up --build -d
```

This starts PostgreSQL, the Spring Boot backend, and the Nginx-served React frontend. See [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md) for detailed deployment instructions.

## API Endpoints

### Facilities (`/api/facilities`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/facilities` | List all facilities (with pagination, filtering, search) |
| GET | `/api/facilities/{id}` | Get facility by ID |
| GET | `/api/facilities/code/{code}` | Get facility by code |
| GET | `/api/facilities/region/{region}` | Get facilities by region |
| GET | `/api/facilities/active` | Get all active facilities |
| POST | `/api/facilities` | Create a new facility |
| PUT | `/api/facilities/{id}` | Update a facility |
| DELETE | `/api/facilities/{id}` | Delete a facility |

**Query parameters for GET /api/facilities:** `region`, `type`, `active`, `search`, `page`, `size`, `sort`, `direction`

### Health Indicators (`/api/indicators`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/indicators` | List all indicators |
| GET | `/api/indicators/{id}` | Get indicator by ID |
| GET | `/api/indicators/code/{code}` | Get indicator by code |
| GET | `/api/indicators/category/{category}` | Get indicators by category |
| GET | `/api/indicators/data-type/{dataType}` | Get indicators by data type |
| GET | `/api/indicators/active` | Get all active indicators |
| POST | `/api/indicators` | Create a new indicator |
| PUT | `/api/indicators/{id}` | Update an indicator |
| DELETE | `/api/indicators/{id}` | Delete an indicator |

### Data Values (`/api/data-values`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/data-values` | Submit a new data value |
| GET | `/api/data-values/{id}` | Get data value by ID |
| GET | `/api/data-values/facility/{facilityId}` | Get all data for a facility |
| GET | `/api/data-values/facility/{facilityId}/period` | Get facility data in date range |
| GET | `/api/data-values/indicator/{indicatorId}` | Get all data for an indicator |
| GET | `/api/data-values/aggregate/region` | Aggregate data by region |
| GET | `/api/data-values/total` | Get total for an indicator |
| GET | `/api/data-values/average` | Get average for an indicator |
| DELETE | `/api/data-values/{id}` | Delete a data value |

For detailed request/response examples and cURL commands, see [docs/API.md](docs/API.md) and [docs/API_TESTING.md](docs/API_TESTING.md).

## API Documentation (Swagger)

When the backend is running, interactive API documentation is available at:

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs

## Database Schema

```
┌──────────────────┐       ┌──────────────────────┐
│    facilities     │       │  health_indicators   │
├──────────────────┤       ├──────────────────────┤
│ id (PK)          │       │ id (PK)              │
│ code (UNIQUE)    │       │ code (UNIQUE)        │
│ name             │       │ name                 │
│ type             │       │ description          │
│ region           │       │ category             │
│ district         │       │ data_type            │
│ latitude         │       │ unit                 │
│ longitude        │       │ active               │
│ active           │       │ created_at           │
│ created_at       │       │ updated_at           │
│ updated_at       │       └──────────┬───────────┘
└────────┬─────────┘                  │
         │                            │
         │       ┌────────────────────┘
         │       │
         ▼       ▼
┌──────────────────────────────────┐
│          data_values             │
├──────────────────────────────────┤
│ id (PK)                         │
│ facility_id (FK)                │
│ indicator_id (FK)               │
│ period_start                    │
│ period_end                      │
│ period_type                     │
│ value                           │
│ comment                         │
│ created_by                      │
│ created_at                      │
│ updated_at                      │
├──────────────────────────────────┤
│ UNIQUE(facility_id,             │
│   indicator_id, period_start)   │
└──────────────────────────────────┘
```

## Testing

### Backend Tests

```bash
cd backend

# Run all tests
./mvnw test
```

Tests include:
- **Unit tests** for all service layers (`FacilityServiceTest`, `HealthIndicatorServiceTest`, `DataValueServiceTest`)
- **Integration tests** for all controllers using MockMvc (`FacilityControllerTest`, `HealthIndicatorControllerTest`, `DataValueControllerTest`)

### Frontend Tests

```bash
cd frontend/client

# Run all tests
npm test
```

Tests include component tests for Dashboard, FacilityList, and DataEntryForm using React Testing Library.

## Future Enhancements

- User authentication and role-based access control
- CSV/Excel data export
- Additional chart types and dashboard customization
- Email notifications and alerts for threshold breaches
- Audit trail for data changes
- Multi-language support (i18n)

## Contributing

Contributions are welcome! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines on how to get started.

## Author

**Joan Karantourou**

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
