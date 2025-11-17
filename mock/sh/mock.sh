#!/bin/bash

if [ "$#" -ne 1 ]; then
    echo "Usage: $0 <nombre_d_essai>"
    exit 1
fi

FILE="IDs.txt"
IP_MQTT="172.31.253.240"

TOTAL_LINES=$(wc -l < "$FILE")

for i in $(seq 1 $1)
do
        NUMERO_LIGNE=$(( RANDOM % TOTAL_LINES + 1 ))
        CONETENU_LIGNE=$(sed -n "${NUMERO_LIGNE}p" "$FILE")
        sleep $(( RANDOM % 10 + 2 ))
        mosquitto_pub -h $IP_MQTT -t lock/telemetry -m '{"badgeId":'$CONETENU_LIGNE'}'
        echo "requete à:$IP_MQTT ID:$CONETENU_LIGNE fait à $(date +"%H:%M:%S")" >> mock.log
done