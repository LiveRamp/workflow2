package com.liveramp.workflow_monitor.alerts.execution;

import java.io.IOException;
import java.util.Collection;

import com.liveramp.databases.workflow_db.IDatabases;
import com.liveramp.databases.workflow_db.models.WorkflowAttempt;
import com.liveramp.databases.workflow_db.models.WorkflowExecution;
import com.liveramp.workflow_monitor.alerts.execution.alert.AlertMessage;

public interface ExecutionAlertGenerator {
  AlertMessage generateAlert(long fetchTime, WorkflowExecution execution, Collection<WorkflowAttempt> attempts, IDatabases db) throws IOException;
}
