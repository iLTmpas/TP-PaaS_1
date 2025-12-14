#!/bin/bash
# Script de démarrage du Watchdog pour Linux/Mac

# Vérifier que Python 3 est installé
if ! command -v python3 &> /dev/null; then
    echo "Python 3 is required but not installed"
    exit 1
fi

# Vérifier que SSH est disponible
if ! command -v ssh &> /dev/null; then
    echo "SSH is required but not installed"
    exit 1
fi

# Aller au répertoire du watchdog
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" &> /dev/null && pwd)"
cd "$SCRIPT_DIR"

# Installer les dépendances si nécessaire
echo "Installing dependencies..."
pip3 install -r requirements.txt

# Lancer le watchdog
echo "Starting Watchdog Service..."
python3 watchdog.py
