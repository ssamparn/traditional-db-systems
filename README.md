# Traditional DB Systems

A multi-module Spring Boot workspace for learning relational database mappings with PostgreSQL.

## Tech baseline
- Java 25
- Spring Boot 4.1.0
- Maven multi-module build with a single parent (`traditional-db-systems/pom.xml`)

## Modules
- `jdbc-postgresql-entity-relations`: entity-first relation mapping examples
- `jdbc-postgresql-one-two-one-bi-directional-relation`: one-to-one bidirectional mapping
- `jdbc-postgresql-one-two-many-bi-directional-relation`: one-to-many and many-to-one bidirectional mapping
- `jdbc-postgresql-many-two-many-bi-directional-relation`: many-to-many bidirectional mapping

## Build and test
Run from repository root:

```bash
mvn clean test
```

Run a module with its Maven wrapper:

```bash
cd jdbc-postgresql-entity-relations
./mvnw spring-boot:run
```

## Why this structure
- Shared dependency and plugin management is centralized in one parent POM.
- Module POMs focus only on learning-specific dependencies and behavior.
- Version drift is minimized across modules, which keeps examples comparable.

