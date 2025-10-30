# User Service Database Migrations

## Running Migrations

Since this project uses JPA with `ddl-auto: validate`, migrations must be run manually.

### Option 1: Run via psql (Development)

```bash
# Connect to PostgreSQL
psql -h localhost -U oddiya_user -d oddiya

# Run the migration
\i services/user-service/src/main/resources/db/migration/V2__add_password_hash.sql
```

### Option 2: Run via Docker (if using docker-compose)

```bash
docker-compose exec postgres psql -U oddiya_user -d oddiya -f /migrations/V2__add_password_hash.sql
```

### Option 3: Temporary ddl-auto update (Development Only)

For local development, you can temporarily change `application.yml`:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update  # Changed from validate
```

This will auto-create the `password_hash` column on application startup.

**⚠️ WARNING: Never use `ddl-auto: update` in production!**

## Migration History

- **V1**: Initial schema (users table with email, name, provider, providerId)
- **V2**: Add password_hash column for email/password authentication

## Verifying Migration

After running the migration, verify with:

```sql
\d user_service.users

-- Should show password_hash column:
-- password_hash | character varying(255) |
```
