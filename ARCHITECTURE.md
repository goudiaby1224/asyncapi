# Architecture Documentation

## System Architecture

### High-Level Overview

```
┌─────────────────────────────────────────────────────────────────────┐
│                     Kafka AsyncAPI Application                      │
└─────────────────────────────────────────────────────────────────────┘

External Systems              Application Layer              Internal Components
─────────────────────────────────────────────────────────────────────────

┌──────────────┐            ┌──────────────────┐           ┌─────────────┐
│   Kafka      │            │  Spring Boot     │           │  Jackson    │
│   Broker     │───────────▶│  Application     │◀──────────│  Mapper     │
│  (Topic)     │  consume   │                  │  JSON     │             │
└──────────────┘            └──────────────────┘           └─────────────┘
                                     │
                                     │ process
                                     ▼
                            ┌──────────────────┐
                            │  Message         │
                            │  Consumer        │
                            │  (Stream)        │
                            └──────────────────┘
                                     │
                                     │ forward
                                     ▼
                            ┌──────────────────┐           ┌─────────────┐
                            │  External API    │◀──────────│  WebClient  │
┌──────────────┐            │  Service         │  REST     │  (Reactive) │
│  External    │◀───────────│                  │           │             │
│  REST API    │  HTTP POST │                  │           └─────────────┘
└──────────────┘            └──────────────────┘
```

## Component Architecture

### 1. Consumer Layer

**MessageConsumer** (Stream-based)
```
┌─────────────────────────────────────────────────────────┐
│                   MessageConsumer                        │
│                                                          │
│  @Bean Consumer<Message<String>>                        │
│  ┌────────────────────────────────────────────┐         │
│  │  1. Receive Message from Kafka             │         │
│  │  2. Parse JSON → MessagePayload            │         │
│  │  3. Call ExternalApiService                │         │
│  │  4. Block until response received          │         │
│  │  5. Auto-commit offset on success          │         │
│  └────────────────────────────────────────────┘         │
│                                                          │
│  Error Handling:                                        │
│  • Throw exception → prevents commit                   │
│  • Message remains in queue for retry                  │
└─────────────────────────────────────────────────────────┘
```

### 2. Service Layer

**ExternalApiService** (REST Client)
```
┌─────────────────────────────────────────────────────────┐
│               ExternalApiService                         │
│                                                          │
│  Uses: WebClient (Spring WebFlux)                       │
│  ┌────────────────────────────────────────────┐         │
│  │  POST /api/messages                        │         │
│  │  • Body: MessagePayload (JSON)             │         │
│  │  • Response: Mono<String>                  │         │
│  │  • Logging: Success/Error                  │         │
│  └────────────────────────────────────────────┘         │
│                                                          │
│  Features:                                              │
│  • Non-blocking (Reactive)                             │
│  • Error handling with doOnError                       │
│  • Structured logging                                  │
└─────────────────────────────────────────────────────────┘
```

### 3. Configuration Layer

```
┌──────────────────┐    ┌──────────────────┐    ┌──────────────────┐
│ WebClientConfig  │    │ ObjectMapper     │    │  application.yml │
│                  │    │ Config           │    │                  │
│ • Base URL       │    │                  │    │ • Kafka brokers  │
│ • Timeout        │    │ • JSON settings  │    │ • Topic name     │
│ • Headers        │    │ • Serialization  │    │ • Consumer group │
└──────────────────┘    └──────────────────┘    └──────────────────┘
```

## Data Flow

### Message Processing Flow

```
Step 1: Message Arrival
─────────────────────────────────────────────────────────────
Kafka Topic: message-topic
Message: {"id":"msg-001","content":"Hello",...}

                    ↓

Step 2: Spring Cloud Stream Binding
─────────────────────────────────────────────────────────────
Binding: messageConsumer-in-0
Function: messageConsumer (Consumer<Message<String>>)

                    ↓

Step 3: Message Parsing
─────────────────────────────────────────────────────────────
Input: String (JSON)
Parser: Jackson ObjectMapper
Output: MessagePayload object

                    ↓

Step 4: External API Call
─────────────────────────────────────────────────────────────
Client: WebClient (WebFlux)
Method: POST
URL: {base-url}/api/messages
Body: MessagePayload (JSON)

                    ↓

Step 5: Response Handling
─────────────────────────────────────────────────────────────
Success: Log response
Error: Log error, throw exception

                    ↓

Step 6: Offset Commit
─────────────────────────────────────────────────────────────
Success: Auto-commit offset
Error: Offset NOT committed (message reprocessed)
```

## Spring Cloud Stream Binding

### Functional Programming Model

```
┌─────────────────────────────────────────────────────────────┐
│              Spring Cloud Stream Architecture               │
└─────────────────────────────────────────────────────────────┘

Application Configuration:
┌──────────────────────────────────────────────────────────┐
│ spring.cloud.stream:                                     │
│   function:                                              │
│     definition: messageConsumer                          │
│   bindings:                                              │
│     messageConsumer-in-0:                                │
│       destination: message-topic                         │
│       group: message-consumer-group                      │
└──────────────────────────────────────────────────────────┘

Function Bean:
┌──────────────────────────────────────────────────────────┐
│ @Bean                                                    │
│ public Consumer<Message<String>> messageConsumer() {     │
│     return message -> {                                  │
│         // Process message                               │
│     };                                                   │
│ }                                                        │
└──────────────────────────────────────────────────────────┘

Binding Convention:
• Function name: messageConsumer
• Input binding: messageConsumer-in-0
• Output binding: messageConsumer-out-0 (if Function<T,R>)
```

## Testing Architecture

### Test Infrastructure

```
┌─────────────────────────────────────────────────────────────┐
│                    Test Environment                          │
└─────────────────────────────────────────────────────────────┘

Unit Tests:
┌──────────────────────────────────────────────────────────┐
│ • ExternalApiServiceTest                                 │
│   └─ MockWebServer (OkHttp)                              │
│ • MessagePayloadTest                                     │
│   └─ Jackson ObjectMapper                                │
└──────────────────────────────────────────────────────────┘

Integration Tests (Cucumber):
┌──────────────────────────────────────────────────────────┐
│ Test Infrastructure:                                     │
│ ┌──────────────────┐  ┌──────────────────┐              │
│ │ Embedded Kafka   │  │ WireMock Server  │              │
│ │ • In-memory      │  │ • HTTP mocking   │              │
│ │ • Auto-start     │  │ • Dynamic port   │              │
│ │ • Isolated       │  │ • Stub config    │              │
│ └──────────────────┘  └──────────────────┘              │
│                                                          │
│ Test Context:                                            │
│ • Full Spring Boot application                          │
│ • Real message processing                               │
│ • Actual Kafka consumer                                 │
│ • Real WebClient (pointing to WireMock)                 │
└──────────────────────────────────────────────────────────┘

Test Flow:
┌──────────────────────────────────────────────────────────┐
│ 1. Cucumber scenario starts                              │
│ 2. WireMock stub configured                              │
│ 3. Test produces message to embedded Kafka              │
│ 4. Application consumer receives message                │
│ 5. WebClient calls WireMock                              │
│ 6. Test verifies WireMock received correct payload     │
│ 7. Test verifies offset committed                       │
└──────────────────────────────────────────────────────────┘
```

## Configuration Management

### Profile-based Configuration

```
Development (default):
┌──────────────────────────────────────────────────────────┐
│ application.yml                                          │
│ • Kafka: localhost:9092                                  │
│ • External API: http://localhost:9999                    │
│ • Logging: DEBUG                                         │
└──────────────────────────────────────────────────────────┘

Testing:
┌──────────────────────────────────────────────────────────┐
│ application-test.yml                                     │
│ • Kafka: ${spring.embedded.kafka.brokers}                │
│ • External API: ${wiremock.server.port}                  │
│ • Logging: DEBUG                                         │
└──────────────────────────────────────────────────────────┘

Production (example):
┌──────────────────────────────────────────────────────────┐
│ application-prod.yml                                     │
│ • Kafka: kafka-cluster.prod:9092                         │
│ • External API: https://api.production.com               │
│ • Logging: INFO                                          │
│ • Security: SSL enabled                                  │
└──────────────────────────────────────────────────────────┘
```

## Error Handling Strategy

### Consumer Error Handling

```
┌─────────────────────────────────────────────────────────────┐
│                 Error Handling Flow                          │
└─────────────────────────────────────────────────────────────┘

Scenario 1: Parsing Error
┌──────────────────────────────────────────────────────────┐
│ Invalid JSON → Exception → Logged → Offset NOT committed │
│ Result: Message reprocessed                              │
└──────────────────────────────────────────────────────────┘

Scenario 2: External API Error
┌──────────────────────────────────────────────────────────┐
│ API 500 → doOnError → Exception → Offset NOT committed  │
│ Result: Message reprocessed                              │
└──────────────────────────────────────────────────────────┘

Scenario 3: Success
┌──────────────────────────────────────────────────────────┐
│ API 200 → doOnSuccess → Normal flow → Auto-commit       │
│ Result: Message marked as processed                      │
└──────────────────────────────────────────────────────────┘

Future Enhancement: Dead Letter Queue (DLQ)
┌──────────────────────────────────────────────────────────┐
│ After N retries → Move to DLQ topic                      │
│ Manual review and reprocessing                           │
└──────────────────────────────────────────────────────────┘
```

## Scalability Considerations

### Horizontal Scaling

```
Single Instance:
┌──────────────┐
│  Consumer 1  │ ──▶ Partition 0
└──────────────┘

Multiple Instances (Partitioned Topic):
┌──────────────┐
│  Consumer 1  │ ──▶ Partition 0
└──────────────┘
┌──────────────┐
│  Consumer 2  │ ──▶ Partition 1
└──────────────┘
┌──────────────┐
│  Consumer 3  │ ──▶ Partition 2
└──────────────┘

Benefits:
• Load distribution across instances
• Parallel processing
• Higher throughput
• Fault tolerance
```

## Monitoring Points

```
Application Metrics:
┌──────────────────────────────────────────────────────────┐
│ • Messages consumed/sec                                  │
│ • Processing time per message                            │
│ • External API response time                             │
│ • Error rate                                             │
│ • Success rate                                           │
└──────────────────────────────────────────────────────────┘

Kafka Metrics:
┌──────────────────────────────────────────────────────────┐
│ • Consumer lag                                           │
│ • Offset position                                        │
│ • Partition assignment                                   │
│ • Consumer group health                                  │
└──────────────────────────────────────────────────────────┘

Infrastructure Metrics:
┌──────────────────────────────────────────────────────────┐
│ • JVM heap usage                                         │
│ • Thread count                                           │
│ • CPU usage                                              │
│ • Network I/O                                            │
└──────────────────────────────────────────────────────────┘
```

## Technology Decisions

### Why These Technologies?

| Technology | Reason |
|-----------|--------|
| Spring Cloud Stream | Abstraction, maintainability, testability |
| Functional Model | Clean code, easy testing, modern approach |
| WebFlux | Non-blocking, efficient, reactive |
| Cucumber BDD | Business-readable tests, living documentation |
| Embedded Kafka | Fast tests, no external dependencies |
| WireMock | Reliable API mocking, full control |
| Docker Compose | Easy local development, reproducible |

### Alternative Considerations

| Alternative | Not Chosen Because |
|------------|-------------------|
| Kafka Streams | Overkill for simple consume-forward pattern |
| REST Template | Blocking, less efficient than WebClient |
| Manual Kafka Consumer | More code, less abstraction |
| TestContainers | Slower than Embedded Kafka for tests |

## Security Considerations

### Current Implementation
- No authentication (development mode)
- Plain text communication
- No encryption

### Production Requirements
```
Kafka Security:
• SASL/SSL authentication
• Encrypted communication
• ACLs for topic access

External API Security:
• OAuth2 or API key
• HTTPS only
• Rate limiting

Application Security:
• Secrets management (Vault, K8s secrets)
• No credentials in code
• Encrypted configuration
```

## Performance Characteristics

```
Expected Performance:
┌──────────────────────────────────────────────────────────┐
│ Throughput: ~1000 messages/sec (single instance)         │
│ Latency: 50-100ms (depends on external API)              │
│ Memory: ~512MB heap                                       │
│ CPU: Low (I/O bound, not CPU bound)                      │
└──────────────────────────────────────────────────────────┘

Bottlenecks:
1. External API response time (blocking call)
2. Network latency
3. Kafka broker throughput

Optimization Options:
• Batch processing
• Async API calls (remove .block())
• Connection pooling
• Caching (if applicable)
```

---

## Summary

This architecture demonstrates:
✅ Modern Spring Boot patterns (functional, reactive)  
✅ Clean separation of concerns  
✅ Comprehensive testing strategy  
✅ Production-ready error handling  
✅ Scalable design  
✅ Well-documented components

