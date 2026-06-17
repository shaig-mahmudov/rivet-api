# Risk Remediation Tickets

Generated: 2026-06-17

## Ticket 1: Validate JWT Users Against Current Database State

Severity: High

Problem:
JWT authorization currently trusts role claims until token expiry. A soft-deleted or demoted admin can keep using admin-only routes while an old access token is valid.

Acceptance criteria:
- JWT authentication rejects deleted users.
- JWT authentication uses the user's current database role.
- Tests cover deleted-admin and demoted-admin tokens on admin-only endpoints.

## Ticket 2: Make Refresh Token Rotation Single-Use Under Concurrency

Severity: High

Problem:
Two concurrent refresh requests can validate the same refresh token before either request revokes it, creating multiple valid replacement tokens.

Acceptance criteria:
- Refresh token lookup used for rotation/revocation locks the token row.
- A refresh token cannot be rotated twice successfully.
- Existing refresh-token behavior remains unchanged for normal requests.

## Ticket 3: Prevent Concurrent First-Admin Bootstrap

Severity: High

Problem:
Two first-time admin bootstrap requests can both pass the "no active admin exists" check before either saves a user.

Acceptance criteria:
- Admin bootstrap check and user creation run in a transaction strong enough to prevent concurrent first-admin creation.
- Existing invalid-token, duplicate-email, and repeat-bootstrap responses remain clean.

## Ticket 4: Avoid Holding Database Transactions During AI Provider Calls

Severity: Medium

Problem:
AI draft generation runs inside a read-only transaction while calling the provider. A slow provider can keep database/session resources open unnecessarily.

Acceptance criteria:
- Task context and existing criteria are loaded before the provider call.
- The provider call happens outside a transactional method.
- Existing AI draft behavior and tests remain green.

## Ticket 5: Reduce Task Dependency Query Bottlenecks

Severity: Medium

Problem:
Circular dependency checks recurse with one query per graph node, and the global blocked-tasks endpoint loads all incomplete dependency rows without pagination.

Acceptance criteria:
- Circular dependency checks use a bounded or batched traversal rather than unbounded recursive repository calls.
- Global blocked tasks can be requested with pagination.
- Existing list behavior stays available for current clients.

## Ticket 6: Clean Up Expired And Revoked Refresh Tokens

Severity: Medium

Problem:
Expired and revoked refresh tokens accumulate indefinitely.

Acceptance criteria:
- Expired and revoked refresh tokens can be removed automatically or through a simple maintenance path.
- Cleanup behavior is covered by tests.
- Token retention configuration is documented if configurable.
