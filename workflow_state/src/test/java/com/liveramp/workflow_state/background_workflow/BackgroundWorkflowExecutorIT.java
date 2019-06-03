package com.liveramp.workflow_state.background_workflow;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLNonTransientException;
import java.time.Duration;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.joda.time.DateTime;
import org.junit.Test;
import org.mockito.Mockito;

import com.liveramp.commons.Accessors;
import com.liveramp.commons.test.WaitUntil;
import com.liveramp.databases.workflow_db.DatabasesImpl;
import com.liveramp.databases.workflow_db.IWorkflowDb;
import com.liveramp.databases.workflow_db.models.Application;
import com.liveramp.databases.workflow_db.models.BackgroundStepAttemptInfo;
import com.liveramp.databases.workflow_db.models.BackgroundWorkflowExecutorInfo;
import com.liveramp.databases.workflow_db.models.StepAttempt;
import com.liveramp.databases.workflow_db.models.WorkflowAttempt;
import com.liveramp.databases.workflow_db.models.WorkflowExecution;
import com.liveramp.workflow.types.ExecutorStatus;
import com.liveramp.workflow.types.StepStatus;
import com.liveramp.workflow.types.WorkflowAttemptStatus;
import com.liveramp.workflow.types.WorkflowExecutionStatus;
import com.liveramp.workflow_core.CoreOptions;
import com.liveramp.workflow_core.background_workflow.AlwaysStart;
import com.liveramp.workflow_core.background_workflow.BackgroundAction;
import com.liveramp.workflow_core.background_workflow.BackgroundStep;
import com.liveramp.workflow_core.background_workflow.BackgroundWorkflowPreparer;
import com.liveramp.workflow_core.background_workflow.BackgroundWorkflowSubmitter;
import com.liveramp.workflow_db_state.WorkflowDbStateTestCase;
import com.liveramp.workflow_db_state.background_workflow.BackgroundPersistenceFactory;
import com.liveramp.workflow_db_state.background_workflow.BackgroundWorkflow;
import com.liveramp.workflow_db_state.background_workflow.BackgroundWorkflowIT;
import com.liveramp.workflow_state.WorkflowStatePersistence;

import com.rapleaf.jack.MysqlDatabaseConnection;

import static com.liveramp.workflow_state.background_workflow.BackgroundWorkflowExecutor.workflowDbNonCaching;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;

public class BackgroundWorkflowExecutorIT extends WorkflowDbStateTestCase {

  @Test
  //  test internal claiming logic in executor
  public void testClaiming() throws IOException {

    IWorkflowDb workflowDb = new DatabasesImpl().getWorkflowDb();
    workflowDb.disableCaching();

    BackgroundStepAttemptInfo info = workflowDb.backgroundStepAttemptInfos()
        .create(1, new byte[]{0}, 0L, 5);

    ErrorReporter.InMemoryReporter testClient = new ErrorReporter.InMemoryReporter();

    assertTrue(new BackgroundWorkflowExecutor(
            Lists.newArrayList(),
            1000,
            10,
            testClient,
            InetAddress.getLocalHost().getHostName()
        ).claimStep(workflowDb, 1L, info.getId())
    );

    assertFalse(new BackgroundWorkflowExecutor(
            Lists.newArrayList(),
            1000,
            10,
            testClient,
            InetAddress.getLocalHost().getHostName()
        ).claimStep(workflowDb, 1L, info.getId())
    );

    assertFalse(new BackgroundWorkflowExecutor(
            Lists.newArrayList(),
            1000,
            10,
            testClient,
            InetAddress.getLocalHost().getHostName()
        ).claimStep(workflowDb, 2L, info.getId())
    );

    assertEquals(1L, workflowDb.backgroundStepAttemptInfos().find(info.getId()).getBackgroundWorkflowExecutorInfoId().longValue());

  }

  @Test
  //  simple test that executors run heartbeats and update their status when shut down
  public void testHeartbeatSimple() throws UnknownHostException, InterruptedException {

    ErrorReporter.InMemoryReporter testClient = new ErrorReporter.InMemoryReporter();

    BackgroundWorkflowExecutor executor = new BackgroundWorkflowExecutor(
        workflowDbNonCaching(),
        Lists.newArrayList(),
        1000,
        2000,
        1000,
        10,
        testClient,
        InetAddress.getLocalHost().getHostName()
    );
    executor.start();

    IWorkflowDb db = new DatabasesImpl().getWorkflowDb();

    db.disableCaching();

    long startTime = System.currentTimeMillis();

    WaitUntil.orDie(() -> {
      try {
        return Accessors.only(db.backgroundWorkflowExecutorInfos().findAll()).getLastHeartbeat() > startTime;
      } catch (Exception e) {
        e.printStackTrace();
        return false;
      }
    });

    WaitUntil.orDie(() -> {
      try {
        return Accessors.only(db.backgroundWorkflowExecutorInfos().findAll()).getStatus() == ExecutorStatus.RUNNING.ordinal();
      } catch (Exception e) {
        return false;
      }
    });

    executor.shutdown();

    WaitUntil.orDie(() -> {
      try {
        return Accessors.only(db.backgroundWorkflowExecutorInfos().findAll()).getStatus() == ExecutorStatus.STOPPED.ordinal();
      } catch (Exception e) {
        return false;
      }
    });

    assertEquals(0, testClient.getSentEvents().size());

  }

  @Test
  //  test that an executor will fail steps by other executors which have stopped heartbeating
  public void testDeadWorkerCleanup() throws IOException, InterruptedException {

    IWorkflowDb db = new DatabasesImpl().getWorkflowDb();
    db.disableCaching();

    Application app = db.applications().create("test", 1);

    //  workflow execution
    WorkflowExecution execution = db.workflowExecutions().create(1, "test", null, WorkflowExecutionStatus.INCOMPLETE.ordinal(), 0L, null, app.getIntId(), null);

    //  workflow attempt
    WorkflowAttempt attempt = db.workflowAttempts().create(execution.getIntId(), "a", null, "default", "root", "localhost", 0L, null, WorkflowAttemptStatus.RUNNING.ordinal(), 0L, null, null, null, null, null, null, null);
    db.backgroundAttemptInfos().create(attempt.getId(), null, null);

    //  step attempt
    StepAttempt step = db.stepAttempts().create(attempt.getIntId(), "step", StepStatus.RUNNING.ordinal(), BackgroundWorkflowIT.NoOp.class.getName());

    BackgroundWorkflowExecutorInfo executor = db.backgroundWorkflowExecutorInfos().create("localhost", ExecutorStatus.RUNNING.ordinal(), DateTime.now().minusMinutes(5).getMillis());

    db.backgroundStepAttemptInfos().create(step.getId(), new byte[]{0}, System.currentTimeMillis(), 1, null, executor.getIntId());

    BackgroundWorkflowExecutor runner = new BackgroundWorkflowExecutor(
        workflowDbNonCaching(),
        Lists.newArrayList("test"),
        1000,
        2000,
        1000,
        10,
        new ErrorReporter.InMemoryReporter(),
        InetAddress.getLocalHost().getHostName()
    );
    runner.start();

    WaitUntil.orDie(() -> {
      try {
        return db.stepAttempts().find(step.getId()).getStepStatus() == StepStatus.FAILED.ordinal();
      } catch (IOException e) {
        return false;
      }
    });

    WaitUntil.orDie(() -> {
      try {
        return db.workflowAttempts().find(attempt.getId()).getStatus() == WorkflowAttemptStatus.FAILED.ordinal();
      } catch (IOException e) {
        return false;
      }
    });

  }

  @Test
  //  test that triggering a shutdown on an executor lets running steps drain
  public void testGracefulShutdown() throws IOException, InterruptedException {
    BLOCK = true;

    BackgroundWorkflow initialized = BackgroundWorkflowPreparer.initialize(
        "test",
        new BackgroundPersistenceFactory(),
        CoreOptions.test()
    );


    VoidContext context = new VoidContext();

    BackgroundStep action1 = new BackgroundStep(
        initialized.getRootScope(),
        "action1",
        StaticBlock.class,
        context,
        Duration.ofSeconds(2),
        Sets.newHashSet()
    );

    //  this is executed by the launcher process.  it can exit afterwards.  or hang around.  doesn't really matter.
    WorkflowStatePersistence persistence = BackgroundWorkflowSubmitter.submit(
        initialized,
        Lists.newArrayList(action1)
    );

    ErrorReporter.InMemoryReporter client = new ErrorReporter.InMemoryReporter();

    BackgroundWorkflowExecutor executor = new BackgroundWorkflowExecutor(
        Lists.newArrayList("test"),
        1000,
        10,
        client,
        "localhost"
    );

    executor.start();

    WaitUntil.orDie(() -> {
      try {
        return persistence.getStatus("action1") == StepStatus.RUNNING;
      } catch (IOException e) {
        return false;
      }
    });

    //  shutdown is blocking, need to run it in a thread
    new Thread(() -> {
      try {
        executor.shutdown();
      } catch (InterruptedException e) {
        fail();
      }
    }).start();

    BLOCK = false;

    WaitUntil.orDie(() -> {
      try {
        return persistence.getStatus("action1") == StepStatus.COMPLETED;
      } catch (IOException e) {
        return false;
      }
    });

    assertEquals(0, client.getSentEvents().size());

  }

  @Test
  //  tests that on a db connection error, executors will gracefully drain running tasks
  public void testDbDisconnect() throws InterruptedException, SQLException, IOException {
    BLOCK = true;

    BackgroundWorkflow initialized = BackgroundWorkflowPreparer.initialize(
        "test",
        new BackgroundPersistenceFactory(),
        CoreOptions.test()
    );


    VoidContext context = new VoidContext();

    BackgroundStep action1 = new BackgroundStep(
        initialized.getRootScope(),
        "action1",
        StaticBlock.class,
        context,
        Duration.ofSeconds(2),
        Sets.newHashSet()
    );

    WorkflowStatePersistence persistence = BackgroundWorkflowSubmitter.submit(
        initialized,
        Lists.newArrayList(action1)
    );

    PreparedStatement statement = Mockito.spy(PreparedStatement.class);

    Mockito.when(statement.executeQuery())
        .thenThrow(new SQLNonTransientException("Percona is on fire!"));

    MysqlDatabaseConnection conn = Mockito.spy(new MysqlDatabaseConnection("workflow_db"));

    Mockito.when(conn.getPreparedStatement(anyString()))
        .thenCallRealMethod()
        .thenCallRealMethod()
        .thenCallRealMethod()
        .thenCallRealMethod()
        .thenCallRealMethod()
        .thenCallRealMethod()
        .thenCallRealMethod()
        .thenCallRealMethod()
        .thenReturn(statement)
        .thenCallRealMethod();

    IWorkflowDb mockedDb = new DatabasesImpl(conn).getWorkflowDb();

    ErrorReporter.InMemoryReporter testClient = new ErrorReporter.InMemoryReporter();

    BackgroundWorkflowExecutor executor = new BackgroundWorkflowExecutor(
        mockedDb,
        Lists.newArrayList("test"),
        1000,
        2000,
        1000,
        10,
        testClient,
        "localhost"
    );

    executor.start();

    //  tests draining on an error

    //  make sure action started
    WaitUntil.orDie(() -> {
      try {
        return persistence.getStatus("action1") == StepStatus.RUNNING;
      } catch (IOException e) {
        return false;
      }
    });

    //  then we get an exception in the executor
    WaitUntil.orDie(() -> testClient.getSentEvents().size() == 1);

    //  let action drain
    BLOCK = false;

    WaitUntil.orDie(() -> {
      try {
        return persistence.getStatus("action1") == StepStatus.COMPLETED;
      } catch (IOException e) {
        return false;
      }
    });

    assertTrue(Accessors.only(testClient.getSentEvents()).getMessage().contains("java.io.IOException: java.sql.SQLNonTransientException: Percona is on fire"));

    executor.shutdown();

  }

  @Test
  //  tests that if an executor misses enough heartbeats to be timed out, it will immediately shut down running tasks.
  //  this seems harsh, but in this case, other executors might have started doing the same work (the workflow been cancelled
  //  and re-run etc).  multiple executors doing the same work leads to chaos.
  public void testSlowHeartbeat() throws IOException, InterruptedException {

    //  step that can be interrupted
    BackgroundWorkflow initialized = BackgroundWorkflowPreparer.initialize(
        "test",
        new BackgroundPersistenceFactory(),
        CoreOptions.test()
    );

    VoidContext context = new VoidContext();

    BackgroundStep action1 = new BackgroundStep(
        initialized.getRootScope(),
        "action1",
        SleepStep.class,
        context,
        Duration.ofSeconds(2),
        Sets.newHashSet()
    );

    WorkflowStatePersistence persistence = BackgroundWorkflowSubmitter.submit(
        initialized,
        Lists.newArrayList(action1)
    );

    IWorkflowDb db = new DatabasesImpl().getWorkflowDb();
    db.disableCaching();

    ErrorReporter.InMemoryReporter test = new ErrorReporter.InMemoryReporter();

    BackgroundWorkflowExecutor executor = new BackgroundWorkflowExecutor(
        db,
        Lists.newArrayList("test"),
        1000,
        2000,
        1000,
        10,
        test,
        "localhost"
    );

    executor.start();

    //  wait until step is running
    WaitUntil.orDie(() -> {
      try {
        return persistence.getStatus("action1") == StepStatus.RUNNING;
      } catch (IOException e) {
        return false;
      }
    });

    //  expect step to fail
    WaitUntil.orDie(() -> {
      try {

        //  fake heartbeat out by setting executor heartbeat in the past
        Accessors.only(db.backgroundWorkflowExecutorInfos().findAll()).setLastHeartbeat(0L).save();

        Thread.sleep(1500);

        return persistence.getStatus("action1") == StepStatus.FAILED;
      } catch (Exception e) {
        return false;
      }
    });

    System.out.println(persistence.getStatus("action1"));
    System.out.println(test.getSentEvents());

    //  verify we got an alert that the executor committed sepuku
    assertEquals("Worker heartbeat encountered exception", Accessors.only(
        test.getSentEvents()
    ).getTitle());

  }

  @Test
  //  test that the executor respects the max task limits
  public void testMaxConcurrency() throws IOException, InterruptedException {
    BLOCK = true;

    BackgroundWorkflow initialized = BackgroundWorkflowPreparer.initialize(
        "test",
        new BackgroundPersistenceFactory(),
        CoreOptions.test()
    );

    VoidContext context = new VoidContext();

    //  two StaticBlock head actions

    BackgroundStep action1 = new BackgroundStep(
        initialized.getRootScope(),
        "action1",
        StaticBlock.class,
        context
    );

    BackgroundStep action2 = new BackgroundStep(
        initialized.getRootScope(),
        "action2",
        StaticBlock.class,
        context
    );

    WorkflowStatePersistence persistence = BackgroundWorkflowSubmitter.submit(
        initialized,
        Lists.newArrayList(action1, action2)
    );


    ErrorReporter.InMemoryReporter test = new ErrorReporter.InMemoryReporter();

    BackgroundWorkflowExecutor executor1 = new BackgroundWorkflowExecutor(
        new DatabasesImpl().getWorkflowDb(),
        Lists.newArrayList("test"),
        1000,
        2000,
        1000,
        1,
        test,
        "localhost"
    );

    executor1.start();

    BackgroundWorkflowExecutor executor2 = new BackgroundWorkflowExecutor(
        new DatabasesImpl().getWorkflowDb(),
        Lists.newArrayList("test"),
        1000,
        2000,
        1000,
        1,
        test,
        "localhost"
    );

    executor2.start();

    //  wait until both steps are running

    WaitUntil.orDie(() -> {
      try {
        return persistence.getStatus("action1") == StepStatus.RUNNING &&
            persistence.getStatus("action2") == StepStatus.RUNNING;
      } catch (IOException e) {
        return false;
      }
    });

    //  unblock
    BLOCK = false;

    //  verify that each was run by a separate executor

    IWorkflowDb db = new DatabasesImpl().getWorkflowDb();

    WaitUntil.orDie(() -> {

      try {

        return db.backgroundStepAttemptInfos().findAll().stream()
            .map(BackgroundStepAttemptInfo::getBackgroundWorkflowExecutorInfoId)
            .collect(Collectors.toSet()).size() == 2;

      } catch (IOException e) {
        return false;
      }

    });


    executor1.shutdown();
    executor2.shutdown();

  }

  //  TODO max concurrent steps in executor


  private static class VoidContext implements Serializable {
  }


  private static boolean BLOCK = true;


  public static class SleepStep extends BackgroundAction<VoidContext> {

    public SleepStep() {
      super(new AlwaysStart<>());
    }

    @Override
    public void initializeInternal(VoidContext voidContext) {

    }

    @Override
    protected void execute() throws Exception {
      Thread.sleep(Integer.MAX_VALUE);
    }
  }

  public static class StaticBlock extends BackgroundAction<VoidContext> {

    public StaticBlock() {
      super(new AlwaysStart<>());
    }

    @Override
    public void initializeInternal(VoidContext aVoid) {
    }

    @Override
    protected void execute() throws Exception {

      while (BLOCK) {
        Thread.sleep(1000);
      }
    }
  }


}
