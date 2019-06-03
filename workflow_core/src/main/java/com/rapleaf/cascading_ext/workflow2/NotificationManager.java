package com.rapleaf.cascading_ext.workflow2;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liveramp.java_support.alerts_handler.AlertMessages;
import com.liveramp.java_support.alerts_handler.AlertsHandler;
import com.liveramp.java_support.alerts_handler.configs.AlertsHandlerConfig;
import com.liveramp.java_support.alerts_handler.recipients.AddRecipientContext;
import com.liveramp.java_support.alerts_handler.recipients.AlertRecipient;
import com.liveramp.java_support.alerts_handler.recipients.AlertSeverity;
import com.liveramp.java_support.alerts_handler.recipients.RecipientListBuilder;
import com.liveramp.workflow.types.StepStatus;
import com.liveramp.workflow_core.WorkflowConstants;
import com.liveramp.workflow_core.alerting.AlertsHandlerFactory;
import com.liveramp.workflow_state.StepState;
import com.liveramp.workflow_state.WorkflowRunnerNotification;
import com.liveramp.workflow_state.WorkflowStatePersistence;

public class NotificationManager {
  private static final Logger LOG = LoggerFactory.getLogger(NotificationManager.class);

  private final WorkflowStatePersistence persistence;

  private final TrackerURLBuilder trackerURLBuilder;

  private final AlertsHandlerFactory alertsHandlerFactory;

  private final String prefix;

  public NotificationManager(String prefix,
                             WorkflowStatePersistence persistence,
                             AlertsHandlerFactory alertsHandlerFactory,
                             TrackerURLBuilder builder){
    this.persistence = persistence;
    this.trackerURLBuilder = builder;
    this.prefix = prefix;
    this.alertsHandlerFactory = alertsHandlerFactory;
  }

  public void sendInternalErrorMessage(List<Exception> internalErrors) throws IOException {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);

    pw.write("WorkflowRunner failed with an internal error.  Manual cleanup may be necessary.");

    for (Exception error : internalErrors) {
      pw.append(error.getMessage())
          .append("\n");
      error.printStackTrace(pw);
      pw.append("---------------------\n");
    }

    mail(getFailureSubject(), pw.toString(), WorkflowRunnerNotification.INTERNAL_ERROR);

  }

  public String buildStepFailureMessage(String step) throws IOException {
    return "Workflow will continue running non-blocked steps \n\n Step "
        + step + " failed with exception: "
        + persistence.getStepStates().get(step).getFailureMessage();
  }

  public String buildStepsFailureMessage() throws IOException {
    int n = 1;
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);

    int numFailed = 0;
    Map<String, StepState> statuses = persistence.getStepStates();
    for (Map.Entry<String, StepState> status : statuses.entrySet()) {
      if (status.getValue().getStatus() == StepStatus.FAILED) {
        numFailed++;
      }
    }

    for (Map.Entry<String, StepState> status : statuses.entrySet()) {
      StepState value = status.getValue();
      if (value.getStatus() == StepStatus.FAILED) {
        pw.println("(" + n + "/" + numFailed + ") Step "
            + status.getKey() + " failed with exception: "
            + value.getFailureMessage());
        pw.println(value.getFailureTrace());
        n++;
      }
    }
    return sw.toString();
  }

  public void sendStartEmail() throws IOException {
    mail(getStartSubject(), WorkflowRunnerNotification.START);
  }

  public void sendSuccessEmail() throws IOException {
    mail(getSuccessSubject(), WorkflowRunnerNotification.SUCCESS);
  }

  public void sendFailureEmail(String msg) throws IOException {
    mail(getFailureSubject(), msg, WorkflowRunnerNotification.FAILURE);
  }

  public void sendShutdownEmail(String cause) throws IOException {
    mail(getShutdownSubject(cause), WorkflowRunnerNotification.SHUTDOWN);
  }

  private String getDisplayName() throws IOException {
    return persistence.getName() + (this.persistence.getScopeIdentifier() == null ? "" : " (" + this.persistence.getScopeIdentifier() + ")");
  }

  public String getStartSubject() throws IOException {
    return "Started: " + getDisplayName();
  }

  public String getSuccessSubject() throws IOException {
    return "Succeeded: " + getDisplayName();
  }

  public String getFailureSubject() throws IOException {
    return "Failed: " + getDisplayName();
  }

  private String getStepFailureSubject() throws IOException {
    return "Step has failed in: " + getDisplayName();
  }

  public void sendStepFailureEmail(String stepToken) throws IOException {
    mail(getStepFailureSubject(), buildStepFailureMessage(stepToken), WorkflowRunnerNotification.STEP_FAILURE);
  }

  public String getShutdownSubject(String reason) throws IOException {
    return "Shutdown requested: " + getDisplayName() + ". Reason: " + reason;
  }

  private void mail(String subject, WorkflowRunnerNotification notification) throws IOException {
    mail(subject, "", notification);
  }


  private static AlertRecipient engineering(final AlertSeverity severity) {
    return new AlertRecipient() {
      @Override
      public void addRecipient(RecipientListBuilder recipientListBuilder, AlertsHandlerConfig alertsHandlerConfig, AddRecipientContext context) {
        final AlertRecipient engineeringRecipient = alertsHandlerConfig.getEngineeringRecipient();
        final AddRecipientContext contextWithSeverity = new AddRecipientContext(Optional.of(severity));

        engineeringRecipient.addRecipient(recipientListBuilder, alertsHandlerConfig, contextWithSeverity);
      }

      @Override
      public String toString() {
        return "Engineering{" + severity.toString() + "}";
      }
    };
  }

  private void mail(String subject, String body, WorkflowRunnerNotification notification) throws IOException {

    for (AlertsHandler handler : persistence.getRecipients(notification, alertsHandlerFactory)) {

      try {

        if(prefix != null){
          subject = subject + " ("+prefix+")";
        }

        AlertMessages.Builder builder = AlertMessages.builder(subject)
            .setBody(appendTrackerUrl(body))
            .addToDefaultTags(WorkflowConstants.WORKFLOW_EMAIL_SUBJECT_TAG);

        if (notification.serverity() == AlertSeverity.ERROR) {
          builder.addToDefaultTags(WorkflowConstants.ERROR_EMAIL_SUBJECT_TAG);
        }

        handler.sendAlert(
            builder.build(),
            engineering(notification.serverity())
        );

      } catch (Exception e) {
        LOG.error("Failed to notify AlertsHandler " + handler, e);
      }

    }
  }

  private String appendTrackerUrl(String messageBody) throws IOException {
    return "Tracker URL: " + getTrackerURL() + "<br><br>" + messageBody;
  }

  public String getTrackerURL() throws IOException {
    return trackerURLBuilder.buildURL(persistence);
  }

  public String getReasonForShutdownRequest() throws IOException {
    return persistence.getShutdownRequest();
  }

}
