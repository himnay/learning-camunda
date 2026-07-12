package com.learning.camunda8;

import static io.camunda.zeebe.process.test.assertions.BpmnAssert.assertThat;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Drives order-process against the embedded zeebe-process-test engine
 * (in-JVM, no Docker). Jobs are completed manually here instead of running the
 * Spring job workers -- the point is to verify the BPMN wiring.
 */
@ZeebeProcessTest
class OrderProcessTest {

    private ZeebeTestEngine engine;
    private ZeebeClient client;

    @BeforeEach
    void deployProcess() {
        client.newDeployResourceCommand()
                .addResourceFromClasspath("order-process.bpmn")
                .send()
                .join();
    }

    @Test
    void happyPathCompletesBothServiceTasks() throws Exception {
        final ProcessInstanceEvent instance = client.newCreateInstanceCommand()
                .bpmnProcessId("order-process")
                .latestVersion()
                .variables(Map.of("orderId", "order-1", "amount", 42.0))
                .send()
                .join();

        completeNextJob("validate-order", Map.of("orderValid", true));
        completeNextJob("charge-payment", Map.of("paymentId", "pay-1"));
        engine.waitForIdleState(Duration.ofSeconds(5));

        assertThat(instance)
                .hasPassedElement("task-validate-order")
                .hasPassedElement("task-charge-payment")
                .hasPassedElement("end-completed")
                .isCompleted();
    }

    @Test
    void invalidOrderTakesErrorPathToRejectedEnd() throws Exception {
        final ProcessInstanceEvent instance = client.newCreateInstanceCommand()
                .bpmnProcessId("order-process")
                .latestVersion()
                .variables(Map.of("orderId", "order-2", "amount", -1.0))
                .send()
                .join();

        final ActivatedJob job = activateNextJob("validate-order");
        client.newThrowErrorCommand(job.getKey())
                .errorCode("INVALID_ORDER")
                .errorMessage("negative amount")
                .send()
                .join();
        engine.waitForIdleState(Duration.ofSeconds(5));

        assertThat(instance)
                .hasPassedElement("end-rejected")
                .isCompleted();
    }

    private ActivatedJob activateNextJob(String jobType) throws Exception {
        engine.waitForIdleState(Duration.ofSeconds(5));
        final List<ActivatedJob> jobs = client.newActivateJobsCommand()
                .jobType(jobType)
                .maxJobsToActivate(1)
                .send()
                .join()
                .getJobs();
        org.assertj.core.api.Assertions.assertThat(jobs)
                .as("expected one activatable %s job", jobType)
                .hasSize(1);
        return jobs.get(0);
    }

    private void completeNextJob(String jobType, Map<String, Object> variables) throws Exception {
        final ActivatedJob job = activateNextJob(jobType);
        client.newCompleteCommand(job.getKey()).variables(variables).send().join();
    }
}
