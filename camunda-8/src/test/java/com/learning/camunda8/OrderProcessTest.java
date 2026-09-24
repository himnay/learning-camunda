package com.learning.camunda8;

import static io.camunda.process.test.api.CamundaAssert.assertThat;
import static io.camunda.process.test.api.assertions.ElementSelectors.byId;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.ProcessInstanceEvent;
import io.camunda.process.test.api.CamundaSpringProcessTest;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Boots the real application against a Camunda Process Test runtime (camunda/camunda
 * container, managed via Testcontainers — Docker required). The app's own
 * {@code @Deployment} deploys order-process.bpmn and its {@code @JobWorker}s do the work,
 * so this covers the BPMN wiring <em>and</em> the Spring workers end to end.
 */
@SpringBootTest
@CamundaSpringProcessTest
class OrderProcessTest {

    @Autowired
    private CamundaClient client;

    @Test
    void validOrderRunsBothWorkersToCompletion() {
        final ProcessInstanceEvent instance = startOrder("order-1", 42.0);

        assertThat(instance)
                .isCompleted()
                .hasCompletedElements(byId("task-validate-order"), byId("task-charge-payment"), byId("end-completed"));
    }

    @Test
    void invalidOrderThrowsBpmnErrorToRejectedEnd() {
        // ValidateOrderWorker throws BPMN error INVALID_ORDER for a non-positive amount
        final ProcessInstanceEvent instance = startOrder("order-2", -1.0);

        assertThat(instance)
                .isCompleted()
                .hasCompletedElements(byId("end-rejected"));
    }

    private ProcessInstanceEvent startOrder(String orderId, double amount) {
        return client.newCreateInstanceCommand()
                .bpmnProcessId("order-process")
                .latestVersion()
                .variables(Map.of("orderId", orderId, "amount", amount))
                .send()
                .join();
    }
}
