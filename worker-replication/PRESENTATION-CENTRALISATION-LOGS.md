# Centralisation des Logs avec Grafana Loki

## Sommaire

1. [Contexte et Problematique](#1-contexte-et-problematique)
2. [Solution Proposee](#2-solution-proposee)
3. [Architecture](#3-architecture)
4. [Les Composants](#4-les-composants)
5. [Mise en Place](#5-mise-en-place)
6. [Demonstration](#6-demonstration)
7. [Avantages](#7-avantages)
8. [Conclusion](#8-conclusion)

---

## 1. Contexte et Problematique

### Le contexte

Dans une architecture microservices, les applications sont deployees sur plusieurs machines virtuelles ou conteneurs.
Chaque service genere ses propres logs.

### Les problemes rencontres

| Probleme                 | Impact                                                                            |
|--------------------------|-----------------------------------------------------------------------------------|
| **Logs disperses**       | Les logs sont stockes localement sur chaque VM, difficile d'avoir une vue globale |
| **Acces complexe**       | Necessite de se connecter en SSH sur chaque machine pour consulter les logs       |
| **Pas de correlation**   | Impossible de suivre une requete qui traverse plusieurs services                  |
| **Recherche inefficace** | Utilisation de `grep` sur des fichiers volumineux                                 |
| **Pas d'historique**     | Les logs peuvent etre perdus lors du redemarrage des conteneurs                   |
| **Pas d'alerting**       | Aucune notification en cas d'erreur critique                                      |

### Exemple concret

```
Situation : Une erreur se produit en production

Sans centralisation :
1. Se connecter en SSH sur la VM 1 → chercher dans les logs
2. Se connecter en SSH sur la VM 2 → chercher dans les logs
3. Se connecter en SSH sur la VM 3 → chercher dans les logs
4. Correler manuellement les informations
5. Temps perdu : 30 minutes a 1 heure

Avec centralisation :
1. Ouvrir Grafana
2. Rechercher l'erreur sur tous les services
3. Temps : 2 minutes
```

---

## 2. Solution Proposee

### Stack technique : Grafana Loki

Nous avons choisi la stack **Grafana Loki** composee de trois outils :

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  Promtail   │ ──► │    Loki     │ ◄── │   Grafana   │
│ (Collecte)  │     │ (Stockage)  │     │ (Visualise) │
└─────────────┘     └─────────────┘     └─────────────┘
```

### Pourquoi Loki plutot qu'ELK ?

| Critere                 | ELK Stack                   | Grafana Loki                 |
|-------------------------|-----------------------------|------------------------------|
| **Ressources**          | Gourmand (Elasticsearch)    | Leger                        |
| **Complexite**          | 3 composants lourds         | Simple a deployer            |
| **Cout**                | Elevee (RAM, CPU, Stockage) | Faible                       |
| **Indexation**          | Indexe le contenu complet   | Indexe uniquement les labels |
| **Integration Grafana** | Possible mais separee       | Native                       |
| **Cas d'usage**         | Recherche full-text avancee | Logs applicatifs standards   |

**Conclusion** : Loki est ideal pour notre besoin de centralisation simple et efficace.

---

## 3. Architecture

### Vue globale

```
                           ┌─────────────────────────────────────┐
                           │      VM Monitoring                  │
                           │      172.31.250.178                 │
                           │                                     │
┌────────────────────┐     │   ┌───────────┐    ┌───────────┐   │
│ VM Application 1   │     │   │           │    │           │   │
│                    │     │   │   Loki    │◄───│  Grafana  │   │
│ ┌────────────────┐ │     │   │  :3100    │    │  :3000    │   │
│ │ worker-replica │ │     │   │           │    │           │   │
│ └────────────────┘ │     │   └─────▲─────┘    └───────────┘   │
│         │          │     │         │                          │
│         ▼          │     │         │                          │
│ ┌────────────────┐ │     │         │                          │
│ │   Promtail     │─┼─────┼─────────┘                          │
│ └────────────────┘ │ HTTP POST                                │
└────────────────────┘     │         ▲                          │
                           │         │                          │
┌────────────────────┐     │         │                          │
│ VM Application 2   │     │         │                          │
│                    │     │         │                          │
│ ┌────────────────┐ │     │         │                          │
│ │ autre-service  │ │     │         │                          │
│ └────────────────┘ │     │         │                          │
│         │          │     │         │                          │
│         ▼          │     │         │                          │
│ ┌────────────────┐ │     │         │                          │
│ │   Promtail     │─┼─────┼─────────┘                          │
│ └────────────────┘ │ HTTP POST                                │
└────────────────────┘     └─────────────────────────────────────┘
```

### Flux des donnees

```
1. Application          2. Docker             3. Promtail           4. Loki              5. Grafana
   genere un log   ──►     capture le    ──►     collecte et   ──►     stocke et    ──►     affiche
   (stdout)                log                   envoie               indexe               et recherche
```

---

## 4. Les Composants

### 4.1 Promtail - Le collecteur

**Role** : Collecte les logs et les envoie a Loki

**Fonctionnement** :

- Se connecte au socket Docker (`/var/run/docker.sock`)
- Decouvre automatiquement les conteneurs
- Filtre par labels (seuls les conteneurs avec `logging: "promtail"`)
- Envoie les logs a Loki via HTTP

**Configuration cle** :

```yaml
clients:
  - url: http://172.31.250.178:3100/loki/api/v1/push

scrape_configs:
  - job_name: docker
    docker_sd_configs:
      - host: unix:///var/run/docker.sock
    relabel_configs:
      - source_labels: [ '__meta_docker_container_label_logging' ]
        regex: promtail
        action: keep  # Ne garde que les conteneurs avec le label
```

### 4.2 Loki - Le stockage

**Role** : Stocke et indexe les logs

**Caracteristiques** :

- N'indexe que les **labels** (pas le contenu) → leger et rapide
- Compresse les logs pour economiser l'espace
- Supporte la retention configurable
- API compatible avec Prometheus

**Labels indexes** :

```
container="worker-replication"
service="core-backend"
env="production"
level="ERROR"
```

### 4.3 Grafana - La visualisation

**Role** : Interface web pour explorer et visualiser les logs

**Fonctionnalites** :

- Explore : recherche interactive des logs
- Dashboards : tableaux de bord personnalises
- Alerting : notifications en cas d'erreur
- Multi-datasources : peut combiner metriques et logs

---

## 5. Mise en Place

### Etape 1 : Deploiement de Loki et Grafana (VM Monitoring)

**Fichiers crees sur la VM 172.31.250.178** :

```
~/monitoring/
├── docker-compose.yml
├── loki-config.yml
└── grafana-datasources.yml
```

**docker-compose.yml** :

```yaml
version: '3.8'

services:
  loki:
    image: grafana/loki:latest
    container_name: loki
    ports:
      - "3100:3100"
    volumes:
      - ./loki-config.yml:/etc/loki/local-config.yaml:ro
      - loki-data:/loki
    command: -config.file=/etc/loki/local-config.yaml
    restart: unless-stopped

  grafana:
    image: grafana/grafana:latest
    container_name: grafana
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_USER=admin
      - GF_SECURITY_ADMIN_PASSWORD=admin
    volumes:
      - grafana-data:/var/lib/grafana
      - ./grafana-datasources.yml:/etc/grafana/provisioning/datasources/datasources.yml:ro
    restart: unless-stopped
    depends_on:
      - loki

volumes:
  loki-data:
  grafana-data:
```

**Demarrage** :

```bash
docker-compose up -d
```

---

### Etape 2 : Configuration de l'application (VM Application)

**Ajout de Promtail dans docker-compose.yml** :

```yaml
services:
  core-backend:
    # ... configuration existante ...
    labels:
      logging: "promtail"      # Active la collecte des logs
      environment: "production"

  promtail:
    image: grafana/promtail:latest
    container_name: promtail
    volumes:
      - ./promtail-config.yml:/etc/promtail/config.yml:ro
      - /var/run/docker.sock:/var/run/docker.sock:ro
    command: -config.file=/etc/promtail/config.yml
    restart: unless-stopped
```

**Demarrage** :

```bash
docker-compose up -d
```

---

### Etape 3 : Logs structures JSON (Optionnel mais recommande)

Pour faciliter le filtrage, nous avons configure l'application Spring Boot pour produire des logs JSON :

**Avant (logs classiques)** :

```
2025-12-14 10:30:00.123 INFO fr.episen.worker.Service - Message
```

**Apres (logs JSON)** :

```json
{
  "timestamp": "2025-12-14T10:30:00.123Z",
  "level": "INFO",
  "logger": "fr.episen.worker.Service",
  "message": "Message",
  "application": "worker-replication"
}
```

**Avantages** :

- Filtrage precis par champ
- Extraction automatique des labels
- Meilleure lisibilite dans Grafana

---

## 6. Demonstration

### 6.1 Acces a Grafana

- **URL** : http://172.31.250.178:3000
- **Login** : admin
- **Password** : admin

### 6.2 Explorer les logs

1. Cliquer sur **Explore** (icone boussole)
2. Selectionner **Loki** comme datasource
3. Entrer une requete LogQL

### 6.3 Requetes LogQL essentielles

| Objectif                     | Requete                                                |
|------------------------------|--------------------------------------------------------|
| Tous les logs d'un service   | `{container="worker-replication"}`                     |
| Filtrer les erreurs          | `{container="worker-replication"} \|= "ERROR"`         |
| Rechercher un texte          | `{container="worker-replication"} \|= "utilisateur"`   |
| Exclure un texte             | `{container="worker-replication"} != "DEBUG"`          |
| Expression reguliere         | `{container="worker-replication"} \|~ "user.*created"` |
| Logs des 5 dernieres minutes | Selecteur de temps en haut a droite                    |

### 6.4 Dashboard

Un dashboard pre-configure est disponible avec :

| Panel              | Description                                 |
|--------------------|---------------------------------------------|
| Logs en temps reel | Affiche tous les logs du service            |
| Erreurs            | Filtre uniquement les erreurs et exceptions |
| Warnings           | Filtre les avertissements                   |
| Volume de logs     | Graphique du nombre de logs par niveau      |

**Acces** : http://172.31.250.178:3000/d/worker-logs/worker-replication-logs

---

## 7. Avantages

### 7.1 Gains operationnels

| Avant                       | Apres                  |
|-----------------------------|------------------------|
| Connexion SSH sur chaque VM | Interface web unique   |
| `grep` dans les fichiers    | Recherche instantanee  |
| Pas d'historique            | Retention configurable |
| Aucune alerte               | Alerting integre       |

### 7.2 Gains pour le developpement

- **Debug facilite** : Correlation des logs entre services
- **Visibilite** : Vue temps reel de l'activite
- **Analyse** : Statistiques sur les erreurs

### 7.3 Scalabilite

- **Ajout simple** : Un label suffit pour integrer un nouveau service
- **Performance** : Loki gere des millions de lignes de logs
- **Cout maitrise** : Ressources minimales requises

---

## 8. Conclusion

### Ce qui a ete mis en place

| Composant     | Localisation           | Role                     |
|---------------|------------------------|--------------------------|
| **Loki**      | VM 172.31.250.178:3100 | Stockage des logs        |
| **Grafana**   | VM 172.31.250.178:3000 | Visualisation            |
| **Promtail**  | VM Application         | Collecte des logs        |
| **Dashboard** | Grafana                | Monitoring pre-configure |

### Procedure pour ajouter un service

```yaml
# 1. Ajouter le label au service
labels:
  logging: "promtail"

# 2. Redemarrer
  docker-compose up -d

# 3. Visualiser dans Grafana
  { container="nom-du-service" }
```

### Points cles a retenir

1. **Architecture decouplée** : Les services n'ont pas connaissance de Loki
2. **Zero configuration applicative** : Seul un label Docker est necessaire
3. **Centralisation** : Tous les logs accessibles depuis une interface unique
4. **Evolutif** : Facile d'ajouter de nouveaux services ou VMs

---

## Annexes

### A. URLs importantes

| Service        | URL                                      |
|----------------|------------------------------------------|
| Grafana        | http://172.31.250.178:3000               |
| Loki API       | http://172.31.250.178:3100               |
| Dashboard Logs | http://172.31.250.178:3000/d/worker-logs |

### B. Commandes utiles

```bash
# Verifier l'etat de Loki
curl http://172.31.250.178:3100/ready

# Voir les logs de Promtail
docker logs promtail

# Verifier les labels d'un conteneur
docker inspect nom-du-service | grep -A5 Labels
```

### C. Documentation officielle

- Grafana Loki : https://grafana.com/docs/loki/latest/
- Promtail : https://grafana.com/docs/loki/latest/clients/promtail/
- LogQL : https://grafana.com/docs/loki/latest/logql/
