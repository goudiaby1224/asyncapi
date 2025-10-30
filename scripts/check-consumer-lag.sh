#!/bin/bash

# Script to check Kafka consumer lag

KAFKA_BROKER="${KAFKA_BROKER:-localhost:9092}"
CONSUMER_GROUP="${CONSUMER_GROUP:-message-consumer-group}"

echo "========================================="
echo "Kafka Consumer Lag Monitor"
echo "========================================="
echo "Broker: $KAFKA_BROKER"
echo "Consumer Group: $CONSUMER_GROUP"
echo ""

if docker ps | grep -q kafka; then
    docker exec -it kafka kafka-consumer-groups \
        --bootstrap-server localhost:9092 \
        --group "$CONSUMER_GROUP" \
        --describe
    
    echo ""
    echo "Legend:"
    echo "  CURRENT-OFFSET: Current position of consumer"
    echo "  LOG-END-OFFSET: Latest message in partition"
    echo "  LAG: Number of messages not yet consumed"
    echo ""
else
    echo "❌ Error: Kafka container is not running"
    echo "Start it with: docker-compose up -d kafka"
    exit 1
fi

