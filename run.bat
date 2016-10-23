@echo off
cls
echo Publishing...
call gradlew -Pfrom=scratch clean publishToMavenLocal -q
if %errorlevel% neq 0 exit /b %errorlevel%
echo [Done]
echo.
echo Testing...
REM call gradlew clean :ex:app:aDeb :ex:lib:aDeb :ex:java:jar -s
call gradlew clean check
