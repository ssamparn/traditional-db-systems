# Traditional DB Systems

A multi-module Spring Boot workspace for learning relational database mappings with PostgreSQL.

## Tech baseline
- Java 25
- Spring Boot 4.1.0
- Maven multi-module build with a single parent in `pom.xml`

## Learning goals
This repository is organized so you can study each association type in isolation and compare:

- **owning side vs inverse side**
- **foreign key vs join table**
- **entity graph synchronization**
- **DTO responses vs direct entity exposure**
- **safe cascade usage**

## Modules

### `jdbc-entity-relations`
Focus: a mixed model with `@OneToOne`, `@ManyToMany`, and a self-referencing category tree.

- `Book -> Photo`: one-to-one
- `Book <-> Author`: many-to-many
- `Book <-> Category`: many-to-many
- `Category -> Category`: parent/child hierarchy

### `jdbc-one-to-one-uni-directional-relation`
Focus: unidirectional one-to-one with strict owner-side control.

- `Employee -> Workstation`
- No reverse field from `Workstation` to `Employee`
- Useful when reverse navigation has no business value

### `jdbc-one-to-one-bi-directional-relation`
Focus: classic bidirectional one-to-one.

- `Organization -> Address`
- Owning side stores the foreign key

### `jdbc-one-to-many-bi-directional-relation`
Focus: canonical bidirectional one-to-many / many-to-one.

- `Role -> users`
- `User -> role`
- `User` is the owning side because it contains the foreign key column

### `jdbc-one-to-many-uni-directional-relation`
Focus: owner-only one-to-many where child has no reverse navigation.

- `Team -> members`
- No reverse field from `Member` to `Team`
- Useful when parent-side aggregate operations matter, but child-to-parent traversal does not

### `jdbc-many-to-many-bi-directional-relation`
Focus: bidirectional many-to-many with an explicit join table.

- `Student <-> Course`
- Join table: `students_courses`
- `Student` is the owning side because it declares `@JoinTable`

### `jdbc-many-to-many-with-join-entity`
Focus: many-to-many with **business meaning** represented as a first-class association entity.

- `Student -> Enrollment <- Course`
- `Enrollment` stores `enrolledAt`, `status`, `grade`, `createdBy`
- Unique association guard for `(student, course)`
- Demonstrates why real systems often promote pure join tables into explicit entities

### `jdbc-fetch-strategies-n-plus-1-query-problem`
Focus: fetch-planning and query explosion analysis on nested graphs.

- `Author -> books -> reviews`
- Demonstrates `LAZY` vs `EAGER` impact on SQL count
- Covers `N+1` and `N+1+N` patterns on one-parent and many-parent retrieval
- Shows mitigation with `JOIN FETCH` and `@EntityGraph`

## Quick mental model

### Owning side
The owning side is the side that writes the relationship to the database.

- `@ManyToOne` is typically the owning side of a one-to-many relationship.
- The side with `@JoinColumn` is the owning side in one-to-one.
- The side with `@JoinTable` is the owning side in many-to-many.
- If the relationship itself has behavior or attributes, model a dedicated join entity instead of `@ManyToMany`.

### Inverse side
The inverse side uses `mappedBy` and mirrors the association for navigation in Java.

### Rule of thumb for bidirectional relations
Always update **both sides** of the object graph in code.

Examples from this workspace:

- `Role.addUser(user)` also sets `user.setRole(role)`
- `Team.addMember(member)` updates only owner collection because the model is intentionally unidirectional
- `User.addRole(role)` also adds the user to `role.getUsers()`
- `Organization.setAddress(address)` also sets `address.setOrganization(organization)`

## Architecture guardrails (important)

These modules intentionally use a teaching-oriented architecture:

- Controllers and services expose `Mono`/`Flux` for API consistency across modules.
- Persistence uses blocking `JpaRepository` calls, executed on `Schedulers.boundedElastic()`.
- `@Transactional` boundaries are still applied in service methods; this is a learning simplification, not a production reactive persistence model.
- Request validation is handled in explicit service-layer validators to make invariants visible in code.
- Security config currently permits all requests and disables CSRF to reduce friction in learning flows.
- `ddl-auto: create-drop` is used for demo speed and should not be used in production.

## Build and test
Run from repository root:

```bash
mvn clean test
```

Run a single module:

```bash
cd jdbc-one-to-one-uni-directional-relation
./mvnw spring-boot:run
```

## Notes
- Shared dependency and plugin management is centralized in the root parent POM.
- The modules now prefer DTOs for HTTP responses where returning entities directly would create recursion or overexpose graph structure.
- If Maven dependency resolution fails in your environment, check your JDK truststore, proxy, and Maven certificate settings first.
