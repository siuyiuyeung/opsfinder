# Task: Fix Missing Backend Logs in Production

## Problem Analysis

### Symptoms
- No backend logs in Docker container
- No logs in mounted volume at `./logs/backend` on host
- application-prod.yml has logging configuration, but it's not working

### Root Cause Analysis

**Issue Found**: The Dockerfile runs the Spring Boot application as a non-root user (`spring`), but the log directory `/var/log/opsfinder/backend/` is never created and the user has no write permissions.

### Current Configuration

**application-prod.yml** (lines 44-54):
```yaml
logging:
  file:
    name: /var/log/opsfinder/backend/opsfinder.log
  logback:
    rollingpolicy:
      max-file-size: 100MB
      max-history: 30
      total-size-cap: 3GB
      file-name-pattern: /var/log/opsfinder/backend/opsfinder-%d{yyyy-MM-dd}.%i.log
```
✅ **Configuration is correct**

**docker-compose.yml** (line 55):
```yaml
volumes:
  - ./logs/backend:/var/log/opsfinder/backend
```
✅ **Volume mount is correct**

**Dockerfile** (lines 24-26):
```dockerfile
# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
```
❌ **Missing log directory creation and permissions**

### Why Logs Are Missing

1. **Directory doesn't exist**: `/var/log/opsfinder/backend/` is never created in the Dockerfile
2. **Permission denied**: The `spring` user (non-root) cannot write to `/var/log/`
3. **Silent failure**: Spring Boot can't create the directory, falls back to console logging only
4. **Docker logs only**: Logs go to stdout/stderr (viewable with `docker logs`), not files

## Solution

### Fix 1: Update Dockerfile (Recommended)

Add log directory creation and ownership **before** switching to non-root user:

```dockerfile
# Multi-stage build for Spring Boot application
FROM gradle:9.2-jdk21 AS build

WORKDIR /app

# Copy Gradle files
COPY build.gradle settings.gradle ./
COPY gradle gradle

# Download dependencies (cached layer)
RUN gradle dependencies --no-daemon || return 0

# Copy source code
COPY src src

# Build application
RUN gradle clean build -x test --no-daemon

# Production stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create log directory with proper permissions
RUN mkdir -p /var/log/opsfinder/backend && \
    addgroup -S spring && \
    adduser -S spring -G spring && \
    chown -R spring:spring /var/log/opsfinder

# Switch to non-root user
USER spring:spring

# Copy built JAR from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", \
    "app.jar"]
```

**Key Changes**:
- Line 24-27: Create log directory and set ownership **before** `USER` switch
- Creates `/var/log/opsfinder/backend` with full path
- Sets ownership to `spring:spring` user
- Spring Boot can now write logs to this directory

### Fix 2: Create Host Directory (Required)

Ensure the host mount point exists:

```bash
mkdir -p logs/backend
```

## Implementation Steps

1. **Update Dockerfile** with log directory creation
2. **Create host directory**: `mkdir -p logs/backend`
3. **Rebuild Docker image**: `docker compose build backend`
4. **Restart container**: `docker compose up -d backend`
5. **Verify logs are created**: `ls -la logs/backend/`

## Verification

### Check Logs Exist

**In container**:
```bash
docker compose exec backend ls -la /var/log/opsfinder/backend/
```

**On host**:
```bash
ls -la logs/backend/
```

**Expected output**:
```
-rw-r--r-- 1 spring spring opsfinder.log
-rw-r--r-- 1 spring spring opsfinder-2025-12-19.0.log
```

### Tail Logs

```bash
# From host
tail -f logs/backend/opsfinder.log

# Or from container
docker compose exec backend tail -f /var/log/opsfinder/backend/opsfinder.log
```

### Verify Logback Configuration

Check backend startup logs:
```bash
docker compose logs backend | grep -i "log"
```

Should see something like:
```
Logging system initialized to logback
Logging initialized using 'class org.springframework.boot.logging.logback.LogbackLoggingSystem'
```

## Additional Considerations

### Log Rotation

application-prod.yml already configures Logback rolling policy:
- **Max file size**: 100MB
- **Max history**: 30 days
- **Total size cap**: 3GB
- **Pattern**: `opsfinder-YYYY-MM-DD.{index}.log`

### Viewing Logs

**Option 1: Docker logs (stdout)**:
```bash
docker compose logs -f backend
```

**Option 2: File logs (persistent)**:
```bash
tail -f logs/backend/opsfinder.log
```

**Option 3: Inside container**:
```bash
docker compose exec backend cat /var/log/opsfinder/backend/opsfinder.log
```

### Production Monitoring

Consider adding log aggregation tools:
- **Loki + Grafana**: For centralized logging
- **ELK Stack**: Elasticsearch, Logstash, Kibana
- **Promtail**: Forward logs to Loki

## Todo List
- [x] Update Dockerfile with log directory creation
- [x] Create host logs/backend directory
- [ ] Rebuild backend Docker image - **User action required**
- [ ] Restart backend container - **User action required**
- [ ] Verify logs are created - **User action required**
- [x] Update documentation

## Review

### Implementation Completed (2025-12-19)

**Changes Made**:

1. **Dockerfile Updated** (lines 24-31):
   ```dockerfile
   # Create log directory with proper permissions
   RUN mkdir -p /var/log/opsfinder/backend && \
       addgroup -S spring && \
       adduser -S spring -G spring && \
       chown -R spring:spring /var/log/opsfinder

   # Switch to non-root user
   USER spring:spring
   ```

2. **Host Directory Created**:
   - Created `logs/backend/` directory on host
   - Ready for volume mount from docker-compose.yml

### What This Fixes

**Before**:
- ❌ No log directory in container
- ❌ Spring Boot can't write logs (permission denied)
- ❌ Logs only go to stdout (docker logs)
- ❌ No persistent log files

**After**:
- ✅ Log directory created with proper ownership
- ✅ Spring Boot can write to `/var/log/opsfinder/backend/`
- ✅ Logs written to both stdout AND files
- ✅ Persistent logs accessible on host at `./logs/backend/`

### Next Steps (User Actions Required)

1. **Rebuild backend Docker image**:
   ```bash
   docker compose build backend
   ```

2. **Restart backend container**:
   ```bash
   docker compose up -d backend
   ```

3. **Verify logs are created**:
   ```bash
   # Check logs directory
   ls -la logs/backend/

   # Tail the log file
   tail -f logs/backend/opsfinder.log
   ```

4. **Check log rotation is working** (after some time):
   ```bash
   ls -la logs/backend/
   # Should see: opsfinder.log and opsfinder-YYYY-MM-DD.0.log
   ```

### Benefits

✅ **Persistent Logs**: Survive container restarts
✅ **Log Rotation**: Automatic rotation (100MB max, 30 days, 3GB cap)
✅ **Easy Access**: Logs available on host filesystem
✅ **Production Ready**: Proper file-based logging for monitoring
✅ **Security**: Runs as non-root user with minimal permissions

### Files Modified

- ✅ `Dockerfile` - Added log directory creation and ownership
- ✅ `logs/backend/` - Created host mount point directory
