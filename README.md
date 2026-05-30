# TaskManagement

TaskManagement is a Spring Boot backend API for managing projects and tasks.

## Stack

- Java 21
- Spring Boot
- Spring Web MVC
- Spring Data JPA
- Bean Validation
- MySQL for development
- PostgreSQL profile for production
- Maven

## Current Features

- Project CRUD basics
- Task CRUD basics
- Soft delete and restore for projects and tasks
- Hard delete endpoints for local/prototype use
- Task status update
- Task priority update
- Validation error handling
- Environment-based database configuration

## Setup

Create a local `.env` file in the project root. Do not commit it.

```env
SPRING_PROFILES_ACTIVE=dev
SERVER_PORT=8080
DB_URL=jdbc:mysql://localhost:3306/task_management
DB_USERNAME=your_username
DB_PASSWORD=your_password
```

Make sure the MySQL database exists:

```sql
CREATE DATABASE task_management;
```

## Run

```bash
mvn spring-boot:run
```

The API runs on:

```text
http://localhost:8080
```

Swagger UI should be available at:

```text
http://localhost:8080/swagger-ui/index.html
```

## Test

```bash
mvn test
```

## Main Endpoints

### Projects

```text
POST   /api/projects
GET    /api/projects
PUT    /api/projects/{id}
DELETE /api/projects/{id}
DELETE /api/projects/{id}/hard
POST   /api/projects/{id}/restore
```

### Tasks

```text
POST   /api/tasks
GET    /api/tasks
GET    /api/tasks/deleted
GET    /api/tasks/{id}
PUT    /api/tasks/{id}
PATCH  /api/tasks/{id}
DELETE /api/tasks/{id}
DELETE /api/tasks/{id}/hard
POST   /api/tasks/{id}/restore
POST   /api/tasks/{id}/status
POST   /api/tasks/{id}/priority
```

## Notes

- The first MVP is focused on task and project management.
- User and auth packages exist but are not finished yet.
- Hard delete endpoints should be protected or removed before a real production release.
- Keep secrets in `.env` or environment variables, not in committed files.
