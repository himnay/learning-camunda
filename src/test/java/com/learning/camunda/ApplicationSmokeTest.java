package com.learning.camunda;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Boots the real Spring Boot application (engine, REST API, webapps) on an in-memory H2.
 * Unlike the engine-only tests, this exercises Spring's component scan, which parses the
 * compiled classes with Spring's ASM, and the auto-deployment of every model in
 * src/main/resources, so it proves the app starts on the JDK and bytecode level we build with.
 */
@SpringBootTest
@ActiveProfiles("test")
class ApplicationSmokeTest {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Test
    void everyMainModelIsAutoDeployed() {
        assertThat(repositoryService.createProcessDefinitionQuery().latestVersion().list())
                .extracting(ProcessDefinition::getKey)
                .contains(
                        "learning-camunda-process", "tasks-learning", "Process_0wa2vtk",
                        "exclusive-gateway", "parallel-gateway", "inclusive-gateway", "event-based-gateway",
                        "message-start-event", "signal-start-event", "conditional-start-event",
                        "subprocess-test", "asynchornous-test", "leave-management");
    }

    @Test
    void leaveRequestRunsTheBalanceCheckThenWaitsForTheManager() {
        ProcessInstance instance = runtimeService.startProcessInstanceByKey(
                "leave-management", Map.of("empName", "himansu", "days", 3));

        Task task = taskService.createTaskQuery().processInstanceId(instance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("manager-approval");
        assertThat(task.getAssignee()).isEqualTo("manager");

        taskService.complete(task.getId());
        Task next = taskService.createTaskQuery().processInstanceId(instance.getId()).singleResult();
        assertThat(next.getName()).isEqualTo("HR Approval");
        assertThat(next.getAssignee()).isEqualTo("hr");

        taskService.complete(next.getId());
        assertThat(runtimeService.createProcessInstanceQuery().processInstanceId(instance.getId()).count()).isZero();
    }
}
