# Task: Fix 403 Forbidden Error on Frontend Login

## Problem Analysis

### Symptoms
- **Frontend request**: 403 Forbidden error
- **Postman request**: 200 Success
- **Same endpoint**: `POST /api/auth/login`

### Nginx Logs Comparison

**Frontend (403 Error)**:
```
192.168.31.189 - - [19/Dec/2025:08:41:44 +0000] "POST /api/auth/login HTTP/1.1" 403 31
"http://192.168.31.115:81/login?redirect=/" "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N)"
```

**Postman (200 Success)**:
```
192.168.31.189 - - [19/Dec/2025:08:41:51 +0000] "POST /api/auth/login HTTP/1.1" 200 433
"-" "PostmanRuntime/7.51.0"
```

### Key Differences
1. **Referer Header**: Frontend has referer, Postman doesn't
2. **User Agent**: Browser vs Postman
3. **Origin Header**: Browser sends origin, Postman doesn't

### Root Cause Analysis

The 403 error indicates **Spring Security is blocking the request**. Likely causes:

1. **CSRF Protection** (Most Likely)
   - Spring Security CSRF is enabled by default
   - Frontend doesn't send CSRF token
   - Postman bypasses CSRF (no browser context)

2. **CORS Configuration** (Possible)
   - Origin `http://192.168.31.115:81` not allowed
   - Missing CORS headers in Spring Security

3. **Security Headers** (Less Likely)
   - X-Frame-Options, CSP, or other security policies

## Investigation Steps

### Step 1: Check Spring Security Configuration
Need to examine:
- CSRF configuration (enabled/disabled)
- CORS configuration
- Security filter chain

### Step 2: Check Frontend Request Headers
What headers is the frontend sending?

### Step 3: Check Backend Response
What does the 403 response body contain?

## Solution Options

### Option 1: Disable CSRF for /api/** (Common for REST APIs)
**Pros**: Simple, standard for stateless JWT APIs
**Cons**: Reduces security (acceptable for JWT-based auth)

### Option 2: Configure CSRF Token Handling
**Pros**: Maintains CSRF protection
**Cons**: More complex, requires frontend changes

### Option 3: Fix CORS Configuration
**Pros**: Proper cross-origin handling
**Cons**: May not be the root cause

## Todo List
- [x] Read SecurityConfig.java
- [x] Read CORS configuration
- [x] Identify exact cause - **CORS origin mismatch**
- [x] Document fix
- [ ] User applies fix in production
- [ ] User tests with frontend

## Investigation Results

### Root Cause: CORS Origin Mismatch

**Confirmed Issue**: The production frontend origin `http://192.168.31.115:81` is NOT in the allowed CORS origins list.

### Current Configuration

**application.yml** (line 78):
```yaml
cors:
  allowed-origins: ${ALLOWED_ORIGINS:http://localhost:3000,http://localhost:5173,http://localhost:8080}
```

**Default Allowed Origins**:
- `http://localhost:3000` (React dev)
- `http://localhost:5173` (Vite dev)
- `http://localhost:8080` (Spring Boot)

**Production Origin**: `http://192.168.31.115:81` ❌ NOT ALLOWED

### Why Postman Works

Postman doesn't send the `Origin` header, so CORS checks don't apply. The browser sends:
```
Origin: http://192.168.31.115:81
```

Spring Security CORS filter blocks this origin because it's not in the allowed list, resulting in 403 Forbidden.

### SecurityConfig.java Analysis

**Line 55**: CSRF is disabled ✅
```java
.csrf(AbstractHttpConfigurer::disable)
```

**Line 56**: CORS is enabled with custom configuration
```java
.cors(cors -> cors.configurationSource(corsConfigurationSource()))
```

**Lines 61-69**: `/api/auth/login` is public (permitAll) ✅

The endpoint is properly configured as public, but CORS filter blocks the request BEFORE it reaches the endpoint.

## Solution

Add the production origin to the `ALLOWED_ORIGINS` environment variable in docker-compose.yml.

### Fix: Update docker-compose.yml

Add `ALLOWED_ORIGINS` environment variable to the backend service:

```yaml
backend:
  # ... existing config ...
  environment:
    # ... existing environment variables ...
    ALLOWED_ORIGINS: "http://192.168.31.115:81,http://localhost:3000,http://localhost:5173"
```

### Complete Fix Instructions

**Option 1: Edit docker-compose.yml** (Recommended)

1. Open `docker-compose.yml`
2. Find the `backend` service `environment` section (around line 42)
3. Add the `ALLOWED_ORIGINS` variable:

```yaml
backend:
  environment:
    SPRING_PROFILES_ACTIVE: ${SPRING_PROFILES_ACTIVE:-prod}
    SPRING_DATASOURCE_URL: jdbc:postgresql://database:5432/${DB_NAME:-opsfinder}
    SPRING_DATASOURCE_USERNAME: ${DB_USER:-opsuser}
    SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD:?Database password required}
    JWT_SECRET: ${JWT_SECRET:?JWT secret required}
    JWT_EXPIRATION: ${JWT_EXPIRATION:-86400000}
    SERVER_PORT: 8080
    LOGGING_LEVEL_ROOT: ${LOG_LEVEL:-INFO}
    LOGGING_LEVEL_COM_IGSL_OPSFINDER: ${APP_LOG_LEVEL:-DEBUG}
    JAVA_OPTS: "-Xms512m -Xmx1024m"
    ALLOWED_ORIGINS: "http://192.168.31.115:81,http://localhost:3000,http://localhost:5173"  # ADD THIS LINE
```

4. Restart the backend container:
```bash
docker compose restart backend
```

**Option 2: Use .env file**

1. Create/edit `.env` file in the project root:
```bash
ALLOWED_ORIGINS=http://192.168.31.115:81,http://localhost:3000,http://localhost:5173
```

**IMPORTANT**:
- NO spaces around commas
- Include ALL origins you need (the env variable REPLACES the default list)
- Must include protocol (http://) and port if non-standard

2. Update docker-compose.yml to use the env variable:
```yaml
backend:
  environment:
    # ... existing vars ...
    ALLOWED_ORIGINS: ${ALLOWED_ORIGINS}
```

3. Restart:
```bash
docker compose restart backend
```

**Common Mistakes**:
- ❌ `ALLOWED_ORIGINS=http://192.168.31.115:81` (missing localhost origins)
- ❌ `ALLOWED_ORIGINS=http://192.168.31.115:81, http://localhost:3000` (spaces around comma)
- ❌ `ALLOWED_ORIGINS=192.168.31.115:81` (missing protocol)
- ✅ `ALLOWED_ORIGINS=http://192.168.31.115:81,http://localhost:3000,http://localhost:5173`

**Option 3: Wildcard (Less Secure, Development Only)**

For development/testing ONLY, you can temporarily allow all origins by changing application.yml:

```yaml
cors:
  allowed-origins: "*"  # WARNING: DO NOT USE IN PRODUCTION
```

### Verification Steps

After applying the fix:

1. **Restart backend**:
   ```bash
   docker compose restart backend
   ```

2. **Check backend logs** for CORS configuration:
   ```bash
   docker compose logs backend | grep -i cors
   ```

3. **Test frontend login** from browser

4. **Check nginx logs** - should see 200 instead of 403:
   ```bash
   docker compose logs frontend | tail -20
   ```

5. **Expected log entry**:
   ```
   192.168.31.189 - - [19/Dec/2025:XX:XX:XX +0000] "POST /api/auth/login HTTP/1.1" 200 433
   ```

## Review

### Root Cause
CORS origin mismatch - production frontend URL not in allowed origins list

### Solution Applied
Used Option 2 - `.env` file approach with complete origins list

### Implementation Details

**File Modified**: `.env` (production environment)

**Configuration Added**:
```bash
ALLOWED_ORIGINS=http://192.168.31.115:81,http://localhost:3000,http://localhost:5173,http://localhost:8080
```

**Key Learnings**:
1. Environment variable **replaces** default values, doesn't append
2. Must include ALL origins needed (production + development)
3. No spaces around commas in the list
4. Protocol (http://) and port must match exactly what browser sends

### Testing Results ✅

**Before Fix**:
- Frontend login: 403 Forbidden
- Postman (no Origin header): 200 Success
- Postman (with Origin header): 403 Invalid CORS request

**After Fix**:
- Frontend login: ✅ 200 Success
- Postman (with Origin header): ✅ 200 Success
- CORS headers present in response: ✅ Confirmed

### Files Modified
- `.env` (added ALLOWED_ORIGINS environment variable)

### Impact
- ✅ Frontend login works from `http://192.168.31.115:81`
- ✅ Maintains security with explicit origin allowlist
- ✅ No code changes needed, only configuration
- ✅ Production environment properly configured

### Deployment Notes
- Backend container restart required after .env changes: `docker compose restart backend`
- Environment variable validated in backend logs
- CORS configuration now environment-aware and flexible

---

## Implementation in docker-compose.yml (2025-12-19)

### Changes Made

**File Modified**: `docker-compose.yml`

**Added Environment Variable** (line 53):
```yaml
ALLOWED_ORIGINS: ${ALLOWED_ORIGINS:-http://localhost:3000,http://localhost:5173,http://localhost:8080}
```

### How It Works

**Default Behavior** (no .env file):
- Uses development defaults: `http://localhost:3000,http://localhost:5173,http://localhost:8080`
- Suitable for local development

**Production Deployment** (with .env file):
1. Create `.env` file in project root
2. Set `ALLOWED_ORIGINS` with your production URL(s):
   ```bash
   ALLOWED_ORIGINS=http://192.168.31.115:81,http://localhost:3000,http://localhost:5173
   ```
3. Restart backend: `docker compose restart backend`

**Example .env Configuration**:
The `.env.example` file already includes CORS documentation (lines 33-45):
```bash
# CORS Configuration
# ALLOWED_ORIGINS - Controls which frontend domains can access the backend API
# Format: Comma-separated list of URLs (no spaces)
#
# Development (default in application.yml): http://localhost:3000,http://localhost:5173,http://localhost:8080
# Production: Set this to your actual frontend domain(s)
#
# Examples:
#   Single domain:    ALLOWED_ORIGINS=https://your-domain.com
#   Multiple domains: ALLOWED_ORIGINS=https://your-domain.com,https://www.your-domain.com
#
# Security: Never use "*" (wildcard) in production
ALLOWED_ORIGINS=https://your-domain.com
```

### Benefits

✅ **Out-of-the-box Development**: Works locally without configuration
✅ **Production-Ready**: Override via .env for deployment
✅ **Documented**: Clear examples in .env.example
✅ **Secure**: Explicit origin allowlist, no wildcards
✅ **Flexible**: Easy to add/remove origins without code changes

### Usage Instructions

**For Local Development**:
No changes needed - defaults work out of the box

**For Production Deployment**:
1. Copy `.env.example` to `.env`
2. Update `ALLOWED_ORIGINS` with your production domain(s)
3. Ensure format: `http://domain:port,http://domain2:port` (no spaces)
4. Deploy: `docker compose up -d`

**For Testing**:
Verify environment variable is loaded:
```bash
docker compose exec backend env | grep ALLOWED_ORIGINS
```
