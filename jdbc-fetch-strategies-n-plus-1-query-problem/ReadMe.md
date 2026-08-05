# JDBC Fetch Strategies and N+1 Query Problem

This module demonstrates production-relevant fetch planning with:

- `Author`
- `Book`
- `Review`

It is intentionally designed for learning query fan-out, fetch-plan trade-offs, and how to explain ORM performance decisions in interviews and system design rounds.

## Why this relation matters (architect point of view)

At scale, correctness is not enough. The same domain model can be:

- functionally correct
- operationally unstable

depending on fetch strategy.

Typical system-design failure pattern:

1. Endpoint works with 20 rows in dev.
2. Data grows to thousands of parents.
3. Lazy traversal triggers `1 + N` or `1 + N + N` query chains.
4. DB CPU/IO rises, p95 latency spikes, autoscaling hides root cause.

This module makes that failure mode visible through explicit APIs, query counts, and integration tests.

## When should you care about fetch planning?

You should treat fetch strategy as a design concern when:

- list endpoints return aggregate graphs (`authors -> books -> reviews`)
- APIs are latency-sensitive (customer search, dashboards, feeds)
- data cardinality is expected to grow unpredictably
- DB is a shared dependency for multiple services

## Common business scenarios

- Catalog service: `Category -> Products -> Prices`
- Education platform: `Course -> Modules -> Lessons`
- Marketplace: `Seller -> Listings -> Offers`
- Publishing: `Author -> Books -> Reviews`

All can look correct in code but fail under load if fetch plans are not explicit.

## Interview perspective: LAZY vs EAGER vs fetch plans

Use **`LAZY (By default)`** when:

- large child collections are not needed in every use case
- you can control loading at query/use-case boundaries

Use **`EAGER selectively`** when:

- association is tiny and always needed
- you can prove no list-path over-fetching impact

Use **`JOIN FETCH` / `@EntityGraph`** when:

- endpoint needs full graph in one use case
- you want predictable query count for a read path

## Fetch mapping and design in this module

### Entity mapping choices

- `Author.books` -> `LAZY`
- `Book.reviews` -> `LAZY`
- `Book.author` -> `EAGER`

This intentionally demonstrates:

- lazy query explosion on deep traversal
- eager over-fetch risk on many-to-one
- mitigation patterns with `JOIN FETCH` and `EntityGraph`

### Repository fetch-plan methods

This module includes both strategies:

- Lazy baseline queries (`findAllByOrderByIdAsc`, `findById`)
- `JOIN FETCH` queries for nested graph loading
- `@EntityGraph(attributePaths = {"books", "books.reviews"})`

## Exact table behavior

Expected schema shape:

```text
authors
  - id (PK)
  - first_name
  - last_name
  - email (UNIQUE)

books
  - id (PK)
  - title
  - isbn (UNIQUE)
  - description
  - published_year
  - author_id_fk (FK -> authors.id)

reviews
  - id (PK)
  - rating
  - comment
  - reviewer_name
  - book_id_fk (FK -> books.id)
```

## API surface

Base path: `/api/v1`

### Author

- `POST /author/create`
- `GET /author/get/{authorId}`
- `GET /author/get/all`
- `PUT /author/update/{authorId}`
- `DELETE /author/delete/{authorId}`

### Book

- `POST /book/create`
- `GET /book/get/{bookId}`
- `GET /book/get/all`
- `PUT /book/update/{bookId}`
- `DELETE /book/delete/{bookId}`

### Review

- `POST /review/create`
- `GET /review/get/{reviewId}`
- `GET /review/get/by-book/{bookId}`
- `PUT /review/update/{reviewId}`
- `DELETE /review/delete/{reviewId}`

### Fetch strategy demos

- `GET /fetch-demo/author/{authorId}/lazy`
- `GET /fetch-demo/author/{authorId}/join-fetch`
- `GET /fetch-demo/author/{authorId}/entity-graph`
- `GET /fetch-demo/authors/lazy`
- `GET /fetch-demo/authors/join-fetch`
- `GET /fetch-demo/authors/entity-graph`
- `GET /fetch-demo/books/eager`

Each fetch-demo response returns:

- `scenario`
- `queryCount`
- `authorCount`, `bookCount`, `reviewCount`
- `loadedAssociationCount`
- `notes`

## Query explosion walkthrough

### One parent with children and grandchildren

`GET /fetch-demo/author/{id}/lazy`

Typical pattern:

- 1 query for author
- +1 query for books
- +N queries for reviews per book

### Many parents with children and grandchildren

`GET /fetch-demo/authors/lazy`

Typical pattern:

- 1 query for authors
- +N queries for each author's books
- +N queries for each book's reviews

This is the practical `N+1+N` query explosion.

### Mitigation A: join fetch

`GET /fetch-demo/authors/join-fetch`

- lower query fan-out
- risk of row multiplication/cartesian effects
- requires careful `distinct` usage
- in this module, the optimized path focuses on preloading `Author -> Book` so the demo stays stable with nested `Review` bags

### Mitigation B: entity graph

`GET /fetch-demo/authors/entity-graph`

- cleaner method signatures than many custom fetch-join JPQLs
- reusable fetch plans for multiple use cases
- in this module, the entity graph is used to prefetch the parent->book layer while keeping review loading lazy for the comparison demo

### EAGER caveat scenario

`GET /fetch-demo/books/eager`

- demonstrates that eager many-to-one can load data even when caller does not need it

## SQL checks to run

```sql
-- 1) Check row counts in each table
select count(*) as authors from authors;
select count(*) as books from books;
select count(*) as reviews from reviews;

-- 2) Verify FK distribution
select author_id_fk, count(*)
from books
group by author_id_fk
order by author_id_fk;

select book_id_fk, count(*)
from reviews
group by book_id_fk
order by book_id_fk;

-- 3) Quick join sanity check
select a.id as author_id, b.id as book_id, r.id as review_id
from authors a
left join books b on b.author_id_fk = a.id
left join reviews r on r.book_id_fk = b.id
order by a.id, b.id, r.id;
```

## SQL logs and statistics

This module enables:

- `spring.jpa.show-sql=true`
- `hibernate.generate_statistics=true`
- `logging.level.org.hibernate.SQL=DEBUG`
- `logging.level.org.hibernate.orm.jdbc.bind=TRACE`

Use logs plus `queryCount` from fetch-demo endpoints to explain performance behavior clearly in interviews.

## API behavior and validation

Service-level validation enforces:

- `Author`: required `firstName`, `lastName`, `email` with max lengths; email must contain `@`
- `Book`: required `authorId`, `title`, `isbn`; `publishedYear >= 1000` when present
- `Review`: required `bookId`, `comment`, `reviewerName`; `rating` in `[1..5]`

Invalid payload returns HTTP `400` with structured error payload from `GlobalExceptionHandler`.

## Lifecycle behavior

Current model uses aggregate-friendly cascading rules:

- deleting `Author` cascades to `Book` and `Review`
- deleting `Book` removes its `Review` children
- deleting `Review` keeps `Book` and `Author`

This is intentionally exercised in integration tests.

## Test taxonomy in this module

- **Controller integration**: `AuthorControllerIntegrationTest`, `BookControllerIntegrationTest`, `ReviewControllerIntegrationTest`
- **Fetch behavior integration**: `FetchStrategyControllerIntegrationTest` (lazy vs join-fetch vs entity-graph query counts)
- **Lifecycle integration**: `AuthorBookReviewLifecycleIntegrationTest`

## Runtime profile strategy

- `application.yaml` sets default profile to `postgres`
- `application-postgres.yaml` targets PostgreSQL with SQL/statistics logging
- `application-test.yaml` targets H2 for integration tests with the same observability flags
- Maven Surefire sets `spring.profiles.active=test`

## Run app with Docker PostgreSQL



## Interview-ready explanation (short version)

- **Why LAZY default?** Prevent accidental over-fetch and let use case decide graph shape.
- **When does N+1 happen?** Parent rows loaded first, then each child collection lazily.
- **How to fix N+1?** `JOIN FETCH`, `EntityGraph`, projections, pagination, or query redesign.
- **Is EAGER safe?** Only for small always-needed associations; otherwise it can hide expensive joins.
- **How to prove optimization?** Show before/after query counts and latency in integration tests.

## Anti-patterns to avoid

- Marking most associations `EAGER` globally.
- Returning JPA entities directly from controllers (uncontrolled graph loading).
- Adding fetch joins blindly on paginated endpoints.
- Optimizing without measuring query count and response time.
- Ignoring test coverage for list endpoints with realistic graph size.
- Keeping teaching-only settings (`permitAll`, `create-drop`) in production deployments.

