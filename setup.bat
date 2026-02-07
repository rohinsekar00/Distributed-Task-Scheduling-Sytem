@echo off
REM Distributed Task Scheduler - Windows Setup Script
REM Run this script to set up the complete development environment

echo ========================================
echo Distributed Task Scheduler Setup
echo ========================================
echo.

REM Check Java
echo [1/6] Checking Java installation...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Java 17+ is not installed
    echo Please install Java from: https://adoptium.net/
    pause
    exit /b 1
)
echo ✓ Java found

REM Check Maven
echo [2/6] Checking Maven installation...
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Maven is not installed
    echo Please install Maven from: https://maven.apache.org/download.cgi
    pause
    exit /b 1
)
echo ✓ Maven found

REM Setup PostgreSQL
echo [3/6] Setting up PostgreSQL database...
echo Creating database and tables...
psql -U postgres -c "CREATE DATABASE taskscheduler;" 2>nul
echo ✓ PostgreSQL setup complete (if already exists, that's fine)

REM Check Redis
echo [4/6] Checking Redis...
redis-cli ping >nul 2>&1
if %errorlevel% neq 0 (
    echo WARNING: Redis is not running
    echo Please start Redis server manually
    echo Download from: https://github.com/microsoftarchive/redis/releases
) else (
    echo ✓ Redis is running
)

REM Check Kafka
echo [5/6] Checking Kafka...
echo NOTE: Kafka must be started manually:
echo   1. Download Kafka from: https://kafka.apache.org/downloads
echo   2. Extract to C:\kafka
echo   3. Start Zookeeper: bin\windows\zookeeper-server-start.bat config\zookeeper.properties
echo   4. Start Kafka: bin\windows\kafka-server-start.bat config\server.properties

REM Build project
echo [6/6] Building project...
call mvn clean install -DskipTests
if %errorlevel% neq 0 (
    echo ERROR: Maven build failed
    pause
    exit /b 1
)
echo ✓ Build successful

echo.
echo ========================================
echo Setup Complete!
echo ========================================
echo.
echo Next steps:
echo 1. Ensure PostgreSQL, Redis, and Kafka are running
echo 2. Run Scheduler: start-scheduler.bat
echo 3. Run Workers: start-worker.bat (can run multiple instances)
echo 4. Access API: http://localhost:8080
echo 5. View metrics: http://localhost:8080/actuator/metrics
echo.
pause
