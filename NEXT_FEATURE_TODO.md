# TaskManagement MVP TODO

Generated: 2026-05-30

## Current Snapshot

The backend is close to a task/project CRUD MVP.

Already present:

- Spring Boot backend with Java 21 and Maven.
- `.env` support through `springboot4-dotenv`.
- Dev MySQL profile and prod PostgreSQL profile.
- Base entity with auditing, soft delete, and restore support.
- Task create, list, get by id, full update, partial update, soft delete, hard delete, deleted list, restore, status change, and priority change endpoints.
- Project create, list, update, soft delete, hard delete, and restore endpoints.
- Project list returns active projects.
- Task create preserves default status/priority when request values are missing.
- Simple `ProjectResponse` with `id`, `name`, and `description`.
- `UserResponse` does not expose password.
- User entity, repository, mapper, service interface, and service implementation skeleton.
- Global exception handler with Spring's real `MethodArgumentNotValidException` imported.

Important note: Java works when `JAVA_HOME` is set, but Maven wrapper still fails before compilation.

## Phase 1: Environment And Build

### 1. Fix Maven Wrapper Execution

- [ ] Fix the `mvnw.cmd` failure:

```text
Cannot index into a null array.
Cannot start maven from wrapper
```

- [ ] Run `./mvnw test`.
- [ ] Run `./mvnw spring-boot:run`.
- [ ] Confirm MySQL is running.
- [ ] Confirm database `task_management` exists.
- [ ] Confirm `.env` values are loaded.
- [ ] Confirm Swagger/OpenAPI page works if enabled.

### 2. Make Java Configuration Persistent

Current working JDK path:

```powershell
C:\Users\Guven Servis\.jdks\ms-21.0.11-1
```

Temporary command:

```powershell
$env:JAVA_HOME = "C:\Users\Guven Servis\.jdks\ms-21.0.11-1"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
```

TODO:

- [ ] Set `JAVA_HOME` permanently in Windows environment variables.
- [ ] Add `%JAVA_HOME%\bin` to permanent `Path`.
- [ ] In IntelliJ, set Project SDK to Java 21.
- [ ] In IntelliJ, set Maven runner/importer JDK to the same Java 21 SDK.
- [ ] Restart IntelliJ and verify `java -version` and Maven still work.

## Phase 2: Remaining MVP Code Cleanup

### 3. Fix Validation Error Response

Good news: the handler now imports Spring's real:

```java
org.springframework.web.bind.MethodArgumentNotValidException
```

Remaining TODO:

- [ ] Return `HttpStatus.BAD_REQUEST`, not `HttpStatus.CONFLICT`.
- [ ] Set response status field to `400`.
- [ ] Set response error field to `Bad Request`.
- [ ] Extract field-level validation messages into `ErrorResponse.details`.
- [ ] Use a clear message like `Validation failed`.

### 4. Decide About Hard Delete In MVP

Current public endpoints:

- `DELETE /api/tasks/{id}/hard`
- `DELETE /api/projects/{id}/hard`

TODO:

- [ ] Keep them if this is only a local prototype.
- [ ] Remove or protect them before a shared demo.
- [ ] Later, make hard delete admin-only after auth exists.

### 5. Clean ProjectResponse Comments

`ProjectResponse` is effectively simplified, but old fields are commented out.

- [ ] Remove commented fields from `ProjectResponse`.
- [ ] Add them back later only when owner/task count are actually mapped.

### 6. Optional Response Cleanup

- [ ] Map `deletedAt` in `TaskResponse` if deleted-task list should show deletion time.
- [ ] Add `deletedAt` to `ProjectResponse` only if you add a deleted-project list endpoint.

## Phase 3: Manual MVP Test Checklist

Use Postman, Swagger, or curl after Maven/app startup works.

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
- [ ] `POST /api/tasks/{id}/status` changes status on an active task.
- [ ] `POST /api/tasks/{id}/priority` changes priority on an active task.
- [ ] Optional: `DELETE /api/tasks/{id}/hard` permanently deletes a task.

### Error Flow

- [ ] Missing task id returns 404.
- [ ] Missing project id returns 404.
- [ ] Creating task without title returns 400.
- [ ] Creating project without name returns 400.
- [ ] Updating task with invalid data returns 400.
- [ ] Updating project with invalid data returns 400.

## Phase 4: User Module Only If Needed

For a task/project MVP, users can wait.

If users are required:

- [ ] Add `UserController` endpoints.
- [ ] Return `userMapper.toResponse(savedUser)` after create.
- [ ] Implement `updateUser`.
- [ ] Add `findAllByDeletedAtIsNull()` to `UserRepository`.
- [ ] Fix `getAllUsers()` to return active users, not deleted users.
- [ ] Implement `getAllDeletedUsers()`.
- [ ] Decide password strategy before exposing user creation.
- [ ] Check password and confirm password match.
- [ ] Hash passwords before any login/register feature.

## Phase 5: After MVP Works

### Task And Project Features

- [ ] Add task filtering by status.
- [ ] Add task filtering by priority.
- [ ] Add title search.
- [ ] Add overdue task filter.
- [ ] Add pagination and sorting.
- [ ] Add `projectId` to task create request.
- [ ] Validate project exists when assigning a task to project.
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
- [ ] Document permanent `JAVA_HOME` setup.

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
- [x] Fix standalone task restore logic.
- [x] Implement task status change endpoint.
- [x] Implement task priority change endpoint.
- [x] Fix task status change to load active tasks.
- [x] Fix task priority change to load active tasks.
- [x] Preserve task default priority/status during create.
- [x] Remove duplicate task description `@Size`.
- [x] Fix project list to return active projects.
- [x] Implement project update service and endpoint.
- [x] Implement project soft delete endpoint.
- [x] Implement project hard delete endpoint.
- [x] Implement project restore endpoint.
- [x] Add `@Valid` to task full update endpoint.
- [x] Add `@Valid` to project update endpoint.
- [x] Clean task restore mapping slash.
- [x] Make project restore controller method public.
- [x] Delete custom `MethodArgumentNotValidException`.
- [x] Import Spring's real `MethodArgumentNotValidException`.

## Current Highest Priority Order

1. Fix Maven wrapper execution.
2. Make `JAVA_HOME` persistent in Windows/IntelliJ.
3. Run tests or start the app.
4. Fix validation handler to return 400 with field details.
5. Remove commented fields from `ProjectResponse`.
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
