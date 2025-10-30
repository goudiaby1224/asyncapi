package com.asyncapi.service;

import com.asyncapi.model.MessagePayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class ExternalApiService {

    private static final Logger log = LoggerFactory.getLogger(ExternalApiService.class);
    
    private final WebClient webClient;
    
    public ExternalApiService(WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Sends the message payload to an external API endpoint
     * 
     * @param payload The message payload to send
     * @return Mono<String> Response from the external API
     */
    public Mono<String> sendMessage(MessagePayload payload) {
        log.info("Sending message to external API: {}", payload);
        
        return webClient.post()
                .uri("/api/messages")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(response -> log.info("Successfully sent message. Response: {}", response))
                .doOnError(error -> log.error("Error sending message to external API", error));
    }
}

