# Health Metrics Tracker - Architecture Documentation

## Table of Contents

- [System Overview](#system-overview)
- [Technology Choices](#technology-choices)
- [System Architecture](#system-architecture)
- [Backend Architecture](#backend-architecture)
- [Frontend Architecture](#frontend-architecture)
- [Database Schema](#database-schema)
- [Data Flow](#data-flow)
- [Design Patterns](#design-patterns)
- [Testing Strategy](#testing-strategy)
- [Security](#security)
- [Build & Deployment](#build--deployment)

---

## System Overview

The Health Metrics Tracker (HMT) is a full-stack web application for tracking, aggregating, and visualizing health indicators across facilities. Inspired by DHIS2's core functionality, it allows health organizations to submit data values for specific indicators at various facilities and view analytics through charts and dashboards.

**Key Capabilities:**
- Manage health facilities (hospitals, clinics, health centers)
- Define health indicators (disease counts, vaccination rates, maternal health metrics)
- Submit periodic data values linking facilities to indicators
- Aggregate and visualize data by region, time period, and indicator
- Interactive dashboard with charts and summary statistics

---

## Technology Choices

| Layer         | Technology              | Version | Rationale                                                    |
|---------------|-------------------------|---------|--------------------------------------------------------------|
| **Language**  | Java                    | 21      | LTS release, modern features (records, pattern matching), strong ecosystem |
| **Backend**   | Spring Boot             | 4.0.2   | Industry standard for Java REST APIs, auto-configuration, production-ready |
| **ORM**       | Spring Data JPA / Hibernate | -   | Reduces boilerplate, repository abstraction, JPQL support    |
| **Database**  | PostgreSQL              | 16+     | Robust relational DB, excellent for structured health data, ACID compliance |
| **Frontend**  | React                   | 19      | Component-based UI, large ecosystem, fast rendering          |
| **UI Library**| Material-UI (MUI)       | 7.x     | Pre-built accessible components, consistent design language  |
| **Charts**    | Recharts                | 3.x     | Built on React and D3, declarative chart components          |
| **HTTP Client**| Axios                  | 1.x     | Request/response interceptors, timeout handling, clean API   |
| **Routing**   | React Router            | 7.x     | Client-side routing, nested routes, URL parameters           |
| **Build (BE)**| Maven                   | -       | Dependency management, reproducible builds, Spring Boot plugin |
| **Build (FE)**| Create React App        | 5.x     | Zero-config Webpack setup, fast development server           |
| **Docs**      | SpringDoc OpenAPI       | 2.8.4   | Auto-generated Swagger UI from controller annotations        |
| **Utils**     | Lombok                  | -       | Eliminates boilerplate (getters, setters, constructors)      |
| **Date Utils**| date-fns                | 4.x     | Lightweight, tree-shakeable date manipulation                |

---

## System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         CLIENT (Browser)                        │
│                                                                 │
│  ┌─────────────┐  ┌──────────────┐  ┌───────────────────────┐  │
│  │  Dashboard   │  │  Facilities  │  │    Data Entry Form    │  │
│  │  (Charts)    │  │  (Table)     │  │    (Submission)       │  │
│  └──────┬───────┘  └──────┬───────┘  └───────────┬───────────┘  │
│         │                 │                       │              │
│  ┌──────┴─────────────────┴───────────────────────┴──────────┐  │
│  │              API Service Layer (Axios)                     │  │
│  │         Base URL: http://localhost:3000                    │  │
│  └──────────────────────────┬────────────────────────────────┘  │
└─────────────────────────────┼───────────────────────────────────┘
                              │ HTTP/JSON (CORS)
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    SPRING BOOT SERVER (:8080)                    │
│                                                                 │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                   REST Controllers                        │  │
│  │  /api/facilities  /api/indicators  /api/data-values       │  │
│  └──────────────────────────┬────────────────────────────────┘  │
│                             │                                   │
│  ┌──────────────────────────┴────────────────────────────────┐  │
│  │                    Service Layer                           │  │
│  │  FacilityService  IndicatorService  DataValueService      │  │
│  └──────────────────────────┬────────────────────────────────┘  │
│                             │                                   │
│  ┌──────────────────────────┴────────────────────────────────┐  │
│  │                  Repository Layer (JPA)                    │  │
│  │  FacilityRepo    IndicatorRepo     DataValueRepo          │  │
│  └──────────────────────────┬────────────────────────────────┘  │
│                             │                                   │
│  ┌──────────────────────────┴────────────────────────────────┐  │
│  │              Cross-cutting Concerns                        │  │
│  │  GlobalExceptionHandler │ SecurityConfig │ OpenApiConfig   │  │
│  └───────────────────────────────────────────────────────────┘  │
└─────────────────────────────┼───────────────────────────────────┘
                              │ JDBC
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    PostgreSQL (:5432)                            │
│                                                                 │
│     facilities    health_indicators    data_values              │
└─────────────────────────────────────────────────────────────────┘
```

---

## Backend Architecture

The backend follows a **layered architecture** with clear separation of concerns.

### Project Structure

```
backend/src/main/java/com/healthmetrics/tracker/
├── TrackerApplication.java          # Spring Boot entry point
├── config/
│   ├── SecurityConfig.java          # Spring Security configuration
│   ├── OpenApiConfig.java           # Swagger/OpenAPI metadata
│   └── DataSeeder.java              # Sample data initialization
├── controller/
│   ├── FacilityController.java      # /api/facilities endpoints
│   ├── HealthIndicatorController.java # /api/indicators endpoints
│   └── DataValueController.java     # /api/data-values endpoints
├── service/
│   ├── FacilityService.java         # Facility business logic
│   ├── HealthIndicatorService.java  # Indicator business logic
│   └── DataValueService.java        # Data value logic & aggregation
├── repository/
│   ├── FacilityRepository.java      # Facility data access
│   ├── HealthIndicatorRepository.java # Indicator data access
│   └── DataValueRepository.java     # Data value queries & aggregation
├── entity/
│   ├── Facility.java                # Facility JPA entity
│   ├── HealthIndicator.java         # Indicator JPA entity
│   └── DataValue.java               # Data value JPA entity
├── dto/
│   ├── FacilityDTO.java             # Facility transfer object
│   ├── HealthIndicatorDTO.java      # Indicator transfer object
│   ├── DataValueDTO.java            # Data value transfer object
│   ├── DataValueCreateRequest.java  # Data submission request
│   └── ApiResponse.java             # Generic API response wrapper
└── exception/
    ├── GlobalExceptionHandler.java  # Centralized error handling
    ├── ErrorResponse.java           # Error response DTO
    ├── ResourceNotFoundException.java
    ├── ValidationException.java
    └── DuplicateResourceException.java
```

### Layer Responsibilities

| Layer          | Responsibility                                                           |
|----------------|--------------------------------------------------------------------------|
| **Controller** | HTTP request/response handling, input validation (`@Valid`), status codes |
| **Service**    | Business logic, DTO-entity conversion, transaction management            |
| **Repository** | Database queries (JPQL, native SQL), pagination, Spring Data JPA         |
| **Entity**     | JPA/Hibernate ORM mapping, table structure, relationships                |
| **DTO**        | API request/response shapes, validation annotations, serialization       |
| **Exception**  | Custom exceptions, global error handling, consistent error format        |
| **Config**     | Security, OpenAPI docs, data seeding, application settings               |

### Request Lifecycle

```
HTTP Request
    │
    ▼
Controller (@Valid validation)
    │ ── validation fails ──▶ MethodArgumentNotValidException ──▶ 400
    │
    ▼
Service (business logic)
    │ ── not found ──▶ ResourceNotFoundException ──▶ 404
    │ ── duplicate  ──▶ DuplicateResourceException ──▶ 409
    │ ── invalid    ──▶ ValidationException ──▶ 400
    │
    ▼
Repository (database query)
    │
    ▼
Entity ←──▶ PostgreSQL
    │
    ▼
Service (entity → DTO conversion)
    │
    ▼
Controller (HTTP response + status code)
    │
    ▼
HTTP Response (JSON)
```

### Exception Handling Strategy

All exceptions are caught by `GlobalExceptionHandler` (`@RestControllerAdvice`) and converted to a consistent JSON format:

```json
{
  "timestamp": "2024-02-10T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Facility with ID 999 not found",
  "path": "/api/facilities/999"
}
```

| Exception                          | HTTP Status | Use Case                       |
|------------------------------------|-------------|--------------------------------|
| `ResourceNotFoundException`        | 404         | Entity not found by ID or code |
| `ValidationException`              | 400         | Business rule violations        |
| `DuplicateResourceException`       | 409         | Duplicate code or unique constraint |
| `MethodArgumentNotValidException`  | 400         | `@Valid` annotation failures    |
| `Exception` (catch-all)            | 500         | Unexpected server errors        |

---

## Frontend Architecture

The frontend is a React single-page application (SPA) using component-based architecture.

### Project Structure

```
frontend/client/src/
├── App.js                           # Root component with routing
├── App.css                          # Global styles
├── index.js                         # React DOM entry point
├── components/
│   ├── common/
│   │   ├── Navigation.jsx           # Header navigation bar
│   │   └── Navigation.css
│   ├── dashboard/
│   │   ├── Dashboard.jsx            # Main dashboard with charts
│   │   └── Dashboard.css
│   ├── facilities/
│   │   ├── FacilityList.jsx         # Searchable, paginated facility table
│   │   └── FacilityList.css
│   └── dataentry/
│       ├── DataEntryForm.jsx        # Data submission form
│       └── DataEntryForm.css
├── pages/
│   ├── FacilitiesPage.jsx           # Facilities page wrapper
│   └── DataEntryPage.jsx            # Data entry page wrapper
├── services/
│   ├── api.js                       # Axios client with interceptors
│   ├── facilityService.js           # Facility API methods
│   ├── indicatorService.js          # Indicator API methods
│   ├── dataValueService.js          # Data value API methods
│   └── index.js                     # Service exports
└── utils/                           # Utility functions (future)
```

### Routing

| Route          | Component      | Description                             |
|----------------|----------------|-----------------------------------------|
| `/`            | Dashboard      | Summary statistics, charts, analytics   |
| `/facilities`  | FacilitiesPage | Facility list with search and filters   |
| `/data-entry`  | DataEntryPage  | Form for submitting health data values  |

### State Management

The application uses **local component state** via React hooks (`useState`, `useEffect`). Each component manages its own data fetching and state:

- **Dashboard** — stats, indicators, chart data, selected indicator
- **FacilityList** — facilities list, filters, pagination, sorting
- **DataEntryForm** — form data, facilities/indicators dropdowns, validation errors

TanStack React Query is included as a dependency for future optimization of server state management.

### API Service Layer

All backend communication goes through an Axios client configured with:
- **Base URL:** `http://localhost:8080/api`
- **Timeout:** 10 seconds
- **Request interceptor:** Logs outgoing requests
- **Response interceptor:** Logs responses, handles error categorization

Each resource has a dedicated service module (`facilityService`, `indicatorService`, `dataValueService`) that encapsulates API calls.

---

## Database Schema

### Entity Relationship Diagram

```
┌──────────────────────────┐          ┌──────────────────────────┐
│       facilities         │          │    health_indicators     │
├──────────────────────────┤          ├──────────────────────────┤
│ id          BIGSERIAL PK │          │ id          BIGSERIAL PK │
│ code        VARCHAR(50)  │ UNIQUE   │ code        VARCHAR(50)  │ UNIQUE
│ name        VARCHAR(200) │          │ name        VARCHAR(200) │
│ type        VARCHAR(50)  │          │ description VARCHAR(500) │
│ region      VARCHAR(100) │          │ category    VARCHAR(100) │
│ district    VARCHAR(100) │          │ data_type   VARCHAR(20)  │
│ latitude    FLOAT8       │          │ unit        VARCHAR(50)  │
│ longitude   FLOAT8       │          │ active      BOOLEAN      │
│ active      BOOLEAN      │          │ created_at  TIMESTAMP    │
│ created_at  TIMESTAMP    │          │ updated_at  TIMESTAMP    │
│ updated_at  TIMESTAMP    │          └─────────────┬────────────┘
└─────────────┬────────────┘                        │
              │                                     │
              │ 1:N                            1:N  │
              │                                     │
              ▼                                     ▼
┌─────────────────────────────────────────────────────────────┐
│                        data_values                          │
├─────────────────────────────────────────────────────────────┤
│ id              BIGSERIAL PK                                │
│ facility_id     BIGINT FK → facilities(id)         NOT NULL │
│ indicator_id    BIGINT FK → health_indicators(id)  NOT NULL │
│ period_start    DATE                               NOT NULL │
│ period_end      DATE                               NOT NULL │
│ period_type     VARCHAR(20)                        NOT NULL │
│ value           DECIMAL(19,4)                      NOT NULL │
│ comment         VARCHAR(500)                                │
│ created_by      VARCHAR(100)                                │
│ created_at      TIMESTAMP                          NOT NULL │
│ updated_at      TIMESTAMP                          NOT NULL │
├─────────────────────────────────────────────────────────────┤
│ UNIQUE (facility_id, indicator_id, period_start)            │
└─────────────────────────────────────────────────────────────┘
```

### Key Constraints

| Table              | Constraint                                         | Purpose                              |
|--------------------|----------------------------------------------------|--------------------------------------|
| `facilities`       | `code` UNIQUE                                      | Prevent duplicate facility codes     |
| `health_indicators`| `code` UNIQUE                                      | Prevent duplicate indicator codes    |
| `data_values`      | `(facility_id, indicator_id, period_start)` UNIQUE | One submission per facility/indicator/period |

### Schema Management

Hibernate manages schema updates automatically (`ddl-auto: update`). On application startup, Hibernate compares entity definitions with the existing database schema and applies any necessary changes (new columns, tables, constraints).

A `DataSeeder` component (`ApplicationRunner`) populates the database with sample data on first run, checking if tables are empty before inserting to prevent duplicates on subsequent restarts.

---

## Data Flow

### Data Submission Flow

```
User fills DataEntryForm
        │
        ▼
Client-side validation (required fields, date logic)
        │
        ▼
POST /api/data-values (DataValueCreateRequest JSON)
        │
        ▼
Controller validates with @Valid annotations
        │
        ▼
DataValueService.submitDataValue():
  ├── Verify facility exists and is active
  ├── Verify indicator exists and is active
  ├── Validate periodEnd >= periodStart
  ├── Check for duplicate (unique constraint)
  ├── Type-specific validation (percentage 0-100, boolean 0/1)
  └── Save DataValue entity
        │
        ▼
Return DataValueDTO (with nested Facility & Indicator details)
        │
        ▼
Frontend shows success message, resets form
```

### Dashboard Analytics Flow

```
Dashboard loads
    │
    ├── GET /api/facilities?size=1000 ──▶ Count total & active
    ├── GET /api/indicators ──▶ Populate dropdown, count total
    │
    ▼
User selects indicator
    │
    ▼
GET /api/data-values/indicator/{id}
    │
    ▼
Frontend processes response:
    ├── Group by month (periodStart) ──▶ Time series line chart
    └── Group by facility region   ──▶ Regional bar chart
```

### Pagination Flow (Facilities)

```
User adjusts filters or page
        │
        ▼
GET /api/facilities?page=0&size=10&sort=name&direction=asc&region=Attica
        │
        ▼
Controller builds PageRequest from params
        │
        ▼
Repository executes paginated query with filters
        │
        ▼
Returns Page<FacilityDTO>:
  {
    content: [...],        // Current page items
    totalPages: 5,         // For pagination controls
    totalElements: 50,     // Total matching records
    first: true,           // Is first page?
    last: false            // Is last page?
  }
        │
        ▼
Frontend updates table and pagination controls
```

---

## Design Patterns

### Backend Patterns

| Pattern                    | Implementation                                                  |
|----------------------------|-----------------------------------------------------------------|
| **Layered Architecture**   | Controller → Service → Repository → Entity                     |
| **DTO Pattern**            | Separate DTOs from entities to decouple API from persistence    |
| **Repository Pattern**     | Spring Data JPA abstracts data access with interface methods    |
| **Service Layer**          | Business logic encapsulated, transaction boundaries defined     |
| **Factory Method**         | `ApiResponse.success()` / `ApiResponse.error()` static methods |
| **Global Exception Handler** | `@RestControllerAdvice` centralizes error handling            |
| **Dependency Injection**   | Constructor injection via Lombok `@RequiredArgsConstructor`     |
| **Entity Auditing**        | `@CreatedDate` / `@LastModifiedDate` for automatic timestamps   |
| **Builder Pattern**        | Lombok `@Builder` on DTOs for fluent object construction        |

### Frontend Patterns

| Pattern                  | Implementation                                                    |
|--------------------------|-------------------------------------------------------------------|
| **Component-Based**      | Reusable React components (Dashboard, FacilityList, DataEntryForm)|
| **Service Layer**        | API calls abstracted into service modules (Axios-based)           |
| **Container/Presenter**  | Pages wrap components, components handle UI logic                 |
| **Interceptor Pattern**  | Axios request/response interceptors for logging and error handling|
| **Controlled Components**| Form inputs bound to React state via `useState`                  |

### Design Decisions

| Decision                     | Rationale                                                          |
|------------------------------|--------------------------------------------------------------------|
| **PostgreSQL over MySQL**    | Superior support for complex queries, JSON types, and analytics    |
| **BigDecimal for values**    | Precision required for health metrics (avoids floating-point errors)|
| **Lazy loading on FK**       | Avoids N+1 queries; data fetched only when explicitly accessed     |
| **Separate create request DTO** | POST body uses IDs; response includes full nested objects       |
| **Hard delete (not soft)**   | Simplicity for MVP; soft delete can be added via `active` flag     |
| **CORS for localhost:3000**  | Allows React dev server to communicate with Spring Boot backend    |
| **Hibernate ddl-auto: update** | Convenient for development; should use Flyway/Liquibase in production |
| **DataSeeder on startup**    | Ensures demo data is available for development and testing         |

---

## Testing Strategy

### Backend Testing

| Type             | Framework         | Scope                                          |
|------------------|-------------------|-------------------------------------------------|
| **Unit Tests**   | JUnit 5 + Mockito | Service layer with mocked repositories          |
| **Integration**  | MockMvc           | Controller endpoints with mocked service layer  |

**Test structure mirrors main source:**
```
backend/src/test/java/com/healthmetrics/tracker/
├── service/
│   ├── FacilityServiceTest.java
│   ├── HealthIndicatorServiceTest.java
│   └── DataValueServiceTest.java
└── controller/
    ├── FacilityControllerTest.java
    ├── HealthIndicatorControllerTest.java
    └── DataValueControllerTest.java
```

**Run:** `./mvnw test`

### Frontend Testing

| Type             | Framework                | Scope                              |
|------------------|--------------------------|------------------------------------|
| **Component**    | React Testing Library    | Rendering, user interaction, state |

**Test files:**
```
frontend/client/src/components/
├── dashboard/Dashboard.test.js
├── facilities/FacilityList.test.js
└── dataentry/DataEntryForm.test.js
```

**Run:** `cd frontend/client && npm test`

---

## Security

### Current State (Development)

Security is **disabled** for development convenience:

```java
http
    .csrf(csrf -> csrf.disable())
    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
```

CORS is configured to allow requests from `http://localhost:3000`.

### Production Recommendations

- Enable JWT-based authentication with Spring Security
- Implement role-based access control (RBAC) for data submission vs. read-only access
- Enable CSRF protection for browser-based clients
- Add rate limiting to prevent API abuse
- Use HTTPS in production
- Externalize database credentials (environment variables or secrets manager)
- Switch `ddl-auto` from `update` to `validate` and use migration tools (Flyway/Liquibase)

---

## Build & Deployment

### Development Setup

```bash
# 1. Database
psql -U postgres -c "CREATE DATABASE health_metrics_db;"

# 2. Backend (terminal 1)
cd backend
./mvnw spring-boot:run          # Starts on http://localhost:8080

# 3. Frontend (terminal 2)
cd frontend/client
npm install
npm start                        # Starts on http://localhost:3000
```

### Production Build

```bash
# Backend - produces executable JAR
cd backend
./mvnw clean package -DskipTests
java -jar target/tracker-0.0.1-SNAPSHOT.jar

# Frontend - produces optimized static files
cd frontend/client
npm run build                    # Output in build/ directory
```

### API Documentation Access

| Resource          | URL                                    |
|-------------------|----------------------------------------|
| Swagger UI        | http://localhost:8080/swagger-ui.html  |
| OpenAPI JSON Spec | http://localhost:8080/api-docs         |
| Postman Collection| `docs/Health-Metrics-Tracker.postman_collection.json` |
