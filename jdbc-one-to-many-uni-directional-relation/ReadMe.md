# One-to-Many Unidirectional Relation

This module demonstrates a production-style **unidirectional one-to-many** model between:

- `Team` (owner side)
- `Member` (child side without reverse navigation)

It is intentionally designed for learning owner-only navigation, foreign-key control, orphan lifecycle handling, and DTO-safe APIs.

## Why this relation matters (architect point of view)

Use unidirectional one-to-many when:

- parent aggregate management is central to business workflows
- reverse child-to-parent navigation is not required in domain logic
- you want a simpler object model with fewer graph synchronization risks

## Why unidirectional one-to-many is important

In this module, the domain usually reads:

- `Team -> members`

but not:

- `Member -> team`

This keeps the object graph smaller while still allowing strict FK-based ownership in the database.

## When to use unidirectional one-to-many or many-to-one?

Use this mapping when the parent owns lifecycle operations and child-side navigation adds little value.

Examples:

- a `Team` onboarding/removing `Member` records
- a `Playlist` managing `Track` entries
- a `Checklist` owning `TaskItem` rows

Choose bidirectional mappings only when child-side traversal is a first-class business requirement.

## Interview perspective: when to choose uni vs bi

Use **unidirectional** when:

- parent-side workflows dominate read/write paths
- reverse link is not needed in services/APIs
- you prefer lower graph complexity

Use **bidirectional** when:

- both traversals are important in domain logic
- child-level workflows frequently need parent context
- you can consistently maintain both sides in memory

## Mapping in this module

### Owner side (`Team`)

```java
@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
@JoinColumn(name = "team_id_fk", nullable = false)
private List<Member> members;
```

- FK lives in `members.team_id_fk`
- no join table is required
- `orphanRemoval = true` deletes removed child rows

### Child side (`Member`)

`Member` intentionally has **no `Team` reference** field.

That is what keeps the association unidirectional in Java.

## Setter/helper rationale (uni vs bi)

In this module, helper logic exists only on the owner (`Team.addMember/removeMember`) because:

- there is no inverse field on `Member` to synchronize
- owner-side collection operations are sufficient

In bidirectional models, additional synchronization helpers/setters are required on both sides.

## API contract

### Team APIs

- `POST /api/v1/team/create`
- `GET /api/v1/team/get/all`
- `GET /api/v1/team/get/{teamId}`
- `PUT /api/v1/team/update/{teamId}`
- `DELETE /api/v1/team/delete/{teamId}`

### Member APIs

- `POST /api/v1/member/create/team/{teamId}`
- `GET /api/v1/member/get/all`
- `GET /api/v1/member/get/{memberId}`
- `PUT /api/v1/member/update/{memberId}`
- `PUT /api/v1/member/reassign/{memberId}/team/{teamId}`
- `DELETE /api/v1/member/delete/{memberId}`
- `GET /api/v1/team/member/info`

## Validation and error contract

Service-level validators enforce required fields, length constraints, and email format:

- `TeamRequestValidator`
- `MemberRequestValidator`

Failures return structured error payloads:

- `400` for request validation failures
- `404` for missing team/member
- `409` for DB uniqueness/conflict violations

## Exact table behavior

Expected schema shape:

```text
teams
  - id (PK)
  - team_code (UNIQUE, NOT NULL)
  - name (NOT NULL)
  - description (NOT NULL)

members
  - id (PK)
  - first_name (NOT NULL)
  - last_name (NOT NULL)
  - email (UNIQUE, NOT NULL)
  - mobile (NOT NULL)
  - team_id_fk (FK -> teams.id, NOT NULL)
```

No join table is created because the owner uses `@JoinColumn`.

## SQL checks to run

```sql
-- 1) Verify schema columns
select column_name, is_nullable
from information_schema.columns
where table_name in ('teams', 'members')
order by table_name, ordinal_position;

-- 2) Verify FK on members
select conname, contype
from pg_constraint
where conrelid = 'members'::regclass;

-- 3) Verify data and FK values
select id, team_code, name from teams;
select id, first_name, last_name, email, team_id_fk from members;
```

## Orphan lifecycle behavior

Because owner side uses `orphanRemoval = true`:

- replacing a team's member collection deletes removed members
- deleting a team cascades delete to all owned members

## Test taxonomy in this module

- **Controller integration**: create, update, delete, reassignment, validation and not-found paths
- **Relation integration**: owner collection persistence and replacement semantics
- **Orphan handling integration**: member orphan deletion and team cascade delete
- **In-memory relation tests**: verify absence of reverse navigation and deterministic owner behavior

## Runtime profile strategy

- `application.yaml` sets default profile to `postgres`
- `application-postgres.yml` configures runtime PostgreSQL
- `application-test.yml` configures H2 for integration tests
- Maven Surefire enforces `spring.profiles.active=test` during tests

## Run app with Docker PostgreSQL

Start PostgreSQL container from repository:

```bash
$ docker compose -f docker-compose.yml up
```

## Interview-ready quick answers

- **Who owns this relation?** `Team` because it defines `@OneToMany + @JoinColumn`.
- **Why no reverse field on Member?** Business flows do not need child-to-parent traversal.
- **Why orphanRemoval?** To prevent stale child rows when owner collection changes.
- **Why DTOs?** To keep API boundaries stable and avoid JPA leakage.

## Anti-patterns to avoid

- Adding a reverse `Team` reference in `Member` without actually needing bidirectional behavior.
- Replacing owner collections without understanding orphan-removal effects.
- Returning entities directly from controllers.
- Treating lazy loading errors by switching everything to eager fetch.

