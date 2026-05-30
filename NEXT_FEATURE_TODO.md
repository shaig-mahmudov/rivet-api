# TaskManagement MVP TODO

Generated: 2026-05-30

## Current Snapshot

The backend is now close to a task/project CRUD MVP.

Already present:

- Spring Boot backend with Java 21 and Maven.
- `.env` support through `springboot4-dotenv`.
- Dev MySQL profile and prod PostgreSQL profile.
- Base entity with auditing, soft delete, and restore support.
- Task create, list, get by id, full update, partial update, soft delete, hard delete, deleted list, and restore endpoints.
- Project create, list, update, soft delete, hard delete, and restore endpoints.
- Project list now returns active projects.
- Task create now preserves default priority/status when request values are missing.
- Simple `ProjectResponse` that avoids returning full `User` and `Task` entities.
- `UserResponse` does not expose password.
- User entity, repository, mapper, service interface, and service implementation skeleton.
- Custom exceptions and a global exception handler.

Important note: this TODO is based on static code review. Build/test was not run in this pass.

## MVP Goal

First prototype target:

- Create, list, update, delete, and restore projects.
- Create, list, view, update, partially update, delete, and restore tasks.
- Return clean enough JSON responses for manual testing in Postman or Swagger.

Authentication and user ownership can wait unless your first demo specifically needs login.

## Phase 1: Must Check Before More Coding

### 1. Compile And Run The App

- [ ] Run `./mvnw test`.
- [ ] Run `./mvnw spring-boot:run`.
- [ ] Confirm the Maven wrapper works.
- [ ] Confirm MySQL is running.
- [ ] Confirm database `task_management` exists.
- [ ] Confirm `.env` values are loaded.
- [ ] Confirm Swagger/OpenAPI page works if enabled.

Why first: the code has moved a lot. Before adding features, prove the current app starts.

## Phase 2: Remaining MVP Bugs

### 2. Fix Task Restore For Standalone Tasks

Current code rejects restore when `task.getProject() == null`.

- [ ] Allow restore when task has no project.
- [ ] Reject restore only when task has a project and that project is deleted.

Expected logic:

```java
if (task.getProject() != null && task.getProject().getDeletedAt() != null) {
    throw new BadRequestException("Cannot restore task because its project is deleted");
}
```

Why this matters: your MVP currently allows tasks without project, so restore should also support standalone tasks.

### 3. Add `@Valid` To Update Endpoints

- [ ] Add `@Valid` to `TaskController.updateTask`.
- [ ] Add `@Valid` to `TaskController.partialUpdateTask` if validation annotations are added there.
- [ ] Add `@Valid` to `ProjectController.updateProject`.

Why this matters: create endpoints validate input, but update endpoints currently bypass request validation.

### 4. Fix Validation Exception Handling

Current handler catches your custom `common.exception.MethodArgumentNotValidException`, not Spring's real validation exception.

- [ ] Replace or supplement it with `org.springframework.web.bind.MethodArgumentNotValidException`.
- [ ] Return HTTP 400 for validation errors.
- [ ] Fill `ErrorResponse.details` with field-level validation messages.
- [ ] Fix the response body mismatch where validation body says `CONFLICT` but HTTP status is `BAD_REQUEST`.

### 5. Clean Minor Endpoint Details

- [ ] Add missing slash in `@PostMapping("{id}/restore")` in `TaskController`; use `@PostMapping("/{id}/restore")`.
- [ ] Make `ProjectController.restoreProject` public instead of private.
- [ ] Decide whether hard delete endpoints should stay in MVP or be removed until admin/auth exists.

These are small, but they make the API cleaner and less surprising.

## Phase 3: Response And Mapper Cleanup

### 6. Map Project Response Extra Fields Or Remove Them

`ProjectResponse` has these fields:

- `ownerId`
- `ownerUsername`
- `taskCount`

But `ProjectMapper.toResponse` currently maps only:

- `id`
- `name`
- `description`

Choose one:

- [ ] Map `ownerId`, `ownerUsername`, and `taskCount`.
- [ ] Or remove those fields until project ownership/tasks are exposed in the response.

For MVP, either choice is fine. Avoid unused response fields if you want the API to look tidy.

### 7. Add Deleted Timestamp To Responses If Needed

- [ ] Map `deletedAt` in `TaskResponse` if deleted-task list should show when deletion happened.
- [ ] Add `deletedAt` to `ProjectResponse` only if you create a deleted-project list endpoint.

Not required for first demo, but useful for soft delete testing.

## Phase 4: Manual MVP Test Checklist

Use Postman, Swagger, or curl.

### Project Flow

- [ ] `POST /api/projects` creates a project.
- [ ] `GET /api/projects` returns the created project.
- [ ] `PUT /api/projects/{id}` updates the project.
- [ ] `DELETE /api/projects/{id}` soft deletes the project.
- [ ] `POST /api/projects/{id}/restore` restores the project.
- [ ] Optional: `DELETE /api/projects/{id}/hard` permanently deletes the project.

### Task Flow

- [ ] `POST /api/tasks` with only title works and defaults status/priority.
- [ ] `POST /api/tasks` with all fields works.
- [ ] `GET /api/tasks` returns active tasks.
- [ ] `GET /api/tasks/{id}` returns one active task.
- [ ] `PUT /api/tasks/{id}` full updates a task.
- [ ] `PATCH /api/tasks/{id}` partially updates a task.
- [ ] `DELETE /api/tasks/{id}` soft deletes a task.
- [ ] `GET /api/tasks/deleted` returns deleted tasks.
- [ ] `POST /api/tasks/{id}/restore` restores a deleted task.
- [ ] Optional: `DELETE /api/tasks/{id}/hard` permanently deletes a task.

### Error Flow

- [ ] Missing task id returns 404.
- [ ] Missing project id returns 404.
- [ ] Creating task without title returns 400.
- [ ] Creating project without name returns 400.
- [ ] Updating task with invalid data returns 400 after validation fix.
- [ ] Updating project with invalid data returns 400 after validation fix.

## Phase 5: User Module Only If Needed

For a task/project MVP, users can wait.

If you decide users are required:

- [ ] Add `UserController` endpoints.
- [ ] Return `userMapper.toResponse(savedUser)` after create.
- [ ] Implement `updateUser`.
- [ ] Add `findAllByDeletedAtIsNull()` to `UserRepository`.
- [ ] Fix `getAllUsers()` to return active users, not deleted users.
- [ ] Implement `getAllDeletedUsers()`.
- [ ] Decide password strategy before exposing user creation.
- [ ] Check password and confirm password match.
- [ ] Hash passwords before any login/register feature.

## Phase 6: After MVP Works

Do these later.

### Task And Project Features

- [ ] Add task filtering by status.
- [ ] Add task filtering by priority.
- [ ] Add title search.
- [ ] Add overdue task filter.
- [ ] Add pagination and sorting.
- [ ] Add `projectId` to task create request.
- [ ] Add project validation when assigning a task to project.
- [ ] Return project summary or `projectId` in `TaskResponse`.

### Auth And Ownership

- [ ] Add Spring Security.
- [ ] Add register endpoint.
- [ ] Add login endpoint.
- [ ] Hash passwords.
- [ ] Add JWT or session strategy.
- [ ] Assign projects to owners.
- [ ] Assign tasks to users.
- [ ] Protect user/project/task endpoints.

### Database And Docs

- [ ] Add Flyway or Liquibase.
- [ ] Stop relying on `ddl-auto=update` long term.
- [ ] Update `README.md`.
- [ ] Document `.env` variables.
- [ ] Document MySQL dev setup.
- [ ] Document PostgreSQL prod setup.
- [ ] Add endpoint examples.

## Completed Since Earlier TODOs

- [x] Move datasource credentials out of `application.properties`.
- [x] Add `.env` support.
- [x] Add dev MySQL profile.
- [x] Add prod PostgreSQL profile.
- [x] Keep MySQL and PostgreSQL drivers.
- [x] Add simple `ProjectResponse`.
- [x] Remove password from `UserResponse`.
- [x] Add `@Service` to `UserServiceImpl`.
- [x] Implement task full update service and endpoint.
- [x] Implement task partial update service and endpoint.
- [x] Implement task soft delete endpoint.
- [x] Implement task hard delete endpoint.
- [x] Implement task restore endpoint.
- [x] Preserve task default priority/status during create.
- [x] Remove duplicate task description `@Size`.
- [x] Fix project list to return active projects.
- [x] Implement project update service and endpoint.
- [x] Implement project soft delete endpoint.
- [x] Implement project hard delete endpoint.
- [x] Implement project restore endpoint.

## Current Highest Priority Order

1. Compile/run the app.
2. Fix standalone task restore.
3. Fix validation exception handling.
4. Add `@Valid` to update endpoints.
5. Clean restore endpoint mappings/access modifiers.
6. Decide whether hard delete endpoints stay in MVP.
7. Manually test project flow.
8. Manually test task flow.
9. Postpone users/auth unless the MVP demo needs them.

## What Not To Do Yet

- Do not build a frontend yet.
- Do not add JWT before the basic task/project API is verified.
- Do not add comments, dashboard, notifications, or teams before MVP CRUD is stable.
- Do not return JPA entities directly from API responses.
- Do not spend time on advanced filters before manual CRUD testing passes.
