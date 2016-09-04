@echo off
call gradlew cle publishToMavenLocal -q
REM call gradlew :examplelibrary:generateDebugPojoBoosterStubs -q
call gradlew :ex:aDeb :test:cJ -s

