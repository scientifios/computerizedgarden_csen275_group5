@echo off
echo Building Smart Garden Simulation...
echo.

REM Check if Java is available
java -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java is not installed!
    echo See INSTALL_JAVA_WINDOWS.md for installation instructions.
    pause
    exit /b 1
)

echo Building with Maven wrapper...
call mvnw.cmd clean install
echo.
echo Build complete!
pause

