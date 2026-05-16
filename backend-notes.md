# 🧠 Backend Engineering Notes

---

## 🐳 Docker

**Why**

- Run app + DB in isolated, reproducible environments

**How I used it (Task Manager API)**

- Containerized Spring Boot app + PostgreSQL using Docker Compose
- Used service name (postgres) for DB connection instead of localhost
- Fixed port conflict (5433 in use → changed mapping)

**Key Commands**

```bash
docker compose up --build
docker compose down
docker ps
docker logs -f <container>
```

**Gotchas**

- Port already in use → change host port
- Containers “Created” but not running → usually startup error

## 🗄️ PostgreSQL (Docker)

**Why**

Persistent relational database for production-like setup

How I used it

Ran Postgres in Docker with volume for persistence
Connected via:
jdbc:postgresql://postgres:5432/db_name

Gotchas

Don’t use localhost inside Docker network
Use service name from docker-compose.yml
🧪 Testing Strategy
Unit Tests (Mockito)

Why

Test business logic in isolation

How I used it

Mocked repository layer
Tested service methods (success + exception cases)

Rule

Test behavior, not implementation
🔥 Integration Tests (Testcontainers) (TO ADD)

Why

Test with real PostgreSQL instead of mocks

What it will do

Spin up temporary Postgres container during tests
Validate JPA, queries, mappings

When to use

Repository layer
Full request → DB flow
📘 Swagger / OpenAPI (TO ADD)

Why

Interactive API documentation

What it gives

UI to test endpoints without Postman
Shows request/response structure

Endpoint

http://localhost:8080/swagger-ui.html
⚙️ GitHub Actions (CI/CD) (TO ADD)

Why

Automatically run tests on every push

Flow

Push code → Run tests → Build project → Pass/Fail

Value

Ensures code is always working
Industry standard
🧱 Architecture (Your Project)

Pattern

Controller → Service → Repository → Database

Why

Separation of concerns
Maintainability and testability
⚠️ Exception Handling

How I handled it

Global exception handler (@ControllerAdvice)
Custom exceptions (e.g., TaskNotFoundException)

Examples

Invalid enum → handled via HttpMessageNotReadableException
Business rules (e.g., past due date) → handled in service layer
📦 API Design

Base URL

/api/v1/tasks

Key Features

CRUD operations
Pagination + sorting
DTO-based requests/responses
🔑 Things I Need to Remember
Service layer = business logic
Controller = request validation + routing
Repository = DB access only
Don’t mix responsibilities
🚀 Future Improvements
Integration tests with Testcontainers
Swagger/OpenAPI documentation
GitHub Actions CI pipeline
Authentication (JWT)
AWS deployment
Kubernetes
Kafka (event-driven architecture)
💬 Interview Notes (VERY IMPORTANT)

Docker

“I containerized my API and database using Docker Compose…”

Testing

“I used Mockito for unit tests and plan to add Testcontainers for integration tests…”

Architecture

“I followed a layered architecture to separate concerns…”
🧩 Common Problems I Solved
Port conflicts in Docker
Enum validation errors in Spring Boot
Testing service layer with mocks
Connecting services inside Docker network
🛠️ Commands Cheat Sheet
mvn clean install
mvn test
docker compose up
docker compose down
git push origin main
