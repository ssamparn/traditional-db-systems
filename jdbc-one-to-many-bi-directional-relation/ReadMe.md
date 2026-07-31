# One-to-Many / Many-to-One Learning Guide

This module demonstrates the canonical bidirectional relationship between:

- `Role -> users` using `@OneToMany(mappedBy = "role")`
- `User -> role` using `@ManyToOne` + `@JoinColumn(name = "role_id_fk")`

## What to learn here

### Owning side
`User` is the owning side because it stores the foreign key column:

- `role_id_fk`

That means database updates to the relationship are driven by `User.role`.

### Inverse side
`Role.users` is the inverse side because it uses:

- `mappedBy = "role"`

This side is useful for navigation in Java but does not own the foreign key.

## Expected table shape

```text
roles
  - id
  - name
  - description

users
  - id
  - first_name
  - last_name
  - mobile
  - email
  - role_id_fk
```

## Object graph rule
When creating a role and its users, always update both sides in memory:

- add the `User` to `Role.users`
- set `User.role`

This module now does that through `Role.addUser(user)`.

## DTOs in this module
This module already returns DTOs instead of JPA entities directly:

- `RoleResponse`
- `UserResponse`
- `RoleUserResponse`

That keeps the API safe from recursion and makes the SQL join example easier to understand.

## Quick SQL checks

```sql
select * from roles;
select * from users;
select id, first_name, last_name, role_id_fk from users;
```

## Observe this behavior

1. Create a role with nested users.
2. Query `users` and inspect `role_id_fk`.
3. Notice there is no separate join table.
4. Compare this to the many-to-many module where a join table is required.

## Architecture guardrails

- Keep owner-side updates (`User.role`) as the source of truth for FK changes.
- Encapsulate graph synchronization in helper methods (`Role.addUser`).
- Keep transaction boundaries in service layer (`@Transactional` on writes).
- Return DTOs at API boundaries and avoid direct entity serialization.
- Treat open security and `create-drop` schema settings as local-learning defaults only.

## References
- DZone: https://dzone.com/articles/introduction-to-spring-data-jpa-part-4-bidirection
- Java Techie: https://www.youtube.com/watch?v=8qhaDBCJh6I&t=1256s
- Techno Town Techie: https://www.youtube.com/watch?v=N7nLUQMmjxs
