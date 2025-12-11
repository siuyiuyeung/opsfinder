#!/bin/bash

# OpsFinder Complete Deployment Script
# Deploys all services (database + backend + frontend)
# For separate deployment, use deploy-db.sh and deploy-app.sh

set -e

echo "==================================="
echo "OpsFinder Complete Deployment"
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

# Check for external database configuration
if [ ! -z "$DB_HOST" ]; then
    echo ""
    echo "==================================="
    echo "External Database Detected"
    echo "==================================="
    echo "Database Host: $DB_HOST"
    echo "Database Port: ${DB_PORT:-5432}"
    echo "Database Name: ${DB_NAME:-opsfinder}"
    echo "Database User: ${DB_USER:-opsuser}"
    echo ""
    echo "Local database container will NOT be deployed."
    echo "Make sure external database is accessible!"
    echo ""

    USE_EXTERNAL_DB=true

    # Create log directories (no database logs needed)
    echo "Creating log directories..."
    mkdir -p logs/backend logs/frontend
    chmod -R 755 logs

    # Create network if it doesn't exist
    echo "Creating Docker network..."
    docker network create opsfinder-network 2>/dev/null || echo "Network already exists"

    # Stop existing containers
    echo "Stopping existing containers..."
    docker compose -f docker-compose.app.yml down 2>/dev/null || true

    # Deploy application services only
    echo "Deploying application services (backend + frontend)..."
    docker compose -f docker-compose.app.yml up -d --build
else
    echo ""
    echo "Using local Docker database (PostgreSQL 16)"
    echo ""

    USE_EXTERNAL_DB=false

    # Create log directories if they don't exist
    echo "Creating log directories..."
    mkdir -p logs/database logs/backend logs/frontend
    chmod -R 755 logs

    # Create network if it doesn't exist
    echo "Creating Docker network..."
    docker network create opsfinder-network 2>/dev/null || echo "Network already exists"

    # Stop existing containers
    echo "Stopping existing containers..."
    docker compose down 2>/dev/null || true

    # Deploy all services
    echo "Deploying all services (database + backend + frontend)..."
    docker compose up -d --build
fi

# Wait for services to be healthy
echo "Waiting for services to be healthy..."
sleep 20

# Check service status
echo ""
echo "==================================="
echo "Service Status:"
echo "==================================="
if [ "$USE_EXTERNAL_DB" = true ]; then
    docker compose -f docker-compose.app.yml ps
else
    docker compose ps
fi

# Show logs
echo ""
echo "==================================="
echo "Recent Logs:"
echo "==================================="
if [ "$USE_EXTERNAL_DB" = true ]; then
    docker compose -f docker-compose.app.yml logs --tail=50
else
    docker compose logs --tail=50
fi

echo ""
echo "==================================="
echo "Complete Deployment Successful!"
echo "==================================="
echo "Frontend URL: http://localhost:${FRONTEND_PORT:-80}"
echo "Backend API: http://localhost:8080/api"

if [ "$USE_EXTERNAL_DB" = true ]; then
    echo "Database: $DB_HOST:${DB_PORT:-5432} (${DB_NAME:-opsfinder}) [EXTERNAL]"
    echo ""
    echo "Log Files:"
    echo "  Backend:  logs/backend/"
    echo "  Frontend: logs/frontend/"
    echo ""
    echo "To view logs:"
    echo "  All services:  docker compose -f docker-compose.app.yml logs -f"
    echo "  Application:   tail -f logs/backend/opsfinder.log"
    echo "  Nginx access:  tail -f logs/frontend/access.log"
    echo ""
    echo "To stop: docker compose -f docker-compose.app.yml down"
    echo "To restart: docker compose -f docker-compose.app.yml restart"
else
    echo "Database: localhost:5432 (${DB_NAME:-opsfinder}) [LOCAL DOCKER]"
    echo ""
    echo "Log Files:"
    echo "  Database: logs/database/"
    echo "  Backend:  logs/backend/"
    echo "  Frontend: logs/frontend/"
    echo ""
    echo "To view logs:"
    echo "  All services:  docker compose logs -f"
    echo "  Application:   tail -f logs/backend/opsfinder.log"
    echo "  Nginx access:  tail -f logs/frontend/access.log"
    echo ""
    echo "Separate deployment scripts:"
    echo "  ./scripts/deploy/deploy-db.sh  - Deploy database only"
    echo "  ./scripts/deploy/deploy-app.sh - Deploy backend + frontend only"
    echo ""
    echo "To stop: docker compose down"
    echo "To restart: docker compose restart"
fi
echo "==================================="
