package com.learning.camunda.tasks;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class SendTasks implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        System.out.println("Calling another API !!!");
        System.out.println("My local variable value is: " + execution.getVariable("local-gender"));


    }

}
