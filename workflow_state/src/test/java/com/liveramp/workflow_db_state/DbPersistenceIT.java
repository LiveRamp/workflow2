package com.liveramp.workflow_db_state;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.liveramp.commons.Accessors;
import com.liveramp.databases.workflow_db.DatabasesImpl;
import com.liveramp.databases.workflow_db.IWorkflowDb;
import com.liveramp.databases.workflow_db.models.WorkflowAttempt;
import com.liveramp.workflow.types.WorkflowAttemptStatus;
import com.liveramp.workflow_core.CoreOptions;
import com.liveramp.workflow_core.runner.BaseStep;
import com.liveramp.workflow_core.step.NoOp;
import com.liveramp.workflow_db_state.runner.WorkflowDbRunners;

import com.rapleaf.cascading_ext.workflow2.BaseWorkflowRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class DbPersistenceIT extends WorkflowDbStateTestCase {

  @Test
  public void testShutdownPostInit() throws IOException {

    DbWorkflow initialized = new BaseWorkflowDbPersistenceFactory()
        .initialize("test", CoreOptions.test());

    initialized.getInitializedPersistence().markWorkflowStopped();

    IWorkflowDb workflowDb = new DatabasesImpl().getWorkflowDb();

    WorkflowAttempt only = Accessors.only(workflowDb.workflowAttempts().findAll());
    assertNotNull(only.getEndTime());
    assertEquals(WorkflowAttemptStatus.FAILED.ordinal(), only.getStatus().longValue());

    initialized.getInitializedPersistence().markWorkflowStopped();

    //  assert that nothing changed
    only = Accessors.only(workflowDb.workflowAttempts().findAll());
    assertNotNull(only.getEndTime());
    assertEquals(WorkflowAttemptStatus.FAILED.ordinal(), only.getStatus().longValue());

  }

  @Test
  public void testShutdownPostRun() throws IOException, InterruptedException {
    IWorkflowDb workflowDb = new DatabasesImpl().getWorkflowDb();


    DbWorkflow initialized = new BaseWorkflowDbPersistenceFactory()
        .initialize("test", CoreOptions.test());


    BaseStep<Void> step = new BaseStep<>(new NoOp<>("step"));

    BaseWorkflowRunner<Void> runner = WorkflowDbRunners.baseWorkflowDbRunner(initialized, Collections.singleton(step));
    runner.run();

    List<WorkflowAttempt> all = workflowDb.workflowAttempts().findAll();
    WorkflowAttempt only = Accessors.only(all);

    Long endTime = only.getEndTime();
    assertEquals(WorkflowAttemptStatus.FINISHED.ordinal(), only.getStatus().longValue());

    Thread.sleep(1000);
    runner.getPersistence().markWorkflowStopped();

    only = Accessors.only(workflowDb.workflowAttempts().findAll());
    assertEquals(WorkflowAttemptStatus.FINISHED.ordinal(), only.getStatus().longValue());
    assertEquals(endTime, only.getEndTime());

    //  TODO I'm not really sure how to test this
    initialized.getInitializedPersistence().shutdown();


  }


}
