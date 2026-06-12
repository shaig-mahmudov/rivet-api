# TaskManagement Project Report

Generated: 2026-06-08

## Summary

TaskManagement is a Spring Boot backend API for managing projects and tasks. The project is now a strong task/project MVP candidate: CRUD, soft delete/restore, project/task search and filtering, project-specific task listing, pagination, Flyway migrations, and integration-style service/controller tests are in place.

Latest migration review found and fixed two important issues:

- Spring Boot 4 Flyway integration was missing, so Flyway libraries were present but migrations were not automatically running.
- `User.role` was mapped as an ordinal enum by default, while both migrations correctly store role values as strings.

The project/task relationship now includes a project-specific task listing endpoint: `GET /api/projects/{id}/tasks`.

## Current Architecture

- Java 21 and Spring Boot 4.
- Maven project structure.
- Layered package style by domain: `task`, `project`, `user`, `auth`, and `common`.
- DTOs are used for request/response objects.
- Service classes contain business behavior.
- Repository classes use Spring Data JPA.
- `TaskRepository` supports `JpaSpecificationExecutor` for dynamic filters.
- `ProjectRepository` supports `JpaSpecificationExecutor` for dynamic filters.
- Global exception handling is present.
- Shared `BaseEntity` contains `createdAt`, `updatedAt`, and `deletedAt`.
- Flyway migrations exist for MySQL and PostgreSQL.
- Spring Boot Flyway auto-configuration is enabled through `spring-boot-flyway`.
- Test profile uses H2 with `ddl-auto=create-drop`.
- Dedicated migration validation tests run the MySQL and PostgreSQL migrations against H2 compatibility modes with Hibernate `ddl-auto=validate`.

## What Exists

- Environment-based configuration with `.env`.
- MySQL development profile.
- PostgreSQL production profile.
- H2 test profile.
- Project API:
  - create project
  - list active projects with pagination and sorting
  - search projects by name/description
  - filter projects by owner id
  - update project
  - soft delete project
  - hard delete project
  - restore project
- Task API:
  - create task
  - list active tasks
  - list deleted tasks
  - get task by id
  - full update
  - partial update
  - soft delete
  - hard delete
  - restore
  - change status
  - change priority
- Project-specific task listing:
  - list active tasks for one active project with `GET /api/projects/{id}/tasks`
  - support pagination, sorting, and the existing task filters
  - return `404` for missing or soft-deleted projects
- Task filtering by:
  - search text
  - status
  - priority
  - project id
  - due date from
  - due date to
  - due from today with an upper due date
- Project and task pagination and sorting.
- Optional task-to-project assignment through `projectId`.
- Service and controller tests for project/task flows.
- Search and due-date filter tests.
- MySQL and PostgreSQL migration schema validation tests.
- User/auth package placeholders and database tables. Role storage is now explicitly string-based to match the migrations.

## Latest Verification

- `.\mvnw.cmd test` passed on 2026-06-08.
- Test result: 70 tests, 0 failures, 0 errors, 0 skipped.
- The Windows Maven wrapper was fixed so it can start Maven when `.m2` is a normal directory.
- Migration validation confirmed that both V1 migration files apply successfully and match the current JPA mappings under H2 MySQL/PostgreSQL compatibility modes.

## What Is Not Finished

- User API is not implemented.
- Register/login are not implemented.
- Password hashing is not implemented.
- Authorization is not implemented.
- Hard delete endpoints are intentionally still public for easier development.
- Project ownership and task assignment to users are modeled in the database/entities, but no API flow uses them yet.

## Strengths

- The project has a clear MVP boundary around projects and tasks.
- Domain packages are easy to navigate.
- DTOs prevent direct entity exposure.
- Soft delete and restore are handled consistently in the main flows.
- Project and task filtering use a scalable Specification-based approach.
- Search combines cleanly with status, priority, project, and due-date filters.
- Pagination and sorting are available for project and task lists.
- Tests cover many service and controller behaviors.
- Flyway support makes the database setup more production-like.
- Migration validation tests now protect the initial schema from drifting away from JPA mappings.

## Risks And Gaps

- Hard delete should become admin-only before real deployment.
- The user/auth packages currently look like future scaffolding, not usable features.
- Task and project description validation are now capped at 250 characters, which fits the `VARCHAR(255)` migration.
- Project soft delete now also soft-deletes active child tasks. Project restore intentionally does not auto-restore tasks, so previously deleted tasks are not accidentally resurrected.
- Project restore does not need special handling yet, but task restore correctly blocks restore when its project is deleted.
- Project search/owner filtering is implemented and covered by service/controller tests.
- Existing local databases created before Flyway may need reset or baseline.
- Migration validation currently uses H2 compatibility modes. Real MySQL and PostgreSQL startup should still be checked before deployment.
- Spring warns that returning `PageImpl` directly may produce unstable JSON structure. It is fine for the MVP, but a DTO page wrapper would be cleaner later.
- Manual Swagger testing is still needed after endpoint changes.

## Recently Completed Feature

Completed feature: project-specific task listing.

Suggested endpoint:

```text
GET /api/projects/{id}/tasks
```

Why this should come next:

- It improves the project/task relationship that already exists in the database.
- It is useful for any future frontend project detail page.
- It reuses the existing pagination/filtering model.
- It is smaller than auth and keeps the MVP focused.

Implemented scope:

- Return active tasks for one active project.
- Support pagination and sorting.
- Support the same filters as `GET /api/tasks`.
- Return `404` if the project does not exist or is soft deleted.
- Add service tests.
- Add controller tests.
- Add README examples.

## How To Continue

1. Keep tests green with `.\mvnw.cmd test` before and after each feature.
2. Manually test search and due-date filters in Swagger.
3. Improve validation error response details.
4. Add Swagger examples/descriptions.
5. Move hard delete behind admin authorization when auth is ready.
6. Build user/auth after the task/project API feels stable.

## Suggested Roadmap

1. Validation error response improvements.
2. Swagger examples/descriptions.
3. User registration and login.
4. Password hashing and authorization.
5. Admin-only hard delete.
6. User assignment for tasks.
7. Frontend or API client.
