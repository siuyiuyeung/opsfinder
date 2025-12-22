# Task: Add Timezone Configuration to Docker Compose

## Overview

Configure timezone settings for all Docker containers to ensure consistent timestamps across the application stack (database, backend, frontend).

## Implementation Details

### Changes Made

**1. docker-compose.yml** - Added TZ environment variable to all services:

- **Database service** (line 12):
  ```yaml
  environment:
    TZ: ${TZ:-Asia/Shanghai}
  ```

- **Backend service** (line 54):
  ```yaml
  environment:
    TZ: ${TZ:-Asia/Shanghai}
  ```

- **Frontend service** (lines 86-87):
  ```yaml
  environment:
    TZ: ${TZ:-Asia/Shanghai}
  ```

**2. docker-compose.app.yml** - Added TZ environment variable:

- **Backend service** (line 22):
  ```yaml
  environment:
    TZ: ${TZ:-Asia/Shanghai}
  ```

- **Frontend service** (lines 50-51):
  ```yaml
  environment:
    TZ: ${TZ:-Asia/Shanghai}
  ```

**3. .env.example** - Added timezone documentation (lines 47-60):
```bash
# Timezone Configuration
# Set timezone for all containers (database, backend, frontend)
# Default: Asia/Shanghai (UTC+8)
#
# Common timezones:
#   Asia/Shanghai (China Standard Time, UTC+8)
#   Asia/Hong_Kong (Hong Kong Time, UTC+8)
#   Asia/Tokyo (Japan Standard Time, UTC+9)
#   UTC (Coordinated Universal Time, UTC+0)
#   America/New_York (Eastern Time, UTC-5/-4)
#   Europe/London (GMT/BST, UTC+0/+1)
#
# Full list: https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
TZ=Asia/Shanghai
```

## Benefits

✅ **Consistent Timestamps**: All containers use the same timezone
✅ **Accurate Logs**: Log timestamps match local time (Shanghai/UTC+8)
✅ **Database Time Functions**: PostgreSQL CURRENT_TIMESTAMP matches local time
✅ **Java Date/Time**: Spring Boot uses Shanghai timezone for all operations
✅ **Nginx Logs**: Access logs use local timestamps
✅ **Configurable**: Override via .env file for different deployments

## Default Configuration

**Timezone**: `Asia/Shanghai` (UTC+8)
- Also known as: China Standard Time (CST)
- Offset: UTC+8 (no daylight saving time)
- Covers: Mainland China, including Shanghai, Beijing, Shenzhen, etc.

## Usage

### Using Default Timezone (Asia/Shanghai)

No configuration needed - defaults to Asia/Shanghai:
```bash
docker compose up -d
```

### Override Timezone

Set TZ in your `.env` file:
```bash
# For Hong Kong
TZ=Asia/Hong_Kong

# For UTC
TZ=UTC

# For US Eastern Time
TZ=America/New_York
```

Then restart containers:
```bash
docker compose restart
```

## Verification

### Check Container Timezone

**Backend**:
```bash
docker compose exec backend date
# Should show: Mon Dec 22 12:00:00 CST 2025
```

**Database**:
```bash
docker compose exec database date
# Should show: Mon Dec 22 12:00:00 CST 2025
```

**Frontend**:
```bash
docker compose exec frontend date
# Should show: Mon Dec 22 12:00:00 CST 2025
```

### Check Application Logs

Backend logs should show local time:
```bash
tail logs/backend/opsfinder.log
# Should show: 2025-12-22 12:00:00 [thread] INFO ...
```

### Check Database Time

```sql
SELECT CURRENT_TIMESTAMP;
-- Should return: 2025-12-22 12:00:00+08
```

## Impact on Application

### Backend (Spring Boot)
- All `LocalDateTime`, `ZonedDateTime` operations use Shanghai timezone
- Log timestamps in Shanghai time
- Scheduled tasks run in Shanghai time
- API responses with timestamps use Shanghai time

### Database (PostgreSQL)
- `CURRENT_TIMESTAMP` returns Shanghai time
- `NOW()` returns Shanghai time
- All timestamp columns stored with +08 offset
- Date/time comparisons use Shanghai timezone

### Frontend (Nginx)
- Access log timestamps in Shanghai time
- Error log timestamps in Shanghai time

## Common Timezones

| Location | Timezone | UTC Offset |
|----------|----------|------------|
| Shanghai, Beijing | Asia/Shanghai | UTC+8 |
| Hong Kong | Asia/Hong_Kong | UTC+8 |
| Singapore | Asia/Singapore | UTC+8 |
| Tokyo | Asia/Tokyo | UTC+9 |
| Seoul | Asia/Seoul | UTC+9 |
| UTC | UTC | UTC+0 |
| London | Europe/London | UTC+0/+1 |
| New York | America/New_York | UTC-5/-4 |
| Los Angeles | America/Los_Angeles | UTC-8/-7 |

Full list: https://en.wikipedia.org/wiki/List_of_tz_database_time_zones

## Files Modified

- ✅ `docker-compose.yml` - Added TZ to all services (database, backend, frontend)
- ✅ `docker-compose.app.yml` - Added TZ to backend and frontend services
- ✅ `.env.example` - Added TZ configuration with documentation

## Deployment Notes

- **Existing deployments**: Restart containers to apply timezone
  ```bash
  docker compose restart
  ```

- **New deployments**: Timezone applied automatically on first start

- **No rebuild needed**: Timezone is a runtime environment variable

## Review

### Implementation Completed (2025-12-22)

**Status**: ✅ Complete

**Services Configured**:
1. ✅ PostgreSQL Database - Asia/Shanghai
2. ✅ Spring Boot Backend - Asia/Shanghai
3. ✅ Nginx Frontend - Asia/Shanghai

**Files Updated**:
1. ✅ `docker-compose.yml` - Added TZ to 3 services
2. ✅ `docker-compose.app.yml` - Added TZ to 2 services
3. ✅ `.env.example` - Added timezone documentation

**Testing**:
- [ ] User to verify timezone after container restart
- [ ] User to check log timestamps
- [ ] User to verify database CURRENT_TIMESTAMP

**Default**: Asia/Shanghai (UTC+8)
**Configurable**: Yes, via TZ environment variable in .env file
