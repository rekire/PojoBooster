@echo off
echo Publishing...
call gradlew cle publishToMavenLocal -q
if %errorlevel% neq 0 exit /b %errorlevel%
echo [Done]
echo.
echo Testing...
REM call gradlew :examplelibrary:generateDebugPojoBoosterStubs -q
call gradlew clean :ex:app:aDeb :ex:lib:aDeb :ex:java:jar -s

