# One-to-Many Bidirectional Relation

This module demonstrates a production-style **bidirectional one-to-many / many-to-one** model between:

- `Role` (inverse side) -> many `User`
- `User` (owning side) -> one `Role`

It is intentionally designed for learning JPA ownership/mapping semantics, graph synchronization, orphan lifecycle handling, and DTO-safe APIs.

## Why this relation matters (architect point of view)

Use bidirectional one-to-many when:

- parent-level aggregate operations and child-level navigation are both business-critical.
- you need role-centric and user-centric use cases in the same bounded context.
- you want explicit service-level control over lifecycle rules (replace, reassign, delete)

## When to use bidirectional one-to-many or many-to-one?

Use this mapping when both parent and child navigations are meaningful in domain behavior:

- parent aggregate operations are central (for example: add/remove many children in one transaction)
- child workflows also need a direct link to parent ownership/authorization
- you need lifecycle control from parent (`orphanRemoval`, cascade policies) while still querying from child perspective

Prefer unidirectional one-to-many or plain many-to-one when reverse traversal does not add business value.

## Common business scenarios

- `Department <-> Employee`
- `Account <-> Transaction`
- `Course <-> Enrollment`
- `Project <-> Task`

## Interview perspective: when to choose uni vs bi

Use **bidirectional** when:

- both traversals are required in core business logic
- parent-side lifecycle control over children is important
- you can enforce graph synchronization helpers reliably

Use **unidirectional** when:

- only one side is queried most of the time
- you want simpler entity maintenance and lower graph-coupling risk
- reverse association would only add accidental complexity

## Mapping in this module

### Owning side (`User`)

```java
@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "role_id_fk", nullable = false)
private Role role;
```

- FK lives in `users.role_id_fk`
- owner side drives relationship updates at DB level

### Inverse side (`Role`)

```java
@OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
private List<User> users;
```

- inverse side is for navigation and aggregate operations
- `orphanRemoval = true` deletes users removed from the parent collection

## Setter/helper design rationale (uni vs bi)

This is intentional and has a technical reason.

### Why bidirectional entities use custom setters/helpers

Bidirectional models must keep both sides of the object graph consistent in memory:

- `Role.addUser(user)` updates `User.role`
- `User.setRole(role)` updates both old/new role collections

Without this, persistence can produce surprising FK states and business logic/tests may observe half-linked objects.

### Why unidirectional entities can use plain Lombok setters

In unidirectional models there is only one navigable side. Since no inverse field exists, there is nothing to synchronize, and a plain setter is usually enough.

### When to still add a custom setter in unidirectional models

Add one when you need domain rules or controlled side effects:

- state-transition guards
- audit/event hooks
- strict replacement semantics

Rule of thumb:

- bidirectional -> synchronization helpers recommended
- unidirectional -> plain setter usually fine

## API contract

### Role APIs

- `POST /api/v1/role/create`
- `GET /api/v1/role/get/all`
- `GET /api/v1/role/get/{roleId}`
- `PUT /api/v1/role/update/{roleId}`
- `DELETE /api/v1/role/delete/{roleId}`

### User APIs

- `POST /api/v1/user/create/role/{roleId}`
- `GET /api/v1/user/get/all`
- `GET /api/v1/user/get/{userId}`
- `PUT /api/v1/user/update/{userId}`
- `PUT /api/v1/user/reassign/{userId}/role/{roleId}`
- `DELETE /api/v1/user/delete/{userId}`
- `GET /api/v1/role/user/info`

## Validation and error contract

Service-layer validators enforce required fields, max lengths, and email shape.

- `RoleRequestValidator`
- `UserRequestValidator`

Failures return structured errors from `GlobalExceptionHandler`:

- `400` for validation/argument issues
- `404` for missing role or user
- `409` for DB constraint conflicts

## Exact table behavior

Expected schema shape:

```text
roles
  - id (PK)
  - name (UNIQUE, NOT NULL)
  - description (NOT NULL)

users
  - id (PK)
  - first_name (NOT NULL)
  - last_name (NOT NULL)
  - mobile (NOT NULL)
  - email (UNIQUE, NOT NULL)
  - role_id_fk (FK -> roles.id, NOT NULL)
```

No join table is required for one-to-many.

## SQL checks to run

```sql
-- 1) Verify roles and users shape
select column_name, is_nullable
from information_schema.columns
where table_name in ('roles', 'users')
order by table_name, ordinal_position;

-- 2) Verify FK from users to roles
select conname, contype
from pg_constraint
where conrelid = 'users'::regclass;

-- 3) Verify relationship values
select id, name, description from roles;
select id, first_name, last_name, email, role_id_fk from users;
```

## Test taxonomy in this module

- **Controller integration**: create/validation/reassign flow verification
- **Relation integration**: owner/inverse synchronization before flush and after persist
- **Orphan handling integration**: user orphan deletion and role cascade delete
- **In-memory relation tests**: pure object graph guarantees without repository calls

## Runtime profile strategy

- `application.yml` sets default profile to `postgres`
- `application-postgres.yml` is runtime configuration for PostgreSQL
- `application-test.yml` is H2 integration-test configuration
- Maven Surefire enforces `spring.profiles.active=test` during tests

## Run app with Docker PostgreSQL

```bash
cd /Users/sashank/Personal/projects/backend/traditional-db-systems
POSTGRES_USER=postgres POSTGRES_PASSWORD=password docker compose up -d postgres
```

```bash
docker exec -it postgres psql -U postgres -c "CREATE DATABASE jdbc_one_to_many_bi_directional_relations;"
```

```bash
cd /Users/sashank/Personal/projects/backend/traditional-db-systems/jdbc-one-to-many-bi-directional-relation
DB_HOST=localhost DB_PORT=5432 DB_NAME=jdbc_one_to_many_bi_directional_relations DB_USERNAME=postgres DB_PASSWORD=password ./mvnw spring-boot:run
```

## Interview-ready quick answers

- **Who owns one-to-many?** The `@ManyToOne` side (`User.role`) because it stores the FK.
- **Why bidirectional here?** Both aggregate navigation (`Role.users`) and child navigation (`User.role`) are useful.
- **Why helper methods?** To avoid half-linked in-memory graphs and FK anomalies.
- **Why orphanRemoval?** To automatically clean removed children.
- **Why DTOs?** To prevent entity leakage and recursion issues at API boundaries.

## Anti-patterns to avoid

- Updating only inverse collection (`Role.users`) without setting owner side (`User.role`).
- Exposing JPA entities directly from controller responses.
- Treating lazy proxy exceptions as a reason to switch everything to eager fetch.
- Replacing child collections without explicit orphan/removal intent.
- Mixing transaction logic into controllers instead of keeping it in services.

