# TaskManagement Project Report

Generated: 2026-05-31

## Summary

TaskManagement is now close to a usable backend MVP for projects and tasks. The strongest part of the project is the task/project API with soft delete, restore, filtering, pagination, Flyway migrations, and tests.

## What Exists

- Spring Boot backend with Java 21.
- Maven project structure.
- Environment-based configuration with `.env`.
- MySQL profile for development.
- PostgreSQL profile for production.
- H2 profile for tests.
- Flyway migrations for MySQL and PostgreSQL.
- Base entity with `createdAt`, `updatedAt`, and `deletedAt`.
- Global exception handling.
- Project API:
  - create
  - list active projects
  - update
  - soft delete
  - hard delete
  - restore
- Task API:
  - create
  - list active tasks
  - list deleted tasks
  - get by id
  - full update
  - partial update
  - soft delete
  - hard delete
  - restore
  - change status
  - change priority
- Task filtering by status, priority, and project id.
- Task pagination and sorting.
- Task-project relation through `projectId`.
- Service and controller tests for project/task flows.
- User/auth package structure.

## What Is Not Finished

- User API is not ready.
- Auth/register/login are not implemented.
- Password hashing is not implemented.
- Authorization is not implemented.
- Project list is not paginated yet.
- Task search is not implemented yet.
- Due date range filtering is not implemented yet.
- Hard delete endpoints are not protected.

## Pluses

- Good MVP direction: project/task features are the main focus.
- Domain packages are separated clearly.
- DTOs are used instead of exposing entities directly.
- Soft delete and restore behavior is already part of the model.
- Database config no longer uses committed secrets.
- Flyway is ready for both MySQL and PostgreSQL.
- Task filtering and pagination make the API more realistic.
- Tests now cover the important task/project service and controller flows.

## Minuses And Risks

- Hard delete endpoints are public.
- User/auth code is incomplete and should not be treated as ready.
- Task description validation allows 500 characters, while the migration currently uses `VARCHAR(255)`.
- Existing local databases created before Flyway may need reset or baseline.
- Project list may need pagination later if it grows.
- Manual Swagger/API testing is still needed after every endpoint change.

## Recommended Next Steps

1. Run `mvn test` and keep tests green.
2. Run the app and manually test project/task flows in Swagger.
3. Add task search.
4. Add due date range filters.
5. Add tests for the new filters.
6. Decide hard delete policy.
7. Start user/auth only after the task/project MVP is stable.
