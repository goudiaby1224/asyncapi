# Quick Start Guide

Get the Kafka AsyncAPI application up and running in minutes!

## Prerequisites Check

```bash
# Check Java version (need 17+)
java -version

# Check Maven version
mvn -version

# Check Docker (optional, for local Kafka)
docker --version
```

## Option 1: Run Tests Only (Fastest)

This uses embedded Kafka and doesn't require any external dependencies.

```bash
# Clone/navigate to project
cd asyncapi

# Run tests
mvn clean test

# View test results
open target/cucumber-reports/cucumber.html
```

✅ **Expected Result**: All tests pass (green)

## Option 2: Run with Docker (Recommended)

### Step 1: Start Infrastructure

```bash
# Start Kafka, Zookeeper, Mock API, and Kafka UI
docker-compose up -d

# Verify services are running
docker-compose ps
```

Services available:
- Kafka: `localhost:9092`
- Kafka UI: `http://localhost:8090`
- Mock API: `http://localhost:9999`

### Step 2: Build Application

```bash
mvn clean package -DskipTests
```

### Step 3: Run Application

```bash
mvn spring-boot:run
```

Expected output:
```
Started KafkaAsyncApiApplication in X seconds
```

### Step 4: Send Test Message

**Option A: Using Kafka UI**
1. Open `http://localhost:8090`
2. Navigate to Topics → `message-topic`
3. Click "Produce Message"
4. Enter message:
```json
{
  "id": "test-001",
  "content": "Hello from Kafka UI",
  "timestamp": 1698768000000,
  "source": "kafka-ui"
}
```
5. Click Send

**Option B: Using Kafka Console Producer**
```bash
docker exec -it kafka kafka-console-producer \
  --broker-list localhost:9092 \
  --topic message-topic

# Then paste this JSON and press Enter:
{"id":"test-002","content":"Hello from console","timestamp":1698768000000,"source":"console"}
```

**Option C: Using curl with Kafka REST Proxy**
(If you have Kafka REST Proxy installed)
```bash
curl -X POST http://localhost:8082/topics/message-topic \
  -H "Content-Type: application/vnd.kafka.json.v2+json" \
  -d '{
    "records": [{
      "value": {
        "id": "test-003",
        "content": "Hello from REST",
        "timestamp": 1698768000000,
        "source": "rest"
      }
    }]
  }'
```

### Step 5: Verify Processing

**Check Application Logs**:
```
Received message from Kafka: {"id":"test-001",...}
Sending message to external API: MessagePayload(id=test-001,...)
Successfully sent message. Response: {"status":"success"}
Message consumption and processing completed successfully
```

**Check Mock API Logs**:
```bash
docker-compose logs mock-api
```

### Step 6: Monitor

**Kafka UI Dashboard**:
- Consumer Groups: `http://localhost:8090/clusters/local/consumer-groups`
- Topics: `http://localhost:8090/clusters/local/topics`
- Messages: View consumed messages

**Application Metrics**:
```bash
# Watch logs in real-time
tail -f logs/application.log

# Check consumer group lag
docker exec -it kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --group message-consumer-group \
  --describe
```

### Step 7: Cleanup

```bash
# Stop application (Ctrl+C)

# Stop Docker services
docker-compose down

# Remove volumes (optional)
docker-compose down -v
```

## Option 3: Run with Local Kafka

If you have Kafka installed locally:

### Step 1: Start Kafka
```bash
# Start Zookeeper
zookeeper-server-start.sh config/zookeeper.properties

# Start Kafka (in new terminal)
kafka-server-start.sh config/server.properties
```

### Step 2: Create Topic
```bash
kafka-topics.sh --create \
  --bootstrap-server localhost:9092 \
  --replication-factor 1 \
  --partitions 1 \
  --topic message-topic
```

### Step 3: Start Mock API
```bash
docker run -d -p 9999:8080 \
  -v $(pwd)/wiremock:/home/wiremock \
  wiremock/wiremock:3.3.1 \
  --global-response-templating
```

### Step 4: Run Application
```bash
mvn spring-boot:run
```

### Step 5: Send Messages
```bash
kafka-console-producer.sh \
  --broker-list localhost:9092 \
  --topic message-topic

# Paste JSON messages
```

## Troubleshooting

### Application Won't Start

**Issue**: `Connection refused to localhost:9092`
```bash
# Check if Kafka is running
docker-compose ps kafka

# Restart Kafka
docker-compose restart kafka
```

**Issue**: `Port 8080 already in use`
```bash
# Change port in application.yml
server:
  port: 8081

# Or stop service using port 8080
lsof -ti:8080 | xargs kill -9
```

### Messages Not Being Consumed

**Issue**: Consumer not receiving messages
```bash
# 1. Check topic exists
docker exec -it kafka kafka-topics --list --bootstrap-server localhost:9092

# 2. Check if messages in topic
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic message-topic \
  --from-beginning

# 3. Check consumer group
docker exec -it kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --group message-consumer-group \
  --describe
```

### Tests Failing

```bash
# Clean and rebuild
mvn clean install

# Run with debug logging
mvn test -X

# Run single test
mvn test -Dtest=CucumberTestRunner
```

## Useful Commands

### Maven Commands
```bash
# Clean build
mvn clean install

# Skip tests
mvn clean package -DskipTests

# Run specific test
mvn test -Dtest=MessagePayloadTest

# Generate coverage report
mvn clean test jacoco:report
```

### Docker Commands
```bash
# View logs
docker-compose logs -f

# Restart service
docker-compose restart kafka

# Check status
docker-compose ps

# Remove everything
docker-compose down -v
```

### Kafka Commands
```bash
# List topics
docker exec -it kafka kafka-topics --list --bootstrap-server localhost:9092

# Describe topic
docker exec -it kafka kafka-topics --describe --topic message-topic --bootstrap-server localhost:9092

# Consumer groups
docker exec -it kafka kafka-consumer-groups --list --bootstrap-server localhost:9092

# Read from beginning
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic message-topic \
  --from-beginning
```

## Next Steps

1. **Customize Message Format**: Edit `MessagePayload.java`
2. **Add Error Handling**: Implement DLQ (Dead Letter Queue)
3. **Add Monitoring**: Integrate Prometheus/Grafana
4. **Scale Consumers**: Increase partition count
5. **Add Security**: Configure SSL/SASL authentication

## Quick Reference

### Project Structure
```
asyncapi/
├── src/main/java/com/asyncapi/    # Application code
├── src/test/                       # Tests
├── docker-compose.yml              # Docker services
├── pom.xml                         # Maven dependencies
└── README.md                       # Documentation
```

### Key Endpoints
- Application: `http://localhost:8080`
- Kafka: `localhost:9092`
- Kafka UI: `http://localhost:8090`
- Mock API: `http://localhost:9999`

### Important Configuration
- **Topic**: `message-topic`
- **Consumer Group**: `message-consumer-group`
- **External API**: `/api/messages`

## Support

For issues or questions:
1. Check logs: `docker-compose logs`
2. Review README.md
3. Check TESTING_GUIDE.md
4. Enable debug logging in application.yml

---

🎉 **Congratulations!** You're now ready to use the Kafka AsyncAPI application!

