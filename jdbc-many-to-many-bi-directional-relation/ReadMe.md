# Many-to-Many Bidirectional Relation

This module demonstrates a production-style **bidirectional many-to-many** model between:

- `Student` (owning side)
- `Course` (inverse side)

It is intentionally designed for learning join-table ownership, bidirectional graph synchronization, relation lifecycle operations, and DTO-safe APIs.

## Why this relation matters (architect point of view)

Use many-to-many when both domains independently exist and can be linked in multiple combinations.

In this module:

- one `Student` can enroll in multiple `Course`
- one `Course` can contain multiple `Student`

## When to use bidirectional many-to-many?

Use bidirectional mapping when both traversals are valuable in business logic:

- `Student -> courses` for enrollment and planning workflows
- `Course -> students` for roster and scheduling workflows

If reverse traversal is never used, prefer unidirectional mapping to reduce graph complexity.

## Common business scenarios

- `Student <-> Course`
- `Student <-> Course`
- `Doctor <-> Specialty`
- `Employee <-> Skill`

## Interview perspective: when to choose uni vs bi

Use **bidirectional** when:

- both side traversals are domain requirements
- audits/reporting need reverse lookup
- helper methods can consistently maintain both sides

Use **unidirectional** when:

- only one direction is queried by business flows
- reverse side adds complexity without value

## Mapping in this module

### Owning side (`Student`)

```java
@ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
@JoinTable(
    name = "students_courses",
    joinColumns = @JoinColumn(name = "student_id_fk"),
    inverseJoinColumns = @JoinColumn(name = "course_id_fk")
)
private List<Course> courses;
```

- owning side writes join-table rows
- relation updates should be driven through owner helpers

### Inverse side (`Course`)

```java
@ManyToMany(fetch = FetchType.LAZY, mappedBy = "courses")
private List<Student> students;
```

### Graph synchronization rule

Entity helpers ensure both sides stay in-sync in memory:

- `student.addCourse(course)` updates `course.students`
- `course.addStudent(student)` updates `student.courses`

## API contract

### Student APIs

- `POST /api/v1/student/create`
- `GET /api/v1/student/get/all`
- `GET /api/v1/student/get/{studentId}`
- `PUT /api/v1/student/update/{studentId}`
- `DELETE /api/v1/student/delete/{studentId}`

### Course APIs

- `POST /api/v1/course/create`
- `GET /api/v1/course/get/all`
- `GET /api/v1/course/get/{courseId}`
- `PUT /api/v1/course/update/{courseId}`
- `DELETE /api/v1/course/delete/{courseId}`

## Validation and error contract

Service validators enforce required fields and length constraints:

- `StudentRequestValidator`
- `CourseRequestValidator`

Failures are emitted through `GlobalExceptionHandler`:

- `400` validation/argument errors
- `404` missing student/course
- `409` DB uniqueness/conflict errors

## Exact table behavior

Expected schema shape:

```text
students
  - id (PK)
  - first_name (NOT NULL)
  - last_name (NOT NULL)
  - mobile (NOT NULL)
  - email (UNIQUE, NOT NULL)

courses
  - id (PK)
  - name (UNIQUE, NOT NULL)
  - description (NOT NULL)

students_courses
  - student_id_fk (FK -> students.id)
  - course_id_fk (FK -> courses.id)
```

## SQL checks to run

```sql
-- 1) Verify base entities
select id, first_name, last_name, email from students;
select id, name, description from courses;

-- 2) Verify join-table rows
select student_id_fk, course_id_fk from students_courses;

-- 3) Verify constraints
select conname, contype
from pg_constraint
where conrelid in ('students'::regclass, 'courses'::regclass, 'students_courses'::regclass);
```

## Test taxonomy in this module

- **Controller integration**: create/update/delete + validation and not-found paths
- **Relation integration**: owning/inverse side synchronization before flush and after persist
- **Association lifecycle integration**: deleting/rewiring links should preserve independent entities
- **In-memory relation tests**: helper methods keep object graph consistent before persistence

## Runtime profile strategy

- `application-postgres.yml` sets default profile to `postgres`
- `application-postgres.yml` is runtime PostgreSQL config
- `application-test.yml` is H2 integration-test config
- Maven Surefire sets `spring.profiles.active=test` during tests

## Run app with Docker PostgreSQL

```bash
cd /Users/sashank/Personal/projects/backend/traditional-db-systems
POSTGRES_USER=postgres POSTGRES_PASSWORD=password docker compose up -d postgres
```

```bash
docker exec -it postgres psql -U postgres -c "CREATE DATABASE jdbc_many_to_many_bi_directional_relations;"
```

```bash
cd /Users/sashank/Personal/projects/backend/traditional-db-systems/jdbc-many-to-many-bi-directional-relation
DB_HOST=localhost DB_PORT=5432 DB_NAME=jdbc_many_to_many_bi_directional_relations DB_USERNAME=postgres DB_PASSWORD=password ./mvnw spring-boot:run
```

## Interview-ready quick answers

- **Why join table in many-to-many?** Neither side can hold a single FK without losing multiplicity.
- **Who owns bidirectional many-to-many?** Side with `@JoinTable` (`Student`).
- **Why sync helpers?** Prevent half-linked in-memory graphs before persistence.
- **Why DTOs?** Avoid recursive entity serialization and keep API contracts stable.

## Anti-patterns to avoid

- Updating only one side of the relation and assuming JPA will infer the rest.
- Using `CascadeType.REMOVE` in many-to-many and accidentally deleting shared entities.
- Returning entities directly from controllers.
- Switching all relations to eager fetch to hide lazy-loading design issues.
