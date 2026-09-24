package com.learning.camunda8;

import io.camunda.client.annotation.Deployment;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Camunda 8 learning app. Unlike the C7 module (embedded engine in this JVM),
 * this app is a client of a remote Camunda 8 cluster (CamundaClient, gRPC/REST): it deploys the BPMN
 * on startup and runs job workers that poll the broker for work.
 */
@SpringBootApplication
@Deployment(resources = "classpath:order-process.bpmn")
class LearningCamunda8Application {

    /** Application entry point. */
    public static void main(String[] args) {
        SpringApplication.run(LearningCamunda8Application.class, args);
    }
}
