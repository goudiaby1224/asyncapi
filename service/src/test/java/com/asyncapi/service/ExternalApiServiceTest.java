package com.asyncapi.service;

import com.asyncapi.model.MessagePayload;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class ExternalApiServiceTest {

    private MockWebServer mockWebServer;
    private ExternalApiService externalApiService;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        WebClient webClient = WebClient.builder()
            .baseUrl(mockWebServer.url("/").toString())
            .build();

        externalApiService = new ExternalApiService(webClient);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void sendMessage_ShouldReturnSuccessResponse_WhenApiRespondsSuccessfully() throws InterruptedException {
        // Given
        MessagePayload payload = MessagePayload.builder()
            .id("test-001")
            .content("Test message")
            .timestamp(System.currentTimeMillis())
            .source("unit-test")
            .build();

        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{\"status\":\"success\"}")
            .addHeader("Content-Type", "application/json"));

        // When
        Mono<String> result = externalApiService.sendMessage(payload);

        // Then
        StepVerifier.create(result)
            .expectNext("{\"status\":\"success\"}")
            .verifyComplete();

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("POST");
        assertThat(recordedRequest.getPath()).isEqualTo("/api/messages");
    }

    @Test
    void sendMessage_ShouldHandleError_WhenApiReturnsError() {
        // Given
        MessagePayload payload = MessagePayload.builder()
            .id("test-002")
            .content("Test error message")
            .timestamp(System.currentTimeMillis())
            .source("unit-test")
            .build();

        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(500)
            .setBody("Internal Server Error"));

        // When
        Mono<String> result = externalApiService.sendMessage(payload);

        // Then
        StepVerifier.create(result)
            .expectError()
            .verify();
    }
}

