FROM python:3.11-slim

# Définir le répertoire de travail
WORKDIR /app

# Installer les dépendances système
RUN apt-get update && apt-get install -y \
    openssh-client \
    && rm -rf /var/lib/apt/lists/*

# Copier les fichiers de l'application
COPY requirements.txt .
COPY config.py .
COPY watchdog.py .
COPY config.json .

# Installer les dépendances Python
RUN pip install --no-cache-dir -r requirements.txt

# Créer le répertoire de logs
RUN mkdir -p /var/log/watchdog && chmod 777 /var/log/watchdog

# Définir les volumes pour les logs et la configuration
VOLUME ["/var/log/watchdog", "/app/config.json"]

# Lancer le watchdog
CMD ["python", "watchdog.py"]
