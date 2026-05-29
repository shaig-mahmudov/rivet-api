# TaskManagement Next Feature TODO

Generated: 2026-05-29

## Current State Snapshot

The project has grown since the previous report. It now has:

- Task entity, DTOs, mapper, repository, service, and controller.
- Project entity, DTOs, mapper, repository, service, and controller.
- User entity, DTOs, repository, and service interface.
- Global exception handler and custom runtime exceptions.
- Relationships started between `Task`, `Project`, and `User`.

The project is still not ready for bigger features yet because several core flows are unfinished or partially connected:

- Maven wrapper still fails before tests can run.
- Task update returns `null`.
- Task controller has no update endpoint.
- Project list currently returns deleted projects instead of active projects.
- User service implementation and controller are empty.
- Auth service and controller are empty.
- Passwords are exposed in entity/response DTOs and database config.
- Task creation can overwrite default priority/status with `null`.
- Project and user responses expose entity objects directly, which can cause recursion, lazy-loading issues, and password leaks.

## Do First: Foundation Fixes

These should happen before adding new business features.

### 1. Fix Build And Test Execution

- [ ] Fix the Maven wrapper issue on Windows.
- [ ] Make `mvnw test` run successfully.
- [ ] Confirm the project compiles after the recent package moves.
- [ ] Keep one repeatable command for verification, ideally:

```bash
./mvnw test
```

Why first: if the app cannot compile/test reliably, every new feature becomes guesswork.

### 2. Secure Basic Configuration

- [ ] Move datasource password out of `application.properties`.
- [ ] Use environment variables for database URL, username, and password.
- [ ] Keep only safe defaults in committed config.
- [ ] Decide whether this project uses MySQL or PostgreSQL, then remove the unused driver.
- [ ] Add real `application-dev.properties` values or remove the empty dev profile file.

Why next: credentials and unclear database setup are easy to fix now and painful later.

### 3. Finish Error Handling

- [ ] Add validation error handling for `MethodArgumentNotValidException`.
- [ ] Add fallback handling for unexpected exceptions.
- [ ] Return consistent `ErrorResponse` objects everywhere.
- [ ] Add useful `details` for field validation errors.

Why next: clean API errors make every endpoint easier to test and debug.

## Next: Stabilize The Task Module

This is the most complete module, so finish it before expanding heavily.

### 4. Fix Task Create Behavior

- [ ] In `TaskMapper.toEntity`, do not set `priority` to `null` when request priority is missing.
- [ ] In `TaskMapper.toEntity`, do not set `status` to `null` when request status is missing.
- [ ] Return `taskMapper.toResponse(savedTask)` after saving.
- [ ] Add `deletedAt` to `TaskResponse` mapping if deleted tasks are returned.

Suggested behavior:

- Missing priority defaults to `MEDIUM`.
- Missing status defaults to `TODO`.

### 5. Implement Task Update

- [ ] Implement `TaskService.updateTask(Long id, UpdateTaskRequest request)`.
- [ ] Add `PUT /api/tasks/{id}` endpoint.
- [ ] Decide whether update is full update or partial update.
- [ ] If partial update, make fields optional and only update provided values.
- [ ] Return the updated task response.

### 6. Add Task Status And Priority Change Endpoints

- [ ] Fill `ChangeTaskStatusRequest`.
- [ ] Fill `ChangeTaskPriorityRequest`.
- [ ] Add `PATCH /api/tasks/{id}/status`.
- [ ] Add `PATCH /api/tasks/{id}/priority`.
- [ ] Validate status and priority transitions if needed.

### 7. Improve Task Delete Flow

- [ ] Use `findByIdAndDeletedAtIsNull` before deleting.
- [ ] Prevent deleting already deleted tasks twice.
- [ ] Add restore endpoint: `PATCH /api/tasks/{id}/restore`.
- [ ] Optional later: add permanent delete endpoint for admins.

### 8. Add Task Filtering

- [ ] Filter by status.
- [ ] Filter by priority.
- [ ] Search by title.
- [ ] Filter by project.
- [ ] Filter by assignee.
- [ ] Filter overdue tasks.
- [ ] Add pagination and sorting.

## Then: Stabilize The Project Module

### 9. Fix Project List Bug

- [ ] Change `getAllProjects()` to use `findAllByDeletedAtIsNull()`.
- [ ] Return deleted projects only from a separate deleted-project endpoint.

This is currently one of the most important behavior bugs.

### 10. Complete Project CRUD

- [ ] Add get project by id.
- [ ] Add update project.
- [ ] Add soft delete project.
- [ ] Add restore project.
- [ ] Add validation for project name length.
- [ ] Prevent duplicate project names per owner if ownership is introduced.

### 11. Clean Project DTOs

- [ ] Do not expose `User` entity directly in `ProjectResponse`.
- [ ] Do not expose `Task` entity directly in `ProjectResponse`.
- [ ] Use small nested DTOs or id fields instead.
- [ ] Include `ownerId`, `ownerUsername`, `taskCount`, or simple task summaries.

Why: returning entities from API responses often creates recursion, lazy-loading errors, and accidental sensitive data leaks.

## Then: Build The User Module

### 12. Fix User Model

- [ ] Remove `confirmPassword` from the `User` entity.
- [ ] Keep `confirmPassword` only in request DTOs.
- [ ] Make username/email uniqueness clear.
- [ ] Add default role, probably `USER`.
- [ ] Add assigned tasks relationship if tasks can be assigned to users.

### 13. Fix User DTOs

- [ ] Add Lombok getters/setters or records for user request/response DTOs.
- [ ] Remove password from `UserResponse`.
- [ ] Do not expose projects as entity objects in `UserResponse`.
- [ ] Add validation:
  - [ ] valid email format
  - [ ] password min length
  - [ ] password and confirm password match

### 14. Implement User Service And Controller

- [ ] Make `UserServiceImpl` implement `UserService`.
- [ ] Add `@Service`.
- [ ] Implement create user.
- [ ] Implement update user.
- [ ] Add get user by id.
- [ ] Add get all users.
- [ ] Add soft delete user if needed.
- [ ] Add `UserController` endpoints.

## Then: Add Auth And Security

Do this after user creation is clean.

### 15. Add Authentication

- [ ] Add Spring Security dependency.
- [ ] Add password encoder.
- [ ] Hash passwords before saving.
- [ ] Add register endpoint.
- [ ] Add login endpoint.
- [ ] Return JWT or session token.
- [ ] Add request/response DTOs for auth.

### 16. Add Authorization

- [ ] Protect task/project/user endpoints.
- [ ] Only allow users to see their own tasks/projects unless admin.
- [ ] Add role checks for admin-only operations.
- [ ] Ensure deleted data is not visible to normal users.

## After That: Quality And Developer Experience

### 17. Add Tests In This Order

- [ ] Mapper tests for task/project/user.
- [ ] Service tests for task CRUD.
- [ ] Controller tests for task endpoints.
- [ ] Project service/controller tests.
- [ ] User service/controller tests.
- [ ] Auth tests.
- [ ] Exception handler tests.

Start with task tests because that module is closest to finished.

### 18. Improve Documentation

- [ ] Expand `README.md`.
- [ ] Add setup requirements.
- [ ] Add database setup.
- [ ] Add environment variable examples.
- [ ] Add API endpoint examples.
- [ ] Add sample request/response JSON.
- [ ] Add troubleshooting section for Maven wrapper and database connection.

### 19. Add Database Migrations

- [ ] Add Flyway or Liquibase.
- [ ] Replace reliance on `spring.jpa.hibernate.ddl-auto=update`.
- [ ] Create initial migration for users, projects, and tasks.

### 20. Add Local Dev Environment

- [ ] Add Docker Compose for database.
- [ ] Optional: add app container later.
- [ ] Add `.env.example`.

## Feature Ideas After The Core Is Stable

These are good next features, but not before the core modules work.

- [ ] Comments on tasks.
- [ ] Task activity history.
- [ ] Task labels/tags.
- [ ] Attachments.
- [ ] Due date reminders.
- [ ] Kanban board endpoints.
- [ ] Project members.
- [ ] Team/workspace model.
- [ ] Dashboard statistics.
- [ ] Export tasks to CSV.
- [ ] Frontend web app.
- [ ] Admin panel.

## Recommended Order Summary

1. Fix Maven/test execution.
2. Secure configuration and remove committed secrets.
3. Finish exception handling.
4. Finish task create/update/delete/status/priority flows.
5. Fix project list and complete project CRUD.
6. Clean all response DTOs so entities and passwords are not exposed.
7. Implement user service and controller.
8. Add authentication and password hashing.
9. Add authorization.
10. Add tests.
11. Add migrations and Docker Compose.
12. Add comments, dashboard, frontend, and other larger features.

## What Not To Do Yet

- Do not build a frontend yet.
- Do not add comments before tasks/projects/users are stable.
- Do not add JWT before password storage and user creation are correct.
- Do not add many endpoints before `mvnw test` works.
- Do not return JPA entities directly from API responses.

## Best Immediate Next Task

Start with this exact sequence:

1. Fix Maven wrapper/test execution.
2. Fix `TaskMapper.toEntity` so missing status/priority keep defaults.
3. Implement task update service and endpoint.
4. Fix project `getAllProjects()` to return active projects.
5. Remove password from all response DTOs.

That gives you a stable base quickly and clears the biggest current risks.
