package com.liveramp.workflow.state;

import java.io.IOException;
import java.util.List;

import com.google.common.collect.Sets;
import com.liveramp.databases.workflow_db.DatabasesImpl;
import com.liveramp.databases.workflow_db.IWorkflowDb;
import com.liveramp.databases.workflow_db.models.*;
import org.apache.hadoop.util.Time;
import org.junit.Before;
import org.junit.Test;

import com.liveramp.commons.Accessors;
import com.liveramp.workflow.types.StepStatus;
import com.liveramp.workflow.types.WorkflowAttemptStatus;
import com.liveramp.workflow.types.WorkflowExecutionStatus;
import com.liveramp.workflow_db_state.DbPersistence;
import com.liveramp.workflow_state.WorkflowRunnerNotification;
import com.rapleaf.cascading_ext.workflow2.Step;
import com.rapleaf.cascading_ext.workflow2.WorkflowRunner;
import com.rapleaf.cascading_ext.workflow2.WorkflowTestCase;
import com.rapleaf.cascading_ext.workflow2.action.NoOpAction;
import com.rapleaf.cascading_ext.workflow2.options.HadoopWorkflowOptions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WorkflowDbPersistenceFactoryIT extends WorkflowTestCase {

  @Before
  public void setUp() throws Exception {
    new DatabasesImpl().getWorkflowDb().deleteAll();
  }

  @Test
  public void testAutoCleanup() throws Exception {
    testAutoCleanup(WorkflowAttemptStatus.RUNNING);
  }

  @Test
  public void testAutoCleanup2() throws Exception {
    testAutoCleanup(WorkflowAttemptStatus.INITIALIZING);
  }

  public void testAutoCleanup(WorkflowAttemptStatus dead) throws IOException {
    IWorkflowDb workflow_db = new DatabasesImpl().getWorkflowDb();
    workflow_db.disableCaching();

    Application app = workflow_db.applications().create("Workflow");

    WorkflowExecution ex = workflow_db.workflowExecutions().create("Workflow", WorkflowExecutionStatus.INCOMPLETE.ordinal())
        .setStartTime(Time.now())
        .setEndTime(Time.now() + 1)
        .setApplicationId(app.getIntId());

    ex.save();

    long currentTime = System.currentTimeMillis();

    WorkflowAttempt workflowAttempt = workflow_db.workflowAttempts().create(ex.getIntId(), "bpodgursky", "default", "default", "localhost")
        .setStatus(dead.ordinal())
        .setLastHeartbeat(currentTime - (DbPersistence.HEARTBEAT_INTERVAL * DbPersistence.NUM_HEARTBEAT_TIMEOUTS * 2));
    workflowAttempt.save();

    StepAttempt stepAttempt = workflow_db.stepAttempts().create(workflowAttempt.getIntId(), "step1", StepStatus.RUNNING.ordinal(), Object.class.getName());

    WorkflowRunner workflowRunner = new WorkflowRunner("Workflow",
        new WorkflowDbPersistenceFactory(),
        HadoopWorkflowOptions.test(),
        Sets.newHashSet(new Step(new NoOpAction("step1"))));
    workflowRunner.run();

    assertEquals(WorkflowAttemptStatus.FAILED.ordinal(),
        workflow_db.workflowAttempts().find(workflowAttempt.getId()).getStatus().intValue());

    assertEquals(StepStatus.FAILED.ordinal(),
        workflow_db.stepAttempts().find(stepAttempt.getId()).getStepStatus());

  }


  @Test
  public void testApplicationCreation() throws Exception {


    IWorkflowDb workflow_db = new DatabasesImpl().getWorkflowDb();
    workflow_db.disableCaching();

    WorkflowRunner workflowRunner = new WorkflowRunner("Workflow",
        new WorkflowDbPersistenceFactory(),
        HadoopWorkflowOptions.test(),
        Sets.newHashSet(new Step(new NoOpAction("step1"))));
    workflowRunner.run();

    assertEquals(1, workflow_db.applications().findByName("Workflow").size());

    WorkflowRunner workflowRunner2 = new WorkflowRunner("Workflow",
        new WorkflowDbPersistenceFactory(),
        HadoopWorkflowOptions.test(),
        Sets.newHashSet(new Step(new NoOpAction("step1"))));
    workflowRunner2.run();

    List<Application> applications = workflow_db.applications().findByName("Workflow");
    assertEquals(1, applications.size());

    Application app = Accessors.only(applications);
    assertEquals(2, app.getWorkflowExecution().size());

    assertEquals(1, app.getApplicationConfiguredNotification().size());

    ApplicationConfiguredNotification only = Accessors.only(app.getApplicationConfiguredNotification());
    assertEquals("dt-workflow-alerts@liveramp.com", only.getConfiguredNotification().getEmail());
    assertEquals(WorkflowRunnerNotification.PERFORMANCE.ordinal(), only.getConfiguredNotification().getWorkflowRunnerNotification());

  }

  @Test
  public void testFailOnCleanup() throws Exception {

    IWorkflowDb workflow_db = new DatabasesImpl().getWorkflowDb();
    workflow_db.disableCaching();

    Application app = workflow_db.applications().create("Workflow");

    WorkflowExecution ex = workflow_db.workflowExecutions().create("Workflow", WorkflowExecutionStatus.INCOMPLETE.ordinal())
        .setStartTime(Time.now())
        .setEndTime(Time.now() + 1)
        .setApplicationId(app.getIntId());

    ex.save();

    long currentTime = System.currentTimeMillis();

    WorkflowAttempt workflowAttempt = workflow_db.workflowAttempts().create((int)ex.getId(), "bpodgursky", "default", "default", "localhost")
        .setStatus(WorkflowAttemptStatus.RUNNING.ordinal())
        .setLastHeartbeat(currentTime - (DbPersistence.HEARTBEAT_INTERVAL * 2));
    workflowAttempt.save();

    workflow_db.stepAttempts().create((int)workflowAttempt.getId(), "step1", StepStatus.RUNNING.ordinal(), Object.class.getName());

    Exception exception = getException(() -> {
      new WorkflowRunner("Workflow",
          new WorkflowDbPersistenceFactory(),
          HadoopWorkflowOptions.test(),
          Sets.newHashSet(new Step(new NoOpAction("step1"))));
      return null;
    });


    assertTrue(exception.getMessage().startsWith("Cannot start, a previous attempt is still alive!"));

  }

  @Test
  public void testCleanupNullStatus() throws Exception {

    IWorkflowDb workflow_db = new DatabasesImpl().getWorkflowDb();
    workflow_db.disableCaching();

    Application app = workflow_db.applications().create("Workflow");

    WorkflowExecution ex = workflow_db.workflowExecutions().create("Workflow", WorkflowExecutionStatus.INCOMPLETE.ordinal())
        .setStartTime(Time.now())
        .setEndTime(Time.now() + 1)
        .setApplicationId(app.getIntId());

    ex.save();

    long currentTime = System.currentTimeMillis();

    WorkflowAttempt workflowAttempt = workflow_db.workflowAttempts().create((int)ex.getId(), "bpodgursky", "default", "default", "localhost")
        .setLastHeartbeat(currentTime - (DbPersistence.HEARTBEAT_INTERVAL * 2));
    workflowAttempt.save();

    workflow_db.stepAttempts().create((int)workflowAttempt.getId(), "step1", StepStatus.RUNNING.ordinal(), Object.class.getName());

    new WorkflowRunner("Workflow",
        new WorkflowDbPersistenceFactory(),
        HadoopWorkflowOptions.test(),
        Sets.newHashSet(new Step(new NoOpAction("step1"))));

  }

}