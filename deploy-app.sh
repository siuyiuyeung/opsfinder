#!/bin/bash

# OpsFinder Application Deployment Script
# Deploy backend and frontend services (requires database to be running)

set -e

echo "==================================="
echo "OpsFinder Application Deployment"
echo "==================================="

# Check if .env file exists
if [ ! -f .env ]; then
    echo "ERROR: .env file not found!"
    echo "Please copy .env.example to .env and configure it:"
    echo "  cp .env.example .env"
    echo "  nano .env  # Edit with your values"
    exit 1
fi

# Load environment variables
set -a
source .env
set +a

# Validate required environment variables
if [ -z "$DB_PASSWORD" ] || [ "$DB_PASSWORD" = "your_secure_database_password_here" ]; then
    echo "ERROR: Please set a secure DB_PASSWORD in .env file"
    exit 1
fi

if [ -z "$JWT_SECRET" ] || [ "$JWT_SECRET" = "your_super_secure_jwt_secret_key_at_least_256_bits_long_change_this_in_production" ]; then
    echo "ERROR: Please set a secure JWT_SECRET in .env file"
    exit 1
fi

echo "Environment variables loaded successfully"

# Create log directories if they don't exist
echo "Creating log directories..."
mkdir -p logs/backend logs/frontend
chmod -R 755 logs

# Check for external database configuration
if [ ! -z "$DB_HOST" ]; then
    echo ""
    echo "==================================="
    echo "External Database Configuration"
    echo "==================================="
    echo "Database Host: $DB_HOST"
    echo "Database Port: ${DB_PORT:-5432}"
    echo "Database Name: ${DB_NAME:-opsfinder}"
    echo "Database User: ${DB_USER:-opsuser}"
    echo ""
    echo "Using external database - local database checks skipped"
    echo ""

    USE_EXTERNAL_DB=true
else
    echo ""
    echo "Using local Docker database"
    echo ""

    USE_EXTERNAL_DB=false

    # Check if network exists
    if ! docker network inspect opsfinder-network >/dev/null 2>&1; then
        echo "ERROR: Docker network 'opsfinder-network' not found!"
        echo "Please deploy the database first: ./deploy-db.sh"
        exit 1
    fi

    # Check if database is running
    if ! docker ps | grep -q opsfinder-db; then
        echo "WARNING: Database container is not running!"
        echo "Please start the database first: ./deploy-db.sh"
        exit 1
    fi
fi

# Ensure network exists (create if needed for external DB)
if [ "$USE_EXTERNAL_DB" = true ]; then
    echo "Creating Docker network..."
    docker network create opsfinder-network 2>/dev/null || echo "Network already exists"
fi

# Stop existing application containers
echo "Stopping existing application containers..."
docker compose -f docker-compose.app.yml down

# Build and start application services
echo "Building and starting application services..."
docker compose -f docker-compose.app.yml up -d --build

# Wait for services to be healthy
echo "Waiting for services to be healthy..."
sleep 20

# Check service status
echo ""
echo "==================================="
echo "Application Status:"
echo "==================================="
docker compose -f docker-compose.app.yml ps

# Show logs
echo ""
echo "==================================="
echo "Recent Application Logs:"
echo "==================================="
docker compose -f docker-compose.app.yml logs --tail=50

echo ""
echo "==================================="
echo "Application Deployment Complete!"
echo "==================================="
echo "Frontend URL: http://localhost:${FRONTEND_PORT:-80}"
echo "Backend API: http://localhost:8080/api"

if [ "$USE_EXTERNAL_DB" = true ]; then
    echo "Database: $DB_HOST:${DB_PORT:-5432} (${DB_NAME:-opsfinder}) [EXTERNAL]"
else
    echo "Database: localhost:5432 (${DB_NAME:-opsfinder}) [LOCAL DOCKER]"
fi

echo ""
echo "Log Files:"
echo "  Backend:  logs/backend/"
echo "  Frontend: logs/frontend/"
echo ""
echo "To view logs:"
echo "  Docker logs:   docker compose -f docker-compose.app.yml logs -f"
echo "  Application:   tail -f logs/backend/opsfinder.log"
echo "  Nginx access:  tail -f logs/frontend/access.log"
echo ""
echo "To stop: docker compose -f docker-compose.app.yml down"
echo "To restart: docker compose -f docker-compose.app.yml restart"
echo "==================================="
