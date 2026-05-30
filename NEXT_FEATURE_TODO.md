# TaskManagement MVP TODO

Generated: 2026-05-30

## Current Status

The project is close to a task/project CRUD MVP.

Working commands:

- [x] `mvn test`
- [x] `mvn spring-boot:run`

## Do Next

### 1. Manual Project Testing

- [ ] Create project.
- [ ] List projects.
- [ ] Update project.
- [ ] Soft delete project.
- [ ] Restore project.
- [ ] Test hard delete if it stays in MVP.

### 2. Manual Task Testing

- [ ] Create task with only title.
- [ ] Create task with status, priority, and due date.
- [ ] List active tasks.
- [ ] Get task by id.
- [ ] Full update task.
- [ ] Partial update task.
- [ ] Change task status.
- [ ] Change task priority.
- [ ] Soft delete task.
- [ ] List deleted tasks.
- [ ] Restore task.
- [ ] Test hard delete if it stays in MVP.

### 3. Clean MVP API Decisions

- [ ] Decide whether hard delete endpoints should stay public.
- [ ] Remove hard delete endpoints if this API will be shared.
- [ ] Add request examples to `README.md`.
- [ ] Add simple manual test notes to `README.md`.

### 4. Add Basic Tests

- [ ] Task service tests.
- [ ] Project service tests.
- [ ] Task controller tests.
- [ ] Project controller tests.
- [ ] Validation error tests.

## Later

- [ ] Task filtering by status.
- [ ] Task filtering by priority.
- [ ] Task title search.
- [ ] Pagination and sorting.
- [ ] Assign tasks to projects.
- [ ] User CRUD.
- [ ] Register/login.
- [ ] Password hashing.
- [ ] Authorization.
- [ ] Database migrations.

## Not For First MVP

- Frontend.
- Comments.
- Dashboard.
- Notifications.
- Team/workspace features.
