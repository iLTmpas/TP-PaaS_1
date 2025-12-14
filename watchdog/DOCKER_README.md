# Watchdog Service - Docker

Service de surveillance et de redémarrage automatique pour le core_backend.

## Configuration

### 1. Configurer les services à surveiller

Éditer le fichier `config.json` avec les paramètres de vos services :

```json
{
  "check_interval": 30,
  "max_consecutive_failures": 2,
  "recovery_wait_time": 15,
  "health_check_timeout": 10,
  "ssh_timeout": 30,
  "services": [
    {
      "name": "core_backend_1",
      "host": "192.168.1.100",
      "port": 8080,
      "ssh_host": "192.168.1.100",
      "ssh_port": 22,
      "ssh_user": "root",
      "ssh_mdp": "your_password",
      "docker_compose_path": "/path/to/core_backend",
      "docker_service_name": "core_backend",
      "docker_container_name": "core_backend",
      "use_docker_api_fallback": true
    }
  ]
}
```

## Déploiement

### Option 1: Avec Docker Compose (recommandé)

```bash
docker-compose up -d
```

### Option 2: Avec Docker manuellement

```bash
# Construire l'image
docker build -t watchdog:latest .

# Exécuter le conteneur
docker run -d \
  --name watchdog \
  --restart unless-stopped \
  -v /var/log/watchdog:/var/log/watchdog \
  -v $(pwd)/config.json:/app/config.json:ro \
  -v ~/.ssh:/root/.ssh:ro \
  watchdog:latest
```

## Vérifier les logs

```bash
# Afficher les derniers logs
docker logs watchdog

# Afficher les logs en continu
docker logs -f watchdog

# Accéder directement aux logs
tail -f /var/log/watchdog/watchdog.log
```

## Arrêter le watchdog

```bash
docker-compose down
# ou
docker stop watchdog
docker rm watchdog
```

## Reconstruire et relancer

```bash
docker-compose down
docker-compose build --no-cache
docker-compose up -d
```

## Notes

- Les logs sont stockés dans `/var/log/watchdog/watchdog.log` sur l'hôte
- La configuration se trouve dans `config.json` (monté en lecture seule)
- Les clés SSH doivent être présentes dans `~/.ssh/` pour la connexion SSH
- Le conteneur redémarre automatiquement en cas de crash
