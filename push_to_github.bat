@echo off
echo =================================================================
echo Periodontal AI Web App - Push to GitHub
echo =================================================================
echo.
echo This script will push your local changes to your GitHub repository.
echo Please make sure you have set the correct remote origin using:
echo   git remote set-url origin <your-github-repo-url>
echo.
echo Current Remote URL config:
git remote -v
echo.
echo If a GitHub login window pops up, please authenticate to complete the push.
echo.
pause
echo.
echo Running: git push -u origin master...
echo.
git push -u origin master
echo.
if %ERRORLEVEL% EQU 0 (
    echo [SUCCESS] Pushed successfully to GitHub!
    echo Your GitHub Actions workflow is now running.
) else (
    echo [ERROR] Push failed. If this is a new repository with conflicting files,
    echo we can try a force push. Press any key to try a force push, or close this window.
    pause
    echo.
    echo Running: git push -f -u origin master...
    git push -f -u origin master
)
echo.
echo =================================================================
pause
