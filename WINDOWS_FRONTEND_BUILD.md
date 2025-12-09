# Windows Frontend Build Guide

This guide explains how to build and run the OpsFinder frontend Docker container on Windows.

## Prerequisites

### Required Software
1. **Docker Desktop for Windows**
   - Download: https://www.docker.com/products/docker-desktop
   - Minimum version: 4.0.0
   - Requires Windows 10 64-bit: Pro, Enterprise, or Education (Build 19044 or higher)
   - Or Windows 11 64-bit

2. **WSL2 (Windows Subsystem for Linux 2)**
   - Docker Desktop will prompt you to install WSL2 if not already installed
   - Follow Docker Desktop installation wizard

### Verify Installation
Open Command Prompt or PowerShell and verify:
```cmd
docker --version
docker compose version
```

You should see output like:
```
Docker version 24.x.x
Docker Compose version v2.x.x
```

## Quick Start

### Option 1: Using Batch Scripts (Easiest)

#### 1. Build with Default Configuration
```cmd
build-frontend-windows.bat
```
This builds the frontend with default API URL: `http://localhost:8080/api`

#### 2. Build with Custom Backend URL
```cmd
build-frontend-windows.bat http://backend:8080/api
```

For external backend:
```cmd
build-frontend-windows.bat https://api.your-domain.com/api
```

#### 3. Run the Container
```cmd
run-frontend-windows.bat
```

Access the application at: http://localhost

### Option 2: Using Docker Commands

#### 1. Build the Image
```cmd
cd frontend
docker build --build-arg VITE_API_BASE_URL=http://localhost:8080/api -t opsfinder-frontend:local .
cd ..
```

#### 2. Run the Container
```cmd
docker run -d -p 80:80 --name opsfinder-frontend opsfinder-frontend:local
```

## Configuration Options

### Backend API URL Configuration

The frontend needs to know where the backend API is located. Configure this using the `VITE_API_BASE_URL` build argument:

**Local Development (Backend on Windows)**:
```cmd
build-frontend-windows.bat http://localhost:8080/api
```

**Docker Compose Deployment (Backend in Docker)**:
```cmd
build-frontend-windows.bat http://backend:8080/api
```

**Production with Custom Domain**:
```cmd
build-frontend-windows.bat https://api.your-domain.com/api
```

**Production with IP Address**:
```cmd
build-frontend-windows.bat http://192.168.1.100:8080/api
```

### Port Configuration

By default, the frontend runs on port 80. To use a different port:

```cmd
docker run -d -p 8080:80 --name opsfinder-frontend opsfinder-frontend:local
```

Access at: http://localhost:8080

## Common Scenarios

### Scenario 1: Full Docker Deployment
Backend and database in Docker, frontend in Docker:
```cmd
build-frontend-windows.bat http://backend:8080/api
run-frontend-windows.bat
```

### Scenario 2: Backend on Windows, Frontend in Docker
Backend running directly on Windows, frontend in Docker:
```cmd
build-frontend-windows.bat http://host.docker.internal:8080/api
run-frontend-windows.bat
```
Note: `host.docker.internal` is a special DNS name that resolves to the host machine from inside Docker.

### Scenario 3: Production Deployment
External production backend:
```cmd
build-frontend-windows.bat https://api.production.com/api
run-frontend-windows.bat
```

### Scenario 4: Development with Hot Reload
For development with hot reload, use npm directly instead of Docker:
```cmd
cd frontend
npm install
npm run dev
```
Then edit `frontend/.env`:
```
VITE_API_BASE_URL=http://localhost:8080/api
```

## Docker Container Management

### View Logs
```cmd
docker logs opsfinder-frontend
```

### Follow Logs (Live)
```cmd
docker logs -f opsfinder-frontend
```

### Stop Container
```cmd
docker stop opsfinder-frontend
```

### Start Stopped Container
```cmd
docker start opsfinder-frontend
```

### Restart Container
```cmd
docker restart opsfinder-frontend
```

### Remove Container
```cmd
docker stop opsfinder-frontend
docker rm opsfinder-frontend
```

### Remove Image
```cmd
docker rmi opsfinder-frontend:local
```

## Troubleshooting

### Issue: Build fails with "docker: command not found"
**Solution**: Docker Desktop is not installed or not running
- Install Docker Desktop from https://www.docker.com/products/docker-desktop
- Start Docker Desktop from Start Menu
- Wait for Docker Desktop to fully start (whale icon in system tray)

### Issue: Build fails with "Cannot connect to Docker daemon"
**Solution**: Docker Desktop is not running
- Open Docker Desktop from Start Menu
- Wait until the whale icon in system tray shows "Docker Desktop is running"

### Issue: Port 80 already in use
**Solution**: Another service is using port 80
```cmd
REM Option 1: Stop the conflicting service (e.g., IIS, Apache)
net stop http

REM Option 2: Use a different port
docker run -d -p 8080:80 --name opsfinder-frontend opsfinder-frontend:local
```

### Issue: Cannot access http://localhost
**Solution**: Check if container is running
```cmd
docker ps

REM If not listed, check for errors
docker logs opsfinder-frontend

REM Try restarting
docker restart opsfinder-frontend
```

### Issue: "502 Bad Gateway" or "Connection Refused"
**Solution**: Backend is not accessible
- Verify backend is running
- Check the API URL used during build
- Rebuild with correct API URL:
  ```cmd
  docker stop opsfinder-frontend
  docker rm opsfinder-frontend
  build-frontend-windows.bat http://correct-backend-url:8080/api
  run-frontend-windows.bat
  ```

### Issue: Frontend shows "Network Error"
**Solution**: API URL is incorrect
1. Check browser console (F12) for the actual API URL being used
2. Rebuild with correct URL:
   ```cmd
   build-frontend-windows.bat http://correct-url:8080/api
   ```

### Issue: Changes to frontend code not reflected
**Solution**: Rebuild the Docker image
```cmd
docker stop opsfinder-frontend
docker rm opsfinder-frontend
build-frontend-windows.bat
run-frontend-windows.bat
```

### Issue: "Error response from daemon: Conflict"
**Solution**: Container name already exists
```cmd
docker rm -f opsfinder-frontend
run-frontend-windows.bat
```

## Development Workflow

### 1. Initial Setup
```cmd
REM Clone repository
git clone <repository-url>
cd OpsFinder

REM Build frontend
build-frontend-windows.bat http://localhost:8080/api
```

### 2. Making Changes
```cmd
REM Edit files in frontend/src/
REM After changes, rebuild
build-frontend-windows.bat http://localhost:8080/api

REM Restart container
docker stop opsfinder-frontend
docker rm opsfinder-frontend
run-frontend-windows.bat
```

### 3. Development with Hot Reload (Recommended)
For active development, use Vite dev server instead of Docker:
```cmd
cd frontend

REM Create .env file
echo VITE_API_BASE_URL=http://localhost:8080/api > .env

REM Install dependencies (first time only)
npm install

REM Start dev server with hot reload
npm run dev
```
Access at: http://localhost:5173

Changes will auto-reload without rebuilding Docker image.

## Testing Different Backend URLs

### Test Local Backend
```cmd
build-frontend-windows.bat http://localhost:8080/api
run-frontend-windows.bat
```

### Test Docker Backend
```cmd
build-frontend-windows.bat http://backend:8080/api
run-frontend-windows.bat
```

### Test Production Backend
```cmd
build-frontend-windows.bat https://api.production.com/api
run-frontend-windows.bat
```

### Verify API URL in Running Container
```cmd
REM Check the built API URL
docker exec opsfinder-frontend cat /usr/share/nginx/html/assets/index-*.js | findstr VITE_API_BASE_URL
```

## Integration with Full Stack

### Running Full Stack on Windows

#### Option 1: All in Docker Compose
```cmd
REM Set environment variables
copy .env.example .env
REM Edit .env with your configuration

REM Deploy everything
docker compose up -d --build

REM Access frontend
start http://localhost
```

#### Option 2: Backend on Windows, Frontend in Docker
```cmd
REM Terminal 1: Start backend on Windows
./gradlew bootRun

REM Terminal 2: Build and run frontend in Docker
build-frontend-windows.bat http://host.docker.internal:8080/api
run-frontend-windows.bat
```

## Performance Tips

### Speed Up Builds
1. **Use Docker BuildKit**:
   ```cmd
   set DOCKER_BUILDKIT=1
   build-frontend-windows.bat
   ```

2. **Clean Docker Cache** (if builds are slow):
   ```cmd
   docker builder prune
   ```

3. **Pre-pull Base Images**:
   ```cmd
   docker pull node:20-alpine
   docker pull nginx:alpine
   ```

## Security Considerations

### Production Deployments
1. **Use HTTPS**: Always use `https://` URLs for production
2. **Environment Variables**: Never commit `.env` files with production secrets
3. **API URLs**: Use environment-specific API URLs
4. **Container Updates**: Regularly update base images:
   ```cmd
   docker pull node:20-alpine
   docker pull nginx:alpine
   build-frontend-windows.bat https://api.production.com/api
   ```

## Additional Resources

- **OpsFinder Documentation**: See `README.md`, `DEPLOYMENT.md`
- **Docker Documentation**: https://docs.docker.com
- **Vite Documentation**: https://vitejs.dev
- **Vue.js Documentation**: https://vuejs.org

## Support

If you encounter issues not covered here:
1. Check Docker Desktop logs (Settings → Troubleshoot → Show logs)
2. Check container logs: `docker logs opsfinder-frontend`
3. Verify backend connectivity: `curl http://localhost:8080/actuator/health`
4. Review DEPLOYMENT.md for full stack deployment guidance
