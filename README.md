# Residential Parking API

A small Spring Boot service for managing guest parking in residential communities.

Residents can check which parking spots are available for a time range, create a booking, start and release a parking session, and view their current and future bookings.

I also added booking cancellation as an extra resident use case. Communities, residents, vehicles, and parking spots are treated as pre-existing reference data for this task.

## Tech Stack

- Java 17
- Spring Boot
- Spring Web MVC
- Spring Data JPA
- PostgreSQL 17
- Flyway
- Docker Compose
- JUnit 5 / Mockito / MockMvc

## Running the project

Requirements:

- Java 17+
- Docker with Docker Compose
- Maven is not required because the Maven wrapper is included.

Start PostgreSQL:

```bash
docker compose up -d
```

The default database is available on `localhost:5433`. Flyway creates and validates the schema when the application starts.

Load the rerunnable demo data:

```bash
docker compose exec -T postgres \
  psql -U parking_user -d residential_parking \
  < scripts/demo-data.sql
```

The script creates one community, one resident, one vehicle, and three parking spots. The IDs used in API calls can be copied from `scripts/demo-data.sql`.

Run the application:

```bash
./mvnw spring-boot:run
```

The API is available at `http://localhost:8080`. Its health endpoint is:

```http
GET /actuator/health
```

Run tests while PostgreSQL is running:

```bash
./mvnw test
```

The default database settings can be overridden with `DB_URL`, `DB_NAME`, `DB_USERNAME`, and `DB_PASSWORD`.

## Find available parking spots

```http
GET /api/communities/{communityId}/parking-spots/available?start=2030-01-01T10:00:00Z&end=2030-01-01T12:00:00Z
```

Example response:

```json
[
  {
    "id": "40000000-0000-0000-0000-000000000004",
    "communityId": "10000000-0000-0000-0000-000000000001",
    "code": "A-1",
    "status": "ACTIVE"
  }
]
```

A spot is available when it is active and has no overlapping `CONFIRMED` or `USED` booking. An empty result is returned as `200 OK` with `[]`.

## Create a booking

```http
POST /api/bookings
Content-Type: application/json
```

```json
{
  "communityId": "10000000-0000-0000-0000-000000000001",
  "spotId": "40000000-0000-0000-0000-000000000004",
  "residentId": "20000000-0000-0000-0000-000000000002",
  "vehicleId": "30000000-0000-0000-0000-000000000003",
  "start": "2030-01-01T10:00:00Z",
  "end": "2030-01-01T12:00:00Z"
}
```

Example response:

```json
{
  "id": "50000000-0000-0000-0000-000000000005",
  "communityId": "10000000-0000-0000-0000-000000000001",
  "spotId": "40000000-0000-0000-0000-000000000004",
  "residentId": "20000000-0000-0000-0000-000000000002",
  "vehicleId": "30000000-0000-0000-0000-000000000003",
  "start": "2030-01-01T10:00:00Z",
  "end": "2030-01-01T12:00:00Z",
  "checkInDeadline": "2030-01-01T10:15:00Z",
  "status": "CONFIRMED"
}
```

The application checks availability before saving. PostgreSQL also has an exclusion constraint that prevents overlapping active bookings even when two requests arrive concurrently.

## View a resident's current and future bookings

```http
GET /api/residents/{residentId}/bookings
```

Example response (short version):

```json
{
  "current": [
    {
      "id": "50000000-0000-0000-0000-000000000005",
      "start": "2030-01-01T10:00:00Z",
      "end": "2030-01-01T12:00:00Z",
      "status": "USED"
    }
  ],
  "future": [
    {
      "id": "60000000-0000-0000-0000-000000000006",
      "start": "2030-01-02T10:00:00Z",
      "end": "2030-01-02T12:00:00Z",
      "status": "CONFIRMED"
    }
  ]
}
```

Current bookings have already started but have not ended. Future bookings start after the current server time. Cancelled, expired, and already-ended bookings are excluded, and results are ordered by start time.

## Start a parking session

```http
POST /api/bookings/{bookingId}/parking-session
```

The server generates the start time. A session can start from the booking's start time until its check-in deadline, which is at most 15 minutes after the start. When trying this manually, create the booking with a start time around the current UTC time.

Example response:

```json
{
  "id": "70000000-0000-0000-0000-000000000007",
  "bookingId": "50000000-0000-0000-0000-000000000005",
  "spotId": "40000000-0000-0000-0000-000000000004",
  "vehicleId": "30000000-0000-0000-0000-000000000003",
  "startedAt": "2030-01-01T10:05:00Z",
  "finishedAt": null,
  "status": "ACTIVE"
}
```

## Release a parking session

```http
POST /api/parking-sessions/{sessionId}/release
```

The server generates the finish time and changes the session status to `FINISHED`.

## Cancel a booking

```http
POST /api/bookings/{bookingId}/cancel
```

A resident can cancel a confirmed booking before its check-in deadline. The booking remains stored for history, but its status becomes `CANCELLED` and it no longer blocks the parking spot.

## Error responses

Domain and input failures use one response shape:

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Parking spot is not available for the requested time.",
  "path": "/api/bookings"
}
```

The main mappings are:

- `400 Bad Request` for invalid input, time ranges, and path values.
- `404 Not Found` when a requested booking, parking spot, or parking session does not exist.
- `409 Conflict` for invalid state transitions, unavailable spots, and database conflicts caused by concurrent writes.

Database exception details are not returned to the client.

## Design

The project is split into API, application, domain, and infrastructure layers.

The REST controllers own HTTP parsing and response mapping. Application services coordinate use cases and transaction boundaries. The domain contains booking, parking spot, parking session, and time-range rules without depending on Spring. Repository interfaces belong to the domain, while JPA entities and Spring Data adapters stay in infrastructure.

I used an injected `Clock` for server-generated timestamps so starting, releasing, cancelling, and querying bookings can be tested deterministically.

Time ranges use half-open interval semantics: `[start, end)`. Two bookings can therefore touch at a boundary without overlapping. The same rule is used in the domain overlap logic, the JPA query, and PostgreSQL's exclusion constraint.

Parking state is represented separately from booking state. A booking reserves a future time range, while a parking session represents the vehicle actually occupying a spot. This keeps reservation history even after a session is released.
