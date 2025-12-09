# OpsFinder Production Deployment Guide

Complete guide for deploying OpsFinder to a Linux VM using Docker.

## Prerequisites

- Linux VM (Ubuntu 20.04+ recommended)
- Docker Engine 20.10+
- Docker Compose v2 (comes with Docker Engine, or install separately)
- At least 2GB RAM, 20GB disk space
- Open ports: 80 (frontend), 8080 (backend), 5432 (database)

## Quick Start

### 1. Install Docker and Docker Compose

```bash
# Update package index
sudo apt-get update

# Install Docker (includes Docker Compose v2)
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Add your user to docker group
sudo usermod -aG docker $USER

# Verify installation
docker --version
docker compose version

# Log out and back in for group changes to take effect
```

**Note**: Docker Compose v2 is integrated into Docker CLI as `docker compose` (not `docker-compose`). If you have the old standalone version, either update to Docker Engine 20.10+ or install Docker Compose v2 plugin.

### 2. Clone or Upload Project

```bash
# Option A: Clone from repository
git clone <your-repo-url> opsfinder
cd opsfinder

# Option B: Upload via SCP
scp -r /path/to/OpsFinder user@your-vm:/home/user/opsfinder
ssh user@your-vm
cd opsfinder
```

### 3. Configure Environment

```bash
# Copy environment template
cp .env.example .env

# Edit configuration
nano .env
```

**IMPORTANT:** Update these values in `.env`:
- `DB_PASSWORD`: Strong database password (min 16 characters)
- `JWT_SECRET`: Secure random string (min 32 characters, use `openssl rand -base64 32`)

Example secure values:
```bash
# Generate secure password
openssl rand -base64 24

# Generate JWT secret
openssl rand -base64 48
```

### 4. Deploy Application

**Option A: Complete Deployment (Recommended for first-time setup)**

```bash
# Make deploy scripts executable
chmod +x deploy.sh deploy-db.sh deploy-app.sh

# Deploy all services
./deploy.sh
```

**Option B: Separate Deployment (Database + Application)**

```bash
# Deploy database first
./deploy-db.sh

# Then deploy backend + frontend
./deploy-app.sh
```

The scripts will:
1. Validate environment configuration
2. Create Docker network and log directories
3. Stop existing containers
4. Build Docker images
5. Start services with health checks
6. Display service status and logs

For detailed deployment options, see [DOCKER_DEPLOYMENT.md](DOCKER_DEPLOYMENT.md)

### 5. Access Application

- **Frontend**: http://your-vm-ip:80
- **Backend API**: http://your-vm-ip:8080/api
- **Health Check**: http://your-vm-ip:8080/actuator/health

**Default Admin User** (dev context):
- Username: `admin`
- Password: `admin123`

⚠️ **IMPORTANT**: Change the default admin password immediately after first login!

## Docker Architecture

### Services

1. **database** (postgres:16-alpine)
   - Port: 5432
   - Volume: postgres_data
   - Health checks enabled
   - Can be deployed separately via docker-compose.db.yml

2. **backend** (Spring Boot 4.0 + Java 21)
   - Port: 8080
   - Connects to database service
   - Auto-runs Liquibase migrations
   - Health checks via /actuator/health
   - Can be deployed with frontend via docker-compose.app.yml

3. **frontend** (Vue 3 + Nginx)
   - Can be deployed with backend via docker-compose.app.yml
   - Port: 80
   - Proxies API requests to backend
   - SPA with client-side routing

### Volumes

- `postgres_data`: Persistent database storage

### Network

- `opsfinder-network`: Bridge network for internal communication

## Common Operations

### View Logs

```bash
# All services
docker compose logs -f

# Specific service
docker compose logs -f backend
docker compose logs -f frontend
docker compose logs -f database

# Last 100 lines
docker compose logs --tail=100
```

### Restart Services

```bash
# Restart all
docker compose restart

# Restart specific service
docker compose restart backend
```

### Stop Application

```bash
# Stop but keep data
docker compose down

# Stop and remove volumes (WARNING: deletes database!)
docker compose down -v
```

### Update Application

```bash
# Pull latest code
git pull

# Rebuild and restart
docker compose up -d --build

# Or use deploy script
./deploy.sh
```

### Using External Database

OpsFinder supports external PostgreSQL databases (AWS RDS, Azure Database, etc.):

```bash
# Configure .env
DB_HOST=your-external-db-host.com
DB_PORT=5432
DB_NAME=opsfinder
DB_USER=your_user
DB_PASSWORD=your_password

# Deploy (automatically skips local database)
./deploy.sh
```

For comprehensive external database setup, see [EXTERNAL_DATABASE.md](EXTERNAL_DATABASE.md)

### Database Backup

```bash
# Create backup
docker exec opsfinder-db pg_dump -U opsuser opsfinder > backup_$(date +%Y%m%d_%H%M%S).sql

# Restore backup
docker exec -i opsfinder-db psql -U opsuser opsfinder < backup_20231201_120000.sql
```

### Scale Backend (if needed)

```bash
# Run multiple backend instances
docker compose up -d --scale backend=3
```

## Security Hardening

### 1. Firewall Configuration

```bash
# Allow only necessary ports
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw allow 22/tcp
sudo ufw enable
```

### 2. HTTPS with Let's Encrypt (Recommended)

Install Certbot and configure SSL:

```bash
# Install Certbot
sudo apt-get install certbot python3-certbot-nginx

# Get certificate (requires domain name)
sudo certbot --nginx -d your-domain.com

# Update nginx config to use HTTPS
```

### 3. Regular Updates

```bash
# Update Docker images
docker compose pull
docker compose up -d

# Update system packages
sudo apt-get update && sudo apt-get upgrade
```

### 4. Change Default Credentials

1. Login as admin
2. Create new admin user
3. Delete or disable default admin account

## Monitoring

### Check Service Health

```bash
# Backend health
curl http://localhost:8080/actuator/health

# Database connection
docker exec opsfinder-db pg_isready -U opsuser

# Frontend
curl http://localhost/
```

### Resource Usage

```bash
# Container stats
docker stats

# Disk usage
docker system df
```

## Troubleshooting

### Backend Won't Start

```bash
# Check logs
docker compose logs backend

# Common issues:
# - Database not ready: Wait 30s and retry
# - Port conflict: Change port in .env
# - Memory: Increase JAVA_OPTS in docker-compose.yml
```

### Database Connection Issues

```bash
# Test connection
docker exec -it opsfinder-db psql -U opsuser -d opsfinder

# Reset database (WARNING: deletes all data!)
docker compose down -v
docker compose up -d database
```

### Frontend 404 Errors

```bash
# Rebuild frontend
docker compose up -d --build frontend

# Check nginx config
docker exec opsfinder-frontend cat /etc/nginx/conf.d/default.conf
```

### Port Already in Use

```bash
# Find process using port 80
sudo lsof -i :80

# Kill process or change FRONTEND_PORT in .env
```

### Log Issues

```bash
# Logs not appearing in logs/ directory
# Check volume mounts
docker compose config | grep -A 5 volumes

# Ensure log directories exist with correct permissions
mkdir -p logs/backend logs/frontend logs/database
chmod -R 755 logs

# Check if backend is writing logs
docker exec opsfinder-backend ls -la /var/log/opsfinder/backend/

# Check nginx log configuration
docker exec opsfinder-frontend nginx -T | grep log

# Disk full - clean old logs
du -sh logs/*
find logs -name "*.gz" -mtime +60 -delete
```

## Performance Tuning

### Java Heap Size

Edit `docker-compose.yml`:
```yaml
backend:
  environment:
    JAVA_OPTS: "-Xms1g -Xmx2g"  # Adjust based on available RAM
```

### Database Connection Pool

Edit `application-prod.yml`:
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20  # Increase for high load
      minimum-idle: 10
```

### Nginx Worker Processes

Edit `frontend/nginx.conf`:
```nginx
worker_processes auto;
worker_connections 1024;
```

## Maintenance

### Scheduled Backups

Add to crontab:
```bash
# Daily backup at 2 AM
0 2 * * * /path/to/opsfinder/backup.sh

# Create backup.sh:
#!/bin/bash
cd /path/to/opsfinder
docker exec opsfinder-db pg_dump -U opsuser opsfinder | gzip > /backups/opsfinder_$(date +\%Y\%m\%d).sql.gz
find /backups -name "opsfinder_*.sql.gz" -mtime +30 -delete
```

### Log Management

**Log Locations** (mounted outside containers):
- **Backend**: `logs/backend/opsfinder.log`
- **Frontend**: `logs/frontend/access.log`, `logs/frontend/error.log`
- **Database**: `logs/database/` (PostgreSQL logs)
- **Docker**: Docker JSON logs (max 10MB, 3 files per container)

**View Application Logs**:
```bash
# Backend application logs
tail -f logs/backend/opsfinder.log

# Frontend access logs
tail -f logs/frontend/access.log

# Frontend error logs
tail -f logs/frontend/error.log

# All Docker container logs
docker compose logs -f

# Specific service
docker compose logs -f backend
```

**Log Rotation Setup**:

Install logrotate for automatic log management:
```bash
# Run the setup script as root
sudo ./setup-logrotate.sh
```

This installs:
- Daily rotation for all log files
- 30 days retention
- Compression after 1 day
- Maximum file sizes (100MB backend/database, 50MB frontend)

**Manual Log Rotation**:
```bash
# Rotate backend logs
sudo logrotate -f /etc/logrotate.d/opsfinder-backend

# Rotate frontend logs
sudo logrotate -f /etc/logrotate.d/opsfinder-frontend

# Rotate database logs
sudo logrotate -f /etc/logrotate.d/opsfinder-database
```

**Docker Container Log Rotation**:

Docker automatically rotates container logs. Configuration in `docker-compose.yml`:
```yaml
logging:
  driver: "json-file"
  options:
    max-size: "10m"
    max-file: "3"
```

**Clean Old Logs**:
```bash
# Remove logs older than 30 days
find logs -name "*.log.*" -mtime +30 -delete

# Remove compressed logs older than 60 days
find logs -name "*.gz" -mtime +60 -delete
```

## Uninstall

```bash
# Stop and remove containers
docker compose down

# Remove volumes (deletes data!)
docker volume rm opsfinder_postgres_data

# Remove images
docker rmi opsfinder-backend opsfinder-frontend postgres:15-alpine

# Remove project directory
cd ..
rm -rf opsfinder
```

## Support

- Check logs: `docker compose logs -f`
- GitHub Issues: [Your repo issues]
- Documentation: See IMPLEMENTATION_PLAN.md

## License

[Your License]
