# TaskManagement Next Feature TODO

Generated: 2026-06-03

## Current Status

The project is a working backend MVP candidate for project and task management.

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
- [x] Filter tasks by due date from
- [x] Filter tasks by due date to
- [x] Filter tasks from today until a due date
- [x] Pagination and sorting for task list
- [x] Project service tests
- [x] Task service tests
- [x] Project controller tests
- [x] Task controller tests

## Do First

- [ ] Run `mvn test` and keep the suite green.
- [ ] Run `mvn spring-boot:run`.
- [ ] Open Swagger UI.
- [ ] Create a project.
- [ ] Create a task with `projectId`.
- [ ] List tasks with pagination.
- [ ] Filter tasks by `status`.
- [ ] Filter tasks by `priority`.
- [ ] Filter tasks by `projectId`.
- [ ] Filter tasks by `dueDateFrom`.
- [ ] Filter tasks by `dueDateTo`.
- [ ] Filter tasks by `dueFromToday=true&dueDateTo=YYYY-MM-DD`.
- [ ] Soft delete and restore a task.
- [ ] Soft delete and restore a project.

## Best Next Feature

Best next feature: task search.

Why:

- It improves the most-used endpoint: `GET /api/tasks`.
- It fits the existing `FilterTaskRequest` and `TaskSpecification` design.
- It can be combined with existing filters, pagination, and sorting.
- It is smaller and safer than starting auth right now.
- It gives good practice with dynamic JPA queries.

Recommended behavior:

- [ ] Add `search` to `FilterTaskRequest`.
- [ ] Treat blank search as no search filter.
- [ ] Search task title case-insensitively.
- [ ] Also search description case-insensitively if you want broader search.
- [ ] Keep deleted tasks excluded from normal search results.
- [ ] Allow search to combine with `status`, `priority`, `projectId`, `dueDateFrom`, and `dueDateTo`.

Suggested API examples:

```text
GET /api/tasks?search=invoice
GET /api/tasks?search=invoice&status=TODO
GET /api/tasks?search=invoice&priority=HIGH&page=0&size=10&sort=createdAt,desc
GET /api/tasks?search=invoice&dueDateFrom=2026-06-01&dueDateTo=2026-06-30
```

## Implementation Checklist

- [ ] Add `private String search;` to `FilterTaskRequest`.
- [ ] In `TaskSpecification`, trim and check the search value.
- [ ] Convert the search value to lowercase.
- [ ] Use `criteriaBuilder.lower(root.get("title"))`.
- [ ] Add a `LIKE` predicate with `%search%`.
- [ ] Optionally add description search with `OR`.
- [ ] Combine the search predicate with the existing `AND` predicates.
- [ ] Add service tests for title search.
- [ ] Add service tests for case-insensitive search.
- [ ] Add service tests for search combined with status or priority.
- [ ] Add service tests for `dueDateFrom` and `dueDateTo`.
- [ ] Add service tests for `dueFromToday`.
- [ ] Add controller tests for `search` query parameter.
- [ ] Add controller tests for due-date query parameters.
- [ ] Update README filter examples.
- [ ] Run `mvn test`.
- [ ] Manually test in Swagger.

## Code Hints

Good starting files:

- `src/main/java/com/engine/taskmanagement/task/dto/request/FilterTaskRequest.java`
- `src/main/java/com/engine/taskmanagement/task/specification/TaskSpecification.java`
- `src/test/java/com/engine/taskmanagement/task/service/implementation/TaskServiceImplTest.java`
- `src/test/java/com/engine/taskmanagement/task/controller/TaskControllerTest.java`
- `README.md`

Specification idea:

```java
if (request.getSearch() != null && !request.getSearch().isBlank()) {
    String search = "%" + request.getSearch().trim().toLowerCase() + "%";
    predicates.add(
            criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), search),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), search)
            )
    );
}
```

If you search description, consider null handling. Most databases handle `LIKE` on null as false, but title-only search is simpler for the first version.

## Fix Before Bigger Features

- [ ] Decide hard delete policy before sharing the API.
- [ ] Protect or remove hard delete endpoints before production.
- [ ] Align description length between validation and database schema.
- [ ] Decide what should happen to tasks when a project is soft deleted.
- [ ] Decide whether project list needs pagination now.
- [ ] Keep user/auth code out of MVP docs until it is implemented.

## After Task Search

- [ ] Add `GET /api/projects/{id}/tasks`.
- [ ] Add pagination to project list.
- [ ] Improve validation error response details.
- [ ] Add Swagger descriptions/examples.
- [ ] Add basic user CRUD.
- [ ] Add register/login.
- [ ] Add password hashing.
- [ ] Add authorization.
- [ ] Add task assignment to users.

## Not For First MVP

- Frontend
- Comments
- Notifications
- Dashboard
- Team/workspace features
