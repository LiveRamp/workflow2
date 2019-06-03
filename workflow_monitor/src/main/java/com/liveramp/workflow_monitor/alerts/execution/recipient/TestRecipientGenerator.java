package com.liveramp.workflow_monitor.alerts.execution.recipient;

import java.io.IOException;
import java.util.List;

import com.google.common.collect.Lists;

import com.liveramp.databases.workflow_db.models.WorkflowExecution;
import com.liveramp.java_support.alerts_handler.AlertsHandler;
import com.liveramp.workflow_state.WorkflowRunnerNotification;

public class TestRecipientGenerator implements RecipientGenerator {

  private final AlertsHandler handler;

  public TestRecipientGenerator(AlertsHandler handler){
    this.handler = handler;
  }

  @Override
  public List<AlertsHandler> getRecipients(WorkflowRunnerNotification severity, WorkflowExecution execution) throws IOException {
    return Lists.newArrayList(handler);
  }
}
