# Many-to-Many Learning Guide

This module demonstrates a bidirectional many-to-many relation between:

- `User`
- `Role`

## What to learn here

### Owning side
`User` is the owning side because it declares:

- `@ManyToMany`
- `@JoinTable(name = "users_roles")`

That means the relationship is persisted through the join table declared on `User`.

### Inverse side
`Role.users` is the inverse side because it uses:

- `mappedBy = "roles"`

## Expected table shape

```text
users
  - id
  - first_name
  - last_name
  - mobile
  - email

roles
  - id
  - name
  - description

users_roles
  - user_id_fk
  - role_id_fk
```

## Why a join table is required
A many-to-many relation cannot be represented by a single foreign key column on either side without losing multiplicity.

So JPA creates and manages a join table:

- one user can have many roles
- one role can belong to many users

## Object graph rule
This module now keeps both sides synchronized:

- `user.addRole(role)` also updates `role.users`
- `role.addUser(user)` also updates `user.roles`

This matters when you inspect the graph in memory before saving.

## DTO usage
The API now returns cycle-safe DTOs instead of JPA entities:

- `UserResponse`
- `RoleResponse`
- `UserSummaryResponse`
- `RoleSummaryResponse`

This avoids recursion and makes the response payload easier to understand.

## Quick SQL checks

```sql
select * from users;
select * from roles;
select * from users_roles;
```

## Suggested experiments

1. Create a few roles.
2. Create a user with multiple `roleIds`.
3. Query `users_roles` and inspect how one user maps to multiple role rows.
4. Update the user with a different set of `roleIds`.
5. Observe how the join table changes.

