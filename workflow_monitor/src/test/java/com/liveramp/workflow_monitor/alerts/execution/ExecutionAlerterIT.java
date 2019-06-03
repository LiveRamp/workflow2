package com.liveramp.workflow_monitor.alerts.execution;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import org.junit.Test;

import com.liveramp.commons.collections.map.MultimapBuilder;
import com.liveramp.commons.collections.nested_map.TwoNestedMap;
import com.liveramp.databases.workflow_db.DatabasesImpl;
import com.liveramp.databases.workflow_db.IDatabases;
import com.liveramp.databases.workflow_db.IWorkflowDb;
import com.liveramp.databases.workflow_db.models.Application;
import com.liveramp.databases.workflow_db.models.MapreduceJob;
import com.liveramp.databases.workflow_db.models.StepAttempt;
import com.liveramp.databases.workflow_db.models.WorkflowAttempt;
import com.liveramp.databases.workflow_db.models.WorkflowExecution;
import com.liveramp.java_support.alerts_handler.BufferingAlertsHandler;
import com.liveramp.java_support.alerts_handler.configs.DefaultAlertMessageConfig;
import com.liveramp.java_support.alerts_handler.recipients.RecipientUtils;
import com.liveramp.workflow.types.StepStatus;
import com.liveramp.workflow.types.WorkflowExecutionStatus;
import com.liveramp.workflow_monitor.alerts.execution.alert.AlertMessage;
import com.liveramp.workflow_monitor.alerts.execution.recipient.TestRecipientGenerator;
import com.liveramp.workflow_state.WorkflowRunnerNotification;

import static com.liveramp.commons.test.TestUtils.assertStringsContainSubstring;
import static org.junit.Assert.assertEquals;

public class ExecutionAlerterIT extends WorkflowMonitorTestCase {

  private static final String MR_JOB_ALERT_MESSSAGE = "Alerting about job ";
  private static final String WF_EXECUTION_ALERT_MESSAGE = "Alerting about execution ";

  @Test
  public void testQueries() throws Exception {
    DatabasesImpl databases = new DatabasesImpl();
    IWorkflowDb db = databases.getWorkflowDb();
    databases.getWorkflowDb().disableCaching();

    long currentTime = System.currentTimeMillis();

    Application application = db.applications().create("Test Workflow");

    WorkflowExecution execution = db.workflowExecutions().create("Test Workflow", WorkflowExecutionStatus.COMPLETE.ordinal())
        .setStartTime(currentTime - 2)
        .setEndTime(currentTime - 1)
        .setApplicationId(application.getIntId());
    execution.save();
    WorkflowAttempt attempt = db.workflowAttempts().create(execution.getIntId(), "", "", "", "");
    StepAttempt step = db.stepAttempts().create(attempt.getIntId(), "step", StepStatus.COMPLETED.ordinal(), "")
        .setEndTime(System.currentTimeMillis() - 1);
    step.save();

    WorkflowExecution execution2 = db.workflowExecutions().create("Test Workflow 2", WorkflowExecutionStatus.COMPLETE.ordinal())
        .setStartTime(currentTime - 3)
        .setEndTime(currentTime - 2)
        .setApplicationId(application.getIntId());
    execution2.save();
    WorkflowAttempt attempt2 = db.workflowAttempts().create(execution2.getIntId(), "", "", "", "");
    StepAttempt step2 = db.stepAttempts().create(attempt2.getIntId(), "step", StepStatus.COMPLETED.ordinal(), "")
        .setEndTime(System.currentTimeMillis() - 1);
    step2.save();

    MapreduceJob mapreduceJob = db.mapreduceJobs().create("Job1", "JobName", "");
    mapreduceJob.setStepAttemptId(step.getId()).save();
    db.mapreduceCounters().create(mapreduceJob.getIntId(), "Group", "Name", 1);
    MapreduceJob mapreduceJob2 = db.mapreduceJobs().create("Job2", "JobName", "");
    mapreduceJob2.setStepAttemptId(step.getId()).save();
    db.mapreduceCounters().create(mapreduceJob2.getIntId(), "Group", "Name", 1);

    BufferingAlertsHandler.MessageBuffer buffer = new BufferingAlertsHandler.MessageBuffer();
    BufferingAlertsHandler handler = new BufferingAlertsHandler(buffer, RecipientUtils.of("test@test.com"));

    ExecutionAlerter alerter = new ExecutionAlerter(new TestRecipientGenerator(handler),
        Lists.newArrayList(new TestExecutionGenerator()),
        Lists.newArrayList(new TestJobGenerator()),
        databases,
        "fake",
        Integer.MAX_VALUE
    );

    alerter.generateAlerts();


    DefaultAlertMessageConfig config = new DefaultAlertMessageConfig(true, Lists.newArrayList());

    List<String> alerts = buffer.getSentAlerts().stream().map(alertMessage -> alertMessage.getMessage(config)).collect(Collectors.toList());

    assertEquals(4, alerts.size());

    assertStringsContainSubstring(MR_JOB_ALERT_MESSSAGE, alerts);
    assertStringsContainSubstring(WF_EXECUTION_ALERT_MESSAGE, alerts);

    assertEquals(4, db.workflowAlerts().findAll().size());

    for (MapreduceJob mrJob : db.mapreduceJobs().findByJobName("JobName")) {
      assertEquals(
          MR_JOB_ALERT_MESSSAGE + mrJob.getId(),
          mrJob.getWorkflowAlertMapreduceJob().get(0).getWorkflowAlert().getMessage()
      );
    }

    for (WorkflowExecution wfExecution : db.workflowExecutions().findByAppType(1)) {
      assertEquals(
          WF_EXECUTION_ALERT_MESSAGE + wfExecution.getId(),
          wfExecution.getWorkflowAlertWorkflowExecution().get(0).getWorkflowAlert().getMessage()
      );
    }

    alerter.generateAlerts();
    assertEquals(4, alerts.size());

  }

  private static class TestJobGenerator extends MapreduceJobAlertGenerator {
    protected TestJobGenerator() {
      super(new MultimapBuilder<String, String>().put("Group", "Name").get());
    }

    @Override
    public AlertMessage generateAlert(StepAttempt attempt, MapreduceJob job, TwoNestedMap<String, String, Long> counters, IDatabases db) throws IOException {
      if (counters.get("Group", "Name") == 1) {
        return AlertMessage.createAlertMessage(this.getClass().getName(), MR_JOB_ALERT_MESSSAGE + job.getId(), WorkflowRunnerNotification.PERFORMANCE, job, db);
      }
      return null;
    }
  }

  private static class TestExecutionGenerator implements ExecutionAlertGenerator {

    @Override
    public AlertMessage generateAlert(long time, WorkflowExecution execution, Collection<WorkflowAttempt> attempts, IDatabases db) throws IOException {
      return AlertMessage.createAlertMessage(this.getClass().getName(), WF_EXECUTION_ALERT_MESSAGE + execution.getId(), WorkflowRunnerNotification.PERFORMANCE, execution, db);
    }
  }
}