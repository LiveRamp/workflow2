package com.liveramp.workflow_ui.scripts;

import java.io.IOException;

import org.joda.time.DateTime;
import org.junit.Test;

import com.liveramp.databases.workflow_db.DatabasesImpl;
import com.liveramp.databases.workflow_db.IWorkflowDb;
import com.liveramp.databases.workflow_db.iface.IMapreduceCounterPersistence;
import com.liveramp.databases.workflow_db.models.Application;
import com.liveramp.databases.workflow_db.models.MapreduceCounter;
import com.liveramp.databases.workflow_db.models.MapreduceJob;
import com.liveramp.databases.workflow_db.models.StepAttempt;
import com.liveramp.databases.workflow_db.models.WorkflowAttempt;
import com.liveramp.databases.workflow_db.models.WorkflowExecution;
import com.liveramp.workflow.types.StepStatus;
import com.liveramp.workflow.types.WorkflowAttemptStatus;
import com.liveramp.workflow.types.WorkflowExecutionStatus;
import com.liveramp.workflow_ui.WorkflowUITestCase;

import static org.junit.Assert.*;

public class TestSweepOldExecutions extends WorkflowUITestCase {

  @Test
  public void testSafety() throws IOException {

    try {
      SweepOldWorkflowData.sweep(DateTime.now().minusMonths(1).getMillis(), new DatabasesImpl().getWorkflowDb());
      fail();
    } catch (Exception e) {
      //  cool cool
    }

  }

  @Test
  public void test() throws IOException {

    IWorkflowDb db = new DatabasesImpl().getWorkflowDb();
    db.disableCaching();

    Application app = db.applications().create("Test Workflow");

    DateTime now = DateTime.now();
    DateTime oldDate = now.minusYears(2);

    MapreduceCounter detachedCounter1 = db.mapreduceCounters().create(100, "test", "test", 1);
    MapreduceCounter detachedCounter2 = db.mapreduceCounters().create(100, "test", "test", 1);

    //  old execution + counter for a workflow that finished
    MapreduceCounter oldCounter = setupCounter(db, app.getId(), oldDate.getMillis(), oldDate.getMillis());

    //  old execution that started but didn't finish
    MapreduceCounter oldCounter2 = setupCounter(db, app.getId(), oldDate.getMillis(), null);

    //  somewhere in the middle
    DateTime lessOldDate = now.minusMonths(14);
    MapreduceCounter oldCounter3 = setupCounter(db, app.getId(), lessOldDate.getMillis(), lessOldDate.getMillis());

    //  recent
    DateTime newDate = now.minusMonths(2);

    //  new execution + counter
    MapreduceCounter newCounter = setupCounter(db, app.getId(), newDate.getMillis(), newDate.getMillis());

    SweepOldWorkflowData.sweep(now.minusMonths(13).getMillis(), db);

    IMapreduceCounterPersistence counters = db.mapreduceCounters();

    assertNull(counters.find(oldCounter.getId()));
    assertNull(counters.find(oldCounter3.getId()));
    assertNull(counters.find(oldCounter2.getId()));
    assertNotNull(counters.find(newCounter.getId()));

    assertNull(db.mapreduceCounters().find(detachedCounter1.getId()));
    assertNull(db.mapreduceCounters().find(detachedCounter2.getId()));

  }

  private MapreduceCounter setupCounter(IWorkflowDb db, Long appId, Long workflowStartTime, Long workflowEndTime) throws IOException {

    WorkflowExecution execution = db.workflowExecutions().create(
        null,
        "Test Workflow",
        null,
        WorkflowExecutionStatus.COMPLETE.ordinal(),
        workflowStartTime,
        workflowEndTime,
        appId.intValue(),
        null
    );

    WorkflowAttempt attempt = db.workflowAttempts().create(
        (int)execution.getId(),
        "",
        null,
        "",
        "",
        "",
        workflowStartTime,
        workflowEndTime,
        WorkflowAttemptStatus.FINISHED.ordinal(),
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null
    );

    StepAttempt stepAttempt = db.stepAttempts().create(
        (int)attempt.getId(),
        "step",

        workflowStartTime,
        workflowEndTime,
        StepStatus.COMPLETED.ordinal(),
        null,
        null,
        "class",
        null
    );

    MapreduceJob job = db.mapreduceJobs().create(
        stepAttempt.getId(),
        "some_job",
        "some_job",
        "some_url",
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null
    );

    return db.mapreduceCounters().create((int)job.getId(),
        "group",
        "name",
        1l
    );

  }

}