@echo off
call gradlew cle publishToMavenLocal -q
call gradlew :examplelibrary:generateDebugPojoBoosterStubs -q
