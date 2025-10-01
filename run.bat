@echo off
rem IBM Cloud Object Storage Client
rem Launch script for Windows

set JAR_FILE=ibm-cos-client.jar
set JAVA_OPTS=-Xmx512m -Dswing.aatext=true -Dawt.useSystemAAFontSettings=on

rem Check if Java is installed
java -version >nul 2>&1
if errorlevel 1 (
    echo Error: Java is not installed or not in PATH
    echo Please install Java 8 or higher and try again
    pause
    exit /b 1
)

rem Check if JAR file exists
if not exist "%JAR_FILE%" (
    echo Error: %JAR_FILE% not found
    echo Please make sure the JAR file is in the same directory as this script
    pause
    exit /b 1
)

rem Launch the application
echo Starting IBM Cloud Object Storage Client...
java %JAVA_OPTS% -jar "%JAR_FILE%"
pause