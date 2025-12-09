#!/bin/bash

# OpsFinder Deployment Script for Linux VM
# This script handles initial deployment and updates

set -e

echo "==================================="
echo "OpsFinder Deployment Script"
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

# Stop existing containers
echo "Stopping existing containers..."
docker-compose down

# Build and start services
echo "Building and starting services..."
docker-compose up -d --build

# Wait for services to be healthy
echo "Waiting for services to be healthy..."
sleep 10

# Check service status
echo ""
echo "==================================="
echo "Service Status:"
echo "==================================="
docker-compose ps

# Show logs
echo ""
echo "==================================="
echo "Recent Logs:"
echo "==================================="
docker-compose logs --tail=50

echo ""
echo "==================================="
echo "Deployment Complete!"
echo "==================================="
echo "Frontend URL: http://localhost:${FRONTEND_PORT:-80}"
echo "Backend API: http://localhost:8080/api"
echo ""
echo "To view logs: docker-compose logs -f"
echo "To stop: docker-compose down"
echo "To restart: docker-compose restart"
echo "==================================="
