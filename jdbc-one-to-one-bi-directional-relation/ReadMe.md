# One-to-One Learning Guide

This module demonstrates a bidirectional one-to-one relation between:

- `Organization`
- `Address`

## What to learn here

### Owning side
`Organization` is the owning side because it declares:

- `@OneToOne`
- `@JoinColumn(name = "address_id_fk")`

That means the foreign key is stored in the `organizations` table.

### Inverse side
`Address.organization` is the inverse side because it uses:

- `mappedBy = "address"`

## Expected table shape

```text
organizations
  - id
  - name
  - org_id
  - address_id_fk

addresses
  - id
  - building
  - street
  - city
  - state
  - country
  - zipcode
```

## Important observation
There is **no join table** in the current design because this example uses `@JoinColumn`, not `@JoinTable`.

So this query is **not expected** to return data in the current mapping:

```sql
select * from organization_address;
```

Instead, inspect the foreign key on `organizations.address_id_fk`.

## Object graph rule
This module now synchronizes both sides through entity setters:

- `organization.setAddress(address)`
- `address.setOrganization(organization)`

That helps keep the in-memory object graph aligned with the database association.

## DTO usage
The web layer returns:

- `OrganizationResponse`
- `AddressResponse`

So the API is cycle-safe and easier to reason about than returning entities directly.

## Quick SQL checks

```sql
select * from organizations;
select * from addresses;
select id, name, org_id, address_id_fk from organizations;
```

## References
- DZone: https://dzone.com/articles/introduction-to-spring-data-jpa-part-6-bidirection
- Techno Town Techie: https://www.youtube.com/watch?v=N7nLUQMmjxs
