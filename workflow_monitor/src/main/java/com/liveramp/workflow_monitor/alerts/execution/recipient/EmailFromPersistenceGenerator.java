
package com.liveramp.workflow_monitor.alerts.execution.recipient;

import java.io.IOException;
import java.util.List;

import com.google.common.collect.Lists;

import com.liveramp.databases.workflow_db.IDatabases;
import com.liveramp.databases.workflow_db.models.WorkflowExecution;
import com.liveramp.java_support.alerts_handler.AlertsHandler;
import com.liveramp.java_support.alerts_handler.configs.DefaultAlertMessageConfig;
import com.liveramp.java_support.alerts_handler.configs.GenericAlertsHandlerConfig;
import com.liveramp.java_support.alerts_handler.recipients.AlertSeverity;
import com.liveramp.java_support.alerts_handler.recipients.EngineeringAlertRecipient;
import com.liveramp.mail_utils.MailerHelperAlertsHandler;
import com.liveramp.mail_utils.SmtpConfig;
import com.liveramp.workflow_db_state.DbPersistence;
import com.liveramp.workflow_db_state.WorkflowQueries;
import com.liveramp.workflow_state.WorkflowRunnerNotification;

public class EmailFromPersistenceGenerator implements RecipientGenerator {

  private final IDatabases dbs;

  private final String alertList;
  private final String alertDomain;
  private final String smtpHost;

  public EmailFromPersistenceGenerator(IDatabases dbs, String alertList, String alertDomain, String smtpHost) {
    this.dbs = dbs;
    this.alertList = alertList;
    this.alertDomain = alertDomain;
    this.smtpHost = smtpHost;
  }

  @Override
  public List<AlertsHandler> getRecipients(WorkflowRunnerNotification severity, WorkflowExecution execution) throws IOException {
    return DbPersistence.queryPersistence(WorkflowQueries.getLatestAttempt(execution).getId(),
        dbs.getWorkflowDb()).getRecipients(severity,
        (emails, buffer) -> new MailerHelperAlertsHandler(new GenericAlertsHandlerConfig(
            new DefaultAlertMessageConfig(true, Lists.newArrayList()),
            alertList, alertDomain,
            new EngineeringAlertRecipient(AlertSeverity.ERROR)),
            new SmtpConfig(smtpHost)));
  }
}