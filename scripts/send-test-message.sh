#!/bin/bash

# Script to send test messages to Kafka topic

KAFKA_BROKER="${KAFKA_BROKER:-localhost:9092}"
TOPIC_NAME="${TOPIC_NAME:-message-topic}"

echo "========================================="
echo "Kafka Test Message Producer"
echo "========================================="
echo "Broker: $KAFKA_BROKER"
echo "Topic: $TOPIC_NAME"
echo ""

# Check if message ID is provided
if [ -z "$1" ]; then
    MESSAGE_ID="test-$(date +%s)"
else
    MESSAGE_ID="$1"
fi

# Check if content is provided
if [ -z "$2" ]; then
    CONTENT="Test message sent at $(date)"
else
    CONTENT="$2"
fi

TIMESTAMP=$(date +%s)000
SOURCE="${3:-bash-script}"

# Create JSON message
MESSAGE=$(cat <<EOF
{"id":"$MESSAGE_ID","content":"$CONTENT","timestamp":$TIMESTAMP,"source":"$SOURCE"}
EOF
)

echo "Sending message:"
echo "$MESSAGE"
echo ""

# Send message to Kafka using Docker
if docker ps | grep -q kafka; then
    echo "$MESSAGE" | docker exec -i kafka kafka-console-producer \
        --broker-list localhost:9092 \
        --topic "$TOPIC_NAME"
    
    echo ""
    echo "✅ Message sent successfully!"
    echo "Check application logs to verify processing."
else
    echo "❌ Error: Kafka container is not running"
    echo "Start it with: docker-compose up -d kafka"
    exit 1
fi

echo ""
echo "To verify message was consumed:"
echo "  docker-compose logs -f | grep '$MESSAGE_ID'"
echo ""
echo "To check consumer lag:"
echo "  docker exec -it kafka kafka-consumer-groups --bootstrap-server localhost:9092 --group message-consumer-group --describe"

