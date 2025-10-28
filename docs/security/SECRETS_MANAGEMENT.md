# Secrets Management Guide

This guide covers secure credential management for the Oddiya platform, including database credentials and AWS Secrets Manager integration.

## Overview

Oddiya services now use secure secrets management to prevent hardcoded credentials. All database passwords and sensitive configuration must be provided through environment variables or AWS Secrets Manager.

## Security Changes

### Removed Hard-coded Defaults

The following services have been updated to remove hard-coded password defaults:

- `video-service`
- `plan-service`
- `user-service`

**Before (Insecure):**

```yaml
datasource:
  username: ${DB_USER:oddiya_user}
  password: ${DB_PASSWORD:oddiya_password_dev}  # Hard-coded default!
```

**After (Secure):**

```yaml
config:
  import:
    - optional:aws-secretsmanager:oddiya/video-service/db

datasource:
  username: ${DB_USER}  # Must be provided via env/secrets
  password: ${DB_PASSWORD}  # Must be provided via env/secrets
```

## AWS Secrets Manager Integration

### Secret Structure

Each service has its own secret in AWS Secrets Manager:

```text
oddiya/video-service/db
oddiya/plan-service/db
oddiya/user-service/db
```

### Secret Format

Each secret should contain:

```json
{
  "DB_USER": "oddiya_user",
  "DB_PASSWORD": "your-strong-password-here"
}
```

### Creating Secrets

#### Using AWS CLI

```bash
# Create video-service DB secret
aws secretsmanager create-secret \
  --name oddiya/video-service/db \
  --description "Database credentials for video service" \
  --secret-string '{"DB_USER":"oddiya_user","DB_PASSWORD":"your-strong-password"}'

# Create plan-service DB secret
aws secretsmanager create-secret \
  --name oddiya/plan-service/db \
  --description "Database credentials for plan service" \
  --secret-string '{"DB_USER":"oddiya_user","DB_PASSWORD":"your-strong-password"}'

# Create user-service DB secret
aws secretsmanager create-secret \
  --name oddiya/user-service/db \
  --description "Database credentials for user service" \
  --secret-string '{"DB_USER":"oddiya_user","DB_PASSWORD":"your-strong-password"}'
```

#### Using AWS Console

1. Navigate to AWS Secrets Manager
2. Click "Store a new secret"
3. Select "Other type of secret"
4. Add key-value pairs:
   - `DB_USER`: `oddiya_user`
   - `DB_PASSWORD`: `your-strong-password`
5. Name the secret: `oddiya/{service-name}/db`

## Local Development

### Option 1: Environment Variables

For local development, you can still use environment variables:

```bash
export DB_USER=oddiya_user
export DB_PASSWORD=local_dev_password
```

### Option 2: Local Secrets Manager

If you want to test with Secrets Manager locally:

```bash
# Install AWS CLI and configure credentials
aws configure

# Ensure the secrets exist in your AWS account
aws secretsmanager describe-secret --secret-id oddiya/video-service/db
```

## Kubernetes/Docker Deployment

### Environment Variables

```yaml
apiVersion: apps/v1
kind: Deployment
spec:
  template:
    spec:
      containers:
      - name: video-service
        env:
        - name: DB_USER
          valueFrom:
            secretKeyRef:
              name: video-service-db
              key: username
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: video-service-db
              key: password
```

### AWS Secrets Manager (Recommended)

With proper IAM roles, the services will automatically fetch secrets from AWS Secrets Manager using the `spring.config.import` configuration.

Required IAM permissions:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "secretsmanager:GetSecretValue"
      ],
      "Resource": [
        "arn:aws:secretsmanager:*:*:secret:oddiya/*"
      ]
    }
  ]
}
```

## Migration Guide

### For Existing Deployments

1. **Create the secrets** in AWS Secrets Manager as shown above
2. **Update IAM roles** to allow access to the secrets
3. **Remove any hardcoded passwords** from environment files
4. **Restart services** to pick up the new configuration

### Environment Variables Required

All services now require these environment variables (no defaults):

- `DB_USER`
- `DB_PASSWORD`

Or AWS Secrets Manager access to the corresponding secrets.

## Security Best Practices

1. **Never commit** real passwords to git
2. **Use strong passwords** (20+ characters, mixed case, numbers, symbols)
3. **Rotate passwords** regularly
4. **Use different passwords** for each environment
5. **Monitor secret access** through AWS CloudTrail
6. **Use least privilege** IAM policies

## Troubleshooting

### Service Won't Start

If a service fails to start with credential errors:

1. Check that environment variables are set:

   ```bash
   echo $DB_USER
   echo $DB_PASSWORD
   ```

2. Verify AWS Secrets Manager access:

   ```bash
   aws secretsmanager get-secret-value --secret-id oddiya/video-service/db
   ```

3. Check IAM permissions for the service's role

4. Verify the secret exists and has the correct format

### Local Development Issues

For local development, the simplest approach is to use environment variables:

```bash
# In your .env.local file
DB_USER=oddiya_user
DB_PASSWORD=local_dev_password

# Or export directly
export DB_USER=oddiya_user
export DB_PASSWORD=local_dev_password
```

## References

- [Spring Cloud AWS Secrets Manager](https://docs.awspring.io/spring-cloud-aws/docs/current/reference/html/index.html#secrets-manager)
- [AWS Secrets Manager Best Practices](https://docs.aws.amazon.com/secretsmanager/latest/userguide/best-practices.html)