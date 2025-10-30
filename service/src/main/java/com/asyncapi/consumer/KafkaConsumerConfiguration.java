package com.asyncapi.consumer;

import com.asyncapi.model.MessagePayload;
import com.asyncapi.service.ExternalApiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

@Configuration
public class KafkaConsumerConfiguration {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerConfiguration.class);
    
    private final ExternalApiService externalApiService;
    private final ObjectMapper objectMapper;
    
    public KafkaConsumerConfiguration(ExternalApiService externalApiService, ObjectMapper objectMapper) {
        this.externalApiService = externalApiService;
        this.objectMapper = objectMapper;
    }

    /**
     * Kafka consumer using Spring Cloud Stream functional programming model
     * This bean name 'messageConsumer' will be bound to the input channel
     */
    @Bean
    public Consumer<Message<String>> messageConsumer() {
        return message -> {
            // Extract Kafka message metadata
            Object offset = message.getHeaders().get("kafka_offset");
            Object partition = message.getHeaders().get("kafka_receivedPartitionId");
            Object topic = message.getHeaders().get("kafka_receivedTopic");
            
            try {
                String payload = message.getPayload();
                
                // Log message reception with Kafka metadata
                log.info("📩 MESSAGE RECEIVED from Kafka - Topic: {}, Partition: {}, Offset: {}", 
                         topic, partition, offset);
                log.info("Message content: {}", payload);

                // Parse the message payload
                MessagePayload messagePayload = objectMapper.readValue(payload, MessagePayload.class);
                log.info("✅ Message parsed successfully - ID: {}, Content: {}", 
                         messagePayload.getId(), messagePayload.getContent());

                // Send to external API
                log.info("📤 Sending message to external API - Message ID: {}", messagePayload.getId());
                externalApiService.sendMessage(messagePayload)
                        .doOnSuccess(response -> {
                            log.info("✅ External API call successful - Message ID: {}, Response: {}", 
                                     messagePayload.getId(), response);
                        })
                        .doOnError(error -> {
                            log.error("❌ External API call failed - Message ID: {}", 
                                      messagePayload.getId(), error);
                            throw new RuntimeException("Failed to send message to external API", error);
                        })
                        .block(); // Block to ensure synchronous processing for proper offset commit

                // Log successful processing before commit
                log.info("✅ Message processing completed successfully - Message ID: {}", 
                         messagePayload.getId());
                log.info("💾 Message ready for commit - Topic: {}, Partition: {}, Offset: {}", 
                         topic, partition, offset);

            } catch (Exception e) {
                log.error("❌ CRITICAL ERROR processing message - Topic: {}, Partition: {}, Offset: {}. Message will NOT be committed.", 
                          topic, partition, offset, e);
                throw new RuntimeException("Failed to process Kafka message", e);
            }
        };
    }
}

