# TaskManagement Project Report

Generated: 2026-06-03

## Summary

TaskManagement is a Spring Boot backend API for managing projects and tasks. The project is a solid task/project MVP candidate: core CRUD, soft delete/restore, task filtering, pagination, Flyway migrations, and service/controller tests are already in place.

The best next move is to finish the task list experience before starting authentication. Specifically: add task text search, add tests and README examples for the due-date filters that already exist, and then manually verify the API in Swagger.

## Current Architecture

- Java 21 and Spring Boot 4.
- Maven project structure.
- Layered package style by domain: `task`, `project`, `user`, `auth`, and `common`.
- DTOs are used for request/response objects.
- Service classes contain business behavior.
- Repository classes use Spring Data JPA.
- `TaskRepository` supports `JpaSpecificationExecutor` for dynamic filters.
- Global exception handling is present.
- Shared `BaseEntity` contains `createdAt`, `updatedAt`, and `deletedAt`.
- Flyway migrations exist for MySQL and PostgreSQL.
- Test profile uses H2 with `ddl-auto=create-drop`.

## What Exists

- Environment-based configuration with `.env`.
- MySQL development profile.
- PostgreSQL production profile.
- H2 test profile.
- Project API:
  - create project
  - list active projects
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
- Task filtering by:
  - status
  - priority
  - project id
  - due date from
  - due date to
  - due from today with an upper due date
- Task pagination and sorting.
- Optional task-to-project assignment through `projectId`.
- Service and controller tests for important project/task flows.
- User/auth package placeholders and database tables.

## What Is Not Finished

- Task text search is not implemented yet.
- Due-date filters exist in code, but they are not covered by service/controller tests.
- README does not show examples for due-date filtering yet.
- User API is not implemented.
- Register/login are not implemented.
- Password hashing is not implemented.
- Authorization is not implemented.
- Project list is not paginated.
- Hard delete endpoints are public.
- Assignment to users is modeled in the database/entity, but no API flow uses it yet.

## Strengths

- The project has a clear MVP boundary around projects and tasks.
- Domain packages are easy to navigate.
- DTOs prevent direct entity exposure.
- Soft delete and restore are handled consistently in the main flows.
- Task filtering already uses a scalable Specification-based approach.
- Pagination and sorting are available for task lists.
- Tests cover many service and controller behaviors.
- Flyway support makes the database setup more production-like.

## Risks And Gaps

- Hard delete endpoints should be protected, hidden, or removed before real deployment.
- The user/auth packages currently look like future scaffolding, not usable features.
- Task and project description validation allows 500 characters, but migrations use `VARCHAR(255)`.
- Project soft delete does not automatically soft delete child tasks. This is acceptable for now, but the expected product behavior should be decided.
- Project restore does not need special handling yet, but task restore correctly blocks restore when its project is deleted.
- Existing local databases created before Flyway may need reset or baseline.
- Manual Swagger testing is still needed after endpoint changes.

## Recommended Next Feature

Recommended next feature: task search plus due-date filter completion.

This is better than starting auth immediately because it builds on the strongest part of the codebase: the task list API. It is small enough to finish cleanly, but useful enough to make the app feel more realistic.

Suggested scope:

- Add a `search` field to `FilterTaskRequest`.
- Match `search` against task title and optionally description.
- Use case-insensitive matching.
- Ignore blank search values.
- Keep search combined with existing status, priority, project, and due-date filters.
- Add service tests for search.
- Add service tests for `dueDateFrom`, `dueDateTo`, and `dueFromToday`.
- Add controller tests for query parameters.
- Update README examples.

## How To Continue

1. Keep tests green with `mvn test` before and after each feature.
2. Finish one backend feature at a time.
3. For the next feature, start in `FilterTaskRequest`, then update `TaskSpecification`, then tests, then README.
4. After task search is done, add `GET /api/projects/{id}/tasks` so project pages can list their tasks directly.
5. Then decide the hard delete policy before adding authentication.
6. Build user/auth after the task/project API feels stable.

## Suggested Roadmap

1. Task search and due-date filter tests.
2. README query examples and Swagger manual testing.
3. Project task listing endpoint.
4. Project list pagination.
5. Validation/database length alignment.
6. Hard delete policy.
7. User registration and login.
8. Password hashing and authorization.
9. User assignment for tasks.
10. Frontend or API client.
