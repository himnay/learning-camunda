package com.learning.camunda8.worker;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Worker for the "charge-payment" task type.
 *
 * Demonstrates the auto-complete style: the returned map is merged into the
 * process variables and the job is completed by the SDK.
 */
@Component
public class ChargePaymentWorker {

    private static final Logger log = LoggerFactory.getLogger(ChargePaymentWorker.class);

    /** Returns the charge payment. */
    @JobWorker(type = "charge-payment")
    public Map<String, Object> chargePayment(final ActivatedJob job) {
        final Map<String, Object> variables = job.getVariablesAsMap();
        final String paymentId = UUID.randomUUID().toString();

        log.info("[charge-payment] job {} charging {} for order {} -> paymentId {}",
                job.getKey(), variables.get("amount"), variables.get("orderId"), paymentId);

        return Map.of("paymentId", paymentId);
    }
}
