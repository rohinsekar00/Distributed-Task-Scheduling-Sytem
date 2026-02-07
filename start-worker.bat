@echo off
set WORKER_ID=worker-%RANDOM%
echo Starting Worker Node: %WORKER_ID%
echo.
echo This worker will process tasks from the queue
echo Press Ctrl+C to stop
echo.

mvn spring-boot:run -Dspring-boot.run.profiles=worker -Dspring-boot.run.arguments="--WORKER_ID=%WORKER_ID%"

pause
