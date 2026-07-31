# One-to-One Unidirectional Relation

This module demonstrates a production-style **unidirectional one-to-one** between:

- `Employee` (owning side)
- `Workstation` (dependent side without reverse navigation)

It is intentionally designed for learning JPA ownership, one-to-one constraints, orphan lifecycle handling, and API-safe DTO contracts.

## Why this relation matters (architect point of view)

Use one-to-one when two tables have strict **1:1 cardinality** but should stay separate for architecture reasons.

Typical reasons to split:

- **Security boundaries**: isolate sensitive or operationally restricted columns
- **Data ownership**: two teams/domains may evolve attributes at different speeds
- **Write-frequency separation**: volatile child data can change without bloating the parent table
- **Storage and access patterns**: parent lookups are frequent; child fields are fetched only in specific use cases

## Why unidirectional one-to-one is important

Unidirectional one-to-one is often the best default when only one side needs navigation in business logic.

In this module, the domain usually reads:

- `Employee -> Workstation`

but not:

- `Workstation -> Employee`

This avoids unnecessary object graph complexity and reduces accidental recursive serialization paths.

## Common business scenarios

- `Employee -> Workstation`
- `Vehicle -> RegistrationCertificate`
- `Order -> FraudScreeningDecision`
- `Subscription -> BillingProfile`
- `Patient -> InsuranceCard`

## Interview perspective: when to choose uni vs bi

Use **unidirectional** when:

- only one side is queried in code
- reverse traversal has no business value
- you want simpler APIs and fewer synchronization bugs

Use **bidirectional** when:

- both navigations are business-critical
- reverse lookups are frequent in domain logic
- you deliberately manage graph consistency on both sides

## Mapping in this module

### Owning side (`Employee`)

```java
@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, optional = false)
@JoinColumn(name = "workstation_id_fk", referencedColumnName = "id", nullable = false, unique = true)
private Workstation workstation;
```

- FK lives in `employees.workstation_id_fk`
- `unique = true` enforces one-to-one at DB level
- `orphanRemoval = true` removes replaced workstation rows
- `optional = false` enforces mandatory association at persistence level

### Dependent side (`Workstation`)

There is **no reverse field** to `Employee`. That is exactly what makes this mapping unidirectional.

## Exact table behavior

Expected schema shape:

```text
employees
  - id (PK)
  - employee_code
  - full_name
  - workstation_id_fk (FK -> workstations.id, UNIQUE, NOT NULL)

workstations
  - id (PK)
  - desk_code
  - building
  - floor_number
  - zone
```

No join table is created because one-to-one is modeled with `@JoinColumn`.

## SQL checks to run

```sql
-- 1) Verify schema columns
select column_name, is_nullable
from information_schema.columns
where table_name in ('employees', 'workstations')
order by table_name, ordinal_position;

-- 2) Verify uniqueness + FK constraints on owner table
select conname, contype
from pg_constraint
where conrelid = 'employees'::regclass;

-- 3) Verify rows and FK values
select id, employee_code, full_name, workstation_id_fk from employees;
select id, desk_code, building, floor_number, zone from workstations;
```

## Validation and API behavior

Service-level validation enforces:

- `employeeCode`, `fullName` are required with max length checks
- nested `workstation` is required
- `workstation.floorNumber` must be within an allowed range

Invalid payload returns HTTP `400` with structured error payload.

## Orphan lifecycle behavior

Because owner side uses `orphanRemoval = true`:

- Updating employee with a new workstation deletes the old workstation row
- Deleting employee cascades delete to the workstation row (`CascadeType.ALL`)

## Test taxonomy in this module

- **Controller integration**: API create path + validation failure
- **Relation integration**: owner-side persistence and replacement behavior
- **Orphan handling integration**: orphan deletion + cascade delete
- **In-memory relation tests**: prove the model is unidirectional and deterministic before persistence

## Runtime profile strategy

- `application.yaml` sets default profile to `postgres`
- `application-postgres.yml` is for normal app runs against PostgreSQL
- `application-test.yml` is for integration tests against H2
- Maven Surefire sets `spring.profiles.active=test` during test runs

## Run app with Docker PostgreSQL

Start PostgreSQL container from repository root:

```bash
cd /Users/sashank/Personal/projects/backend/traditional-db-systems
POSTGRES_USER=postgres POSTGRES_PASSWORD=password docker compose up -d postgres
```

Ensure DB exists for this module:

```bash
docker exec -it postgres psql -U postgres -c "CREATE DATABASE jdbc_one_to_one_uni_directional_relations;"
```

Run the module (defaults to `postgres` profile):

```bash
cd /Users/sashank/Personal/projects/backend/traditional-db-systems/jdbc-one-to-one-uni-directional-relation
DB_HOST=localhost DB_PORT=5432 DB_NAME=jdbc_one_to_one_uni_directional_relations DB_USERNAME=postgres DB_PASSWORD=password ./mvnw spring-boot:run
```

## Interview-ready quick answers

- **Where is ownership in one-to-one?** The side with `@JoinColumn`.
- **How is one-to-one enforced in DB?** FK + unique constraint.
- **Why unidirectional here?** Reverse navigation is not part of business behavior.
- **Why orphanRemoval?** Prevent stale dependent rows when relation is replaced.
- **Why DTOs in API?** Stable contracts and no entity leakage.

## Anti-patterns to avoid

- Modeling bidirectional mapping when reverse navigation is never used
- Exposing entities directly from controllers
- Replacing one-to-one child objects without orphan handling
- Ignoring non-null owner constraints during update flows

