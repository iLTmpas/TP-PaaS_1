#!/bin/bash
# ============================================================================
# Script d'initialisation Kafka - Création automatique des topics
# ============================================================================
# Ce script s'exécute automatiquement au démarrage du container kafka-init
# Il crée les topics nécessaires avec les configurations optimales
# ============================================================================

set -e  # Arrêt en cas d'erreur

echo "============================================"
echo "🚀 Initialisation des Topics Kafka"
echo "============================================"
echo ""

# Configuration
KAFKA_BROKER="kafka:29092"
MAX_RETRIES=30
RETRY_INTERVAL=2

# Fonction pour attendre que Kafka soit prêt
wait_for_kafka() {
    echo "⏳ Attente de Kafka sur $KAFKA_BROKER..."
    local retries=0

    while [ $retries -lt $MAX_RETRIES ]; do
        if kafka-broker-api-versions --bootstrap-server $KAFKA_BROKER > /dev/null 2>&1; then
            echo "✅ Kafka est prêt!"
            return 0
        fi

        retries=$((retries + 1))
        echo "   Tentative $retries/$MAX_RETRIES..."
        sleep $RETRY_INTERVAL
    done

    echo "❌ ERREUR: Kafka n'est pas accessible après $MAX_RETRIES tentatives"
    exit 1
}

# Fonction pour créer un topic
create_topic() {
    local topic_name=$1
    local partitions=$2
    local retention_ms=$3
    local segment_ms=$4
    local description=$5

    echo ""
    echo "📋 Création du topic: $topic_name"
    echo "   Description: $description"
    echo "   Partitions: $partitions"
    echo "   Retention: $((retention_ms / 86400000)) jours"

    # Vérifier si le topic existe déjà
    if kafka-topics --bootstrap-server $KAFKA_BROKER --list | grep -q "^${topic_name}$"; then
        echo "   ⚠️  Topic '$topic_name' existe déjà, skip..."
        return 0
    fi

    # Créer le topic
    kafka-topics --create \
        --bootstrap-server $KAFKA_BROKER \
        --topic $topic_name \
        --partitions $partitions \
        --replication-factor 1 \
        --config retention.ms=$retention_ms \
        --config segment.ms=$segment_ms \
        --config compression.type=lz4 \
        --config cleanup.policy=delete \
        --config min.insync.replicas=1

    if [ $? -eq 0 ]; then
        echo "   ✅ Topic '$topic_name' créé avec succès!"
    else
        echo "   ❌ ERREUR lors de la création du topic '$topic_name'"
        exit 1
    fi
}

# Fonction pour afficher les détails d'un topic
describe_topic() {
    local topic_name=$1
    echo ""
    echo "🔍 Détails du topic: $topic_name"
    kafka-topics --describe \
        --bootstrap-server $KAFKA_BROKER \
        --topic $topic_name
}

# ============================================================================
# MAIN - Création des topics
# ============================================================================

# 1. Attendre que Kafka soit prêt
wait_for_kafka

echo ""
echo "============================================"
echo "📝 Création des Topics"
echo "============================================"

# 2. Créer entrance-logs (logs d'entrées réussies)
create_topic \
    "entrance-logs" \
    6 \
    7776000000 \
    86400000 \
    "Logs des entrées autorisées (GRANTED) - Retention 90 jours"

# 3. Créer attempt-logs (logs de tentatives refusées)
create_topic \
    "attempt-logs" \
    6 \
    2592000000 \
    86400000 \
    "Logs des tentatives refusées (DENIED) - Retention 30 jours"

# 4. Créer telemetry-data (données IoT des capteurs)
create_topic \
    "telemetry-data" \
    12 \
    604800000 \
    3600000 \
    "Télémétrie IoT (status serrures, capteurs) - Retention 7 jours"

# 5. Créer exit-logs (logs de sorties - optionnel pour tracking occupancy)
create_topic \
    "exit-logs" \
    6 \
    2592000000 \
    86400000 \
    "Logs des sorties de zones - Retention 30 jours"

# 6. Créer audit-logs (logs d'administration)
create_topic \
    "audit-logs" \
    3 \
    15552000000 \
    86400000 \
    "Logs d'audit des actions admin - Retention 180 jours"

echo ""
echo "============================================"
echo "📊 Vérification des Topics Créés"
echo "============================================"
echo ""

# 7. Lister tous les topics
echo "📋 Liste des topics disponibles:"
kafka-topics --list --bootstrap-server $KAFKA_BROKER

# 8. Afficher les détails de chaque topic principal
describe_topic "entrance-logs"
describe_topic "attempt-logs"
describe_topic "telemetry-data"

echo ""
echo "============================================"
echo "✅ Initialisation Kafka Terminée!"
echo "============================================"
echo ""
echo "📊 Topics créés:"
echo "   • entrance-logs (6 partitions, 90j retention)"
echo "   • attempt-logs (6 partitions, 30j retention)"
echo "   • telemetry-data (12 partitions, 7j retention)"
echo "   • exit-logs (6 partitions, 30j retention)"
echo "   • audit-logs (3 partitions, 180j retention)"
echo ""
echo "🌐 Interface Kafka UI: http://localhost:8080"
echo ""
echo "💡 Commandes utiles:"
echo "   - Lister topics: docker exec kafka kafka-topics --list --bootstrap-server localhost:9092"
echo "   - Décrire topic: docker exec kafka kafka-topics --describe --topic entrance-logs --bootstrap-server localhost:9092"
echo "   - Producer test: docker exec kafka kafka-console-producer --topic entrance-logs --bootstrap-server localhost:9092"
echo "   - Consumer test: docker exec kafka kafka-console-consumer --topic entrance-logs --from-beginning --bootstrap-server localhost:9092"
echo ""

# Le container se termine après avoir créé les topics
exit 0