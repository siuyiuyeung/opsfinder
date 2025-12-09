@echo off
REM Run frontend Docker container on Windows

echo Stopping existing container if running...
docker stop opsfinder-frontend >nul 2>&1
docker rm opsfinder-frontend >nul 2>&1

echo Starting frontend container...
docker run -d -p 80:80 --name opsfinder-frontend opsfinder-frontend:local

IF %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo Frontend is running!
    echo ========================================
    echo.
    echo Access the application at: http://localhost
    echo.
    echo To stop: docker stop opsfinder-frontend
    echo To view logs: docker logs opsfinder-frontend
    echo To view logs (follow): docker logs -f opsfinder-frontend
) else (
    echo.
    echo ========================================
    echo Failed to start frontend!
    echo ========================================
    echo.
    echo Make sure you have built the image first:
    echo   build-frontend-windows.bat
)
