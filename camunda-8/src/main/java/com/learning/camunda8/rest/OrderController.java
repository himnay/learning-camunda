package com.learning.camunda8.rest;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Starts an order-process instance on the Zeebe broker via the gRPC client.
 * The HTTP call returns as soon as the broker has recorded the instance --
 * the job workers then pick up the work asynchronously.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final ZeebeClient zeebeClient;

    public OrderController(ZeebeClient zeebeClient) {
        this.zeebeClient = zeebeClient;
    }

    public record OrderRequest(String orderId, double amount) {}

    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody OrderRequest order) {
        final ProcessInstanceEvent instance = zeebeClient
                .newCreateInstanceCommand()
                .bpmnProcessId("order-process")
                .latestVersion()
                .variables(Map.of(
                        "orderId", order.orderId(),
                        "amount", order.amount()))
                .send()
                .join();

        log.info("Started order-process instance {} for order {}",
                instance.getProcessInstanceKey(), order.orderId());

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of(
                "processInstanceKey", instance.getProcessInstanceKey(),
                "bpmnProcessId", instance.getBpmnProcessId(),
                "version", instance.getVersion()));
    }
}
