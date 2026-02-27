@echo off
echo ========================================
echo Smart Garden Simulation - Quick Start
echo ========================================
echo.

REM Check if Java is available
java -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java is not installed or not in PATH!
    echo.
    echo Please install Java 21:
    echo 1. See INSTALL_JAVA_WINDOWS.md for instructions
    echo 2. Or visit: https://adoptium.net/temurin/releases/?version=21
    echo.
    pause
    exit /b 1
)

echo Java found! Checking version...
java -version
echo.

REM Check if JAVA_HOME is set
if "%JAVA_HOME%"=="" (
    echo WARNING: JAVA_HOME is not set!
    echo Maven wrapper will try to use Java from PATH...
    echo.
)

echo Building project...
echo.
call mvnw.cmd clean install -DskipTests
if errorlevel 1 (
    echo.
    echo BUILD FAILED!
    echo Check the error messages above.
    pause
    exit /b 1
)

echo.
echo ========================================
echo Build successful! Starting application...
echo ========================================
echo.
call mvnw.cmd javafx:run

pause

