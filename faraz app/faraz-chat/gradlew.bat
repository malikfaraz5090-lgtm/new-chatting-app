@echo off
set DIR=%~dp0
if "%DIR%"=="" set DIR=.\

setlocal
set JAVA_HOME=%JAVA_HOME%
if "%JAVA_HOME%"=="" (
  where java >nul 2>nul
  if errorlevel 1 (
    echo ERROR: Java not found. Install JDK 17+ and make sure java is on PATH.
    exit /b 1
  )
)

call "%DIR%gradlew" %*
