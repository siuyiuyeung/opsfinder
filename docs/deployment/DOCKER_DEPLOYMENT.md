# Docker Deployment Options

OpsFinder provides flexible deployment options using Docker Compose.

## Architecture

The application is split into three main components:

1. **Database** (PostgreSQL 16) - Data persistence layer
2. **Backend** (Spring Boot 4.0 + Java 21) - REST API and business logic
3. **Frontend** (Vue 3 + Nginx) - User interface

## Deployment Configurations

### Option 1: Complete Deployment (All Services)

Deploy all services together using the main docker-compose.yml:

```bash
# Deploy everything
./scripts/deploy/deploy.sh

# Or manually
docker compose up -d --build
```

**Use when:**
- Initial setup
- Development environment
- Single-server deployment

### Option 2: Separate Deployment (Database + Application)

Deploy database and application services separately:

```bash
# Step 1: Deploy database
./scripts/deploy/deploy-db.sh

# Step 2: Deploy backend + frontend
./scripts/deploy/deploy-app.sh
```

**Use when:**
- Upgrading only application code
- Using external database service
- Scaling frontend/backend independently
- Different maintenance windows

### Option 3: Manual Deployment

Deploy services individually:

```bash
# Create network
docker network create opsfinder-network

# Deploy database only
docker compose -f docker-compose.db.yml up -d

# Deploy application services only
docker compose -f docker-compose.app.yml up -d --build
```

## Docker Compose Files

| File | Services | Purpose |
|------|----------|---------|
| `docker-compose.yml` | database, backend, frontend | Complete deployment |
| `docker-compose.db.yml` | database | Database only |
| `docker-compose.app.yml` | backend, frontend | Application only |

## Deployment Scripts

| Script | Services | Description |
|--------|----------|-------------|
| `scripts/deploy/deploy.sh` | All | Complete deployment |
| `scripts/deploy/deploy-db.sh` | Database | Database deployment |
| `scripts/deploy/deploy-app.sh` | Backend + Frontend | Application deployment |

## Common Scenarios

### Scenario 1: Initial Setup

```bash
# Complete deployment
./scripts/deploy/deploy.sh
```

### Scenario 2: Code Update (Keep Database)

```bash
# Rebuild and restart only application
./scripts/deploy/deploy-app.sh

# Or manually
docker compose -f docker-compose.app.yml up -d --build
```

### Scenario 3: Database Maintenance

```bash
# Stop application
docker compose -f docker-compose.app.yml down

# Backup database
docker exec opsfinder-db pg_dump -U opsuser opsfinder > backup.sql

# Restart database
docker compose -f docker-compose.db.yml restart

# Start application
docker compose -f docker-compose.app.yml up -d
```

### Scenario 4: Rolling Update

```bash
# Update backend only
docker compose -f docker-compose.app.yml up -d --build --no-deps backend

# Update frontend only
docker compose -f docker-compose.app.yml up -d --build --no-deps frontend
```

### Scenario 5: External Database

If using external PostgreSQL (AWS RDS, Azure Database, etc.):

1. Skip database deployment
2. Update `.env` with external database URL
3. Deploy application only:

```bash
# .env configuration
SPRING_DATASOURCE_URL=jdbc:postgresql://external-host:5432/opsfinder
DB_USER=your_user
DB_PASSWORD=your_password

# Deploy application only
./scripts/deploy/deploy-app.sh
```

## Service Dependencies

```
Frontend → Backend → Database
```

- Frontend depends on Backend being healthy
- Backend depends on Database being healthy
- Database is independent

## Health Checks

All services have health checks configured:

- **Database**: `pg_isready` (10s interval, 30s start period)
- **Backend**: `/actuator/health` (30s interval, 60s start period)
- **Frontend**: HTTP GET `/` (30s interval, 10s start period)

## Network Configuration

All services communicate via the `opsfinder-network` Docker bridge network:

- **External Access**: Database network is shared across compose files
- **Internal DNS**: Services resolve by container name (database, backend, frontend)

## Volume Mounts

| Service | Host Path | Container Path | Purpose |
|---------|-----------|----------------|---------|
| Database | `postgres_data` (volume) | `/var/lib/postgresql/data` | Data persistence |
| Database | `./logs/database` | `/var/log/opsfinder/database` | Logs |
| Backend | `./logs/backend` | `/var/log/opsfinder/backend` | Logs |
| Frontend | `./logs/frontend` | `/var/log/opsfinder/frontend` | Logs |

## Port Mappings

| Service | Container Port | Host Port | Configurable |
|---------|----------------|-----------|--------------|
| Database | 5432 | 5432 | No |
| Backend | 8080 | 8080 | No |
| Frontend | 80 | 80 | Yes (FRONTEND_PORT) |

## Environment Variables

All deployment options use the same `.env` file:

```bash
# Required
DB_PASSWORD=your_secure_password
JWT_SECRET=your_jwt_secret

# Optional
DB_NAME=opsfinder
DB_USER=opsuser
SPRING_PROFILES_ACTIVE=prod
FRONTEND_PORT=80
LOG_LEVEL=INFO
APP_LOG_LEVEL=DEBUG
```

## Troubleshooting

### Network Issues

```bash
# Check network exists
docker network inspect opsfinder-network

# Recreate network
docker network rm opsfinder-network
docker network create opsfinder-network
```

### Service Communication

```bash
# Test backend → database connection
docker exec opsfinder-backend ping database

# Test frontend → backend connection
docker exec opsfinder-frontend ping backend
```

### Clean Restart

```bash
# Stop all services
docker compose down
docker compose -f docker-compose.db.yml down
docker compose -f docker-compose.app.yml down

# Remove network
docker network rm opsfinder-network

# Fresh deployment
./scripts/deploy/deploy.sh
```

## Best Practices

1. **Always deploy database first** when using separate deployment
2. **Wait for health checks** before deploying dependent services
3. **Backup database** before major updates
4. **Use separate deployment** for application updates
5. **Monitor logs** during and after deployment
6. **Test health endpoints** after deployment

## Next Steps

See [DEPLOYMENT.md](DEPLOYMENT.md) for comprehensive production deployment guide.
