package com.learning.camunda.tasks;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.community.process_test_coverage.junit5.platform7.ProcessEngineCoverageExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*;

/**
 * Engine-level BPMN test: an in-memory H2 engine from camunda.cfg.xml (no Spring context),
 * the process deployed by {@code @Deployment}, and a coverage report written to
 * target/process-test-coverage.
 */
@ExtendWith(ProcessEngineCoverageExtension.class)
class CamundaIntegrationTest {

    private static final String PROCESS_KEY = "testCaseSample";

    @Test
    @Deployment(resources = {"7.12-bpmn-dmn-files/testCaseSample.bpmn"})
    void testSampleCase_happyPath() {

        ProcessInstance instance = runtimeService().startProcessInstanceByKey(PROCESS_KEY);

        assertThat(instance)
                .isActive()
                .hasPassed("startEvent")
                .isWaitingAtExactly("userTask1")
                .task().isNotAssigned();

        complete(task(), withVariables(
                "assignPerson", "dpoint",
                "attribute1", "value1"
        ));

        assertThat(instance)
                .hasPassed("userTask1")
                .hasPassedInOrder("userTask1", "serviceTask1")
                .isWaitingAt("userTask2")
                .task().isAssignedTo("dpoint");

        complete(task(), withVariables("attributeService", "variableServicevalue"));
        assertThat(instance)
                .hasPassedInOrder("userTask2", "endEvent")
                .isEnded();

    }
}
