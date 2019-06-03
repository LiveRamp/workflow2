package com.liveramp.workflow_db_state.controller;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;

import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liveramp.databases.workflow_db.IWorkflowDb;
import com.liveramp.databases.workflow_db.models.ConfiguredNotification;
import com.liveramp.databases.workflow_db.models.WorkflowExecution;
import com.liveramp.databases.workflow_db.models.WorkflowExecutionConfiguredNotification;
import com.liveramp.workflow.types.WorkflowExecutionStatus;
import com.liveramp.workflow_db_state.Assertions;
import com.liveramp.workflow_db_state.ProcessStatus;
import com.liveramp.workflow_db_state.WorkflowQueries;
import com.liveramp.workflow_state.WorkflowRunnerNotification;

public class ExecutionController {

  private static final Logger LOG = LoggerFactory.getLogger(ExecutionController.class);

  public static void cancelExecution(IWorkflowDb db, WorkflowExecution execution) throws IOException {
    if (isCancelled(execution)) {
      return;
    }

    Assertions.assertCanManuallyModify(db, execution);

    //  cancel execution
    execution
        .setStatus(WorkflowExecutionStatus.CANCELLED.ordinal())
        .save();

  }

  public static void addConfiguredNotifications(IWorkflowDb workflowDb, Long workflowId, String email, Set<WorkflowRunnerNotification> notifications) throws IOException {
    WorkflowExecution execution = workflowDb.workflowExecutions().find(workflowId);

    Set<WorkflowRunnerNotification> existing = Sets.newHashSet();
    for (ConfiguredNotification.Attributes attributes : WorkflowQueries.getExecutionNotifications(workflowDb, execution.getId(), email)) {
      existing.add(WorkflowRunnerNotification.findByValue(attributes.getWorkflowRunnerNotification()));
    }

    for (WorkflowRunnerNotification notification : notifications) {
      if (!existing.contains(notification)) {
        ConfiguredNotification configured = workflowDb.configuredNotifications().create(notification.ordinal(), email, false);
        workflowDb.workflowExecutionConfiguredNotifications().create(execution.getId(), configured.getId());
      }
    }

  }

  public static void removeConfiguredNotifications(IWorkflowDb workflowDb, Long workflowId, String email) throws IOException {
    removeConfiguredNotifications(workflowDb, workflowId, email, EnumSet.allOf(WorkflowRunnerNotification.class));
  }

  public static void removeConfiguredNotifications(IWorkflowDb workflowDb, Long workflowId, String email, Set<WorkflowRunnerNotification> notificaions) throws IOException {
    WorkflowExecution execution = workflowDb.workflowExecutions().find(workflowId);
    long id = execution.getId();

    for (ConfiguredNotification.Attributes attributes : WorkflowQueries.getExecutionNotifications(workflowDb, id, email)) {
      if (notificaions.contains(WorkflowRunnerNotification.findByValue(attributes.getWorkflowRunnerNotification()))) {

        for (WorkflowExecutionConfiguredNotification appNotification : workflowDb.workflowExecutionConfiguredNotifications().query()
            .workflowExecutionId(id)
            .configuredNotificationId(attributes.getId())
            .find()) {

          workflowDb.workflowExecutionConfiguredNotifications().delete(appNotification);
          //  TODO if a ConfiguredNotification has no App/Execution/Attempt ConfiguredNotifications referencing it, delete it
        }
      }
    }

  }

  public static boolean isCancelled(WorkflowExecution execution) {
    if (execution.getStatus() == WorkflowExecutionStatus.CANCELLED.ordinal()) {
      LOG.warn("Workflow execution " + execution.getId() + " is already cancelled.");
      return true;
    } else {
      return false;
    }
  }

  public static boolean isRunning(WorkflowExecution execution) throws IOException {
    return WorkflowQueries.getProcessStatus(WorkflowQueries.getLatestAttempt(execution), execution) == ProcessStatus.ALIVE;
  }
}
