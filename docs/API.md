# Health Metrics Tracker - API Documentation

**Base URL:** `http://localhost:8080`
**Interactive API Docs (Swagger UI):** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
**OpenAPI JSON Spec:** [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

---

## Table of Contents

- [Overview](#overview)
- [Authentication](#authentication)
- [Error Handling](#error-handling)
- [Facilities API](#facilities-api)
- [Health Indicators API](#health-indicators-api)
- [Data Values API](#data-values-api)
- [Data Models](#data-models)

---

## Overview

The Health Metrics Tracker API provides RESTful endpoints for managing health facilities, health indicators, and data values. It supports data submission, retrieval, filtering, pagination, and aggregation for health analytics dashboards.

| Property        | Value                                  |
|-----------------|----------------------------------------|
| **Framework**   | Spring Boot 4.0.2                      |
| **Language**    | Java 21                                |
| **Database**    | PostgreSQL 16                          |
| **Port**        | 8080                                   |
| **Content-Type**| `application/json`                     |
| **CORS Origin** | `http://localhost:3000` (React frontend)|

### Total Endpoints: 26

| Method | Count |
|--------|-------|
| GET    | 18    |
| POST   | 3     |
| PUT    | 2     |
| DELETE | 3     |

---

## Authentication

> **Note:** Security is currently disabled for development purposes. All endpoints are accessible without authentication. In a production environment, JWT-based authentication should be implemented.

---

## Error Handling

All errors follow a consistent JSON response format:

```json
{
  "timestamp": "2024-02-10T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Facility with ID 999 not found",
  "path": "/api/facilities/999"
}
```

### HTTP Status Codes

| Status Code | Meaning                | When It Occurs                                           |
|-------------|------------------------|----------------------------------------------------------|
| 200         | OK                     | Successful GET or PUT request                            |
| 201         | Created                | Successful POST request (resource created)               |
| 204         | No Content             | Successful DELETE request                                |
| 400         | Bad Request            | Validation errors or malformed request body              |
| 404         | Not Found              | Resource does not exist                                  |
| 409         | Conflict               | Duplicate resource (e.g., duplicate facility code)       |
| 500         | Internal Server Error  | Unexpected server error                                  |

---

## Facilities API

**Base Path:** `/api/facilities`

Manage health facilities (hospitals, clinics, health centers).

### GET /api/facilities

Retrieve facilities with optional filtering and pagination.

**Query Parameters:**

| Parameter   | Type    | Default  | Description                         |
|-------------|---------|----------|-------------------------------------|
| `region`    | String  | -        | Filter by region                    |
| `type`      | String  | -        | Filter by facility type             |
| `active`    | Boolean | -        | Filter by active status             |
| `search`    | String  | -        | Search by name or code              |
| `page`      | Integer | 0        | Page number (0-indexed)             |
| `size`      | Integer | 10       | Number of items per page            |
| `sort`      | String  | "name"   | Field to sort by                    |
| `direction` | String  | "asc"    | Sort direction (`asc` or `desc`)    |

**Example Request:**
```
GET /api/facilities?region=Attica&active=true&page=0&size=10&sort=name&direction=asc
```

**Response:** `200 OK`
```json
{
  "content": [
    {
      "id": 1,
      "code": "FAC001",
      "name": "Athens General Hospital",
      "type": "Hospital",
      "region": "Attica",
      "district": "Athens",
      "latitude": 37.9838,
      "longitude": 23.7275,
      "active": true,
      "createdAt": "2024-02-10T10:30:00",
      "updatedAt": "2024-02-10T10:30:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": { "sorted": true, "unsorted": false, "empty": false }
  },
  "totalPages": 5,
  "totalElements": 50,
  "first": true,
  "last": false,
  "empty": false
}
```

---

### GET /api/facilities/{id}

Retrieve a single facility by its ID.

**Path Parameters:**

| Parameter | Type | Description          |
|-----------|------|----------------------|
| `id`      | Long | The facility ID      |

**Response:** `200 OK`
```json
{
  "id": 1,
  "code": "FAC001",
  "name": "Athens General Hospital",
  "type": "Hospital",
  "region": "Attica",
  "district": "Athens",
  "latitude": 37.9838,
  "longitude": 23.7275,
  "active": true,
  "createdAt": "2024-02-10T10:30:00",
  "updatedAt": "2024-02-10T10:30:00"
}
```

**Errors:** `404 Not Found` - Facility does not exist

---

### GET /api/facilities/code/{code}

Retrieve a facility by its unique code.

**Path Parameters:**

| Parameter | Type   | Description              |
|-----------|--------|--------------------------|
| `code`    | String | The unique facility code |

**Response:** `200 OK` - Returns `FacilityDTO`

**Errors:** `404 Not Found` - Facility code does not exist

---

### GET /api/facilities/region/{region}

Retrieve all facilities in a specific region.

**Path Parameters:**

| Parameter | Type   | Description                                    |
|-----------|--------|------------------------------------------------|
| `region`  | String | Region name (e.g., "Attica", "Central Macedonia") |

**Response:** `200 OK` - Returns `List<FacilityDTO>`

---

### GET /api/facilities/active

Retrieve all active facilities.

**Response:** `200 OK` - Returns `List<FacilityDTO>`

---

### POST /api/facilities

Create a new facility.

**Request Body:**
```json
{
  "code": "FAC002",
  "name": "Thessaloniki Central Clinic",
  "type": "Clinic",
  "region": "Central Macedonia",
  "district": "Thessaloniki",
  "latitude": 40.6431,
  "longitude": 22.9135,
  "active": true
}
```

**Validation Rules:**
- `code` - Required, must be unique, max 50 characters
- `name` - Required, max 200 characters
- `type` - Required, max 50 characters

**Response:** `201 Created` - Returns `FacilityDTO`

**Errors:**
- `400 Bad Request` - Validation errors
- `409 Conflict` - Duplicate facility code

---

### PUT /api/facilities/{id}

Update an existing facility.

**Path Parameters:**

| Parameter | Type | Description     |
|-----------|------|-----------------|
| `id`      | Long | The facility ID |

**Request Body:** Same structure as POST

**Response:** `200 OK` - Returns updated `FacilityDTO`

**Errors:**
- `400 Bad Request` - Validation errors
- `404 Not Found` - Facility does not exist

---

### DELETE /api/facilities/{id}

Delete a facility.

**Path Parameters:**

| Parameter | Type | Description     |
|-----------|------|-----------------|
| `id`      | Long | The facility ID |

**Response:** `204 No Content`

**Errors:** `404 Not Found` - Facility does not exist

---

## Health Indicators API

**Base Path:** `/api/indicators`

Manage health indicators (metrics definitions such as disease counts, vaccination rates, etc.).

### GET /api/indicators

Retrieve all health indicators.

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "code": "MAL_001",
    "name": "Malaria Cases Reported",
    "description": "Total number of confirmed malaria cases",
    "category": "Disease Control",
    "dataType": "NUMBER",
    "unit": "cases",
    "active": true,
    "createdAt": "2024-02-10T10:30:00",
    "updatedAt": "2024-02-10T10:30:00"
  }
]
```

---

### GET /api/indicators/{id}

Retrieve a single indicator by its ID.

**Path Parameters:**

| Parameter | Type | Description        |
|-----------|------|--------------------|
| `id`      | Long | The indicator ID   |

**Response:** `200 OK` - Returns `HealthIndicatorDTO`

**Errors:** `404 Not Found` - Indicator does not exist

---

### GET /api/indicators/code/{code}

Retrieve an indicator by its unique code.

**Path Parameters:**

| Parameter | Type   | Description                |
|-----------|--------|----------------------------|
| `code`    | String | The unique indicator code  |

**Response:** `200 OK` - Returns `HealthIndicatorDTO`

**Errors:** `404 Not Found` - Indicator code does not exist

---

### GET /api/indicators/category/{category}

Retrieve all indicators in a specific category.

**Path Parameters:**

| Parameter  | Type   | Description                                          |
|------------|--------|------------------------------------------------------|
| `category` | String | Category name (e.g., "Maternal Health", "Disease Control") |

**Response:** `200 OK` - Returns `List<HealthIndicatorDTO>`

---

### GET /api/indicators/data-type/{dataType}

Retrieve all indicators with a specific data type.

**Path Parameters:**

| Parameter  | Type   | Description                                |
|------------|--------|--------------------------------------------|
| `dataType` | String | Data type: `NUMBER`, `PERCENTAGE`, or `BOOLEAN` |

**Response:** `200 OK` - Returns `List<HealthIndicatorDTO>`

---

### GET /api/indicators/active

Retrieve all active indicators.

**Response:** `200 OK` - Returns `List<HealthIndicatorDTO>`

---

### POST /api/indicators

Create a new health indicator.

**Request Body:**
```json
{
  "code": "TB_DETECT",
  "name": "TB Detection Rate",
  "description": "Percentage of suspected TB cases that are confirmed",
  "category": "Disease Control",
  "dataType": "PERCENTAGE",
  "unit": "%",
  "active": true
}
```

**Validation Rules:**
- `code` - Required, must be unique, max 50 characters
- `name` - Required, max 200 characters
- `category` - Required, max 100 characters
- `dataType` - Required, one of: `NUMBER`, `PERCENTAGE`, `BOOLEAN`

**Response:** `201 Created` - Returns `HealthIndicatorDTO`

**Errors:**
- `400 Bad Request` - Validation errors
- `409 Conflict` - Duplicate indicator code

---

### PUT /api/indicators/{id}

Update an existing indicator.

**Path Parameters:**

| Parameter | Type | Description      |
|-----------|------|------------------|
| `id`      | Long | The indicator ID |

**Request Body:** Same structure as POST

**Response:** `200 OK` - Returns updated `HealthIndicatorDTO`

**Errors:**
- `400 Bad Request` - Validation errors
- `404 Not Found` - Indicator does not exist

---

### DELETE /api/indicators/{id}

Delete an indicator.

**Path Parameters:**

| Parameter | Type | Description      |
|-----------|------|------------------|
| `id`      | Long | The indicator ID |

**Response:** `204 No Content`

**Errors:** `404 Not Found` - Indicator does not exist

---

## Data Values API

**Base Path:** `/api/data-values`

Submit and retrieve health data values reported by facilities for specific indicators and time periods.

### POST /api/data-values

Submit a new data value.

**Request Body:**
```json
{
  "facilityId": 1,
  "indicatorId": 2,
  "periodStart": "2024-01-01",
  "periodEnd": "2024-01-31",
  "periodType": "MONTHLY",
  "value": 150.0,
  "comment": "Data verified by supervisor",
  "createdBy": "john.doe"
}
```

**Validation Rules:**

| Field         | Type       | Required | Constraints                                      |
|---------------|------------|----------|--------------------------------------------------|
| `facilityId`  | Long       | Yes      | Must be positive, facility must exist             |
| `indicatorId` | Long       | Yes      | Must be positive, indicator must exist            |
| `periodStart` | LocalDate  | Yes      | Format: `YYYY-MM-DD`                             |
| `periodEnd`   | LocalDate  | Yes      | Format: `YYYY-MM-DD`, must be >= periodStart     |
| `periodType`  | String     | Yes      | `DAILY`, `WEEKLY`, `MONTHLY`, `QUARTERLY`, `YEARLY` |
| `value`       | BigDecimal | Yes      | Non-negative                                     |
| `comment`     | String     | No       | Max 500 characters                               |
| `createdBy`   | String     | No       | Max 100 characters                               |

**Unique Constraint:** One value per `facilityId` + `indicatorId` + `periodStart` combination.

**Response:** `201 Created`
```json
{
  "id": 1,
  "facility": {
    "id": 1,
    "code": "FAC001",
    "name": "Athens General Hospital",
    "type": "Hospital",
    "region": "Attica",
    "district": "Athens",
    "latitude": 37.9838,
    "longitude": 23.7275,
    "active": true,
    "createdAt": "2024-02-10T10:30:00",
    "updatedAt": "2024-02-10T10:30:00"
  },
  "indicator": {
    "id": 2,
    "code": "MAL_001",
    "name": "Malaria Cases Reported",
    "description": "Total number of confirmed malaria cases",
    "category": "Disease Control",
    "dataType": "NUMBER",
    "unit": "cases",
    "active": true,
    "createdAt": "2024-02-10T10:30:00",
    "updatedAt": "2024-02-10T10:30:00"
  },
  "periodStart": "2024-01-01",
  "periodEnd": "2024-01-31",
  "periodType": "MONTHLY",
  "value": 150.0,
  "comment": "Data verified by supervisor",
  "createdAt": "2024-02-10T10:30:00",
  "updatedAt": "2024-02-10T10:30:00",
  "createdBy": "john.doe"
}
```

**Errors:**
- `400 Bad Request` - Validation errors
- `404 Not Found` - Facility or indicator does not exist
- `409 Conflict` - Data for this facility/indicator/period already exists

---

### GET /api/data-values/{id}

Retrieve a single data value by its ID.

**Path Parameters:**

| Parameter | Type | Description       |
|-----------|------|-------------------|
| `id`      | Long | The data value ID |

**Response:** `200 OK` - Returns `DataValueDTO`

**Errors:** `404 Not Found` - Data value does not exist

---

### GET /api/data-values/facility/{facilityId}

Retrieve all data values for a specific facility.

**Path Parameters:**

| Parameter    | Type | Description     |
|--------------|------|-----------------|
| `facilityId` | Long | The facility ID |

**Response:** `200 OK` - Returns `List<DataValueDTO>`

**Errors:** `404 Not Found` - Facility does not exist

---

### GET /api/data-values/facility/{facilityId}/period

Retrieve data values for a facility within a date range.

**Path Parameters:**

| Parameter    | Type | Description     |
|--------------|------|-----------------|
| `facilityId` | Long | The facility ID |

**Query Parameters:**

| Parameter   | Type      | Required | Description                   |
|-------------|-----------|----------|-------------------------------|
| `startDate` | LocalDate | Yes      | Start date (format: `YYYY-MM-DD`) |
| `endDate`   | LocalDate | Yes      | End date (format: `YYYY-MM-DD`)   |

**Example Request:**
```
GET /api/data-values/facility/1/period?startDate=2024-01-01&endDate=2024-12-31
```

**Response:** `200 OK` - Returns `List<DataValueDTO>`

**Errors:** `404 Not Found` - Facility does not exist

---

### GET /api/data-values/indicator/{indicatorId}

Retrieve all data values for a specific indicator across all facilities.

**Path Parameters:**

| Parameter     | Type | Description      |
|---------------|------|------------------|
| `indicatorId` | Long | The indicator ID |

**Response:** `200 OK` - Returns `List<DataValueDTO>`

**Errors:** `404 Not Found` - Indicator does not exist

---

### GET /api/data-values/aggregate/region

Aggregate data values by region (SUM) for analytics.

**Query Parameters:**

| Parameter     | Type      | Required | Description                          |
|---------------|-----------|----------|--------------------------------------|
| `indicatorId` | Long      | Yes      | The indicator ID to aggregate        |
| `region`      | String    | No       | Filter to a specific region          |
| `startDate`   | LocalDate | Yes      | Start date (format: `YYYY-MM-DD`)    |
| `endDate`     | LocalDate | Yes      | End date (format: `YYYY-MM-DD`)      |

**Example Requests:**
```
GET /api/data-values/aggregate/region?indicatorId=2&startDate=2024-01-01&endDate=2024-12-31
GET /api/data-values/aggregate/region?indicatorId=2&region=Attica&startDate=2024-01-01&endDate=2024-12-31
```

**Response:** `200 OK`
```json
{
  "Attica": 1250.0,
  "Central Macedonia": 980.0,
  "Crete": 450.0
}
```

**Errors:** `404 Not Found` - Indicator does not exist

---

### GET /api/data-values/total

Calculate the total (SUM) for a specific indicator across all facilities.

**Query Parameters:**

| Parameter     | Type      | Required | Description                       |
|---------------|-----------|----------|-----------------------------------|
| `indicatorId` | Long      | Yes      | The indicator ID                  |
| `startDate`   | LocalDate | Yes      | Start date (format: `YYYY-MM-DD`) |
| `endDate`     | LocalDate | Yes      | End date (format: `YYYY-MM-DD`)   |

**Example Request:**
```
GET /api/data-values/total?indicatorId=2&startDate=2024-01-01&endDate=2024-12-31
```

**Response:** `200 OK`
```json
2680.0
```

---

### GET /api/data-values/average

Calculate the average value for a specific indicator.

**Query Parameters:**

| Parameter     | Type      | Required | Description                       |
|---------------|-----------|----------|-----------------------------------|
| `indicatorId` | Long      | Yes      | The indicator ID                  |
| `startDate`   | LocalDate | Yes      | Start date (format: `YYYY-MM-DD`) |
| `endDate`     | LocalDate | Yes      | End date (format: `YYYY-MM-DD`)   |

**Example Request:**
```
GET /api/data-values/average?indicatorId=2&startDate=2024-01-01&endDate=2024-12-31
```

**Response:** `200 OK`
```json
67.25
```

---

### DELETE /api/data-values/{id}

Delete a data value.

**Path Parameters:**

| Parameter | Type | Description       |
|-----------|------|-------------------|
| `id`      | Long | The data value ID |

**Response:** `204 No Content`

**Errors:** `404 Not Found` - Data value does not exist

---

## Data Models

### FacilityDTO

| Field       | Type          | Description                          |
|-------------|---------------|--------------------------------------|
| `id`        | Long          | Auto-generated primary key           |
| `code`      | String        | Unique facility code (e.g., FAC001)  |
| `name`      | String        | Facility name                        |
| `type`      | String        | Type (Hospital, Clinic, Health Center)|
| `region`    | String        | Administrative region                |
| `district`  | String        | District within region               |
| `latitude`  | Double        | Geographic latitude                  |
| `longitude` | Double        | Geographic longitude                 |
| `active`    | Boolean       | Active status (default: true)        |
| `createdAt` | LocalDateTime | Creation timestamp                   |
| `updatedAt` | LocalDateTime | Last modification timestamp          |

### HealthIndicatorDTO

| Field         | Type          | Description                               |
|---------------|---------------|-------------------------------------------|
| `id`          | Long          | Auto-generated primary key                |
| `code`        | String        | Unique indicator code (e.g., MAL_001)     |
| `name`        | String        | Human-readable name                       |
| `description` | String        | Detailed description                      |
| `category`    | String        | Category (Maternal Health, Disease Control, etc.) |
| `dataType`    | String        | `NUMBER`, `PERCENTAGE`, or `BOOLEAN`      |
| `unit`        | String        | Unit of measurement (cases, %, etc.)      |
| `active`      | Boolean       | Active status (default: true)             |
| `createdAt`   | LocalDateTime | Creation timestamp                        |
| `updatedAt`   | LocalDateTime | Last modification timestamp               |

### DataValueDTO

| Field         | Type               | Description                            |
|---------------|--------------------|----------------------------------------|
| `id`          | Long               | Auto-generated primary key             |
| `facility`    | FacilityDTO        | Full facility object (nested)          |
| `indicator`   | HealthIndicatorDTO | Full indicator object (nested)         |
| `periodStart` | LocalDate          | Start of reporting period              |
| `periodEnd`   | LocalDate          | End of reporting period                |
| `periodType`  | String             | `DAILY`, `WEEKLY`, `MONTHLY`, `QUARTERLY`, `YEARLY` |
| `value`       | BigDecimal         | The actual data value (precision: 19, scale: 4) |
| `comment`     | String             | Optional comment or note               |
| `createdAt`   | LocalDateTime      | Creation timestamp                     |
| `updatedAt`   | LocalDateTime      | Last modification timestamp            |
| `createdBy`   | String             | Username of data submitter             |

### DataValueCreateRequest

| Field         | Type       | Required | Constraints                                      |
|---------------|------------|----------|--------------------------------------------------|
| `facilityId`  | Long       | Yes      | Must be positive                                 |
| `indicatorId` | Long       | Yes      | Must be positive                                 |
| `periodStart` | LocalDate  | Yes      | Format: `YYYY-MM-DD`                            |
| `periodEnd`   | LocalDate  | Yes      | Must be >= `periodStart`                         |
| `periodType`  | String     | Yes      | `DAILY`, `WEEKLY`, `MONTHLY`, `QUARTERLY`, `YEARLY` |
| `value`       | BigDecimal | Yes      | Non-negative                                     |
| `comment`     | String     | No       | Max 500 characters                               |
| `createdBy`   | String     | No       | Max 100 characters                               |

---

## Database Schema

### Unique Constraints

| Table              | Constraint                                       |
|--------------------|--------------------------------------------------|
| `facilities`       | `code` must be unique                            |
| `health_indicators`| `code` must be unique                            |
| `data_values`      | `facility_id` + `indicator_id` + `period_start` must be unique |

### Relationships

```
facilities (1) ----< (N) data_values (N) >---- (1) health_indicators
```

- A **Facility** can have many **DataValues**
- A **HealthIndicator** can have many **DataValues**
- Each **DataValue** belongs to exactly one Facility and one HealthIndicator
