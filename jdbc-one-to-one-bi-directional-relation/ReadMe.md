# One-to-One Bidirectional Relation

This module demonstrates a production-style **bidirectional one-to-one** model between:

- `Organization` (owning side)
- `Address` (inverse side)

It is intentionally designed for learning JPA mapping semantics, object-graph consistency, and API-safe modeling.

## Why this relation matters (architect point of view)

Use one-to-one when two aggregates have a strict **cardinality of exactly one related row** and you still want separate tables.

Typical reasons to split into two tables:

- **Bounded context separation**: core entity (`Organization`) vs optional/volatile details (`Address`)
- **Security/governance**: PII fields can be isolated in a separate table with tighter controls
- **Evolution and ownership**: independent schema evolution without bloating a single table
- **Performance tuning**: fetch only what is needed by use case (especially with larger dependent payloads)

## When to use bidirectional one-to-one?

Choose bidirectional one-to-one only when both navigations are part of real business behavior:

- `Organization -> Address` for normal read/use cases
- `Address -> Organization` for reverse lookups, reporting, or domain rules

If reverse navigation is never used, prefer unidirectional mapping for simpler code.

## Common business scenarios

- `User <-> UserProfile`
- `Employee <-> EmployeeBadge`
- `Organization <-> Address`
- `Order <-> PaymentDetail`
- `Device <-> DeviceConfiguration`

## Setter design rationale (uni vs bi)

### Why bidirectional entities use custom setters

In bidirectional models (for example, `Organization <-> Address`), custom setters help keep both sides of the object graph consistent in memory:

- `Organization.setAddress(...)` updates `Address.organization`
- `Address.setOrganization(...)` updates `Organization.address`

Without this synchronization, JPA can persist surprising states and business logic/tests may observe half-linked objects.

Rule of thumb:

- bidirectional -> synchronization helpers strongly recommended
- unidirectional -> simple setter is typically fine

## Mapping in this module

### Owning side (`Organization`)

```java
@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, optional = false)
@JoinColumn(name = "address_id_fk", referencedColumnName = "id", nullable = false, unique = true)
private Address address;
```

- Foreign key lives in `organizations.address_id_fk`
- `unique = true` enforces one-to-one at DB level
- `orphanRemoval = true` deletes replaced child rows (teaching orphan lifecycle)

### Inverse side (`Address`)

```java
@OneToOne(mappedBy = "address")
private Organization organization;
```

- No FK column here
- `mappedBy` points to owner field name (`Organization.address`)

### Object-graph synchronization

Custom setters on both entities keep both sides aligned:

- `Organization#setAddress(...)`
- `Address#setOrganization(...)`

This prevents half-linked objects and makes persistence behavior deterministic.

## Exact table behavior

Expected schema shape:

```text
organizations
  - id (PK)
  - name
  - org_id
  - address_id_fk (FK -> addresses.id, UNIQUE, NOT NULL)

addresses
  - id (PK)
  - building
  - street
  - city
  - state
  - country
  - zipcode
```

No join table is created because one-to-one is modeled with `@JoinColumn`.

## SQL checks to run

```sql
-- 1) Verify schema columns
select column_name, is_nullable
from information_schema.columns
where table_name in ('organizations', 'addresses')
order by table_name, ordinal_position;

-- 2) Verify one-to-one uniqueness constraint from owning side
select conname, contype
from pg_constraint
where conrelid = 'organizations'::regclass;

-- 3) Verify stored rows and foreign key values
select id, name, org_id, address_id_fk from organizations;
select id, building, street, city, state, country, zipcode from addresses;
```

## API behavior and Validation

Input validation is enforced in the service layer before mapping/persistence:

- `organizationName`, `organizationId` required
- nested `address` required
- address fields required with size limits

Invalid payload returns HTTP `400` with structured error details.

## Orphan handling behavior

Because owner uses `orphanRemoval = true`:

- Updating an organization with a **new address object** removes the old address row
- Deleting organization cascades delete to its address (through `CascadeType.ALL`)

## Integration tests included

- request validation test (`400` on invalid payload)
- create/read response path test
- orphan replacement test (old address row is deleted after update)

## Runtime profile strategy

- `application-postgres.yml` is the default runtime profile (`spring.profiles.default=postgres`).
- `application-test.yml` is dedicated to integration tests and uses in-memory H2.
- Maven Surefire sets `spring.profiles.active=test`, so `mvn test` runs against H2 automatically.

## Run app with Docker PostgreSQL

Start PostgreSQL container from repository root:

Start PostgreSQL container from repository:

```bash
$ docker compose -f docker-compose.yml up
```

## Interview-ready explanation (short version)

- **What is owner in 1-1?** The side with `@JoinColumn`; it controls FK updates.
- **What does `mappedBy` do?** Marks the inverse side; no separate FK column created there.
- **Why bidirectional?** Needed when both navigations are business-relevant.
- **Why `orphanRemoval`?** Ensures replaced detached child records do not remain as stale rows.
- **Why DTOs?** Avoid recursive serialization and keep API contracts stable.

## Anti-patterns to avoid

- Returning entities directly from controllers in bidirectional graphs
- Using bidirectional mapping when only one direction is ever read
- Forgetting sync helper methods, causing inconsistent object state
- Replacing one-to-one children without orphan handling
- These settings are appropriate for local learning and tests, but should be replaced with real authn/authz and migration-based schema management in production.
