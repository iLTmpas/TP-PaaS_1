#!/bin/bash
# Script de simulation de panne et de récupération pour un cluster Redis directement exécutable depuis la VM cache_slave
# Présentation de la mise en place du redis cluster : https://www.canva.com/design/DAG7iPuKFgU/xzLfKI5OLI8GBeYvl_Ukmg/edit?utm_content=DAG7iPuKFgU&utm_campaign=designshare&utm_medium=link2&utm_source=sharebutton
CACHE_URL="http://172.31.250.176:8083/cache/23"
REDIS_HOST="172.31.253.41"
MASTER_PORT="7001"
CHECK_PORT="7002"

echo "=============================="
echo "1 Etat initial du cluster"
echo "=============================="
redis-cli -c -h $REDIS_HOST -p $CHECK_PORT CLUSTER NODES | awk '{print $2,$3,$8}'
echo ""
sleep 12
echo "=============================="
echo "2 Test du cache AVANT panne"
echo "=============================="
curl -s $CACHE_URL
echo -e "\n"

sleep 5

echo "=============================="
echo "3 Arrêt du master Redis sur $REDIS_HOST:$MASTER_PORT"
echo "=============================="
redis-cli -h $REDIS_HOST -p $MASTER_PORT shutdown || true
echo "Master arrêté"
echo ""

echo "=============================="
echo "4 Etat du cluster après arrêt"
echo "=============================="
redis-cli -c -h $REDIS_HOST -p $CHECK_PORT CLUSTER NODES | awk '{print $2,$3,$8}'
echo ""

sleep 10

echo "=============================="
echo "5 Etat du cluster APRÈS panne"
echo "=============================="
redis-cli -c -h $REDIS_HOST -p $MASTER_PORT CLUSTER NODES | awk '{print $2, $3, $8}'
echo ""

sleep 5

echo "=============================="
echo "6 Test du cache APRÈS panne"
echo "=============================="
curl -s $CACHE_URL
echo -e "\n"
sleep 5
echo "=============================="
echo "7 Redémarrage du master Redis sur $REDIS_HOST:$MASTER_PORT"
echo "=============================="
redis-server ~/redis-cluster/$MASTER_PORT/redis.conf &
sleep 10
echo -e "\n"

echo "=============================="
echo "8 Etat du cluster après redémarrage"
echo "=============================="
redis-cli -c -h $REDIS_HOST -p $CHECK_PORT CLUSTER NODES | awk '{print $2,$3,$8}'
echo ""
sleep 15
echo "=============================="
echo "9 Test du cache après redémarrage"
echo "=============================="
curl -s $CACHE_URL
echo -e "\n"
sleep 5
echo "=============================="
echo " Démo terminée"
echo "=============================="
