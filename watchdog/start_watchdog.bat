@echo off
REM Script de démarrage du Watchdog pour Windows

REM Vérifier que Python 3 est installé
python --version >nul 2>&1
if errorlevel 1 (
    echo Python 3 is required but not installed
    exit /b 1
)

REM Vérifier que SSH est disponible
where ssh >nul 2>&1
if errorlevel 1 (
    echo SSH is required but not installed
    echo Please install OpenSSH or use Windows Subsystem for Linux (WSL)
    exit /b 1
)

REM Aller au répertoire du watchdog
cd /d "%~dp0"

REM Installer les dépendances si nécessaire
echo Installing dependencies...
pip install -r requirements.txt

REM Lancer le watchdog
echo Starting Watchdog Service...
python watchdog.py

pause
