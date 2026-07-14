@echo off
echo =================================================================
echo Periodontal AI System - Android Studio Environment Configurer
echo =================================================================
echo.

:: Define standard Android Studio JBR path
set "AS_JBR=C:\Program Files\Android\Android Studio\jbr"

if exist "%AS_JBR%\bin\java.exe" (
    echo [INFO] Found Android Studio JBR JDK at: %AS_JBR%
    
    :: Set environment variables for the current session
    set "JAVA_HOME=%AS_JBR%"
    echo [SUCCESS] Set JAVA_HOME to Android Studio JBR.
    echo.
    echo Verify Gradle can run using Android Studio's JBR...
    echo.
    call .\gradlew.bat help
    if %ERRORLEVEL% EQU 0 (
        echo.
        echo [SUCCESS] Gradle compiled successfully using Android Studio JBR.
        echo you can now build and run this mobile app locally using Android Studio.
    ) else (
        echo.
        echo [ERROR] Gradle invocation failed even with JBR. Please check if there is an error in build.gradle.kts files.
    )
) else (
    echo [WARNING] Android Studio JBR was not found at standard path: %AS_JBR%
    echo Please make sure Android Studio is installed and has the JetBrains Runtime ^(jbr^) bundled.
    echo If Android Studio is installed in a custom location, please edit this batch file with the custom path.
)

echo.
echo =================================================================
echo [INSTRUCTION] In Android Studio:
echo 1. Open the project 'Pocket Depth AI' or 'periodontal ai voice based app'.
echo 2. Go to File - Settings - Build, Execution, Deployment - Build Tools - Gradle.
echo 3. Set 'Gradle JDK' to the path:
echo    "C:\Program Files\Android\Android Studio\jbr" (JetBrains Runtime)
echo =================================================================
pause
