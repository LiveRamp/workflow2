package com.liveramp.workflow_db_state;

import java.io.IOException;

import com.liveramp.databases.workflow_db.IWorkflowDb;
import com.liveramp.databases.workflow_db.models.StepAttempt;
import com.liveramp.databases.workflow_db.models.WorkflowAttempt;
import com.liveramp.databases.workflow_db.models.WorkflowExecution;
import com.liveramp.workflow.types.StepStatus;

public class Assertions {

  public static void assertCanManuallyModify(IWorkflowDb workflowDb, WorkflowExecution execution) throws IOException {
    if(!WorkflowQueries.canManuallyModify(workflowDb, execution)){
      throw new RuntimeException("Cannot manually modify execution "+execution.getId()+".  Check status and other executions");
    }
  }

  public static void assertStepCompleted(StepAttempt attempt) {
    if (attempt.getStepStatus() != StepStatus.COMPLETED.ordinal()) {
      throw new RuntimeException("Can only revert completed stepss!");
    }
  }

  public static void assertLive(WorkflowAttempt attempt) throws IOException {
    if (WorkflowQueries.getProcessStatus(attempt, attempt.getWorkflowExecution()) != ProcessStatus.ALIVE) {
      throw new RuntimeException("Invalid operation for current attempt " + attempt);
    }
  }

  public static void assertDead(WorkflowAttempt attempt) throws IOException {

    //  if it's still heartbeating, fail loudly
    if(WorkflowQueries.getProcessStatus(attempt, attempt.getWorkflowExecution()) == ProcessStatus.ALIVE){
      throw new RuntimeException("Cannot start, a previous attempt is still alive! Attempt: "+attempt);
    }

  }

}
