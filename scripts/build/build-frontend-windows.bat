@echo off
REM Build frontend Docker image on Windows
REM Usage: build-frontend-windows.bat [API_URL]
REM Example: build-frontend-windows.bat http://backend:8080/api
REM Example: build-frontend-windows.bat http://localhost:8080/api

REM Change to project root directory (2 levels up from scripts/build/)
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
    echo Or use: scripts\build\run-frontend-windows.bat
) else (
    echo.
    echo ========================================
    echo Build failed!
    echo ========================================
)
