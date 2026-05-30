# TaskManagement Project Report

Generated: 2026-05-30

## 1. Project Summary

TaskManagement is a Java 21 Spring Boot backend for a task/project management API. The project is now close to a working MVP for task and project CRUD.

The task and project modules are the strongest parts of the codebase. User and auth modules exist structurally, but they are not ready for the first MVP unless you choose to finish them next.

## 2. Technology Stack

- Java 21
- Spring Boot 4.0.6
- Spring Web MVC
- Spring Data JPA
- Jakarta Bean Validation
- Lombok
- Springdoc OpenAPI UI
- `springboot4-dotenv`
- MySQL for development
- PostgreSQL for production
- Maven Wrapper
- JUnit/Spring Boot test starter setup

## 3. Current Project Structure

```text
TaskManagement/
|-- pom.xml
|-- README.md
|-- PROJECT_REPORT.md
|-- NEXT_FEATURE_TODO.md
|-- mvnw
|-- mvnw.cmd
|-- .mvn/wrapper/maven-wrapper.properties
|-- src/
|   |-- main/
|   |   |-- java/com/engine/taskmanagement/
|   |   |   |-- auth/
|   |   |   |-- common/
|   |   |   |-- project/
|   |   |   |-- task/
|   |   |   |-- user/
|   |   |   `-- TaskManagementApplication.java
|   |   `-- resources/
|   |       |-- application.properties
|   |       |-- application-dev.properties
|   |       `-- application-prod.properties
|   `-- test/
|       `-- java/com/engine/taskmanagement/
|           `-- TaskManagementApplicationTests.java
```

## 4. What The Project Has

### Configuration

- `.env` support through `springboot4-dotenv`.
- `.env` is ignored by Git.
- Base app config in `application.properties`.
- MySQL dev profile in `application-dev.properties`.
- PostgreSQL prod profile in `application-prod.properties`.
- Datasource credentials are loaded from environment variables instead of being hardcoded in the committed base config.

### Common Layer

- `BaseEntity` with `id`, `createdAt`, `updatedAt`, `deletedAt`, soft delete, restore, and deleted-state helpers.
- Custom exceptions for common API errors.
- `ErrorResponse` DTO.
- `GlobalExceptionHandler`.
- The validation handler now imports Spring's real `org.springframework.web.bind.MethodArgumentNotValidException`.

### Task Module

Task module is MVP-close.

Task endpoints:

| Endpoint | Status | Notes |
|---|---:|---|
| `POST /api/tasks` | Present | Creates task. Missing status/priority keep entity defaults. |
| `GET /api/tasks` | Present | Lists active tasks. |
| `GET /api/tasks/deleted` | Present | Lists soft-deleted tasks. |
| `GET /api/tasks/{id}` | Present | Gets one active task. |
| `PUT /api/tasks/{id}` | Present | Full update with `@Valid`. |
| `PATCH /api/tasks/{id}` | Present | Partial update. |
| `DELETE /api/tasks/{id}` | Present | Soft delete. |
| `DELETE /api/tasks/{id}/hard` | Present | Permanent delete. |
| `POST /api/tasks/{id}/restore` | Present | Restores deleted task. |
| `POST /api/tasks/{id}/status` | Present | Changes status on active task. |
| `POST /api/tasks/{id}/priority` | Present | Changes priority on active task. |

Recent improvements:

- Status/priority change methods now use active tasks.
- Standalone task restore logic is fixed.
- Task create keeps default status/priority if request omits them.
- Duplicate description validation was removed.

### Project Module

Project module is MVP-close.

Project endpoints:

| Endpoint | Status | Notes |
|---|---:|---|
| `POST /api/projects` | Present | Creates project. |
| `GET /api/projects` | Present | Lists active projects. |
| `PUT /api/projects/{id}` | Present | Updates active project with `@Valid`. |
| `DELETE /api/projects/{id}` | Present | Soft delete. |
| `DELETE /api/projects/{id}/hard` | Present | Permanent delete. |
| `POST /api/projects/{id}/restore` | Present | Restores deleted project. |

`ProjectResponse` is simplified to:

- `id`
- `name`
- `description`

The old `ownerId`, `ownerUsername`, and `taskCount` fields are currently commented out. For a clean MVP, they should either be fully removed or mapped later when ownership/task count is needed.

### User Module

User module is not MVP-ready.

Present:

- `User` entity.
- `UserRepository`.
- `UserMapper`.
- `UserService`.
- `UserServiceImpl`.
- User request/response DTOs.

Incomplete:

- `UserController` is empty.
- `updateUser()` returns `null`.
- `getAllUsers()` currently queries deleted users.
- `getAllDeletedUsers()` returns an empty list.
- Password is not mapped or hashed.
- Confirm-password matching is not implemented.

### Auth Module

Auth is still placeholder-level.

Present:

- `Role` enum.
- Auth controller/service files.

Missing:

- Registration.
- Login.
- Password encoder.
- JWT/session logic.
- Security configuration.
- Authorization rules.

## 5. Current Big Issues

### 1. Maven Wrapper Still Fails Before Compilation

I tested Java with your JDK path:

```powershell
$env:JAVA_HOME = "C:\Users\Guven Servis\.jdks\ms-21.0.11-1"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
java -version
```

Java works and reports:

```text
openjdk version "21.0.11" 2026-04-21 LTS
```

But running:

```powershell
$env:JAVA_HOME = "C:\Users\Guven Servis\.jdks\ms-21.0.11-1"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
cmd /c mvnw.cmd test
```

still fails before Maven starts:

```text
Cannot index into a null array.
Cannot start maven from wrapper
```

So the remaining verification blocker is not only Java availability. The Maven wrapper script or its execution environment still needs fixing.

Also, your IntelliJ issue is real: if `JAVA_HOME` disappears every restart, set it permanently in Windows or configure IntelliJ's Project SDK and Maven runner JDK.

### 2. Validation Handler Uses Spring Exception But Still Returns Conflict

The handler now imports Spring's real `MethodArgumentNotValidException`, which is good.

Remaining issue:

- It returns `HttpStatus.CONFLICT`.
- The response body also says `CONFLICT`.
- Validation errors should usually return `400 BAD_REQUEST`.
- `details` is still `null`; field-level validation messages are not extracted yet.

### 3. User/Auth Are Still Incomplete

This is only a blocker if users/login are required for the MVP. For a task/project prototype, they can wait.

### 4. Hard Delete Is Public

Hard delete endpoints exist for tasks and projects. That may be okay for local MVP testing, but once auth exists, these should be protected or removed from normal user access.

### 5. Build/Test Coverage Is Still Unproven

Because Maven wrapper fails before compilation, the project has not been compile-verified in this review.

## 6. Pluses

- Task/project API is now close to an MVP.
- Soft delete and restore are implemented for both tasks and projects.
- Task status and priority changes are implemented.
- DTOs are used instead of returning JPA entities in main responses.
- Project response is now simple and MVP-friendly.
- Environment-based config is much safer than the earlier hardcoded-password setup.
- MySQL dev and PostgreSQL prod profiles are separated.
- Global exception handling exists.
- Validation annotations are present on key request DTOs.
- `@Valid` is present on create and full-update endpoints.

## 7. Minuses And Risks

- Maven wrapper failure blocks reliable verification.
- Validation error response should be cleaned up.
- User service has unfinished methods.
- Auth has no real implementation.
- Hard delete endpoints are exposed without auth.
- No automated coverage beyond a default context-load test.
- No pagination, sorting, or filtering.
- No database migrations.
- `README.md` is still minimal.

## 8. MVP Readiness

For task/project CRUD, the code is close to MVP-ready.

Minimum remaining work:

1. Fix Maven/JDK/wrapper verification.
2. Change validation handler response from `409 CONFLICT` to `400 BAD_REQUEST`.
3. Add validation error details.
4. Manually test all task/project endpoints against MySQL.
5. Decide whether hard delete endpoints stay in MVP.

Users/auth should wait unless they are required for your demo.

## 9. Suggested Next Order

1. Fix Maven wrapper execution.
2. Make `JAVA_HOME` permanent or configure IntelliJ's Project SDK/Maven runner JDK.
3. Run `mvnw test` or `mvnw spring-boot:run`.
4. Fix validation error response to return 400 with field details.
5. Manually test task CRUD.
6. Manually test project CRUD.
7. Update `README.md` with setup and endpoint examples.
8. Decide whether users/auth belong in MVP.

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

The project is now a near-MVP backend for task/project management. The code has improved a lot: the earlier task update, project list, status/priority, config, and response-shape issues are mostly resolved.

The next big step is verification. Get Maven running reliably, then test the task/project API end to end. After that, clean validation responses and decide whether user/auth belongs in this first prototype.
