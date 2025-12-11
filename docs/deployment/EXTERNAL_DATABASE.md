# External Database Setup Guide

This guide explains how to configure OpsFinder to use an external PostgreSQL database instead of the local Docker container.

## Use Cases

- **Cloud Databases**: AWS RDS, Azure Database for PostgreSQL, Google Cloud SQL
- **Managed Services**: DigitalOcean Managed Databases, Heroku Postgres
- **Existing Infrastructure**: Company-wide database servers
- **High Availability**: Multi-region database setups
- **Compliance**: Databases with specific security/compliance requirements

## Requirements

- PostgreSQL 16+ (recommended) or PostgreSQL 15+
- Network access from your deployment server to the database
- Database credentials (host, port, username, password)
- Pre-created database (default name: `opsfinder`)

## Quick Start

### 1. Configure Environment Variables

Edit your `.env` file:

```bash
# Database Configuration
DB_NAME=opsfinder
DB_USER=your_username
DB_PASSWORD=your_secure_password

# External Database Settings
DB_HOST=your-db-host.example.com
DB_PORT=5432
```

**Important**: When `DB_HOST` is set, the local database container will NOT be deployed.

### 2. Deploy Application

```bash
# Deploy only backend + frontend (no local database)
./scripts/deploy/deploy.sh
```

The script automatically detects `DB_HOST` and skips database deployment.

## Environment Variable Reference

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `DB_HOST` | Yes* | `database` | External database hostname or IP |
| `DB_PORT` | No | `5432` | Database port |
| `DB_NAME` | Yes | `opsfinder` | Database name |
| `DB_USER` | Yes | `opsuser` | Database username |
| `DB_PASSWORD` | Yes | - | Database password |

*Required for external database setup

## Cloud Provider Examples

### AWS RDS PostgreSQL

```bash
# .env configuration
DB_HOST=mydb.abc123.us-east-1.rds.amazonaws.com
DB_PORT=5432
DB_NAME=opsfinder
DB_USER=postgres
DB_PASSWORD=your_rds_password
```

### Azure Database for PostgreSQL

```bash
# .env configuration
DB_HOST=myserver.postgres.database.azure.com
DB_PORT=5432
DB_NAME=opsfinder
DB_USER=myadmin@myserver
DB_PASSWORD=your_azure_password
```

### Google Cloud SQL

```bash
# .env configuration
DB_HOST=10.0.0.3  # Private IP
DB_PORT=5432
DB_NAME=opsfinder
DB_USER=postgres
DB_PASSWORD=your_cloudsql_password
```

## Network Configuration

### Test Database Connectivity

```bash
# Test connection
psql -h your-db-host -U your_user -d opsfinder

# Test port accessibility
telnet your-db-host 5432
```

## Deployment Scenarios

### Scenario 1: Fresh Deployment with External Database

```bash
# 1. Set up external database
# 2. Configure .env with DB_HOST
# 3. Deploy application
./scripts/deploy/deploy.sh
```

### Scenario 2: Migrate from Local to External Database

```bash
# 1. Backup local database
docker exec opsfinder-db pg_dump -U opsuser opsfinder > backup.sql

# 2. Restore to external database
psql -h your-db-host -U your_user opsfinder < backup.sql

# 3. Update .env with DB_HOST
# 4. Redeploy
./scripts/deploy/deploy.sh
```

## Reverting to Local Database

To switch back to local Docker database:

1. Remove or comment out `DB_HOST` in `.env`
2. Redeploy: `./scripts/deploy/deploy.sh`

```bash
# .env
# DB_HOST=external-host.com  # Comment this out
DB_NAME=opsfinder
DB_USER=opsuser
DB_PASSWORD=your_password
```

## Troubleshooting

### Connection Refused
- Check firewall rules
- Verify security groups (cloud providers)
- Test with telnet or nc

### Authentication Failed
- Verify credentials
- Check user privileges in database

### Liquibase Migration Failures
- Check backend logs: `docker logs opsfinder-backend`
- Ensure user has CREATE TABLE privileges
- Verify pg_trgm extension is enabled

## Support

For issues with external database setup:
1. Check application logs: `docker logs opsfinder-backend`
2. Verify database connectivity
3. See [DEPLOYMENT.md](DEPLOYMENT.md) for general troubleshooting
