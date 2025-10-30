# Kafka AsyncAPI Application

A Spring Boot application that demonstrates asynchronous message consumption from Kafka using Spring Cloud Stream with comprehensive BDD testing.

## Features

- **Kafka Consumer**: Stream-based listener using Spring Cloud Stream functional programming model
- **REST API Integration**: Automatically forwards consumed messages to an external REST API
- **Comprehensive Testing**: Cucumber BDD tests with embedded Kafka and WireMock
- **Auto-commit**: Messages are automatically committed after successful processing

## Technology Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Cloud Stream 2023.0.0**
- **Apache Kafka**
- **Spring WebFlux** (for REST calls)
- **Cucumber 7.15.0** (BDD testing)
- **WireMock 3.3.1** (REST API mocking)
- **Embedded Kafka** (for integration tests)

## Architecture

### Message Flow

```
Kafka Topic (message-topic)
    ↓
Spring Cloud Stream Consumer (messageConsumer)
    ↓
Parse JSON Message (MessagePayload)
    ↓
External API Service (WebClient)
    ↓
REST POST to /api/messages
    ↓
Auto-commit offset on success
```

## Project Structure

```
src/
├── main/
│   ├── java/com/asyncapi/
│   │   ├── KafkaAsyncApiApplication.java       # Main application
│   │   ├── config/
│   │   │   ├── ObjectMapperConfig.java         # Jackson configuration
│   │   │   └── WebClientConfig.java            # WebClient setup
│   │   ├── consumer/
│   │   │   └── MessageConsumer.java            # Kafka stream consumer
│   │   ├── model/
│   │   │   └── MessagePayload.java             # Message model
│   │   └── service/
│   │       └── ExternalApiService.java         # REST client service
│   └── resources/
│       └── application.yml                      # Application configuration
└── test/
    ├── java/com/asyncapi/
    │   ├── config/
    │   │   ├── TestKafkaConfiguration.java     # Test Kafka setup
    │   │   └── TestObjectMapperConfiguration.java
    │   └── cucumber/
    │       ├── CucumberSpringConfiguration.java # Cucumber test config
    │       ├── CucumberTestRunner.java          # Test runner
    │       └── steps/
    │           └── MessageConsumptionSteps.java # Step definitions
    └── resources/
        ├── application-test.yml                 # Test configuration
        └── features/
            └── message-consumption.feature      # BDD scenarios

```

## Message Format

The application consumes JSON messages with the following structure:

```json
{
  "id": "test-message-001",
  "content": "Hello from Kafka",
  "timestamp": 1698768000000,
  "source": "source-system"
}
```

## Configuration

### Application Configuration (application.yml)

Key configurations:
- **Kafka Broker**: `localhost:9092`
- **Topic**: `message-topic`
- **Consumer Group**: `message-consumer-group`
- **External API Base URL**: `http://localhost:9999`

### Test Configuration (application-test.yml)

- Uses embedded Kafka broker
- WireMock server on dynamic port
- Auto-configured for integration testing

## Running the Application

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- Apache Kafka (for local running)

### Build the Project

```bash
mvn clean install
```

### Run the Application

```bash
mvn spring-boot:run
```

### Run Tests Only

```bash
mvn test
```

## Testing

### BDD Test Scenarios

The project includes comprehensive Cucumber BDD tests that verify:

#### Scenario 1: Single Message Consumption
- ✅ Message is published to Kafka topic
- ✅ Consumer receives and processes the message
- ✅ Message is sent to external REST API with correct payload
- ✅ Kafka offset is committed successfully

#### Scenario 2: Multiple Message Consumption
- ✅ Multiple messages are published to Kafka
- ✅ All messages are consumed and processed
- ✅ All messages are sent to external REST API
- ✅ All offsets are committed successfully

### Test Infrastructure

1. **Embedded Kafka**: Automatically started for each test
2. **WireMock Server**: Mocks external REST API responses
3. **Awaitility**: Ensures async operations complete before assertions

### Running Cucumber Tests

```bash
# Run all tests
mvn test

# Run specific feature
mvn test -Dcucumber.features="src/test/resources/features/message-consumption.feature"
```

### Test Reports

After running tests, reports are generated at:
- HTML Report: `target/cucumber-reports/cucumber.html`
- JSON Report: `target/cucumber-reports/cucumber.json`

## API Endpoints

### Consumer Endpoint

The application doesn't expose REST endpoints. It's a pure Kafka consumer that:
1. Listens to `message-topic`
2. Processes incoming messages
3. Forwards to external API

### External API (Mocked in Tests)

- **POST** `/api/messages`
  - Receives message payload
  - Returns success response

## Error Handling

- Failed messages throw `RuntimeException` preventing offset commit
- Messages remain in Kafka for retry
- Detailed logging at DEBUG level for troubleshooting

## Logging

Configure logging levels in `application.yml`:

```yaml
logging:
  level:
    com.asyncapi: DEBUG
    org.springframework.cloud.stream: DEBUG
    org.apache.kafka: INFO
```

## Development

### Adding New Message Types

1. Create new model in `com.asyncapi.model`
2. Update consumer logic in `MessageConsumer`
3. Add corresponding BDD scenarios

### Customizing REST Client

Modify `ExternalApiService` to:
- Change endpoint URLs
- Add authentication headers
- Implement retry logic
- Add circuit breaker

## Deployment

### Docker Deployment (Future Enhancement)

```bash
# Build Docker image
docker build -t kafka-async-api .

# Run with Docker Compose
docker-compose up
```

### Environment Variables

Override configuration using environment variables:
- `SPRING_KAFKA_BOOTSTRAP_SERVERS`: Kafka broker address
- `EXTERNAL_API_BASE_URL`: External API endpoint

## Monitoring

### Kafka Consumer Metrics

Monitor consumer health:
- Lag monitoring
- Throughput metrics
- Error rates

### Spring Boot Actuator (Add if needed)

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

## Troubleshooting

### Consumer Not Receiving Messages

1. Check Kafka broker connectivity
2. Verify topic name matches configuration
3. Check consumer group offset position
4. Enable DEBUG logging

### External API Calls Failing

1. Verify base URL configuration
2. Check network connectivity
3. Review error logs in `ExternalApiService`

### Tests Failing

1. Ensure no other services using test ports
2. Check embedded Kafka startup logs
3. Verify WireMock stub configuration

## Contributing

1. Fork the repository
2. Create feature branch
3. Add tests for new features
4. Ensure all tests pass
5. Submit pull request

## License

This project is licensed under the MIT License.

## Contact

For questions or support, please contact the development team.

---

**Note**: This is a demonstration project showcasing Spring Cloud Stream with Kafka and comprehensive BDD testing practices.

# asyncapi
