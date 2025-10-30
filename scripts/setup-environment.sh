#!/bin/bash

# Setup script for local development environment

set -e

echo "========================================="
echo "Kafka AsyncAPI - Environment Setup"
echo "========================================="
echo ""

# Check prerequisites
echo "📋 Checking prerequisites..."
echo ""

# Check Java
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
    echo "✅ Java $JAVA_VERSION detected"
else
    echo "❌ Java not found. Please install Java 17 or higher."
    exit 1
fi

# Check Maven
if command -v mvn &> /dev/null; then
    MAVEN_VERSION=$(mvn -version | head -n 1 | cut -d' ' -f3)
    echo "✅ Maven $MAVEN_VERSION detected"
else
    echo "❌ Maven not found. Please install Maven 3.6 or higher."
    exit 1
fi

# Check Docker
if command -v docker &> /dev/null; then
    DOCKER_VERSION=$(docker --version | cut -d' ' -f3 | tr -d ',')
    echo "✅ Docker $DOCKER_VERSION detected"
else
    echo "❌ Docker not found. Please install Docker."
    exit 1
fi

# Check Docker Compose
if command -v docker-compose &> /dev/null; then
    COMPOSE_VERSION=$(docker-compose --version | cut -d' ' -f4 | tr -d ',')
    echo "✅ Docker Compose $COMPOSE_VERSION detected"
else
    echo "❌ Docker Compose not found. Please install Docker Compose."
    exit 1
fi

echo ""
echo "========================================="
echo "📦 Building Application"
echo "========================================="
echo ""

mvn clean package -DskipTests

echo ""
echo "========================================="
echo "🐳 Starting Docker Services"
echo "========================================="
echo ""

docker-compose up -d

echo ""
echo "⏳ Waiting for services to be ready..."
sleep 10

echo ""
echo "========================================="
echo "✅ Environment Setup Complete!"
echo "========================================="
echo ""
echo "Services running:"
echo "  • Kafka Broker: localhost:9092"
echo "  • Kafka UI: http://localhost:8090"
echo "  • Mock API: http://localhost:9999"
echo ""
echo "Next steps:"
echo "  1. Run application: mvn spring-boot:run"
echo "  2. Send test message: ./scripts/send-test-message.sh"
echo "  3. Check consumer lag: ./scripts/check-consumer-lag.sh"
echo "  4. View Kafka UI: open http://localhost:8090"
echo ""
echo "To stop services: docker-compose down"
echo ""

