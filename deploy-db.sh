#!/bin/bash

# OpsFinder Database Deployment Script
# Deploy only the PostgreSQL database service

set -e

echo "==================================="
echo "OpsFinder Database Deployment"
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

echo "Environment variables loaded successfully"

# Create log directories if they don't exist
echo "Creating log directories..."
mkdir -p logs/database
chmod -R 755 logs

# Create network if it doesn't exist
echo "Creating Docker network..."
docker network create opsfinder-network 2>/dev/null || echo "Network already exists"

# Stop existing database container
echo "Stopping existing database container..."
docker-compose -f docker-compose.db.yml down

# Start database service
echo "Starting database service..."
docker-compose -f docker-compose.db.yml up -d

# Wait for database to be healthy
echo "Waiting for database to be healthy..."
sleep 15

# Check service status
echo ""
echo "==================================="
echo "Database Status:"
echo "==================================="
docker-compose -f docker-compose.db.yml ps

# Show logs
echo ""
echo "==================================="
echo "Recent Database Logs:"
echo "==================================="
docker-compose -f docker-compose.db.yml logs --tail=30

echo ""
echo "==================================="
echo "Database Deployment Complete!"
echo "==================================="
echo "PostgreSQL: localhost:5432"
echo "Database: ${DB_NAME:-opsfinder}"
echo "User: ${DB_USER:-opsuser}"
echo ""
echo "Log Files: logs/database/"
echo ""
echo "To view logs: docker-compose -f docker-compose.db.yml logs -f"
echo "To stop: docker-compose -f docker-compose.db.yml down"
echo "==================================="
