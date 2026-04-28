# Task Manager API

A RESTful Task Manager API built with Java and Spring Boot for creating, updating, retrieving, and deleting tasks. This project demonstrates backend development best practices such as layered architecture, DTO usage, validation, exception handling, and unit testing.

## Features

- Create, read, update, and delete tasks
- Request validation for input data
- Global exception handling with consistent error responses
- Pagination and sorting support for task retrieval
- DTO-based request and response structure
- Layered architecture for maintainability
- Unit tests for controller and service layers

## Tech Stack

- Java
- Spring Boot
- Spring Web
- Spring Data JPA
- PostgreSQL
- Maven
- JUnit 5
- Mockito
- MockMvc

## Architecture

The project follows a layered architecture to keep responsibilities separate and the codebase maintainable:

- **Controller Layer**: Handles HTTP requests and responses
- **Service Layer**: Contains business logic and validation
- **Repository Layer**: Manages database access
- **DTO Layer**: Defines request and response models
- **Mapper Layer**: Converts between entities and DTOs
- **Exception Handling**: Centralized using `@ControllerAdvice`

## API Endpoints

| Method | Endpoint             | Description             |
| ------ | -------------------- | ----------------------- |
| POST   | /api/v1/tasks        | Create a new task       |
| GET    | /api/v1/tasks/{id}   | Get a task by ID        |
| GET    | /api/v1/tasks/search | Get all tasks           |
| PUT    | /api/v1/tasks/{id}   | Update an existing task |
| PATCH  | /api/v1/tasks/{id}   | Update a task's status  |
| DELETE | /api/v1/tasks/{id}   | Delete a task           |

The `GET /api/v1/tasks/search` endpoint supports filtering, pagination, and sorting using query parameters. All query parameters are optional and can be combined.Supported query parameters include:

- `q` – search term to filter tasks by title or description
- `status` – filter by task status (e.g. TODO, IN_PROGRESS, DONE)
- `priority` – filter by task priority (e.g. LOW, MEDIUM, HIGH)
- `dueBefore` – filter tasks due before a specific date (format: yyyy-MM-dd)
- `page` – page number (default: 0)
- `size` – number of results per page
- `sort` – field and direction (e.g. `createdAt,desc`)

### Example Search Request

```http
GET /api/v1/tasks/search?dueBefore=2026-04-29&page=0&size=10&sort=createdAt,desc
```

This request filters tasks due before a specific date, returns page 0 with 10 results, and sorts by `createdAt` in descending order.

## Example Request

### Create Task (POST /api/v1/tasks)

```json
{
  "title": "Buy groceries",
  "description": "Buy rice, milk, and eggs",
  "status": "TODO",
  "priority": "HIGH",
  "dueDate": "2026-04-10"
}
```

## Example Response

Response returned after successfully creating a task:

```json
{
  "id": "7d1f1f5e-8b8c-4c30-9c1c-9fd91a9e7a23",
  "title": "Buy groceries",
  "description": "Buy rice, milk, and eggs",
  "status": "TODO",
  "priority": "HIGH",
  "dueDate": "2026-04-10",
  "createdAt": "2026-04-02T14:30:00Z",
  "updatedAt": "2026-04-02T14:30:00Z"
}
```

## Example Error Response

```json
{
  "error": "BAD_REQUEST",
  "message": "The input INVALID is invalid.",
  "fields": {
    "values": "Allowed values: [TODO, IN_PROGRESS, DONE]"
  }
}
```

## Running the Project

### Prerequisites

- Java 17+
- Maven
- PostgreSQL

### Steps

1. Clone the repository
2. Create a PostgreSQL database (e.g. task_manager)
3. Update application.properties with your database credentials:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/task_manager
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### Run the Application

```bash
mvn spring-boot:run
```

### Run with Docker

```bash
docker compose up --build
```

The API will be available at:

```http
http://localhost:8080
```

To stop the containers:

```bash
docker compose down
```

## Future Improvements

- Add authentication and authorization
- Add integration tests with Testcontainers
- Add API documentation with Swagger/OpenAPI
- Add CI/CD pipeline
- Add AWS deployment
- Add Kubernetes orchestration
- Add Kafka integration for event-driven processing
