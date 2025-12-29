# Procédure de centralisation des logs

## Architecture

```
┌─────────────────────────┐         ┌─────────────────────────┐
│   VM Application        │         │   VM Monitoring         │
│                         │         │   172.31.250.178        │
│  ┌─────────────────┐    │         │                         │
│  │   Service 1     │    │         │  ┌─────────────────┐    │
│  └─────────────────┘    │         │  │     Loki        │    │
│  ┌─────────────────┐    │  HTTP   │  │    :3100        │    │
│  │   Service 2     │    │ ──────► │  └─────────────────┘    │
│  └─────────────────┘    │         │           │             │
│           │             │         │           ▼             │
│           ▼             │         │  ┌─────────────────┐    │
│  ┌─────────────────┐    │         │  │    Grafana      │    │
│  │    Promtail     │    │         │  │    :3000        │    │
│  └─────────────────┘    │         │  └─────────────────┘    │
└─────────────────────────┘         └─────────────────────────┘
```

## Ajouter un nouveau service

### Etape 1 : Configurer le service

Ajoutez les labels suivants dans votre `docker-compose.yml` :

```yaml
services:
  mon-nouveau-service:
    image: mon-image:latest
    container_name: mon-nouveau-service
    labels:
      logging: "promtail"           # OBLIGATOIRE - active la collecte des logs
      environment: "production"     # OPTIONNEL - pour filtrer par environnement
    # ... reste de votre configuration
```

### Etape 2 : Verifier que Promtail est present

Si Promtail n'est pas encore configure sur la VM, ajoutez-le dans `docker-compose.yml` :

```yaml
  promtail:
    image: grafana/promtail:latest
    container_name: promtail
    volumes:
      - ./promtail-config.yml:/etc/promtail/config.yml:ro
      - /var/run/docker.sock:/var/run/docker.sock:ro
    command: -config.file=/etc/promtail/config.yml
    restart: unless-stopped
```

### Etape 3 : Fichier promtail-config.yml

Si le fichier n'existe pas, creez `promtail-config.yml` :

```yaml
server:
  http_listen_port: 9080
  grpc_listen_port: 0

positions:
  filename: /tmp/positions.yaml

clients:
  - url: http://172.31.250.178:3100/loki/api/v1/push

scrape_configs:
  - job_name: docker
    docker_sd_configs:
      - host: unix:///var/run/docker.sock
        refresh_interval: 5s
    relabel_configs:
      - source_labels: [ '__meta_docker_container_label_logging' ]
        regex: promtail
        action: keep
      - source_labels: [ '__meta_docker_container_name' ]
        regex: '/(.*)'
        target_label: container
      - source_labels: [ '__meta_docker_container_label_com_docker_compose_service' ]
        target_label: service
      - source_labels: [ '__meta_docker_container_label_environment' ]
        target_label: env
```

### Etape 4 : Deployer

```bash
docker-compose up -d
```

## Visualiser les logs dans Grafana

### Acces

- URL : http://172.31.250.178:3000
- Login : `admin`
- Password : `admin`

### Requetes LogQL utiles

| Objectif                | Requete                                              |
|-------------------------|------------------------------------------------------|
| Logs d'un service       | `{container="nom-du-service"}`                       |
| Tous les logs           | `{job="docker"}`                                     |
| Filtrer par niveau      | `{container="nom-du-service"} \|= "ERROR"`           |
| Rechercher un texte     | `{container="nom-du-service"} \|= "texte recherche"` |
| Logs d'un environnement | `{env="production"}`                                 |

### Dashboard existant

Un dashboard est disponible : **Worker Replication - Logs**

- URL directe : http://172.31.250.178:3000/d/worker-logs/worker-replication-logs

## Logs structures JSON (recommande)

Pour une meilleure exploitation des logs, configurez votre application pour produire des logs JSON.

### Spring Boot

1. Ajoutez la dependance dans `pom.xml` :

```xml

<dependency>
    <groupId>net.logstash.logback</groupId>
    <artifactId>logstash-logback-encoder</artifactId>
    <version>8.0</version>
</dependency>
```

2. Creez `src/main/resources/logback-spring.xml` :

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <include resource="org/springframework/boot/logging/logback/defaults.xml"/>

    <springProperty scope="context" name="appName" source="spring.application.name"/>

    <appender name="JSON_CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <customFields>{"application":"${appName}"}</customFields>
        </encoder>
    </appender>

    <springProfile name="docker,prod">
        <root level="INFO">
            <appender-ref ref="JSON_CONSOLE"/>
        </root>
    </springProfile>
</configuration>
```

3. Activez le profil dans docker-compose :

```yaml
environment:
  - SPRING_PROFILES_ACTIVE=docker
```

## Checklist nouveau service

- [ ] Label `logging: "promtail"` ajoute au service
- [ ] Promtail present sur la VM
- [ ] `promtail-config.yml` configure avec la bonne URL Loki
- [ ] Service demarre avec `docker-compose up -d`
- [ ] Logs visibles dans Grafana avec `{container="nom-du-service"}`

## Depannage

### Les logs n'apparaissent pas

1. Verifier que le conteneur a le label :

```bash
docker inspect nom-du-service | grep -A5 Labels
```

2. Verifier que Promtail tourne :

```bash
docker logs promtail
```

3. Verifier la connectivite vers Loki :

```bash
curl http://172.31.250.178:3100/ready
```

### Promtail ne collecte pas les logs

Verifier les targets Promtail :

```bash
curl http://localhost:9080/targets
```
