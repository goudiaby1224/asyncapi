package com.asyncapi.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MessagePayloadTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldSerializeToJson() throws Exception {
        // Given
        MessagePayload payload = MessagePayload.builder()
            .id("msg-001")
            .content("Test content")
            .timestamp(1698768000000L)
            .source("test-source")
            .build();

        // When
        String json = objectMapper.writeValueAsString(payload);

        // Then
        assertThat(json).contains("\"id\":\"msg-001\"");
        assertThat(json).contains("\"content\":\"Test content\"");
        assertThat(json).contains("\"timestamp\":1698768000000");
        assertThat(json).contains("\"source\":\"test-source\"");
    }

    @Test
    void shouldDeserializeFromJson() throws Exception {
        // Given
        String json = "{\"id\":\"msg-001\",\"content\":\"Test content\"," +
                     "\"timestamp\":1698768000000,\"source\":\"test-source\"}";

        // When
        MessagePayload payload = objectMapper.readValue(json, MessagePayload.class);

        // Then
        assertThat(payload.getId()).isEqualTo("msg-001");
        assertThat(payload.getContent()).isEqualTo("Test content");
        assertThat(payload.getTimestamp()).isEqualTo(1698768000000L);
        assertThat(payload.getSource()).isEqualTo("test-source");
    }

    @Test
    void shouldCreateWithBuilder() {
        // When
        MessagePayload payload = MessagePayload.builder()
            .id("test-id")
            .content("test-content")
            .timestamp(123456789L)
            .source("test-source")
            .build();

        // Then
        assertThat(payload).isNotNull();
        assertThat(payload.getId()).isEqualTo("test-id");
        assertThat(payload.getContent()).isEqualTo("test-content");
        assertThat(payload.getTimestamp()).isEqualTo(123456789L);
        assertThat(payload.getSource()).isEqualTo("test-source");
    }
}

