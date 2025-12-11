#!/bin/bash

# OpsFinder Log Rotation Setup Script
# This script installs logrotate configurations for all services

set -e

echo "==================================="
echo "OpsFinder Log Rotation Setup"
echo "==================================="

# Check if running as root or with sudo
if [ "$EUID" -ne 0 ]; then 
    echo "Please run as root or with sudo"
    exit 1
fi

# Define the project directory
PROJECT_DIR=$(dirname "$(readlink -f "$0")")
LOGROTATE_DIR="/etc/logrotate.d"

echo "Project directory: $PROJECT_DIR"
echo "Installing logrotate configurations..."

# Install backend log rotation
cat > "$LOGROTATE_DIR/opsfinder-backend" << BACKEND_EOF
# OpsFinder Backend Log Rotation
$PROJECT_DIR/logs/backend/*.log {
    daily
    rotate 30
    compress
    delaycompress
    missingok
    notifempty
    create 0644 $(id -u) $(id -g)
    dateext
    dateformat -%Y%m%d
    maxsize 100M
    sharedscripts
    postrotate
        docker exec opsfinder-backend kill -HUP 1 2>/dev/null || true
    endscript
}
BACKEND_EOF

echo "✓ Installed backend log rotation"

# Install frontend log rotation
cat > "$LOGROTATE_DIR/opsfinder-frontend" << FRONTEND_EOF
# OpsFinder Frontend Log Rotation
$PROJECT_DIR/logs/frontend/*.log {
    daily
    rotate 30
    compress
    delaycompress
    missingok
    notifempty
    create 0644 $(id -u) $(id -g)
    dateext
    dateformat -%Y%m%d
    maxsize 50M
    sharedscripts
    postrotate
        docker exec opsfinder-frontend nginx -s reopen 2>/dev/null || true
    endscript
}
FRONTEND_EOF

echo "✓ Installed frontend log rotation"

# Install database log rotation
cat > "$LOGROTATE_DIR/opsfinder-database" << DATABASE_EOF
# OpsFinder Database Log Rotation
$PROJECT_DIR/logs/database/*.log {
    daily
    rotate 30
    compress
    delaycompress
    missingok
    notifempty
    create 0640 $(id -u) $(id -g)
    dateext
    dateformat -%Y%m%d
    maxsize 100M
    sharedscripts
    postrotate
        docker exec opsfinder-db pg_ctl reload -D /var/lib/postgresql/data 2>/dev/null || true
    endscript
}
DATABASE_EOF

echo "✓ Installed database log rotation"

# Test logrotate configuration
echo ""
echo "Testing logrotate configuration..."
logrotate -d "$LOGROTATE_DIR/opsfinder-backend" 2>&1 | head -n 5
logrotate -d "$LOGROTATE_DIR/opsfinder-frontend" 2>&1 | head -n 5
logrotate -d "$LOGROTATE_DIR/opsfinder-database" 2>&1 | head -n 5

echo ""
echo "==================================="
echo "Log Rotation Setup Complete!"
echo "==================================="
echo "Configurations installed in: $LOGROTATE_DIR"
echo "  - opsfinder-backend"
echo "  - opsfinder-frontend"
echo "  - opsfinder-database"
echo ""
echo "Log rotation will run daily via cron"
echo "To manually trigger: sudo logrotate -f $LOGROTATE_DIR/opsfinder-backend"
echo "==================================="
