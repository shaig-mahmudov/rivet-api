# TaskManagement Next Feature TODO

Generated: 2026-06-08

## Current Status

The project is a working backend MVP candidate for project and task management.

Latest verification:

- [x] `.\mvnw.cmd test` passed on 2026-06-12 with 84 tests, 0 failures.

Done:

- [x] Environment-based config with `.env`
- [x] MySQL development profile
- [x] PostgreSQL production profile
- [x] H2 test profile
- [x] Flyway migrations for MySQL and PostgreSQL
- [x] Spring Boot 4 Flyway auto-configuration dependency
- [x] Migration schema validation tests for MySQL and PostgreSQL
- [x] `User.role` enum storage explicitly matches string-based migrations
- [x] Windows Maven wrapper starts Maven in this environment
- [x] Project CRUD basics
- [x] Project pagination and sorting
- [x] Filter projects by search text
- [x] Filter projects by owner id
- [x] Task CRUD basics
- [x] Soft delete and restore
- [x] Task status and priority endpoints
- [x] Assign tasks to projects with `projectId`
- [x] Filter tasks by search text
- [x] Filter tasks by status
- [x] Filter tasks by priority
- [x] Filter tasks by project id
- [x] Filter tasks by due date from
- [x] Filter tasks by due date to
- [x] Filter tasks from today until a due date
- [x] Pagination and sorting for task list
- [x] Project-specific task listing with `GET /api/projects/{id}/tasks`
- [x] Project service tests
- [x] Task service tests
- [x] Project controller tests
- [x] Task controller tests
- [x] Project task listing service tests
- [x] Project task listing controller tests
- [x] Search filter tests
- [x] Due-date filter tests
- [x] Project description validation limit
- [x] Project search and owner filter tests
- [x] Project soft delete also soft-deletes active child tasks
- [x] Field-level validation error details
- [x] Basic auth register/login
- [x] Stateless JWT Bearer authentication
- [x] BCrypt password hashing
- [x] Admin-only hard delete endpoints
- [x] Basic user CRUD
- [x] Task assignment to users with `assigneeId`
- [x] Task filtering by assignee id

## Manual Check

- [ ] Run `.\mvnw.cmd spring-boot:run`.
- [ ] Confirm Flyway applies V1 on a real local MySQL database.
- [ ] Confirm the prod profile applies V1 on a real PostgreSQL database before deployment.
- [ ] Open Swagger UI.
- [ ] Create a project.
- [ ] List projects with pagination.
- [ ] Filter projects by `search`.
- [ ] Filter projects by `ownerId`.
- [ ] Create a task with `projectId`.
- [ ] List tasks with pagination.
- [ ] Filter tasks by `search`.
- [ ] Filter tasks by `status`.
- [ ] Filter tasks by `priority`.
- [ ] Filter tasks by `projectId`.
- [ ] Filter tasks by `dueDateFrom`.
- [ ] Filter tasks by `dueDateTo`.
- [ ] Filter tasks by `dueFromToday=true&dueDateTo=YYYY-MM-DD`.
- [ ] Soft delete and restore a task.
- [ ] Soft delete and restore a project.

## Recently Completed Feature

Completed feature: project-specific task listing.

Suggested endpoint:

```text
GET /api/projects/{id}/tasks
```

Why:

- It makes the existing project/task relation more useful.
- It is a natural endpoint for a future project detail page.
- It can reuse pagination, sorting, and existing task filters.
- It is smaller and safer than starting auth right now.

Implemented behavior:

- [x] Return only active tasks.
- [x] Return tasks only for the requested active project.
- [x] Return `404` when the project does not exist.
- [x] Return `404` when the project is soft deleted.
- [x] Support pagination and sorting.
- [x] Support task filters: `search`, `status`, `priority`, `dueDateFrom`, `dueDateTo`, and `dueFromToday`.
- [x] Force `projectId` from the path variable so clients cannot override it with a query parameter.

Suggested API examples:

```text
GET /api/projects/1/tasks
GET /api/projects/1/tasks?page=0&size=10&sort=createdAt,desc
GET /api/projects/1/tasks?status=TODO
GET /api/projects/1/tasks?search=invoice&dueDateFrom=2026-06-01&dueDateTo=2026-06-30
```

## Implementation Checklist

- [x] Decide whether the endpoint belongs in `ProjectController` or `TaskController`.
- [x] Add a service method for project task listing.
- [x] Verify the project exists with `findByIdAndDeletedAtIsNull`.
- [x] Reuse `FilterTaskRequest` for optional filters.
- [x] Force `projectId` from the path variable so clients cannot override it with a query parameter.
- [x] Return `Page<TaskResponse>`.
- [x] Add service test for active project tasks.
- [x] Add service test that excludes tasks from other projects.
- [x] Add service test for missing/deleted project.
- [x] Add controller test for `GET /api/projects/{id}/tasks`.
- [x] Add controller test with pagination/filter query params.
- [x] Update README endpoint examples.
- [x] Run `.\mvnw.cmd test`.
- [ ] Manually test in Swagger.

## Fix Before Bigger Features

Note: Hard delete is now admin-only through Spring Security JWT authorization.

- [x] Add project description validation limit.
- [ ] Add a container-based MySQL/PostgreSQL migration test if database containers become part of the project.
- [x] Decide what should happen to tasks when a project is soft deleted.
- [x] Add project search and owner filter tests.
- [x] Keep user/auth code out of MVP docs until it is implemented.
- [x] Protect hard delete endpoints when auth/admin roles are implemented.

## After Project Task Listing

- [x] Improve validation error response details.
- [ ] Add Swagger descriptions/examples.
- [x] Add basic user CRUD.
- [x] Add register/login.
- [x] Add password hashing.
- [x] Add authorization.
- [x] Make hard delete admin-only.
- [x] Add task assignment to users.
- [x] Replace HTTP Basic with token-based auth.
- [x] Tighten admin role creation before production.
- [ ] Add project ownership assignment.
- [ ] Add refresh tokens/session rotation.
- [ ] Add a trusted admin bootstrap flow.

## Not For First MVP

- Frontend
- Comments
- Notifications
- Dashboard
- Team/workspace features
