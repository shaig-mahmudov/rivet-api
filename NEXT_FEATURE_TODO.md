# TaskManagement MVP TODO

Generated: 2026-05-30

## Current Snapshot

The project is now closer to an MVP than before.

What is already present:

- Spring Boot backend with Java 21 and Maven.
- `.env` support through `springboot4-dotenv`.
- Dev profile for MySQL and prod profile for PostgreSQL.
- Base entity with `createdAt`, `updatedAt`, `deletedAt`, soft delete, and restore support.
- Task module with create, list, get by id, update, partial update service, soft delete, hard delete service, and restore service.
- Project module with create, list, update service, soft delete service, hard delete service, and restore service.
- User entity, repository, mapper, service interface, and service implementation.
- Custom exception classes and global exception handler.
- Safer `ProjectResponse` shape that no longer returns full `User` and `Task` entities.
- `UserResponse` no longer exposes password.

Important note: I did not run the build/test command during this update. The TODO below is based on static code review.

## MVP Goal

Build a working prototype where a user can:

- Create projects.
- List projects.
- Update projects.
- Create tasks.
- List tasks.
- View one task.
- Update tasks.
- Soft delete tasks.
- Restore tasks if needed.

Authentication can wait unless you specifically want login/register in the first demo.

## Phase 1: Fix MVP Blockers First

### 1. Verify The App Builds And Starts

- [ ] Run the project with the current `.env` and MySQL.
- [ ] Run tests or at least compile the app.
- [ ] Confirm the Maven wrapper works on your machine.
- [ ] Confirm MySQL database `task_management` exists.
- [ ] Confirm Swagger/OpenAPI loads if available.

Suggested commands:

```bash
./mvnw test
./mvnw spring-boot:run
```

Why first: if the app does not compile or start, feature work becomes guesswork.

### 2. Fix Task Create Defaults

- [ ] In `TaskMapper.toEntity`, keep default `TaskPriority.MEDIUM` when request priority is missing.
- [ ] In `TaskMapper.toEntity`, keep default `TaskStatus.TODO` when request status is missing.
- [ ] Remove duplicate `@Size` annotation on task description.
- [ ] Remove unused imports from `CreateTaskRequest` and `TaskMapper`.

Current risk: creating a task with only `title` can set `priority` and `status` to `null`, which conflicts with non-null entity columns.

### 3. Expose Existing Task Service Methods In The Controller

Already exists in service but not fully exposed by controller:

- [ ] Add `PATCH /api/tasks/{id}` for `partialUpdateTask`.
- [ ] Add `PATCH /api/tasks/{id}/restore` for `restoreTask`.
- [ ] Add `DELETE /api/tasks/{id}/hard` only if you really need hard delete in MVP.

Optional for MVP:

- [ ] Add `PATCH /api/tasks/{id}/status`.
- [ ] Add `PATCH /api/tasks/{id}/priority`.

### 4. Fix Task Restore Null Project Bug

- [ ] In `restoreTask`, check whether `task.getProject()` is `null` before calling `task.getProject().getDeletedAt()`.

Current risk: restoring a task without a project can throw `NullPointerException`.

### 5. Fix Project List Query

- [ ] Change project list from `findAllByDeletedAtIsNotNull()` to `findAllByDeletedAtIsNull()`.

Current risk: `GET /api/projects` returns deleted projects instead of active projects, so newly created projects may not appear.

### 6. Expose Existing Project Service Methods In The Controller

Already exists in service but not exposed by controller:

- [ ] Add `DELETE /api/projects/{id}` for soft delete.
- [ ] Add `PATCH /api/projects/{id}/restore`.
- [ ] Add `DELETE /api/projects/{id}/hard` only if needed for MVP.

### 7. Map Project Response Fields Correctly

`ProjectResponse` has:

- `ownerId`
- `ownerUsername`
- `taskCount`

But `ProjectMapper.toResponse` currently maps only:

- `id`
- `name`
- `description`

TODO:

- [ ] Set `ownerId` when owner exists.
- [ ] Set `ownerUsername` when owner exists.
- [ ] Set `taskCount` from project tasks size.
- [ ] Keep full entities out of the response.

This is not a startup blocker, but it makes the API response more useful.

## Phase 2: Minimum User Module

Only do this if MVP needs users. If not, skip to Phase 3.

### 8. Decide Whether Users Are Needed For MVP

- [ ] If MVP is only task/project CRUD, postpone users.
- [ ] If MVP needs users, finish only the minimum user flow.

### 9. Fix User Service Logic

- [ ] Return `userMapper.toResponse(savedUser)` after saving.
- [ ] Implement `updateUser`.
- [ ] Fix `getAllUsers()` to use active users, not deleted users.
- [ ] Implement `getAllDeletedUsers()`.
- [ ] Add `findAllByDeletedAtIsNull()` to `UserRepository`.

### 10. Add User Controller Only If Needed

- [ ] Add create user endpoint.
- [ ] Add list users endpoint.
- [ ] Add update user endpoint.
- [ ] Add soft delete user endpoint if needed.

### 11. Fix User Password Handling

- [ ] In `UserMapper.toEntity`, map password only after deciding password strategy.
- [ ] Do not store `confirmPassword` in entity.
- [ ] Check password and confirm password match.
- [ ] Add password hashing before any real login/register feature.

For a local MVP without auth, you can postpone password hashing, but do not present it as production-ready.

## Phase 3: Error Handling And Validation

### 12. Fix Validation Exception Handling

- [ ] Replace the custom `common.exception.MethodArgumentNotValidException` handler with Spring's real validation exception:

```java
org.springframework.web.bind.MethodArgumentNotValidException
```

- [ ] Return HTTP 400 for validation errors.
- [ ] Fill `ErrorResponse.details` with field error messages.
- [ ] Fix mismatch where validation response body says `CONFLICT` but status is `BAD_REQUEST`.

### 13. Add Basic Request Validation

- [ ] Add name length validation for project create/update.
- [ ] Add description length validation for project create/update.
- [ ] Add validation to `PartialUpdateTaskRequest`.
- [ ] Add `@Valid` to task update endpoint request body.
- [ ] Add `@Valid` to project update endpoint request body.

## Phase 4: MVP Manual Testing Checklist

Use Postman, Swagger, or curl.

### Project Flow

- [ ] Create project.
- [ ] List projects and confirm the new project appears.
- [ ] Update project.
- [ ] Soft delete project.
- [ ] Restore project.

### Task Flow

- [ ] Create task with only title.
- [ ] Create task with title, priority, status, and due date.
- [ ] List tasks.
- [ ] Get task by id.
- [ ] Full update task.
- [ ] Partial update task.
- [ ] Soft delete task.
- [ ] List deleted tasks.
- [ ] Restore task.

### Error Flow

- [ ] Get missing task id returns 404.
- [ ] Get missing project id returns 404.
- [ ] Create task without title returns 400.
- [ ] Create project without name returns 400.

## Phase 5: After MVP Works

Do these after the prototype is already working.

### 14. Add Task Filtering

- [ ] Filter by status.
- [ ] Filter by priority.
- [ ] Search by title.
- [ ] Filter overdue tasks.
- [ ] Add pagination.
- [ ] Add sorting.

### 15. Connect Tasks To Projects

- [ ] Add `projectId` to create task request.
- [ ] Add `projectId` to update task request if needed.
- [ ] Validate project exists and is not deleted.
- [ ] Return `projectId` or project summary in `TaskResponse`.

### 16. Connect Tasks To Users Later

- [ ] Add assignee support only after user module is usable.
- [ ] Add `assigneeId` to task request.
- [ ] Return assignee summary in task response.

### 17. Add Auth Later

- [ ] Add Spring Security.
- [ ] Add register endpoint.
- [ ] Add login endpoint.
- [ ] Hash passwords.
- [ ] Add JWT or session strategy.
- [ ] Protect endpoints.

Auth is not required for the first task/project CRUD MVP unless your demo specifically needs login.

### 18. Add Database Migrations

- [ ] Add Flyway or Liquibase.
- [ ] Stop relying on `ddl-auto=update` for long-term development.
- [ ] Keep MySQL migrations for dev and PostgreSQL compatibility in mind for prod.

### 19. Improve Docs

- [ ] Update `README.md`.
- [ ] Document `.env` variables.
- [ ] Document MySQL dev setup.
- [ ] Document PostgreSQL prod setup.
- [ ] Add endpoint examples.

## Completed Or Mostly Done

- [x] Move datasource credentials out of `application.properties`.
- [x] Add `.env` support.
- [x] Add dev MySQL profile.
- [x] Add prod PostgreSQL profile.
- [x] Keep MySQL and PostgreSQL drivers because both are planned.
- [x] Add safer simple `ProjectResponse`.
- [x] Remove password from `UserResponse`.
- [x] Add `@Service` to `UserServiceImpl`.
- [x] Implement task full update service.
- [x] Implement task soft delete using active-task lookup.
- [x] Add task restore service.
- [x] Add project update service.
- [x] Add project soft delete service.
- [x] Add project restore service.

## Current Highest Priority Order

1. Run/compile the app and confirm it starts.
2. Fix task default priority/status.
3. Fix project list query.
4. Add controller endpoints for existing restore/delete/partial-update services.
5. Fix task restore null project bug.
6. Fix validation exception handling.
7. Manually test project CRUD.
8. Manually test task CRUD.
9. Decide whether users/auth are needed for MVP.

## What Not To Do Yet

- Do not build a frontend yet.
- Do not add JWT before the basic task/project API works.
- Do not add comments, dashboard, notifications, or teams before MVP CRUD is stable.
- Do not return JPA entities directly from API responses.
- Do not spend time on advanced filters before create/list/update/delete works reliably.
