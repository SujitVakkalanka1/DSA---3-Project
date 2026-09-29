@echo off
title Talent Engine - DSA-3 (Team 14)
echo ====================================================================
echo   Resume-Job Matching ^& Talent-Marketplace Engine
echo   Course: Data Structures and Algorithms - 3 (25CS2103E)
echo   KL University, Hyderabad ^| Team 14 ^| Section 10
echo ====================================================================
echo.

where javac >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] 'javac' was not found in your PATH.
    echo Please install Java Development Kit (JDK 17 or higher) and set PATH.
    pause
    exit /b 1
)

echo [1/3] Creating output directory...
if not exist "bin" mkdir "bin"

echo [2/3] Compiling Java source files...
javac -encoding UTF-8 -d bin src/model/*.java src/dsa/*.java src/storage/*.java src/server/*.java src/Main.java
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Compilation failed. Please check the errors above.
    pause
    exit /b 1
)

echo [3/3] Starting Talent Engine HTTP Server on http://localhost:8080...
echo.
echo Press Ctrl+C in this terminal to stop the server at any time.
echo ====================================================================
echo.
java -cp bin Main
pause
