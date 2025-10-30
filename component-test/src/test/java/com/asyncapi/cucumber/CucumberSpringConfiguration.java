package com.asyncapi.cucumber;

import com.asyncapi.config.TestKafkaConfiguration;
import com.asyncapi.config.TestWireMockConfiguration;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@EmbeddedKafka(
    partitions = 1,
    topics = {"message-topic"},
    brokerProperties = {
        "listeners=PLAINTEXT://localhost:0",
        "port=0",
        "auto.create.topics.enable=true"
    }
)
@Import({TestKafkaConfiguration.class, TestWireMockConfiguration.class})
public class CucumberSpringConfiguration {

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("wiremock.server.port", TestWireMockConfiguration::getPort);
    }
}

