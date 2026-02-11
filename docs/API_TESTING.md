# Health Metrics Tracker - API Testing Guide

This document provides comprehensive testing instructions for all REST API endpoints in the Health Metrics Tracker application.

## Base URL

```
http://localhost:8080/api
```

## Table of Contents

1. [Facilities API](#facilities-api)
2. [Health Indicators API](#health-indicators-api)
3. [Data Values API](#data-values-api)
4. [Error Response Format](#error-response-format)
5. [Testing with cURL](#testing-with-curl)
6. [Testing with Postman](#testing-with-postman)

---

## Facilities API

Base Path: `/api/facilities`

### 1. Get All Facilities

**Endpoint:** `GET /api/facilities`

**Description:** Retrieves all health facilities in the system.

**Example Request:**
```bash
curl -X GET http://localhost:8080/api/facilities
```

**Expected Response:** `200 OK`
```json
[
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
    "createdAt": "2025-02-11T10:30:00",
    "updatedAt": "2025-02-11T10:30:00"
  },
  {
    "id": 2,
    "code": "FAC002",
    "name": "Piraeus Health Center",
    "type": "Health Center",
    "region": "Attica",
    "district": "Piraeus",
    "latitude": 37.9420,
    "longitude": 23.6467,
    "active": true,
    "createdAt": "2025-02-11T10:30:00",
    "updatedAt": "2025-02-11T10:30:00"
  }
]
```

---

### 2. Get Facility by ID

**Endpoint:** `GET /api/facilities/{id}`

**Description:** Retrieves a single facility by its database ID.

**Example Request:**
```bash
curl -X GET http://localhost:8080/api/facilities/1
```

**Expected Response:** `200 OK`
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
  "createdAt": "2025-02-11T10:30:00",
  "updatedAt": "2025-02-11T10:30:00"
}
```

**Error Response:** `404 Not Found`
```json
{
  "timestamp": "2025-02-11T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Facility not found with id: 999",
  "path": "/api/facilities/999"
}
```

---

### 3. Get Facility by Code

**Endpoint:** `GET /api/facilities/code/{code}`

**Description:** Retrieves a facility by its unique code (e.g., "FAC001").

**Example Request:**
```bash
curl -X GET http://localhost:8080/api/facilities/code/FAC001
```

**Expected Response:** `200 OK` (Same structure as Get by ID)

---

### 4. Get Facilities by Region

**Endpoint:** `GET /api/facilities/region/{region}`

**Description:** Retrieves all facilities in a specific region.

**Example Request:**
```bash
curl -X GET http://localhost:8080/api/facilities/region/Attica
```

**Expected Response:** `200 OK` (Array of facilities in Attica region)

---

### 5. Get Active Facilities

**Endpoint:** `GET /api/facilities/active`

**Description:** Retrieves all facilities where `active = true`.

**Example Request:**
```bash
curl -X GET http://localhost:8080/api/facilities/active
```

**Expected Response:** `200 OK` (Array of active facilities)

---

### 6. Create Facility

**Endpoint:** `POST /api/facilities`

**Description:** Creates a new health facility.

**Request Body:**
```json
{
  "code": "FAC020",
  "name": "New Test Facility",
  "type": "Clinic",
  "region": "Attica",
  "district": "Athens",
  "latitude": 37.9838,
  "longitude": 23.7275,
  "active": true
}
```

**Example Request:**
```bash
curl -X POST http://localhost:8080/api/facilities \
  -H "Content-Type: application/json" \
  -d '{
    "code": "FAC020",
    "name": "New Test Facility",
    "type": "Clinic",
    "region": "Attica",
    "district": "Athens",
    "latitude": 37.9838,
    "longitude": 23.7275,
    "active": true
  }'
```

**Expected Response:** `201 Created`
```json
{
  "id": 17,
  "code": "FAC020",
  "name": "New Test Facility",
  "type": "Clinic",
  "region": "Attica",
  "district": "Athens",
  "latitude": 37.9838,
  "longitude": 23.7275,
  "active": true,
  "createdAt": "2025-02-11T11:00:00",
  "updatedAt": "2025-02-11T11:00:00"
}
```

**Validation Error Example:** `400 Bad Request`
```json
{
  "timestamp": "2025-02-11T11:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Facility with code 'FAC001' already exists",
  "path": "/api/facilities"
}
```

---

### 7. Update Facility

**Endpoint:** `PUT /api/facilities/{id}`

**Description:** Updates an existing facility. Only provided fields are updated.

**Request Body:**
```json
{
  "name": "Athens General Hospital - Updated Name",
  "active": true
}
```

**Example Request:**
```bash
curl -X PUT http://localhost:8080/api/facilities/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Athens General Hospital - Updated Name",
    "active": true
  }'
```

**Expected Response:** `200 OK` (Updated facility object)

---

### 8. Delete Facility

**Endpoint:** `DELETE /api/facilities/{id}`

**Description:** Permanently deletes a facility (hard delete).

**Example Request:**
```bash
curl -X DELETE http://localhost:8080/api/facilities/17
```

**Expected Response:** `204 No Content` (Empty response body)

---

## Health Indicators API

Base Path: `/api/indicators`

### 1. Get All Indicators

**Endpoint:** `GET /api/indicators`

**Example Request:**
```bash
curl -X GET http://localhost:8080/api/indicators
```

**Expected Response:** `200 OK`
```json
[
  {
    "id": 1,
    "code": "IND001",
    "name": "Malaria Cases",
    "description": "Total confirmed malaria cases reported",
    "category": "Disease Surveillance",
    "dataType": "NUMBER",
    "unit": "cases",
    "active": true
  },
  {
    "id": 2,
    "code": "IND002",
    "name": "Tuberculosis Cases",
    "description": "New tuberculosis cases diagnosed",
    "category": "Disease Surveillance",
    "dataType": "NUMBER",
    "unit": "cases",
    "active": true
  }
]
```

---

### 2. Get Indicator by ID

**Endpoint:** `GET /api/indicators/{id}`

**Example Request:**
```bash
curl -X GET http://localhost:8080/api/indicators/1
```

**Expected Response:** `200 OK` (Single indicator object)

---

### 3. Get Indicator by Code

**Endpoint:** `GET /api/indicators/code/{code}`

**Example Request:**
```bash
curl -X GET http://localhost:8080/api/indicators/code/IND001
```

**Expected Response:** `200 OK` (Single indicator object)

---

### 4. Get Indicators by Category

**Endpoint:** `GET /api/indicators/category/{category}`

**Example Request:**
```bash
curl -X GET "http://localhost:8080/api/indicators/category/Disease Surveillance"
```

**Note:** URL encode spaces as `%20` if needed.

**Expected Response:** `200 OK` (Array of indicators in that category)

---

### 5. Get Indicators by Data Type

**Endpoint:** `GET /api/indicators/data-type/{dataType}`

**Example Request:**
```bash
curl -X GET http://localhost:8080/api/indicators/data-type/PERCENTAGE
```

**Expected Response:** `200 OK` (Array of percentage-based indicators)

---

### 6. Get Active Indicators

**Endpoint:** `GET /api/indicators/active`

**Example Request:**
```bash
curl -X GET http://localhost:8080/api/indicators/active
```

**Expected Response:** `200 OK` (Array of active indicators)

---

### 7. Create Indicator

**Endpoint:** `POST /api/indicators`

**Request Body:**
```json
{
  "code": "IND020",
  "name": "Test Indicator",
  "description": "This is a test indicator",
  "category": "Testing",
  "dataType": "NUMBER",
  "unit": "tests",
  "active": true
}
```

**Example Request:**
```bash
curl -X POST http://localhost:8080/api/indicators \
  -H "Content-Type: application/json" \
  -d '{
    "code": "IND020",
    "name": "Test Indicator",
    "description": "This is a test indicator",
    "category": "Testing",
    "dataType": "NUMBER",
    "unit": "tests",
    "active": true
  }'
```

**Expected Response:** `201 Created`

---

### 8. Update Indicator

**Endpoint:** `PUT /api/indicators/{id}`

**Example Request:**
```bash
curl -X PUT http://localhost:8080/api/indicators/1 \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Updated description for malaria cases"
  }'
```

**Expected Response:** `200 OK`

---

### 9. Delete Indicator

**Endpoint:** `DELETE /api/indicators/{id}`

**Example Request:**
```bash
curl -X DELETE http://localhost:8080/api/indicators/17
```

**Expected Response:** `204 No Content`

---

## Data Values API

Base Path: `/api/data-values`

### 1. Submit Data Value

**Endpoint:** `POST /api/data-values`

**Description:** Submits a new data value (e.g., "50 malaria cases at Athens Hospital in January").

**Request Body:**
```json
{
  "facilityId": 1,
  "indicatorId": 1,
  "periodStart": "2025-01-01",
  "periodEnd": "2025-01-31",
  "periodType": "MONTHLY",
  "value": 45.0,
  "comment": "Data verified by supervisor"
}
```

**Example Request:**
```bash
curl -X POST http://localhost:8080/api/data-values \
  -H "Content-Type: application/json" \
  -d '{
    "facilityId": 1,
    "indicatorId": 1,
    "periodStart": "2025-01-01",
    "periodEnd": "2025-01-31",
    "periodType": "MONTHLY",
    "value": 45.0,
    "comment": "Data verified by supervisor"
  }'
```

**Expected Response:** `201 Created`
```json
{
  "id": 1500,
  "facilityId": 1,
  "facilityName": "Athens General Hospital",
  "indicatorId": 1,
  "indicatorName": "Malaria Cases",
  "periodStart": "2025-01-01",
  "periodEnd": "2025-01-31",
  "periodType": "MONTHLY",
  "value": 45.0,
  "comment": "Data verified by supervisor",
  "createdBy": "SYSTEM_SEEDER",
  "createdAt": "2025-02-11T12:00:00",
  "updatedAt": "2025-02-11T12:00:00"
}
```

**Duplicate Error:** `409 Conflict`
```json
{
  "timestamp": "2025-02-11T12:00:00",
  "status": 409,
  "error": "Conflict",
  "message": "Data already exists for facility 1, indicator 1, period 2025-01-01",
  "path": "/api/data-values"
}
```

---

### 2. Get Data Value by ID

**Endpoint:** `GET /api/data-values/{id}`

**Example Request:**
```bash
curl -X GET http://localhost:8080/api/data-values/1
```

**Expected Response:** `200 OK`

---

### 3. Get Data Values by Facility

**Endpoint:** `GET /api/data-values/facility/{facilityId}`

**Description:** Retrieves all data values for a specific facility (all time periods).

**Example Request:**
```bash
curl -X GET http://localhost:8080/api/data-values/facility/1
```

**Expected Response:** `200 OK` (Array of all data values for that facility)

---

### 4. Get Data Values by Facility and Period

**Endpoint:** `GET /api/data-values/facility/{facilityId}/period?startDate={date}&endDate={date}`

**Description:** Retrieves data values for a facility within a specific date range.

**Query Parameters:**
- `startDate` (required): Start date in format `YYYY-MM-DD`
- `endDate` (required): End date in format `YYYY-MM-DD`

**Example Request:**
```bash
curl -X GET "http://localhost:8080/api/data-values/facility/1/period?startDate=2024-01-01&endDate=2024-12-31"
```

**Expected Response:** `200 OK` (Array of data values in the date range)

---

### 5. Get Data Values by Indicator

**Endpoint:** `GET /api/data-values/indicator/{indicatorId}`

**Description:** Retrieves all data values for a specific indicator across all facilities.

**Example Request:**
```bash
curl -X GET http://localhost:8080/api/data-values/indicator/1
```

**Expected Response:** `200 OK` (Array of data values for that indicator)

---

### 6. Aggregate Data by Region

**Endpoint:** `GET /api/data-values/aggregate/region`

**Description:** Aggregates data values by region for dashboard visualizations. Returns the SUM of values grouped by region.

**Query Parameters:**
- `indicatorId` (required): The indicator to aggregate
- `region` (optional): Filter to a specific region
- `startDate` (required): Start date `YYYY-MM-DD`
- `endDate` (required): End date `YYYY-MM-DD`

**Example Request (All Regions):**
```bash
curl -X GET "http://localhost:8080/api/data-values/aggregate/region?indicatorId=1&startDate=2024-01-01&endDate=2024-12-31"
```

**Expected Response:** `200 OK`
```json
{
  "Attica": 1250.0,
  "Central Macedonia": 980.0,
  "Crete": 450.0,
  "Western Greece": 320.0,
  "Thessaly": 280.0
}
```

**Example Request (Single Region):**
```bash
curl -X GET "http://localhost:8080/api/data-values/aggregate/region?indicatorId=1&region=Attica&startDate=2024-01-01&endDate=2024-12-31"
```

**Expected Response:** `200 OK`
```json
{
  "Attica": 1250.0
}
```

---

### 7. Get Total for Indicator

**Endpoint:** `GET /api/data-values/total`

**Description:** Calculates the total (sum) for a specific indicator across all facilities.

**Query Parameters:**
- `indicatorId` (required)
- `startDate` (required): Format `YYYY-MM-DD`
- `endDate` (required): Format `YYYY-MM-DD`

**Example Request:**
```bash
curl -X GET "http://localhost:8080/api/data-values/total?indicatorId=1&startDate=2024-01-01&endDate=2024-12-31"
```

**Expected Response:** `200 OK`
```json
3280.0
```

---

### 8. Get Average for Indicator

**Endpoint:** `GET /api/data-values/average`

**Description:** Calculates the average value for a specific indicator.

**Query Parameters:**
- `indicatorId` (required)
- `startDate` (required): Format `YYYY-MM-DD`
- `endDate` (required): Format `YYYY-MM-DD`

**Example Request:**
```bash
curl -X GET "http://localhost:8080/api/data-values/average?indicatorId=4&startDate=2024-01-01&endDate=2024-12-31"
```

**Expected Response:** `200 OK`
```json
78.45
```

---

### 9. Delete Data Value

**Endpoint:** `DELETE /api/data-values/{id}`

**Description:** Deletes a data value (for correcting mistakes).

**Example Request:**
```bash
curl -X DELETE http://localhost:8080/api/data-values/1500
```

**Expected Response:** `204 No Content`

---

## Error Response Format

All errors follow this consistent format:

```json
{
  "timestamp": "2025-02-11T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Detailed error message explaining what went wrong",
  "path": "/api/facilities/999"
}
```

### Common HTTP Status Codes

- `200 OK`: Request succeeded
- `201 Created`: Resource successfully created
- `204 No Content`: Successful deletion (no response body)
- `400 Bad Request`: Invalid request data (validation error)
- `404 Not Found`: Resource doesn't exist
- `409 Conflict`: Duplicate resource (e.g., code already exists)
- `500 Internal Server Error`: Server-side error

---

## Testing with cURL

### Tips for cURL Testing:

1. **Pretty-print JSON responses** (requires `jq`):
   ```bash
   curl -X GET http://localhost:8080/api/facilities | jq
   ```

2. **Save response to file**:
   ```bash
   curl -X GET http://localhost:8080/api/facilities > facilities.json
   ```

3. **Include HTTP headers in output**:
   ```bash
   curl -i -X GET http://localhost:8080/api/facilities
   ```

4. **Verbose mode** (see full request/response):
   ```bash
   curl -v -X GET http://localhost:8080/api/facilities/1
   ```

---

## Testing with Postman

### Importing the Collection:

1. Open Postman
2. Click **Import** button
3. Select `Health-Metrics-Tracker.postman_collection.json`
4. All endpoints will be organized in folders

### Environment Variables:

Create a Postman environment with:
- `baseUrl`: `http://localhost:8080/api`

### Running All Tests:

Use Postman's Collection Runner to execute all requests in sequence and verify responses.

---

## Quick Testing Workflow

### 1. Verify Application is Running
```bash
curl -X GET http://localhost:8080/api/facilities
```

### 2. Test CRUD Operations on Facilities

**Create:**
```bash
curl -X POST http://localhost:8080/api/facilities \
  -H "Content-Type: application/json" \
  -d '{"code":"TEST001","name":"Test Facility","type":"Clinic","region":"Attica","district":"Athens","latitude":37.98,"longitude":23.72,"active":true}'
```

**Read:**
```bash
curl -X GET http://localhost:8080/api/facilities/code/TEST001
```

**Update:**
```bash
curl -X PUT http://localhost:8080/api/facilities/{id} \
  -H "Content-Type: application/json" \
  -d '{"name":"Updated Test Facility"}'
```

**Delete:**
```bash
curl -X DELETE http://localhost:8080/api/facilities/{id}
```

### 3. Test Data Submission and Aggregation

**Submit Data:**
```bash
curl -X POST http://localhost:8080/api/data-values \
  -H "Content-Type: application/json" \
  -d '{"facilityId":1,"indicatorId":1,"periodStart":"2025-02-01","periodEnd":"2025-02-28","periodType":"MONTHLY","value":42.0,"comment":"Test data"}'
```

**View Aggregation:**
```bash
curl -X GET "http://localhost:8080/api/data-values/aggregate/region?indicatorId=1&startDate=2024-01-01&endDate=2025-12-31"
```

---

**Document Version:** 1.0  
**Last Updated:** February 11, 2025  
**Application Version:** Spring Boot 4.0.2
