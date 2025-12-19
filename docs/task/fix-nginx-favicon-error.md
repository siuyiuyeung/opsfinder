# Task: Fix Nginx Favicon 404 Error

## Problem Analysis

### Error Message
```
2025/12/19 07:25:10 [error] 31#31: *3 open() "/usr/share/nginx/html/favicon.ico" failed (2: No such file or directory),
client: 192.168.31.189, server: localhost, request: "GET /favicon.ico HTTP/1.1",
host: "192.168.31.115:81", referrer: "http://192.168.31.115:81/api/"
```

### Root Cause
The application uses a multi-container Docker architecture:
- **Frontend container**: Nginx serving Vue.js app from `/usr/share/nginx/html`
- **Backend container**: Spring Boot serving API on port 8080

The favicon.ico was placed in the **backend** (`src/main/resources/static/favicon.ico`), but browsers request it from the **frontend** nginx server. Nginx cannot find it in `/usr/share/nginx/html/`.

### Architecture Understanding
```
Browser Request: GET /favicon.ico
    ↓
Nginx (frontend container)
    → Looks in: /usr/share/nginx/html/favicon.ico ❌ NOT FOUND
    → Should be built into Vue app's dist folder
```

## Solution

Copy the favicon to the **frontend** Vue.js `public/` directory. Vite will automatically copy it to the build output, and nginx will serve it.

## Todo List
- [x] Copy favicon.ico to frontend/public/
- [x] Copy favicon.svg to frontend/public/
- [x] Remove favicon from backend (unnecessary)
- [x] Update task documentation
- [ ] Rebuild and test

## Implementation Notes

**Correct Location**: `frontend/public/favicon.ico`
- Files in `public/` are copied to dist root during build
- Nginx serves dist from `/usr/share/nginx/html/`
- Browser requests `/favicon.ico` → nginx serves from `/usr/share/nginx/html/favicon.ico` ✅

**Why Backend Location Was Wrong**:
- Spring Boot's static resources are served at `http://backend:8080/favicon.ico`
- But browsers request favicon from the frontend domain
- Nginx doesn't proxy favicon requests to backend

## Review

### Completed Actions
✅ Moved favicon files from backend to frontend
  - Source: `src/main/resources/static/` (removed)
  - Destination: `frontend/public/` (active)
✅ Both formats copied: `favicon.ico` (1.1KB) and `favicon.svg` (251 bytes)
✅ Removed unnecessary backend static folder

### Solution Details

**Before** (Incorrect):
```
src/main/resources/static/favicon.ico  → Spring Boot backend (port 8080)
                                       → Not accessible from nginx frontend
                                       → 404 error in nginx
```

**After** (Correct):
```
frontend/public/favicon.ico  → Vite build copies to dist/
                             → Docker build copies to /usr/share/nginx/html/
                             → Nginx serves at /favicon.ico ✅
```

### How It Works
1. **Development**: Vite dev server serves `frontend/public/` files at root
2. **Build**: `npm run build` copies `public/` contents to `dist/`
3. **Docker**: Frontend Dockerfile line 29 copies `dist/` to `/usr/share/nginx/html/`
4. **Runtime**: Nginx serves `/usr/share/nginx/html/favicon.ico` when browser requests `/favicon.ico`

### Files Changed
- ✅ Created: `frontend/public/favicon.ico`
- ✅ Created: `frontend/public/favicon.svg`
- ✅ Removed: `src/main/resources/static/` directory (no longer needed)

### Testing Steps
1. **Rebuild frontend Docker image**:
   ```bash
   docker compose build frontend
   ```

2. **Restart containers**:
   ```bash
   docker compose up -d
   ```

3. **Verify in browser**:
   - Open: `http://192.168.31.115:81/`
   - Check browser tab for magnifying glass icon
   - Check nginx error.log - no more 404 errors

4. **Verify file is served**:
   ```bash
   curl -I http://192.168.31.115:81/favicon.ico
   # Should return: HTTP/1.1 200 OK
   ```

### Why This Fix Works
- Favicons must be served by the **frontend** web server (nginx)
- Vue.js apps use `public/` directory for static assets that don't go through webpack/vite processing
- Vite automatically copies `public/` to build output root
- Nginx serves these files directly without proxying to backend
- This is the standard pattern for SPA applications
