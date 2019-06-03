package com.liveramp.workflow_monitor.alerts.execution;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.hp.gagawa.java.elements.A;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liveramp.commons.collections.nested_map.TwoNestedMap;
import com.liveramp.databases.workflow_db.IDatabases;
import com.liveramp.databases.workflow_db.models.MapreduceCounter;
import com.liveramp.databases.workflow_db.models.MapreduceJob;
import com.liveramp.databases.workflow_db.models.StepAttempt;
import com.liveramp.databases.workflow_db.models.WorkflowAttempt;
import com.liveramp.databases.workflow_db.models.WorkflowExecution;
import com.liveramp.java_support.alerts_handler.AlertMessages;
import com.liveramp.java_support.alerts_handler.AlertsHandler;
import com.liveramp.java_support.alerts_handler.recipients.AlertSeverity;
import com.liveramp.java_support.alerts_handler.recipients.EngineeringAlertRecipient;
import com.liveramp.workflow_core.WorkflowConstants;
import com.liveramp.workflow_db_state.WorkflowQueries;
import com.liveramp.workflow_db_state.jack.JackUtil;
import com.liveramp.workflow_monitor.alerts.execution.alert.AlertMessage;
import com.liveramp.workflow_monitor.alerts.execution.recipient.RecipientGenerator;
import com.liveramp.workflow_state.WorkflowRunnerNotification;


import static com.rapleaf.jack.queries.AggregatedColumn.COUNT;

public class ExecutionAlerter {
  private static final Logger LOG = LoggerFactory.getLogger(ExecutionAlerter.class);

  private final List<ExecutionAlertGenerator> executionAlerts;
  private final List<MapreduceJobAlertGenerator> jobAlerts;

  private final RecipientGenerator generator;
  private final IDatabases db;
  private final String workflowUIServer;

  private final int suppressEmailAppCountThreshold;

  private final Multimap<String, String> countersToFetch = HashMultimap.create();

  public ExecutionAlerter(RecipientGenerator generator,
                          List<ExecutionAlertGenerator> executionAlerts,
                          List<MapreduceJobAlertGenerator> jobAlerts,
                          IDatabases db,
                          String workflowUIserver,
                          int suppressEmailAppCountThreshold) {
    this.executionAlerts = executionAlerts;
    this.jobAlerts = jobAlerts;
    this.generator = generator;
    this.workflowUIServer = workflowUIserver;
    this.db = db;
    this.suppressEmailAppCountThreshold = suppressEmailAppCountThreshold;

    for (MapreduceJobAlertGenerator jobAlert : jobAlerts) {
      countersToFetch.putAll(jobAlert.getCountersToFetch());
    }
  }

  public void generateAlerts() throws IOException, URISyntaxException {

    //  finished in last hour
    long endTime = System.currentTimeMillis();
    long dayAgo = endTime - Duration.ofDays(1).toMillis();

    //  get app counts within the past day
    Set<String> suppressedApps = db.getWorkflowDb().createQuery().from(WorkflowExecution.TBL)
        .where(WorkflowExecution.END_TIME.greaterThan(dayAgo))
        .groupBy(WorkflowExecution.NAME)
        .select(WorkflowExecution.NAME, COUNT(WorkflowExecution.ID)).fetch().stream()
        .filter(record -> record.getInt(COUNT(WorkflowExecution.ID)) >= suppressEmailAppCountThreshold)
        .map(record -> record.getString(WorkflowExecution.NAME)).collect(Collectors.toSet());

    generateExecutionAlerts(
        endTime - Duration.ofDays(7).toMillis(),
        endTime,
        suppressedApps
    );

    generateJobAlerts(
        endTime - Duration.ofHours(1).toMillis(),
        endTime,
        suppressedApps
    );

  }

  private void generateJobAlerts(long jobWindow, long endTime, Set<String> suppressedApps) throws IOException, URISyntaxException {
    LOG.info("Generating job alerts");

    Map<Long, MapreduceJob> jobs = JackUtil.byId(WorkflowQueries.getCompleteMapreduceJobs(db,
        jobWindow,
        endTime
    ));
    LOG.info("Found  " + jobs.size() + " complete jobs");

    Set<Long> stepAttemptIds = stepAttemptIds(jobs.values());

    Multimap<Integer, MapreduceCounter> countersByJob = JackUtil.by(WorkflowQueries.getAllJobCounters(db,
        jobWindow,
        endTime,
        countersToFetch.keySet(),
        Sets.newHashSet(countersToFetch.values())),
        MapreduceCounter._Fields.mapreduce_job_id
    );

    Map<Long, Long> stepAttemptToExecution = WorkflowQueries.getStepAttemptIdtoWorkflowExecutionId(db, stepAttemptIds);
    Map<Long, StepAttempt> stepsById = JackUtil.byId(db.getWorkflowDb().stepAttempts().query().idIn(stepAttemptIds).find());

    Map<Long, WorkflowExecution> relevantExecutions = JackUtil.byId(WorkflowQueries.getExecutionsForStepAttempts(db, stepAttemptIds));

    for (MapreduceJobAlertGenerator jobAlert : jobAlerts) {
      Class<? extends MapreduceJobAlertGenerator> alertClass = jobAlert.getClass();
      LOG.info("Running alerter class: " + jobAlert.getClass().getName());

      for (Map.Entry<Long, MapreduceJob> jobEntry : jobs.entrySet()) {
        long jobId = jobEntry.getKey();
        MapreduceJob mapreduceJob = jobEntry.getValue();
        long stepAttemptId = mapreduceJob.getStepAttemptId();

        WorkflowExecution execution = relevantExecutions.get(stepAttemptToExecution.get(stepAttemptId));

        TwoNestedMap<String, String, Long> counterMap = WorkflowQueries.countersAsMap(countersByJob.get((int)jobId));
        AlertMessage alert = jobAlert.generateAlert(stepsById.get(stepAttemptId), mapreduceJob, counterMap, db);

        if (alert != null && !suppressedApps.contains(execution.getName())) {
          sendAlert(alertClass, execution, alert);
        }

      }
    }
  }


  private void generateExecutionAlerts(long beginWindow, long currentTime, Set<String> suppressedApps) throws IOException, URISyntaxException {
    LOG.info("Fetching executions to attempts since " + beginWindow);

    Multimap<WorkflowExecution, WorkflowAttempt> attempts = WorkflowQueries.getExecutionsToAttempts(db, null, null, null, null, beginWindow, null, null, null);
    LOG.info("Found " + attempts.keySet().size() + " executions");

    for (ExecutionAlertGenerator executionAlert : executionAlerts) {
      Class<? extends ExecutionAlertGenerator> alertClass = executionAlert.getClass();
      LOG.info("Running alert generator " + alertClass.getName());

      for (WorkflowExecution execution : attempts.keySet()) {

        AlertMessage alert = executionAlert.generateAlert(currentTime, execution, attempts.get(execution), db);
        if (alert != null && !suppressedApps.contains(execution.getName())) {
          sendAlert(alertClass, execution, alert);
        }

      }
    }
  }

  private static Set<Long> stepAttemptIds(Collection<MapreduceJob> jobs) {
    Set<Long> stepAttemptIds = Sets.newHashSet();
    for (MapreduceJob job : jobs) {
      stepAttemptIds.add(job.getStepAttemptId());
    }
    return stepAttemptIds;
  }

  private void sendAlert(Class alertClass, WorkflowExecution execution, AlertMessage alertMessage) throws IOException, URISyntaxException {
    LOG.info("Sending alert: " + alertMessage + " type " + alertClass + " for execution " + execution);

    WorkflowRunnerNotification notification = alertMessage.getNotification();
    for (AlertsHandler handler : generator.getRecipients(notification, execution)) {

      AlertMessages.Builder builder = AlertMessages.builder(buildSubject(alertClass.getSimpleName(), execution))
          .setBody(buildMessage(alertMessage.getMessage(), execution))
          .addToDefaultTags(WorkflowConstants.WORKFLOW_EMAIL_SUBJECT_TAG);

      if (notification.serverity() == AlertSeverity.ERROR) {
        builder.addToDefaultTags(WorkflowConstants.ERROR_EMAIL_SUBJECT_TAG);
      }

      handler.sendAlert(
          builder.build(),
          new EngineeringAlertRecipient(notification.serverity())
      );
    }
  }

  private String buildSubject(String alertMessage, WorkflowExecution execution) {

    String name = execution.getName();

    if (execution.getName() == null) {
      LOG.error("Bad execution: "+execution);
    }

    String[] split = name.split("\\.");

    String message = alertMessage + ": " + split[split.length - 1];

    if (execution.getScopeIdentifier() != null) {
      message = message + " (" + execution.getScopeIdentifier() + ")";
    }

    return message;
  }

  private String buildMessage(String alertMessage, WorkflowExecution execution) throws URISyntaxException, UnsupportedEncodingException {

    A executionLink = new A()
        .setHref(new URIBuilder()
            .setScheme("http")
            .setHost(workflowUIServer)
            .setPath("/execution.html")
            .setParameter("id", Long.toString(execution.getId()))
            .build().toString())
        .appendText(Long.toString(execution.getId()));

    A appLink = new A()
        .setHref(new URIBuilder()
            .setScheme("http")
            .setHost(workflowUIServer)
            .setPath("/application.html")
            .setParameter("name", URLEncoder.encode(execution.getName(), "UTF-8"))
            .build().toString())
        .appendText(execution.getName());

    return "Application: " + appLink.write() +
        "\nScope: " + execution.getScopeIdentifier() +
        "\nExecution: " + executionLink.write() +
        "\n\n" + alertMessage;

  }

}
