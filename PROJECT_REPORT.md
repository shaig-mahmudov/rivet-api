# TaskManagement Project Report

Generated: 2026-05-27

## 1. Project Summary

TaskManagement is a Java 21 Spring Boot backend project for task management. It is currently an early-stage REST API with a partially implemented task module and placeholder packages for users, authentication, projects, comments, and common exception handling.

The project already shows the intended architecture: controller, service, repository, entity, DTO, mapper, enum, and common layers. The strongest implemented area is task creation, task listing, task lookup, and soft deletion.

## 2. Technology Stack

- Java 21
- Spring Boot 4.0.6
- Spring Web MVC
- Spring Data JPA
- Jakarta Bean Validation
- Lombok
- Springdoc OpenAPI UI
- MySQL runtime driver
- PostgreSQL runtime driver
- Maven Wrapper
- JUnit/Spring Boot test setup

## 3. Project Structure

```text
TaskManagement/
|-- pom.xml
|-- README.md
|-- mvnw
|-- mvnw.cmd
|-- .mvn/
|   `-- wrapper/
|       `-- maven-wrapper.properties
|-- src/
|   |-- main/
|   |   |-- java/com/engine/taskmanagement/
|   |   |   |-- TaskManagementApplication.java
|   |   |   |-- auth/
|   |   |   |   |-- AuthController.java
|   |   |   |   |-- AuthService.java
|   |   |   |   |-- AuthServiceImpl.java
|   |   |   |   `-- enums/Role.java
|   |   |   |-- common/
|   |   |   |   |-- entity/BaseEntity.java
|   |   |   |   `-- exception/
|   |   |   |-- project/
|   |   |   |   `-- Project.java
|   |   |   |-- task/
|   |   |   |   |-- controller/TaskController.java
|   |   |   |   |-- dto/request/
|   |   |   |   |-- dto/response/
|   |   |   |   |-- entity/Task.java
|   |   |   |   |-- enums/
|   |   |   |   |-- mapper/TaskMapper.java
|   |   |   |   |-- repository/TaskRepository.java
|   |   |   |   `-- service/
|   |   |   `-- user/
|   |   |-- resources/
|   |   |   |-- application.properties
|   |   |   `-- application-dev.properties
|   `-- test/
|       `-- java/com/engine/taskmanagement/
|           `-- TaskManagementApplicationTests.java
```

There are 31 Java files under `src/main/java`, 2 resource config files, and 1 test file.

## 4. What The Project Has

### Application Foundation

- Spring Boot application entry point.
- JPA auditing enabled with `@EnableJpaAuditing`.
- Maven project configuration.
- Maven wrapper files.
- `.gitignore` configured for Maven target files and common IDE folders.

### Task Domain

- `Task` JPA entity with:
  - `title`
  - `description`
  - `priority`
  - `status`
  - `dueDate`
  - inherited `id`, `createdAt`, `updatedAt`, `deletedAt`
- `BaseEntity` mapped superclass with auditing fields and soft delete support.
- Task priority enum:
  - `LOW`
  - `MEDIUM`
  - `HIGH`
  - `URGENT`
- Task status enum:
  - `TODO`
  - `IN_PROGRESS`
  - `DONE`
  - `CANCELLED`
- Task request and response DTOs.
- Task mapper for converting request DTOs to entities and entities to responses.
- Task repository with several finder methods:
  - by title
  - title containing ignore case
  - by status
  - by priority
  - active tasks
  - deleted tasks
  - active task by id
- Task service interface and implementation.
- Task controller with endpoints:
  - `POST /api/tasks`
  - `GET /api/tasks`
  - `GET /api/tasks/deleted`
  - `GET /api/tasks/{id}`
  - `DELETE /api/tasks/{id}`

### Validation

- Basic validation exists on create and update task request DTOs:
  - title is required
  - title max length is 100
  - description max length is 500

### API Documentation Dependency

- Springdoc OpenAPI UI dependency is present, so Swagger/OpenAPI can be enabled by the framework.

### Testing Foundation

- A basic Spring Boot context-load test exists.

## 5. What The Project Does Not Have Yet

### Missing Feature Implementations

- No real authentication implementation.
- No login endpoint.
- No registration endpoint.
- No JWT/session/security configuration.
- No password hashing.
- No authorization rules.
- No implemented user entity, user repository, user service, or user controller.
- No implemented project entity beyond an empty class.
- No comment feature, although a `comment` package directory exists.
- No task update endpoint in the controller.
- `TaskService.updateTask(...)` is declared but currently returns `null`.
- Empty DTOs for changing task status and priority.
- No restore endpoint for soft-deleted tasks.
- No permanent delete endpoint.
- No filtering endpoints for status, priority, due date, title search, or deleted state.
- No pagination or sorting.
- No frontend UI.

### Missing Error Handling

- `GlobalExceptionHandler` is empty.
- Custom exception classes are empty and do not extend `RuntimeException`.
- Task not found currently throws plain `RuntimeException("TaskNotFound")`.
- Validation errors will not be converted into a clean custom API response.

### Missing Data Relationships

- Tasks are not connected to users.
- Tasks are not connected to projects.
- Tasks are not connected to comments.
- No ownership model exists, so all tasks are global.

### Missing Database/DevOps Pieces

- No migration tool such as Flyway or Liquibase.
- No Docker Compose file for local database setup.
- No environment-variable based configuration.
- Database credentials are stored directly in `application.properties`.
- Both MySQL and PostgreSQL drivers are present, but the app is configured only for MySQL.
- No production profile.
- `application-dev.properties` exists but is fully commented out.

### Missing Documentation

- `README.md` only contains the project title.
- No setup instructions.
- No database setup instructions.
- No API examples.
- No endpoint documentation in the repository.
- No architecture notes.

### Missing Tests

- No controller tests.
- No service tests.
- No repository tests.
- No validation tests.
- No mapper tests.
- No error handling tests.
- No integration tests for task CRUD.

## 6. Pluses

- Clear package direction: `auth`, `user`, `task`, `project`, `common`.
- Task module already follows a layered backend style.
- DTOs are separated from entities.
- Mapper class exists, which keeps conversion logic out of controllers.
- Soft delete concept is already present through `deletedAt`.
- JPA auditing is enabled.
- Enums make task status and priority explicit.
- Validation dependency and annotations are already introduced.
- OpenAPI dependency is included for future API documentation.
- The codebase is small and easy to refactor right now.

## 7. Minuses And Risks

- The application is only partially functional.
- Several classes are placeholders, which can give a false impression that features exist.
- `TaskService.updateTask(...)` returns `null`, so update behavior is unfinished.
- `TaskController` does not expose update, status change, or priority change endpoints.
- `TaskMapper.toEntity(...)` sets priority and status directly from the request. If the client omits them, the entity defaults can be overwritten with `null`, which conflicts with non-null database columns.
- `TaskMapper.toResponse(...)` does not set `deletedAt`, even though `TaskResponse` has the field.
- `TaskService.createTask(...)` saves `savedTask` but maps `task`; mapping `savedTask` would be clearer.
- Error handling is not production-ready.
- Empty exception classes do not currently help the API.
- Database password is committed in plain text.
- The active `dev` profile points to an empty/commented dev properties file, while real database settings remain in the base properties file.
- No database migrations means schema changes depend on Hibernate `ddl-auto=update`.
- The Maven wrapper failed to start on this machine, and no global Maven installation is available.
- Only one default context-load test exists.

## 8. Current Endpoint Status

| Endpoint | Status | Notes |
|---|---:|---|
| `POST /api/tasks` | Partial | Creates task, but missing default handling for omitted priority/status. |
| `GET /api/tasks` | Present | Returns non-deleted tasks. |
| `GET /api/tasks/deleted` | Present | Returns soft-deleted tasks. |
| `GET /api/tasks/{id}` | Partial | Works for active tasks, but throws plain runtime exception when missing. |
| `DELETE /api/tasks/{id}` | Partial | Soft deletes task, but can delete already deleted tasks and does not use custom exception. |
| `PUT/PATCH /api/tasks/{id}` | Missing | Service method exists but is unfinished and controller endpoint is missing. |
| Auth endpoints | Missing | Auth classes are placeholders. |
| User endpoints | Missing | User classes are placeholders. |
| Project endpoints | Missing | Project class is placeholder only. |
| Comment endpoints | Missing | Package exists but no files were found. |

## 9. Verification Notes

I attempted to run the test suite with:

```bash
./mvnw.cmd test
```

Result: the Maven wrapper failed before compilation with:

```text
Cannot index into a null array.
Cannot start maven from wrapper
```

I also checked for global Maven:

```bash
mvn -v
```

Result: Maven is not installed or not available on `PATH`.

Because of that, the project was not compiled or tested during this review.

## 10. Recommended Next Fixes

1. Fix task creation defaults.
   - Keep `TaskPriority.MEDIUM` and `TaskStatus.TODO` when request values are missing.

2. Finish task update.
   - Implement `TaskService.updateTask(...)`.
   - Add `PUT /api/tasks/{id}` or `PATCH /api/tasks/{id}`.

3. Implement proper exceptions.
   - Make custom exceptions extend `RuntimeException`.
   - Add `@RestControllerAdvice` to `GlobalExceptionHandler`.
   - Return consistent JSON error responses.

4. Move secrets out of source code.
   - Use environment variables for datasource URL, username, and password.
   - Keep local values in an ignored local profile file or documented environment setup.

5. Improve README.
   - Add requirements, database setup, run commands, test commands, and API examples.

6. Repair Maven/test execution.
   - Investigate Maven wrapper issue.
   - Ensure the project can run `mvnw test` on Windows.

## 11. Next Feature Ideas

### Small, High-Value Features

- Task update endpoint.
- Task restore endpoint.
- Task permanent delete endpoint.
- Change task status endpoint.
- Change task priority endpoint.
- Search tasks by title.
- Filter tasks by status and priority.
- Filter overdue tasks.
- Sort tasks by due date, priority, or creation date.
- Add pagination to task lists.

### Medium Features

- User registration and login.
- JWT authentication.
- Assign tasks to users.
- Project entity and project CRUD.
- Add tasks to projects.
- Comments on tasks.
- Task activity history.
- Due date reminders.
- File attachments.

### Larger Features

- Role-based access control for `ADMIN` and `USER`.
- Team/workspace model.
- Kanban board API.
- Notification system.
- Audit log.
- Reporting dashboard.
- Frontend web app.
- Docker Compose setup for app plus database.
- CI pipeline that runs tests on every push.

## 12. Suggested Development Order

1. Make the current task API reliable.
2. Add clean exception handling.
3. Add tests around task CRUD.
4. Add database/environment setup documentation.
5. Implement user and auth.
6. Connect tasks to users.
7. Add projects.
8. Add comments and activity history.
9. Add filtering, pagination, and reporting.

## 13. Overall Assessment

The project is a good early backend skeleton. It has a sensible package layout and the beginning of a clean task-management domain, but it is not yet a complete task management system. The current strongest piece is the task module, especially entity/repository/controller layering and soft delete. The biggest gaps are unfinished update logic, placeholder auth/user/project code, empty exception handling, weak documentation, no useful tests, and plain-text database credentials.

The best next step is to stabilize the existing task module before adding larger features.
