package com.learning.camunda.tasks;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LeaveBalanceCheck implements JavaDelegate {
    public static final int LEAVE_BALANCE = 5;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        if (LEAVE_BALANCE > 0) {
            logger.info("Employee have Leave Balance!!!");
        } else {
            logger.error("Employee don't have Leave Balance!!!");
        }

    }

}
