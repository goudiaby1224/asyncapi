# Testing Guide

This guide provides detailed information on testing the Kafka AsyncAPI application.

## Test Architecture

### 1. Unit Tests
Located in: `src/test/java/com/asyncapi/`

#### ExternalApiServiceTest
- Tests the REST API client service
- Uses MockWebServer for HTTP mocking
- Verifies request/response handling

#### MessagePayloadTest
- Tests JSON serialization/deserialization
- Validates model builder pattern
- Ensures data integrity

### 2. Integration Tests (Cucumber BDD)
Located in: `src/test/java/com/asyncapi/cucumber/`

#### Test Infrastructure
- **Embedded Kafka**: In-memory Kafka broker
- **WireMock**: HTTP server mocking
- **Spring Test Context**: Full application context

#### Test Configuration
- `CucumberSpringConfiguration`: Main test context setup
- `TestKafkaConfiguration`: Kafka producer for tests
- `TestObjectMapperConfiguration`: JSON mapper configuration

## Running Tests

### Run All Tests
```bash
mvn clean test
```

### Run Only Unit Tests
```bash
mvn test -Dtest=*Test
```

### Run Only Cucumber Tests
```bash
mvn test -Dtest=CucumberTestRunner
```

### Run with Specific Profile
```bash
mvn test -Dspring.profiles.active=test
```

### Run with Debug Logging
```bash
mvn test -Dlogging.level.com.asyncapi=DEBUG
```

## Test Scenarios

### Scenario 1: Single Message Consumption

**Given**: External REST API is available  
**When**: A message is published to Kafka topic  
**Then**: 
- Message is consumed within 10 seconds
- REST API receives correct payload
- Offset is committed successfully

**Test Data**:
```json
{
  "id": "test-message-001",
  "content": "Hello from Kafka integration test",
  "timestamp": 1698768000000,
  "source": "cucumber-test"
}
```

### Scenario 2: Multiple Messages Consumption

**Given**: External REST API is available  
**When**: Multiple messages are published  
**Then**: 
- All messages consumed within 15 seconds
- REST API receives all messages
- All offsets committed successfully

**Test Data**: 3 messages with different IDs and content

## Test Reports

### Cucumber HTML Report
```
target/cucumber-reports/cucumber.html
```
Open in browser to view:
- Scenario execution results
- Step-by-step details
- Execution time
- Pass/fail statistics

### Cucumber JSON Report
```
target/cucumber-reports/cucumber.json
```
Machine-readable format for CI/CD integration

### Maven Surefire Report
```
target/surefire-reports/
```
Contains XML and TXT reports for all tests

## Debugging Tests

### Enable Debug Logging

Add to `application-test.yml`:
```yaml
logging:
  level:
    com.asyncapi: DEBUG
    org.springframework.kafka: DEBUG
    org.springframework.cloud.stream: DEBUG
    com.github.tomakehurst.wiremock: DEBUG
```

### View Embedded Kafka Logs
```bash
mvn test -Dlogging.level.org.apache.kafka=DEBUG
```

### WireMock Request Logging
WireMock automatically logs all requests in test output.

## Common Test Issues

### 1. Port Conflicts
**Problem**: `Address already in use`  
**Solution**: Stop services using conflicting ports

### 2. Kafka Startup Timeout
**Problem**: Embedded Kafka fails to start  
**Solution**: 
- Increase timeout in `@EmbeddedKafka`
- Check system resources
- Clean Maven build: `mvn clean`

### 3. WireMock Stubs Not Working
**Problem**: 404 errors in tests  
**Solution**:
- Verify stub URL matches exactly
- Check WireMock port configuration
- Reset stubs in `@Before` method

### 4. Async Timing Issues
**Problem**: Assertions fail due to timing  
**Solution**:
- Increase Awaitility timeout
- Add proper polling intervals
- Use `await().untilAsserted()`

## CI/CD Integration

### GitHub Actions Example
```yaml
name: Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 17
        uses: actions/setup-java@v2
        with:
          java-version: '17'
      - name: Run tests
        run: mvn clean test
      - name: Publish Test Report
        uses: dorny/test-reporter@v1
        if: always()
        with:
          name: Test Results
          path: target/cucumber-reports/cucumber.json
          reporter: java-junit
```

### Jenkins Pipeline Example
```groovy
pipeline {
    agent any
    stages {
        stage('Test') {
            steps {
                sh 'mvn clean test'
            }
        }
        stage('Publish Reports') {
            steps {
                cucumber buildStatus: 'UNSTABLE',
                    reportTitle: 'Cucumber Report',
                    fileIncludePattern: '**/cucumber.json',
                    trendsLimit: 10
            }
        }
    }
}
```

## Performance Testing

### Load Testing Kafka Consumer

Create multiple messages:
```java
@Test
void loadTest() {
    for (int i = 0; i < 1000; i++) {
        MessagePayload payload = MessagePayload.builder()
            .id("msg-" + i)
            .content("Load test message " + i)
            .timestamp(System.currentTimeMillis())
            .source("load-test")
            .build();
        
        kafkaTemplate.send(TOPIC_NAME, objectMapper.writeValueAsString(payload));
    }
}
```

## Best Practices

1. **Always Reset State**: Use `@Before` to reset WireMock and clear data
2. **Use Awaitility**: For async operations, don't use `Thread.sleep()`
3. **Meaningful Test Data**: Use descriptive IDs and content
4. **Verify All Aspects**: Check message content, API calls, and commits
5. **Isolate Tests**: Each scenario should be independent
6. **Clean Up Resources**: Close consumers in `@After` methods

## Coverage Reports

### Generate Coverage Report
```bash
mvn clean test jacoco:report
```

### View Coverage
```
target/site/jacoco/index.html
```

### Coverage Goals
- Line Coverage: > 80%
- Branch Coverage: > 70%
- Integration Tests: Cover all happy paths and error scenarios

## Troubleshooting Commands

### Check Kafka Topics (if using local Kafka)
```bash
kafka-topics.sh --list --bootstrap-server localhost:9092
```

### View Consumer Groups
```bash
kafka-consumer-groups.sh --bootstrap-server localhost:9092 --list
```

### Check Consumer Lag
```bash
kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
  --group message-consumer-group --describe
```

## Manual Testing

For manual testing with real Kafka:

1. Start Kafka locally
2. Run the application: `mvn spring-boot:run`
3. Publish test message:
```bash
kafka-console-producer.sh --broker-list localhost:9092 \
  --topic message-topic

# Then paste:
{"id":"manual-001","content":"Manual test","timestamp":1698768000000,"source":"manual"}
```

4. Monitor logs: `tail -f logs/application.log`
5. Verify external API received message

## Additional Resources

- [Cucumber Documentation](https://cucumber.io/docs/cucumber/)
- [Spring Cloud Stream Testing](https://docs.spring.io/spring-cloud-stream/docs/current/reference/html/spring-cloud-stream.html#_testing)
- [WireMock Documentation](http://wiremock.org/docs/)
- [Awaitility Guide](https://github.com/awaitility/awaitility/wiki/Usage)

