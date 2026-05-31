# TaskManagement MVP TODO

Generated: 2026-05-31

## Current Status

The project is now a working task/project MVP candidate.

Done:

- [x] Environment-based config with `.env`
- [x] MySQL development profile
- [x] PostgreSQL production profile
- [x] H2 test profile
- [x] Flyway migrations for MySQL and PostgreSQL
- [x] Project CRUD basics
- [x] Task CRUD basics
- [x] Soft delete and restore
- [x] Task status and priority endpoints
- [x] Assign tasks to projects with `projectId`
- [x] Filter tasks by status
- [x] Filter tasks by priority
- [x] Filter tasks by project id
- [x] Pagination and sorting for task list
- [x] Project service tests
- [x] Task service tests
- [x] Project controller tests
- [x] Task controller tests

## Do First

- [ ] Run `mvn test` and make sure all tests are green.
- [ ] Run `mvn spring-boot:run`.
- [ ] Open Swagger UI and test the main flows manually.
- [ ] Create project.
- [ ] Create task with `projectId`.
- [ ] List tasks with pagination.
- [ ] Filter tasks by `status`.
- [ ] Filter tasks by `priority`.
- [ ] Filter tasks by `projectId`.
- [ ] Soft delete and restore task.
- [ ] Soft delete and restore project.

## Fix Before More Big Features

- [ ] Decide hard delete policy before sharing the API.
- [ ] Protect or remove hard delete endpoints before production.
- [ ] Keep user/auth code out of MVP docs until it is implemented.
- [ ] Decide whether project list also needs pagination now or later.
- [ ] Align task description length between validation and database schema.

## Best Next Feature

Best next feature: **task search and due date filtering**.

Why:

- It improves the existing task list immediately.
- It builds on the current filter specification.
- It is smaller than auth and safer for the MVP.
- It makes the API more useful without changing the project structure much.

Suggested scope:

- [ ] Add `search` or `title` to `FilterTaskRequest`.
- [ ] Search task title with case-insensitive matching.
- [ ] Add `dueDateFrom` to `FilterTaskRequest`.
- [ ] Add `dueDateTo` to `FilterTaskRequest`.
- [ ] Update `TaskSpecification`.
- [ ] Add service tests for search and due date filters.
- [ ] Add controller tests for search and due date filters.
- [ ] Add README examples.

## After That

- [ ] Add `GET /api/projects/{id}/tasks`.
- [ ] Add pagination to project list if needed.
- [ ] Improve validation error response details.
- [ ] Add Swagger examples or descriptions.
- [ ] Add basic user CRUD.
- [ ] Add register/login.
- [ ] Add password hashing.
- [ ] Add authorization.

## Not For First MVP

- Frontend
- Comments
- Notifications
- Dashboard
- Team/workspace features
