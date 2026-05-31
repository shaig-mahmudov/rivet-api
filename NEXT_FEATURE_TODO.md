# TaskManagement MVP TODO

Generated: 2026-05-31

## Current Status

The project is close to a task/project CRUD MVP.

Done:

- [x] `mvn test`
- [x] `mvn spring-boot:run`
- [x] H2 test profile
- [x] Task service tests
- [x] Project service tests
- [x] Task controller tests
- [x] Project controller tests
- [x] Flyway dependencies
- [x] MySQL migration
- [x] PostgreSQL migration
- [x] Task filtering by status
- [x] Task filtering by priority
- [x] Fixed duplicate `GET /api/tasks` mapping

## Do Next

### 1. Test The Current API Manually

- [ ] Create project.
- [ ] List projects.
- [ ] Update project.
- [ ] Soft delete project.
- [ ] Restore project.
- [ ] Create task with only title.
- [ ] Create task with status, priority, and due date.
- [ ] List active tasks.
- [ ] Filter tasks by status.
- [ ] Filter tasks by priority.
- [ ] Filter tasks by status and priority.
- [ ] Get task by id.
- [ ] Full update task.
- [ ] Partial update task.
- [ ] Change task status.
- [ ] Change task priority.
- [ ] Soft delete task.
- [ ] List deleted tasks.
- [ ] Restore task.

### 2. Decide Hard Delete Policy

- [ ] Keep hard delete only if this is a private/local prototype.
- [ ] Remove hard delete endpoints if the API will be shared.
- [ ] Later, re-add hard delete as admin-only after auth exists.

### 3. Add Filter Tests

- [x] Task service test for status filter.
- [x] Task service test for priority filter.
- [x] Task service test for status + priority filter.
- [x] Task controller test for `GET /api/tasks?status=TODO`.
- [x] Task controller test for `GET /api/tasks?priority=HIGH`.
- [x] Task controller test for `GET /api/tasks?status=TODO&priority=HIGH`.

### 4. Docs Later Today

- [ ] Add project request examples to `README.md`.
- [ ] Add task request examples to `README.md`.
- [ ] Add filter endpoint examples to `README.md`.
- [ ] Add Flyway notes to `README.md`.

## Best Next Feature

Best next feature for the current state: **assign tasks to projects**.

Why:

- Projects and tasks already exist.
- The entity relationship already exists: `Task.project`.
- It makes the API feel like a real task management app.
- It is smaller and safer than starting auth.

Suggested scope:

- [ ] Add `projectId` to `CreateTaskRequest`.
- [ ] Add optional `projectId` to `UpdateTaskRequest` or partial update.
- [ ] Validate that project exists and is not deleted.
- [ ] Save the project relation on the task.
- [ ] Add `projectId` to `TaskResponse`.
- [ ] Add tests for creating a task inside a project.

## Later

- [ ] Task title search.
- [ ] Pagination and sorting.
- [ ] User CRUD.
- [ ] Register/login.
- [ ] Password hashing.
- [ ] Authorization.
- [ ] Database migration for new fields if model changes.

## Not For First MVP

- Frontend.
- Comments.
- Dashboard.
- Notifications.
- Team/workspace features.
