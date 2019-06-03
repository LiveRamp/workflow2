package com.liveramp.workflow_ui.servlet;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import com.liveramp.commons.collections.map.MapBuilder;
import com.liveramp.databases.workflow_db.DatabasesImpl;
import com.liveramp.databases.workflow_db.IWorkflowDb;
import com.liveramp.databases.workflow_db.models.Application;
import com.liveramp.databases.workflow_db.models.MapreduceJob;
import com.liveramp.databases.workflow_db.models.StepAttempt;
import com.liveramp.databases.workflow_db.models.WorkflowAlert;
import com.liveramp.databases.workflow_db.models.WorkflowAttempt;
import com.liveramp.databases.workflow_db.models.WorkflowExecution;
import com.liveramp.workflow.types.StepStatus;
import com.liveramp.workflow.types.WorkflowExecutionStatus;
import com.liveramp.workflow_ui.WorkflowUITestCase;

import static org.junit.Assert.assertEquals;

public class TestClusterAppServlet extends WorkflowUITestCase {
  private static final DateTimeFormatter FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

  @Test
  public void test() throws Exception {

    long date14 = System.currentTimeMillis();
    long date13 = FORMAT.parseMillis("2016-06-13 12:00:00");

    DatabasesImpl databases = new DatabasesImpl();
    final IWorkflowDb workflowDb = databases.getWorkflowDb();

    Application app = workflowDb.applications().create("Test Workflow");
    WorkflowExecution execution = workflowDb.workflowExecutions().create(null,
        "Test Workflow",
        null,
        WorkflowExecutionStatus.COMPLETE.ordinal(),
        date13,
        date14,
        app.getIntId(),
        null
    );
    WorkflowAttempt attempt = workflowDb.workflowAttempts().create(execution.getIntId(), "user", "HIGH", "default", "localhost");
    StepAttempt step = workflowDb.stepAttempts().create(attempt.getIntId(), "step",
        date13,
        date14,
        StepStatus.COMPLETED.ordinal(),
        null,
        null,
        "class",
        null
    );
    MapreduceJob mrJob = workflowDb.mapreduceJobs().create(step.getId(), "job1",
        "jobname", "url", null, null, null, null, null, null, null, null, null, null, null, null);

    workflowDb.mapreduceCounters().create(mrJob.getIntId(), ClusterConstants.MR2_GROUP, "TOTAL_LAUNCHED_MAPS", 1);
    workflowDb.mapreduceCounters().create(mrJob.getIntId(), ClusterConstants.MR2_GROUP, "TOTAL_LAUNCHED_REDUCES", 1);

    WorkflowAlert alert = workflowDb.workflowAlerts().create("alert_class", "some message");
    workflowDb.workflowAlertMapreduceJobs().create(alert.getId(), mrJob.getId());

    JSONObject response = new ClusterAppAlerts().getData(databases, new MapBuilder<String, String>()
        .put("start_time", Long.toString(date13))
        .put("end_time", Long.toString(date14)+1)
        .get()
    );

    JSONArray responseArray = response.getJSONArray("alerts");
    assertEquals(1, responseArray.length());
    assertEquals(2, responseArray.getJSONObject(0).getInt("alertTasks"));

  }

}
