# Task Manager API

![CI](https://github.com/Nelius1k/Task-Manager-API/actions/workflows/ci.yml/badge.svg)

A RESTful Task Manager API built with Java 17 and Spring Boot. The API supports creating, updating, retrieving, searching, filtering, and deleting tasks while demonstrating backend development practices such as layered architecture, DTO usage, validation, exception handling, database migrations, automated testing, Dockerized deployment, and CI automation.

## Features

- Create, read, update, and delete tasks
- Update task status independently with a PATCH endpoint
- Search tasks by title or description
- Filter tasks by status, priority, and due date
- Pagination and sorting support for task retrieval
- Request validation for input data
- Global exception handling with consistent error responses
- DTO-based request and response structure
- Mapper layer for converting between entities and DTOs
- Spring Data JPA repository layer with PostgreSQL persistence
- Specification-based dynamic search and filtering
- Flyway database migrations
- Swagger/OpenAPI documentation
- Unit tests for the service layer
- Controller tests using MockMvc
- Integration testing with Testcontainers and PostgreSQL
- Docker and Docker Compose support
- GitHub Actions CI pipeline

## Tech Stack

- Java 17
- Spring Boot
- Spring Web
- Spring Data JPA
- PostgreSQL
- Flyway
- Maven
- JUnit 5
- Mockito
- MockMvc
- Testcontainers
- Docker
- Docker Compose
- GitHub Actions
- Swagger/OpenAPI

## Architecture

The project follows a layered architecture to keep responsibilities separate and the codebase maintainable:

- **Controller Layer**: Handles HTTP requests and responses
- **Service Layer**: Contains business logic and validation
- **Repository Layer**: Manages database access through Spring Data JPA
- **DTO Layer**: Defines request and response models
- **Mapper Layer**: Converts between entities and DTOs
- **Specification Layer**: Builds dynamic filtering queries
- **Exception Handling**: Centralized using `@ControllerAdvice`
- **Database Migration Layer**: Manages schema changes with Flyway

## Project Structure

```text
src
|-- main
|   |-- java/com/taskmanager/api
|   |   |-- config          # Application and OpenAPI configuration
|   |   |-- controller      # REST controllers
|   |   |-- dto             # Request, response, and error DTOs
|   |   |-- entity          # JPA entities and enums
|   |   |-- exception       # Custom exceptions and global exception handling
|   |   |-- mapper          # Entity/DTO mapping
|   |   |-- repository      # Spring Data JPA repositories
|   |   |-- service         # Business logic
|   |   `-- specification   # Dynamic query specifications
|   `-- resources
|       |-- application.properties
|       `-- db/migration    # Flyway migration scripts
`-- test
    |-- java/com/taskmanager/api
    |   |-- TaskControllerTests.java
    |   |-- TaskIntegrationTests.java
    |   |-- TaskManagerApiApplicationTests.java
    |   `-- TaskServiceTests.java
    `-- resources
        `-- docker-java.properties
```

## API Endpoints

| Method | Endpoint               | Description                          |
| ------ | ---------------------- | ------------------------------------ |
| POST   | `/api/v1/tasks`        | Create a new task                    |
| GET    | `/api/v1/tasks/{id}`   | Get a task by ID                     |
| GET    | `/api/v1/tasks/search` | Search, filter, page, and sort tasks |
| PUT    | `/api/v1/tasks/{id}`   | Update an existing task              |
| PATCH  | `/api/v1/tasks/{id}`   | Update a task's status               |
| DELETE | `/api/v1/tasks/{id}`   | Delete a task                        |

The `GET /api/v1/tasks/search` endpoint supports filtering, pagination, and sorting using query parameters. All query parameters are optional and can be combined.

Supported query parameters include:

- `q` - search term to filter tasks by title or description
- `status` - filter by task status, such as `TODO`, `IN_PROGRESS`, or `DONE`
- `priority` - filter by task priority, such as `LOW`, `MEDIUM`, or `HIGH`
- `dueBefore` - filter tasks due before a specific date in `yyyy-MM-dd` format
- `page` - page number, starting at `0`
- `size` - number of results per page
- `sort` - field and direction, such as `createdAt,desc`

### Example Search Request

```http
GET /api/v1/tasks/search?dueBefore=2026-04-29&page=0&size=10&sort=createdAt,desc
```

This request filters tasks due before a specific date, returns page 0 with 10 results, and sorts by `createdAt` in descending order.

## Example Request

### Create Task

```http
POST /api/v1/tasks
```

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

## API Documentation

Swagger UI is available when the application is running:

```http
http://localhost:8080/swagger-ui/index.html
```

The generated OpenAPI document is available at:

```http
http://localhost:8080/v3/api-docs
```

These endpoints provide interactive documentation for the available task endpoints, request bodies, response models, and error responses.

## Running the Project

### Prerequisites

- Java 17
- Maven or the included Maven wrapper
- PostgreSQL
- Docker and Docker Compose, if running the application in containers

### Run Locally

1. Clone the repository.
2. Create a PostgreSQL database, for example `task_manager`.
3. Update `src/main/resources/application.properties` with your database credentials:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/task_manager
spring.datasource.username=your_username
spring.datasource.password=your_password
```

4. Start the application:

```bash
mvn spring-boot:run
```

The API will be available at:

```http
http://localhost:8080
```

### Run with Docker Compose

```bash
docker compose up --build
```

Docker Compose starts both the Spring Boot application and a PostgreSQL 16 database.

The API will be available at:

```http
http://localhost:8080
```

To stop the containers:

```bash
docker compose down
```

## Database Migrations

Flyway manages database schema changes. Migration scripts are stored in:

```text
src/main/resources/db/migration
```

The initial migration creates the tasks table and is applied automatically when the application starts.

## Testing

The project includes multiple levels of automated tests:

- **Service layer tests**: Use JUnit 5 and Mockito to verify business logic in isolation.
- **Controller tests**: Use MockMvc to verify request handling, validation, JSON responses, and HTTP status codes.
- **Integration tests**: Use Spring Boot, MockMvc, Testcontainers, and PostgreSQL to test the full request flow through the controller, service, repository, and database.

Run all tests with:

```bash
mvn test
```

The integration tests start a PostgreSQL Testcontainer automatically. Docker must be running for these tests.

## Continuous Integration

This project uses GitHub Actions to automatically run the Maven test suite on every push and pull request to `main`.

The CI pipeline:

- Checks out the repository
- Sets up a Java environment
- Starts a PostgreSQL 16 service container
- Runs unit, controller, and integration tests automatically
- Fails the build if any test or build step fails
