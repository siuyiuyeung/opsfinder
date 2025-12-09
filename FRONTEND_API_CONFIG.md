# Frontend API Configuration Guide

This guide explains how to configure the backend API URL for the OpsFinder frontend.

## Overview

The frontend needs to know where the backend API is located. This is configured at **build time** using the `VITE_API_BASE_URL` environment variable.

**Important:** The API URL is baked into the frontend during Docker build. If you change the backend URL, you must rebuild the frontend image.

## Configuration Methods

### Method 1: Using .env File (Recommended)

Create or edit `.env` in the project root:

```bash
# For Docker deployment (default)
VITE_API_BASE_URL=http://backend:8080/api

# For production with domain name
VITE_API_BASE_URL=https://api.your-domain.com/api

# For external backend
VITE_API_BASE_URL=http://192.168.1.100:8080/api
```

Then deploy:
```bash
./deploy.sh
# or
docker compose up -d --build
```

The deploy script will automatically pass `VITE_API_BASE_URL` to the frontend build.

### Method 2: Using Build Argument

Override the API URL during Docker build:

```bash
docker build --build-arg VITE_API_BASE_URL=https://api.example.com/api \
  -t opsfinder-frontend ./frontend
```

### Method 3: Using Docker Compose Build Args

In `docker-compose.yml`:

```yaml
services:
  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
      args:
        VITE_API_BASE_URL: https://api.example.com/api
```

## Common Scenarios

### Scenario 1: Standard Docker Deployment
**Setup:** Backend, frontend, and database all in Docker on same host

**Configuration:**
```bash
# .env
VITE_API_BASE_URL=http://backend:8080/api
```

**Why:** Frontend container can directly access backend via Docker service name.

### Scenario 2: Production with Reverse Proxy
**Setup:** Nginx reverse proxy in front of backend, custom domain

**Configuration:**
```bash
# .env
VITE_API_BASE_URL=https://api.your-domain.com/api
```

**Why:** Frontend (in browser) needs to use the public API URL.

### Scenario 3: External Backend
**Setup:** Frontend in Docker, backend on different server

**Configuration:**
```bash
# .env
VITE_API_BASE_URL=http://192.168.1.100:8080/api
# or
VITE_API_BASE_URL=https://backend.other-domain.com/api
```

**Why:** Frontend needs the actual network address of the backend server.

### Scenario 4: Local Development (Windows)
**Setup:** Backend running on Windows, frontend in Docker

**Configuration:**
```bash
# Windows batch
build-frontend-windows.bat http://host.docker.internal:8080/api
```

**Why:** `host.docker.internal` is Docker's special DNS name for the Windows host.

### Scenario 5: Local Development (npm dev server)
**Setup:** Both running on local machine, no Docker

**Configuration:**
```bash
# frontend/.env
VITE_API_BASE_URL=http://localhost:8080/api
```

Then run:
```bash
cd frontend
npm run dev
```

**Why:** Dev server supports hot reload with environment variables.

## Default Values

If `VITE_API_BASE_URL` is not set, the default is:
- **Docker builds:** `http://localhost:8080/api`
- **Code default (api.ts):** `http://localhost:8080/api`

## How It Works

### Build-Time Configuration

1. **Docker Build:** Dockerfile accepts `VITE_API_BASE_URL` as build argument
2. **Environment Variable:** Vite reads `VITE_API_BASE_URL` during build
3. **Code Replacement:** Vite replaces `import.meta.env.VITE_API_BASE_URL` in code
4. **Static Bundle:** API URL is embedded in JavaScript bundle
5. **Runtime:** Browser uses the embedded URL for all API calls

### Code Location

The API URL is used in `frontend/src/services/api.ts`:

```typescript
const api: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  // ...
})
```

During build, `import.meta.env.VITE_API_BASE_URL` is replaced with the actual URL.

## Verifying Configuration

### Check Build Argument in Dockerfile
```dockerfile
ARG VITE_API_BASE_URL=http://localhost:8080/api
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}
```

### Check .env File
```bash
cat .env | grep VITE_API_BASE_URL
```

### Check Built Frontend
```bash
# For running container
docker exec opsfinder-frontend sh -c \
  "cat /usr/share/nginx/html/assets/index-*.js | grep -o 'http://[^\"]*api'"

# For local build
cat frontend/dist/assets/index-*.js | grep -o 'http://[^"]*api'
```

### Check Browser Network Tab
1. Open browser DevTools (F12)
2. Go to Network tab
3. Login or make API call
4. Check the Request URL - it should match your configured URL

## Troubleshooting

### Issue: API calls go to wrong URL

**Solution 1:** Check embedded URL
```bash
docker exec opsfinder-frontend sh -c \
  "cat /usr/share/nginx/html/assets/index-*.js | grep 'http.*api'"
```

**Solution 2:** Rebuild with correct URL
```bash
# Update .env
echo "VITE_API_BASE_URL=http://correct-backend:8080/api" >> .env

# Rebuild
docker compose up -d --build frontend
```

### Issue: "Network Error" in browser

**Causes:**
1. Backend is not running
2. Backend URL is incorrect
3. CORS is blocking requests
4. Backend is not accessible from browser

**Debug:**
1. Check backend health:
   ```bash
   curl http://backend-url:8080/actuator/health
   ```

2. Check CORS configuration in backend `application-prod.yml`

3. Verify frontend can reach backend:
   ```bash
   docker exec opsfinder-frontend wget -O- http://backend:8080/actuator/health
   ```

### Issue: Changes to .env not reflected

**Cause:** Frontend was not rebuilt after .env change

**Solution:**
```bash
# Rebuild frontend
docker compose up -d --build frontend

# Or full rebuild
docker compose down
docker compose up -d --build
```

### Issue: Using wrong URL in production

**Cause:** Frontend built with development URL

**Solution:** Use production URL in .env before deployment:
```bash
# .env.production
VITE_API_BASE_URL=https://api.production.com/api

# Build for production
docker build --build-arg VITE_API_BASE_URL=https://api.production.com/api \
  -t opsfinder-frontend:prod ./frontend
```

## Best Practices

### Development
- Use `http://localhost:8080/api` for npm dev server
- Use `http://host.docker.internal:8080/api` for Docker Desktop

### Staging
- Use staging domain: `https://api.staging.your-domain.com/api`
- Separate .env.staging file for clarity

### Production
- Always use HTTPS: `https://api.your-domain.com/api`
- Use environment-specific .env files
- Verify URL before deployment
- Test API connectivity after deployment

### Docker Deployment
- Use Docker service names when services are in same compose file
- Use external URLs when services are on different hosts
- Document the expected VITE_API_BASE_URL in deployment guides

## Related Configuration

### Backend CORS Configuration

The backend must allow requests from the frontend origin. In `application-prod.yml`:

```yaml
allowed:
  origins: ${ALLOWED_ORIGINS:http://localhost}
```

Update `.env`:
```bash
# For Docker deployment
ALLOWED_ORIGINS=http://localhost

# For production
ALLOWED_ORIGINS=https://your-domain.com,https://www.your-domain.com
```

### WebSocket Configuration

WebSocket connections also use the base URL. The `websocket.service.ts` constructs the WebSocket URL from the base URL:

```typescript
const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'
const wsUrl = baseURL.replace(/^http/, 'ws').replace(/\/api$/, '/ws')
```

Ensure your backend URL supports WebSocket upgrades.

## Additional Resources

- **Main Deployment Guide:** [DEPLOYMENT.md](DEPLOYMENT.md)
- **Docker Deployment Options:** [DOCKER_DEPLOYMENT.md](DOCKER_DEPLOYMENT.md)
- **Windows Development:** [WINDOWS_FRONTEND_BUILD.md](WINDOWS_FRONTEND_BUILD.md)
- **External Database Setup:** [EXTERNAL_DATABASE.md](EXTERNAL_DATABASE.md)
- **Vite Environment Variables:** https://vitejs.dev/guide/env-and-mode.html
