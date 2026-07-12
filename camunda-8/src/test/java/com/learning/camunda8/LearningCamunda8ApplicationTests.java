package com.learning.camunda8;

import io.camunda.zeebe.client.ZeebeClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Context-load smoke test. src/test/resources/application.yaml disables the
 * Camunda SDK auto-configuration (no gRPC connection, no deployment, no
 * workers), and the stub client below satisfies the controller's dependency.
 * Building a ZeebeClient opens no connection until a command is sent, so no
 * Zeebe broker (and no Docker) is needed.
 */
@SpringBootTest
class LearningCamunda8ApplicationTests {

    @Test
    void contextLoads() {
    }

    @TestConfiguration
    static class OfflineZeebeClientConfig {

        @Bean
        ZeebeClient offlineZeebeClient() {
            return ZeebeClient.newClientBuilder()
                    .grpcAddress(java.net.URI.create("http://localhost:26500"))
                    .usePlaintext()
                    .build();
        }
    }
}
