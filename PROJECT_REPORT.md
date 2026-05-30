# TaskManagement Project Report

Generated: 2026-05-30

## 1. Project Summary

TaskManagement is a Java 21 Spring Boot backend for a task/project management API. The project has moved from an early skeleton into a near-MVP backend for project and task CRUD.

The strongest current area is the task/project API. Tasks and projects now support create, list, update, soft delete, hard delete, and restore flows. Task-specific status and priority change endpoints also exist. User/auth code is still mostly incomplete and should not be treated as part of the MVP unless it is finished later.

## 2. Technology Stack

- Java 21
- Spring Boot 4.0.6
- Spring Web MVC
- Spring Data JPA
- Jakarta Bean Validation
- Lombok
- Springdoc OpenAPI UI
- `springboot4-dotenv`
- MySQL driver for development
- PostgreSQL driver for production
- Maven Wrapper
- JUnit/Spring Boot test starter setup

## 3. Project Structure

```text
TaskManagement/
|-- pom.xml
|-- README.md
|-- PROJECT_REPORT.md
|-- NEXT_FEATURE_TODO.md
|-- mvnw
|-- mvnw.cmd
|-- src/
|   |-- main/
|   |   |-- java/com/engine/taskmanagement/
|   |   |   |-- TaskManagementApplication.java
|   |   |   |-- auth/
|   |   |   |   |-- controller/
|   |   |   |   |-- service/
|   |   |   |   `-- enums/
|   |   |   |-- common/
|   |   |   |   |-- dto/
|   |   |   |   |-- entity/
|   |   |   |   `-- exception/
|   |   |   |-- project/
|   |   |   |   |-- controller/
|   |   |   |   |-- dto/
|   |   |   |   |-- entity/
|   |   |   |   |-- mapper/
|   |   |   |   |-- repository/
|   |   |   |   `-- service/
|   |   |   |-- task/
|   |   |   |   |-- controller/
|   |   |   |   |-- dto/
|   |   |   |   |-- entity/
|   |   |   |   |-- enums/
|   |   |   |   |-- mapper/
|   |   |   |   |-- repository/
|   |   |   |   `-- service/
|   |   |   `-- user/
|   |   |       |-- controller/
|   |   |       |-- dto/
|   |   |       |-- entity/
|   |   |       |-- mapper/
|   |   |       |-- repository/
|   |   |       `-- service/
|   |   `-- resources/
|   |       |-- application.properties
|   |       |-- application-dev.properties
|   |       `-- application-prod.properties
|   `-- test/
|       `-- java/com/engine/taskmanagement/
|           `-- TaskManagementApplicationTests.java
```

## 4. What The Project Has Now

### Application And Config

- Spring Boot entry point.
- JPA auditing enabled.
- Maven configuration.
- `.env` support through `springboot4-dotenv`.
- `.env` is ignored by Git.
- Base config in `application.properties`.
- MySQL development config in `application-dev.properties`.
- PostgreSQL production config in `application-prod.properties`.
- Datasource password is no longer hardcoded in `application.properties`.

### Common Layer

- `BaseEntity` with:
  - `id`
  - `createdAt`
  - `updatedAt`
  - `deletedAt`
  - `markAsDeleted()`
  - `restore()`
  - `isDeleted()`
- Custom exception classes.
- `ErrorResponse` DTO.
- `GlobalExceptionHandler` with handlers for custom exceptions.

### Task Module

Task has a complete MVP-shaped API.

Entity fields include:

- `title`
- `description`
- `priority`
- `status`
- `dueDate`
- optional project relation
- optional assignee relation

Task statuses:

- `TODO`
- `IN_PROGRESS`
- `DONE`
- `CANCELLED`

Task priorities:

- `LOW`
- `MEDIUM`
- `HIGH`
- `URGENT`

Task endpoints:

| Endpoint | Status | Notes |
|---|---:|---|
| `POST /api/tasks` | Present | Creates task. Defaults are preserved when status/priority are missing. |
| `GET /api/tasks` | Present | Lists active tasks. |
| `GET /api/tasks/deleted` | Present | Lists soft-deleted tasks. |
| `GET /api/tasks/{id}` | Present | Gets one active task. |
| `PUT /api/tasks/{id}` | Present | Full update with validation. |
| `PATCH /api/tasks/{id}` | Present | Partial update. |
| `DELETE /api/tasks/{id}` | Present | Soft delete. |
| `DELETE /api/tasks/{id}/hard` | Present | Permanent delete. |
| `POST /api/tasks/{id}/restore` | Present | Restores soft-deleted task. |
| `POST /api/tasks/{id}/status` | Present but likely bugged | Currently looks up deleted tasks, not active tasks. |
| `POST /api/tasks/{id}/priority` | Present but likely bugged | Currently looks up deleted tasks, not active tasks. |

### Project Module

Project has a mostly complete MVP-shaped API.

Entity fields include:

- `name`
- `description`
- optional owner relation
- task list relation

Project endpoints:

| Endpoint | Status | Notes |
|---|---:|---|
| `POST /api/projects` | Present | Creates project. |
| `GET /api/projects` | Present | Lists active projects. |
| `PUT /api/projects/{id}` | Present | Updates active project. |
| `DELETE /api/projects/{id}` | Present | Soft delete. |
| `DELETE /api/projects/{id}/hard` | Present | Permanent delete. |
| `POST /api/projects/{id}/restore` | Present | Restores soft-deleted project. |

### User Module

User module exists structurally but is not MVP-ready.

Present:

- `User` entity.
- `UserRepository`.
- `UserMapper`.
- `UserService` interface.
- `UserServiceImpl` annotated with `@Service`.
- `CreateUserRequest`.
- `UpdateUserRequest`.
- `UserResponse`.

Still incomplete:

- `UserController` is empty.
- `updateUser()` returns `null`.
- `getAllUsers()` currently returns deleted users.
- `getAllDeletedUsers()` returns an empty list.
- Password from create request is not mapped into entity.
- Password hashing does not exist.
- Email validation is minimal.

### Auth Module

Auth is still placeholder-level.

Present:

- `Role` enum with `USER` and `ADMIN`.
- Auth controller/service files.

Missing:

- Register endpoint.
- Login endpoint.
- Password encoder.
- JWT/session strategy.
- Spring Security config.
- Authorization rules.

## 5. Current Big Issues

### 1. Build/Test Is Not Verified

Running:

```bash
cmd /c mvnw.cmd test
```

still fails before compilation with:

```text
Cannot index into a null array.
Cannot start maven from wrapper
```

So the application has not been compile-verified in this review.

### 2. Task Status/Priority Change Likely Uses Wrong Query

`changeTaskStatus` and `changeTaskPriority` currently use:

```java
findByIdAndDeletedAtIsNotNull(id)
```

That means they look for deleted tasks. For normal status/priority changes, they should probably use active tasks:

```java
findByIdAndDeletedAtIsNull(id)
```

This is probably the most important current business-logic bug.

### 3. Validation Handler Catches The Wrong Exception

The project defines a custom `common.exception.MethodArgumentNotValidException`, and the global handler catches that custom class.

Spring validation failures normally throw:

```java
org.springframework.web.bind.MethodArgumentNotValidException
```

So validation errors from `@Valid` may not be formatted by the custom handler as intended.

### 4. Project Response Has Unmapped Fields

`ProjectResponse` includes:

- `ownerId`
- `ownerUsername`
- `taskCount`

but `ProjectMapper.toResponse()` currently sets only:

- `id`
- `name`
- `description`

This is not a blocker, but the response shape is misleading until those fields are mapped or removed.

### 5. Hard Delete Exists Without Auth

Hard delete endpoints are available for tasks and projects. For an MVP prototype this may be acceptable, but once auth exists, hard delete should probably be protected or removed from public use.

### 6. User/Auth Are Not Ready

If MVP means task/project CRUD only, this is fine.

If MVP includes users or login, the project is not ready yet.

## 6. Pluses

- Project now has a much clearer domain structure.
- Task API is close to usable for a prototype.
- Project API is close to usable for a prototype.
- Soft delete and restore exist consistently.
- Config is much safer than before: passwords are no longer committed in `application.properties`.
- `.env` support makes local development easier.
- Dev/prod database split is clear: MySQL for dev, PostgreSQL for prod.
- DTOs are used for request/response instead of returning entities in the main task/project responses.
- `ProjectResponse` no longer returns full `User` or `Task` objects.
- `UserResponse` no longer exposes password.
- Global exception handling exists.
- Validation annotations are used on important create/update DTOs.

## 7. Minuses And Risks

- Maven wrapper failure blocks confidence.
- No useful automated test coverage yet.
- Status/priority change endpoints likely operate on deleted tasks by mistake.
- Validation exception handling is incomplete.
- User module is half-implemented.
- Auth module is placeholder-only.
- `ProjectResponse` has fields that are not mapped.
- `TaskResponse` has `deletedAt`, but mapper does not set it.
- Hard delete is exposed before authorization exists.
- No pagination, sorting, or filtering.
- No database migrations.
- `ddl-auto=update` is still used in dev.
- `README.md` is still minimal.

## 8. MVP Readiness

For a task/project CRUD prototype, the project is close.

Minimum fixes before calling it MVP:

1. Fix Maven wrapper or verify build another way.
2. Fix task status/priority change queries to use active tasks.
3. Fix Spring validation exception handling.
4. Decide whether hard delete endpoints should remain available.
5. Manually test task and project flows with real MySQL.

Users/auth should wait unless they are part of the required demo.

## 9. Suggested Next Order

1. Make `mvnw test` or `mvnw spring-boot:run` work.
2. Fix `changeTaskStatus` and `changeTaskPriority`.
3. Fix validation handler to catch Spring's real validation exception.
4. Map or remove unused `ProjectResponse` fields.
5. Map `deletedAt` in responses if deleted lists need it.
6. Manually test task CRUD.
7. Manually test project CRUD.
8. Update `README.md` with setup and endpoint examples.
9. Decide whether users/auth are needed in MVP.

## 10. Feature Ideas After MVP

- Task filtering by status and priority.
- Task title search.
- Pagination and sorting.
- Assign tasks to projects through `projectId`.
- Assign tasks to users.
- User CRUD.
- Register/login.
- Password hashing.
- JWT auth.
- Role-based authorization.
- Comments on tasks.
- Activity history.
- Project members.
- Dashboard statistics.
- Flyway or Liquibase migrations.
- Docker Compose for local MySQL.

## 11. Overall Assessment

The project is no longer just a skeleton. It is now close to a working backend prototype for task and project management.

The best next move is not adding more features. The best next move is verification and bug fixing: get the app running, fix the active/deleted lookup bug in task status/priority changes, and make validation errors behave correctly. After that, the task/project MVP should be testable end to end.
