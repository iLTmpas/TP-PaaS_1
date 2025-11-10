# Worker Replication

Application Spring Boot pour la réplication automatique de données entre PostgreSQL et Redis.

## Description

Ce projet est un worker de réplication qui synchronise automatiquement les données des employés depuis PostgreSQL vers Redis. La réplication est effectuée de manière planifiée toutes les minutes pour maintenir un cache Redis à jour.

## Technologies

- **Java**: 21
- **Spring Boot**: 3.5.7
- **PostgreSQL**: 16
- **Redis**: 7-alpine
- **Kafka**: 7.5.0 (avec Zookeeper)
- **Maven**: 3.x

## Architecture

L'application suit une architecture en couches :

```
fr.episen.worker.replication/
├── config/          # Configuration Redis
├── entity/          # Entités JPA
├── repository/      # Repositories Spring Data
├── scheduler/       # Tâches planifiées
└── service/         # Logique métier
```

## Fonctionnalités

- Réplication automatique toutes les minutes (configurable via cron)
- Synchronisation des employés de PostgreSQL vers Redis
- Stockage en cache Redis avec structure Hash et Set
- Logs détaillés des opérations de réplication
- Gestion des erreurs et retry

## Prérequis

- Java 21 ou supérieur
- Maven 3.x
- Docker et Docker Compose

## Installation

### 1. Cloner le projet

```bash
git clone <url-du-repo>
cd worker-replication
```

### 2. Démarrer l'infrastructure Docker

```bash
docker-compose up -d
```

Cela démarre :
- **PostgreSQL** sur le port `5432`
- **Redis** sur le port `6379`
- **RedisInsight** (interface web) sur `http://localhost:5540`
- **Kafka** sur le port `9092`
- **Zookeeper** sur le port `2181`
- **Kafka UI** sur `http://localhost:8080`

### 3. Compiler le projet

```bash
./mvnw clean install
```

### 4. Lancer l'application

```bash
./mvnw spring-boot:run
```

L'application démarre sur le port `9090`.

## Configuration

### Base de données PostgreSQL

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/mydatabase
    username: myuser
    password: mypassword
```

### Redis

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: yourStrongPasswordHere
```

### Réplication planifiée

La réplication s'exécute toutes les minutes (configurable dans `ReplicationScheduler.java`) :

```java
@Scheduled(cron = "0 */1 * * * *") // Toutes les minutes
```

## Structure de données

### PostgreSQL - Table `employes`

| Colonne  | Type    | Description           |
|----------|---------|----------------------|
| id       | BIGINT  | Clé primaire         |
| nom      | VARCHAR | Nom de l'employé     |
| prenom   | VARCHAR | Prénom de l'employé  |
| mail     | VARCHAR | Email (unique)       |
| valide   | BOOLEAN | Statut de validation |

### Redis - Structure

- **Hash** : `employe:{id}` contient les données de chaque employé
  - `id` : identifiant
  - `valide` : statut
- **Set** : `employes:all` contient la liste de tous les IDs d'employés

## Utilisation

### Ajouter un employé en base

Vous pouvez ajouter des employés directement dans PostgreSQL :

```sql
INSERT INTO employes (nom, prenom, mail, valide)
VALUES ('Doe', 'John', 'john.doe@example.com', true);
```

Les données seront automatiquement répliquées vers Redis à la prochaine exécution du scheduler.

### Vérifier la réplication dans Redis

Connectez-vous à RedisInsight (`http://localhost:5540`) ou utilisez redis-cli :

```bash
docker exec -it redis-cache redis-cli -a yourStrongPasswordHere
```

```redis
# Lister tous les employés
SMEMBERS employes:all

# Récupérer un employé spécifique
HGETALL employe:1
```

## Logs

L'application génère des logs détaillés :

```
=== Début de la réplication planifiée ===
Début de la réplication de tous les employés vers Redis
Réplication terminée: 10 employés répliqués sur 10
=== Réplication planifiée terminée avec succès: 10 employés répliqués ===
```

## Interfaces de monitoring

- **RedisInsight**: `http://localhost:5540` - Visualisation des données Redis
- **Kafka UI**: `http://localhost:8080` - Gestion et monitoring de Kafka

## Développement

### Structure du code

- `EmployeEntity.java` : Entité JPA représentant un employé
- `EmployeRepository.java` : Repository Spring Data JPA
- `PgToRedisService.java` : Service de réplication PostgreSQL → Redis
- `ReplicationScheduler.java` : Scheduler de réplication automatique
- `RedisConfig.java` : Configuration de Redis

### Modifier la fréquence de réplication

Éditez le cron dans `ReplicationScheduler.java` :

```java
@Scheduled(cron = "0 */5 * * * *") // Toutes les 5 minutes
```

## Tests

Exécuter les tests :

```bash
./mvnw test
```

## Production

### Build de l'application

```bash
./mvnw clean package
```

Le JAR sera généré dans `target/worker-replication-0.0.1-SNAPSHOT.jar`.

### Exécution en production

```bash
java -jar target/worker-replication-0.0.1-SNAPSHOT.jar
```

### Variables d'environnement recommandées

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://production-db:5432/dbname
export SPRING_DATASOURCE_USERNAME=produser
export SPRING_DATASOURCE_PASSWORD=***
export SPRING_DATA_REDIS_HOST=production-redis
export SPRING_DATA_REDIS_PASSWORD=***
```

## Dépannage

### PostgreSQL ne démarre pas

```bash
docker-compose down -v
docker-compose up -d db
```

### Redis ne se connecte pas

Vérifiez le mot de passe dans `application.yml` et `docker-compose.yml`.

### Les données ne se répliquent pas

Consultez les logs de l'application :

```bash
./mvnw spring-boot:run
```

## Licence

Ce projet est développé dans le cadre d'un projet EPISEN.

## Auteur

EPISEN - Projet Worker Replication
