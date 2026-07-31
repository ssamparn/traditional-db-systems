# Entity Relations Learning Guide

This module demonstrates a mixed model of JPA relations:

- `Book -> Photo`: `@OneToOne`
- `Book <-> Author`: `@ManyToMany`
- `Book <-> Category`: `@ManyToMany`
- `Category -> Category`: self-referencing parent/child hierarchy

## What to learn here

### 1. One-to-one
`Book` is the owning side of the `Book <-> Photo` relationship because `Book` declares:

- `@OneToOne`
- `@JoinColumn(name = "photo_id")`

That means the foreign key is stored in the `books` table.

### 2. Many-to-many
`Book` is the owning side for both many-to-many relations because it declares the join tables:

- `books_authors`
- `books_categories`

`Author` and `Category` are inverse sides using `mappedBy`.

### 3. Self-referencing hierarchy
`Category.parent` and `Category.children` show how a tree can be modeled inside one table.

## Expected table shape

```text
books
  - book_id
  - photo_id
  - title
  - isbn
  - total_pages
  - rating
  - published_date

photo
  - photo_id
  - small_url
  - medium_url
  - large_url

author
  - author_id
  - first_name
  - last_name
  - birth_date

categories
  - category_id
  - category
  - parent_id

books_authors
  - book_id
  - author_id

books_categories
  - book_id
  - category_id
```

## Safe learning notes

- Entities no longer use Lombok `@Data`; this avoids recursive `toString`/`equals` issues on bidirectional graphs.
- `Book` now exposes helper methods like `addAuthor(...)`, `addCategory(...)`, and `setPhoto(...)` so both sides of relations stay in sync in memory.
- The HTTP layer returns `BookResponse` instead of returning a JPA entity graph directly.

## Quick SQL checks

```sql
select * from books;
select * from photo;
select * from author;
select * from categories;
select * from books_authors;
select * from books_categories;
```

## Reference
https://medium.com/huawei-developers/database-relationships-in-spring-data-jpa-8d7181f50f60
