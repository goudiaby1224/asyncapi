package com.asyncapi.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestWireMockConfiguration {

    private static final WireMockServer wireMockServer;

    static {
        // Start WireMock server
        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMockServer.start();
    }

    @Bean
    public WireMockServer wireMockServer() {
        return wireMockServer;
    }

    public static int getPort() {
        return wireMockServer.port();
    }
}

