Feature: Kafka Message Consumption and REST API Integration
  As a system
  I want to consume messages from Kafka
  And send them to an external REST API
  So that the messages are processed correctly

  Scenario: Successfully consume message from Kafka and send to REST API
    Given the external REST API is available
    When a message is published to the Kafka topic with the following data:
      | id        | test-message-001                |
      | content   | Hello from Kafka integration test |
      | timestamp | 1698768000000                   |
      | source    | cucumber-test                   |
    Then the message should be consumed from Kafka within 10 seconds
    And the REST API should receive the message with correct payload
    And the message offset should be committed successfully

  Scenario: Consume multiple messages and verify all are sent to REST API
    Given the external REST API is available
    When the following messages are published to the Kafka topic:
      | id               | content              | timestamp     | source         |
      | test-msg-001     | First test message   | 1698768000000 | test-source-1  |
      | test-msg-002     | Second test message  | 1698768001000 | test-source-2  |
      | test-msg-003     | Third test message   | 1698768002000 | test-source-3  |
    Then all 3 messages should be consumed from Kafka within 15 seconds
    And the REST API should have received 3 messages
    And all message offsets should be committed successfully

