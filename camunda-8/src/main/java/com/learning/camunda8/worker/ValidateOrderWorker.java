package com.learning.camunda8.worker;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Worker for the "validate-order" task type.
 *
 * Demonstrates manual job handling (autoComplete = false): the worker decides
 * whether to complete the job or to throw a BPMN error, which the boundary
 * event on the task catches and routes to the "Order rejected" end event.
 */
@Component
public class ValidateOrderWorker {

    private static final Logger log = LoggerFactory.getLogger(ValidateOrderWorker.class);

    @JobWorker(type = "validate-order", autoComplete = false)
    public void validateOrder(final JobClient client, final ActivatedJob job) {
        final Map<String, Object> variables = job.getVariablesAsMap();
        final Object orderId = variables.get("orderId");
        final Number amount = (Number) variables.getOrDefault("amount", 0);

        log.info("[validate-order] job {} for order {} with amount {}", job.getKey(), orderId, amount);

        if (orderId == null || amount.doubleValue() <= 0) {
            // BPMN error, not a technical failure: no retries, the process takes the error path
            log.warn("[validate-order] rejecting order {}: invalid payload", orderId);
            client.newThrowErrorCommand(job.getKey())
                    .errorCode("INVALID_ORDER")
                    .errorMessage("Order must have an orderId and a positive amount")
                    .send()
                    .join();
            return;
        }

        client.newCompleteCommand(job.getKey())
                .variables(Map.of("orderValid", true))
                .send()
                .join();
        log.info("[validate-order] order {} validated", orderId);
    }
}
