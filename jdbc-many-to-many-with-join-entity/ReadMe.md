# Many-to-Many with Join Entity

This module demonstrates production-style modeling of many-to-many with an explicit association entity:

- `Student`
- `Course`
- `Enrollment`

Unlike a pure `@ManyToMany`, this design treats the relationship row as a first-class domain object.

## Why this relation matters (architect point of view)

In real systems, relationships often carry business meaning:

- who created the link (`createdBy`)
- when the link was created (`enrolledAt`)
- current business state (`status`)
- measurable outcome (`grade`)

If you keep only a technical join table, these attributes either get lost or leak into incorrect entities.

## Pure join table vs business join entity

### 1) Pure join table (`@ManyToMany`)

- table stores only two FKs
- association has no independent lifecycle
- suitable for simple tag-like relations

### 2) Join table with business meaning (this module)

- association becomes explicit entity (`Enrollment`)
- can be created/updated/deleted independently
- can enforce business rules on the link itself
- supports richer querying by association fields

## When to choose a join entity

Choose a join entity when any of these are true:

- relationship needs metadata (`status`, `grade`, timestamps)
- relationship needs auditing or ownership (`createdBy`)
- relationship has workflows (approve/drop/complete/retry)
- relationship requires dedicated endpoints and history

## Common business scenarios

- `Student <-> Course` with enrollment status and grade
- `User <-> Project` with role and allocation percentage
- `Doctor <-> Hospital` with shift and privileges
- `Employee <-> Skill` with proficiency and certification date

## Mapping in this module

- `Student` has `@OneToMany(mappedBy = "student")`
- `Course` has `@OneToMany(mappedBy = "course")`
- `Enrollment` holds two `@ManyToOne` links with business columns

`Enrollment` columns:

- `student_id_fk`
- `course_id_fk`
- `enrolled_at`
- `status`
- `grade`
- `created_by`
- unique key on `(student_id_fk, course_id_fk)`

## API contract

Base path: `/api/v1`

### Student APIs

- `POST /student/create`
- `GET /student/get/all`
- `GET /student/get/{studentId}`
- `PUT /student/update/{studentId}`
- `DELETE /student/delete/{studentId}`

### Course APIs

- `POST /course/create`
- `GET /course/get/all`
- `GET /course/get/{courseId}`
- `PUT /course/update/{courseId}`
- `DELETE /course/delete/{courseId}`

### Enrollment APIs

- `POST /enrollment/create`
- `GET /enrollment/get/all`
- `GET /enrollment/get/{enrollmentId}`
- `GET /enrollment/get/by-student/{studentId}`
- `GET /enrollment/get/by-course/{courseId}`
- `PUT /enrollment/update/{enrollmentId}`
- `DELETE /enrollment/delete/{enrollmentId}`

## Validation and error contract

- `400` invalid payload/domain rule violations (including duplicate enrollment attempts)
- `404` student/course/enrollment not found
- `409` database uniqueness or integrity violations

Global error handling is centralized in `web.exception.GlobalExceptionHandler`.

## Delete lifecycle behavior

- deleting a `Student` removes related `Enrollment` rows and keeps `Course`
- deleting a `Course` removes related `Enrollment` rows and keeps `Student`
- deleting an `Enrollment` removes only the link and keeps both aggregates

Delete enrollment response is built before unlinking to keep stable response payload.

## Exact table behavior

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

enrollments
  - id (PK)
  - student_id_fk (FK -> students.id)
  - course_id_fk (FK -> courses.id)
  - enrolled_at (NOT NULL)
  - status (NOT NULL)
  - grade (NULLABLE)
  - created_by (NOT NULL)
  - uk_enrollment_student_course (UNIQUE student_id_fk, course_id_fk)
```

## SQL checks to run

```sql
-- 1) Verify base entities
select id, first_name, last_name, email from students;
select id, name, description from courses;

-- 2) Verify association entity rows
select id, student_id_fk, course_id_fk, status, grade, created_by, enrolled_at
from enrollments;

-- 3) Verify constraints
select conname, contype
from pg_constraint
where conrelid in ('students'::regclass, 'courses'::regclass, 'enrollments'::regclass);
```

## Test taxonomy in this module

- **Controller integration**: create/get/update/delete + validation and conflict paths
- **Enrollment workflow integration**: status/grade updates and duplicate guards
- **Lifecycle integration**: delete cascade semantics across student/course/enrollment boundaries
- **Context smoke**: application starts with test profile

Key test classes:

- `StudentControllerIntegrationTest`
- `CourseControllerIntegrationTest`
- `EnrollmentControllerIntegrationTest`
- `EnrollmentLifecycleIntegrationTest`

## Runtime profile strategy

- `application.yaml` sets default profile to `postgres`
- `application-postgres.yaml` contains runtime PostgreSQL config
- `application-test.yaml` contains H2 test config
- Maven Surefire sets `spring.profiles.active=test`

## Interview-ready quick answers

- **Why not plain `@ManyToMany` here?** Because relationship fields (`status`, `grade`, `createdBy`, `enrolledAt`) are domain data, not incidental metadata.
- **Where should business rules live?** In `EnrollmentService`, since the rule belongs to the association lifecycle.
- **How do you prevent duplicate enrollment?** Unique DB constraint + service-level pre-check on `(studentId, courseId)`.
- **Why DTOs instead of entity exposure?** Keeps API stable and avoids recursive object graph serialization.
- **What is the aggregate boundary?** `Student` and `Course` remain independent aggregates; `Enrollment` is their business link.

## Anti-patterns to avoid

- Hiding business fields in an invisible join table and still calling it "simple many-to-many".
- Keeping `@ManyToMany` while separately persisting metadata in another table without cohesive lifecycle management.
- Treating enrollment delete as student/course delete.
- Relying only on service checks without DB uniqueness constraints.
- Returning JPA entities directly from controllers.

## Run commands

```bash
cd jdbc-many-to-many-with-join-entity
./mvnw spring-boot:run
```

Run tests for this module:

```bash
cd jdbc-many-to-many-with-join-entity
./mvnw test
```

