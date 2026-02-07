@echo off
echo Starting Distributed Task Scheduler (Scheduler Mode)...
echo.
echo This will start the scheduler on port 8080
echo Press Ctrl+C to stop
echo.

mvn spring-boot:run -Dspring-boot.run.profiles=scheduler

pause
