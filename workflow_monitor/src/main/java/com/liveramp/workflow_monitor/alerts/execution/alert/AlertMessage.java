package com.liveramp.workflow_monitor.alerts.execution.alert;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liveramp.databases.workflow_db.IDatabases;
import com.liveramp.databases.workflow_db.IWorkflowDb;
import com.liveramp.databases.workflow_db.models.MapreduceJob;
import com.liveramp.databases.workflow_db.models.WorkflowAlert;
import com.liveramp.databases.workflow_db.models.WorkflowAlertMapreduceJob;
import com.liveramp.databases.workflow_db.models.WorkflowAlertWorkflowExecution;
import com.liveramp.databases.workflow_db.models.WorkflowExecution;
import com.liveramp.workflow_state.WorkflowRunnerNotification;
import com.rapleaf.jack.queries.GenericQuery;

public class AlertMessage {
  private static final Logger LOG = LoggerFactory.getLogger(AlertMessage.class);

  private final String classname;
  private final String message;
  private final WorkflowRunnerNotification notification;

  public AlertMessage(String classname, String message, WorkflowRunnerNotification notification) {
    this.classname = classname;
    this.message = message;
    this.notification = notification;
  }

  public String getMessage() {
    return message;
  }

  public WorkflowRunnerNotification getNotification() {
    return notification;
  }

  public String getClassname() {
    return classname;
  }

  private static boolean hasAlertedJob(IWorkflowDb wfDb, long mapreduceJobID, String alertClass) throws IOException {
    return existAlerts(wfDb.createQuery().from(WorkflowAlertMapreduceJob.TBL)
            .where(WorkflowAlertMapreduceJob.MAPREDUCE_JOB_ID.equalTo(mapreduceJobID))
            .innerJoin(WorkflowAlert.TBL)
            .on(WorkflowAlert.ID.equalTo(WorkflowAlertMapreduceJob.WORKFLOW_ALERT_ID)),
        alertClass);
  }

  private static boolean hasAlertedExecution(IWorkflowDb wfDb, long executionId, String alertClass) throws IOException {
    return existAlerts(wfDb.createQuery()
            .from(WorkflowAlertWorkflowExecution.TBL)
            .where(WorkflowAlertWorkflowExecution.WORKFLOW_EXECUTION_ID.equalTo(executionId))
            .innerJoin(WorkflowAlert.TBL)
            .on(WorkflowAlert.ID.equalTo(WorkflowAlertWorkflowExecution.WORKFLOW_ALERT_ID)),
        alertClass);
  }

  private static boolean existAlerts(GenericQuery filteredAlerts, String alertClass) throws IOException {
    return !filteredAlerts.where(WorkflowAlert.ALERT_CLASS.equalTo(alertClass))
        .select(WorkflowAlert.ID)
        .fetch().isEmpty();
  }

  public static AlertMessage createAlertMessage(String classname, String message, WorkflowRunnerNotification notification,
                                                WorkflowExecution execution, IDatabases db) throws IOException {
    IWorkflowDb wfDb = db.getWorkflowDb();
    if (!hasAlertedExecution(wfDb, execution.getId(), classname)) {
      WorkflowAlert wa = wfDb.workflowAlerts().create(classname, message);
      wfDb.workflowAlertWorkflowExecutions().create(wa.getId(), execution.getId());
      return new AlertMessage(classname, message, notification);
    } else {
      LOG.debug("Not re-notifying about execution " + execution.getId() + " alert gen " + classname);
    }

    return null;
  }

  public static AlertMessage createAlertMessage(String classname, String message, WorkflowRunnerNotification notification,
                                                MapreduceJob job, IDatabases db) throws IOException {

    IWorkflowDb wfDb = db.getWorkflowDb();
    if (!hasAlertedJob(wfDb, job.getId(), classname)) {
      WorkflowAlert wa = wfDb.workflowAlerts().create(classname, message);
      wfDb.workflowAlertMapreduceJobs().create(wa.getId(), job.getId());
      return new AlertMessage(classname, message, notification);
    } else {
      LOG.debug("Not re-notifying about job " + job.getId() + " alert gen " + classname);
    }

    return null;
  }

  @Override
  public String toString() {
    return "AlertMessage{" +
        "classname=" + classname + "\'" +
        "message='" + message + '\'' +
        ", notification=" + notification +
        '}';
  }
}
