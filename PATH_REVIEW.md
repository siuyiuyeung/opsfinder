# Path Review Report - Script Relocation Impact

## Summary

After moving scripts from project root to `scripts/` subdirectories, several scripts contain **broken relative paths** that need fixing.

## Issues Found

### 1. Build Scripts (scripts/build/)

#### `build-frontend-windows.bat`
**Location**: `scripts/build/build-frontend-windows.bat`

**Issues**:
- **Line 16**: `cd frontend` → Should be `cd ../../frontend`
- **Line 34**: `cd ..` → Should be `cd ../..` (return to project root)

**Impact**: ❌ Script will fail - cannot find `frontend` directory

**Fixed**: ⏳ Pending

---

#### `run-frontend-windows.bat`
**Location**: `scripts/build/run-frontend-windows.bat`

**Issues**: ✅ None - Script doesn't use relative paths

**Impact**: ✅ Script works correctly

---

### 2. Deployment Scripts (scripts/deploy/)

#### `deploy.sh`, `deploy-app.sh`, `deploy-db.sh`
**Location**: `scripts/deploy/*.sh`

**Issues** (all three scripts):
- **Line 14**: `if [ ! -f .env ]` → Should be `if [ ! -f ../../.env ]`
- **Line 24**: `source .env` → Should be `source ../../.env`
- **Docker Compose**: Commands reference files in project root without path adjustment

**Impact**: ❌ Scripts will fail - cannot find `.env` file and docker-compose files

**Recommended Fix**: Change to project root at script start:
```bash
# Change to project root directory
cd "$(dirname "$0")/../.." || exit 1
```

**Fixed**: ⏳ Pending

---

### 3. Setup Scripts (scripts/setup/)

#### `setup-logrotate.sh`
**Location**: `scripts/setup/setup-logrotate.sh`

**Issues**:
- **Line 19**: `PROJECT_DIR=$(dirname "$(readlink -f "$0")")`
  - Gets script directory (`scripts/setup/`)
  - Should get project root directory

**Impact**: ⚠️ Log rotation paths will be incorrect

**Fixed Path**:
```bash
PROJECT_DIR=$(cd "$(dirname "$0")/../.." && pwd)
```

**Fixed**: ⏳ Pending

---

### 4. Database Scripts (scripts/database/)

#### `init-db.sql`, `reset-database.sql`
**Location**: `scripts/database/*.sql`

**Issues**: ✅ None - SQL scripts don't use relative paths

**Impact**: ✅ Scripts work correctly (when called with proper psql command)

---

## Fix Strategy

### Option 1: Change to Project Root (Recommended)
Add this at the start of each script:
```bash
# For .sh scripts
cd "$(dirname "$0")/../.." || exit 1

# For .bat scripts
cd /d "%~dp0..\.."
```

**Pros**:
- All existing paths continue to work
- Minimal changes needed
- Scripts work from any directory

**Cons**:
- Scripts assume they're always 2 levels deep

---

### Option 2: Update All Relative Paths
Update each path reference individually.

**Pros**:
- Scripts can run from their current directory
- More flexible

**Cons**:
- Many changes required
- Error-prone
- Harder to maintain

---

## Recommended Fixes

### build-frontend-windows.bat
```batch
@echo off
REM Build frontend Docker image on Windows

REM Change to project root
cd /d "%~dp0..\.."

SET API_URL=%1
IF "%API_URL%"=="" (
    SET API_URL=http://localhost:8080/api
    echo Using default API URL: %API_URL%
) else (
    echo Using custom API URL: %API_URL%
)

echo Building frontend Docker image...
cd frontend
docker build --build-arg VITE_API_BASE_URL=%API_URL% -t opsfinder-frontend:local .

IF %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo Build completed successfully!
    echo ========================================
    echo.
    echo To run: docker run -d -p 80:80 --name opsfinder-frontend opsfinder-frontend:local
    echo Or use: scripts/build/run-frontend-windows.bat
) else (
    echo.
    echo ========================================
    echo Build failed!
    echo ========================================
)

cd ..
```

### deploy.sh (and deploy-app.sh, deploy-db.sh)
```bash
#!/bin/bash

# OpsFinder Complete Deployment Script

set -e

# Change to project root directory
cd "$(dirname "$0")/../.." || exit 1

echo "==================================="
echo "OpsFinder Complete Deployment"
echo "==================================="
echo "Working directory: $(pwd)"

# Check if .env file exists
if [ ! -f .env ]; then
    echo "ERROR: .env file not found!"
    # ... rest of the script remains the same
```

### setup-logrotate.sh
```bash
#!/bin/bash

# OpsFinder Log Rotation Setup Script

set -e

# Get project root directory (2 levels up from scripts/setup/)
PROJECT_DIR=$(cd "$(dirname "$0")/../.." && pwd)

echo "==================================="
echo "OpsFinder Log Rotation Setup"
echo "==================================="
echo "Project directory: $PROJECT_DIR"

# ... rest of the script remains the same
```

---

## Testing Checklist

After fixes:
- [ ] Run `scripts/build/build-frontend-windows.bat` from project root
- [ ] Run `scripts/build/build-frontend-windows.bat` from scripts/build/
- [ ] Run `scripts/deploy/deploy.sh` from project root
- [ ] Run `scripts/deploy/deploy.sh` from scripts/deploy/
- [ ] Verify .env file is found
- [ ] Verify docker-compose files are found
- [ ] Run `scripts/setup/setup-logrotate.sh` and verify paths
- [ ] Check logrotate configs reference correct log directories

---

## Documentation Updates Needed

After fixing scripts:
- [ ] Update README.md with correct script paths
- [ ] Update docs/deployment/*.md with usage examples
- [ ] Update docs/guides/WINDOWS_FRONTEND_BUILD.md
- [ ] Add note about running scripts from any directory

---

**Status**: 🔴 Critical - Scripts currently broken
**Priority**: High - Fix before next deployment
**Estimated Time**: 30 minutes
