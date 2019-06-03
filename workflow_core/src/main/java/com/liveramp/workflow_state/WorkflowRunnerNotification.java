package com.liveramp.workflow_state;

import com.liveramp.java_support.alerts_handler.recipients.AlertSeverity;

public enum WorkflowRunnerNotification {

  START(AlertSeverity.INFO),
  SUCCESS(AlertSeverity.INFO),
  FAILURE(AlertSeverity.ERROR),
  SHUTDOWN(AlertSeverity.INFO),
  INTERNAL_ERROR(AlertSeverity.ERROR),
  STEP_FAILURE(AlertSeverity.ERROR),

  DIED_UNCLEAN(AlertSeverity.ERROR),
  PERFORMANCE(AlertSeverity.ONCALL);

  private final AlertSeverity severity;

  WorkflowRunnerNotification(AlertSeverity severity){
    this.severity = severity;
  }

  public AlertSeverity serverity(){
    return severity;
  }

  public static WorkflowRunnerNotification findByValue(int val){
    return values()[val];
  }

}
