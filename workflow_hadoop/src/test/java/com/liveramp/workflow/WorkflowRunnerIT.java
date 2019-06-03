package com.liveramp.workflow;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.apache.hadoop.mapreduce.TaskCounter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import cascading.flow.Flow;
import cascading.flow.FlowProcess;
import cascading.operation.BaseOperation;
import cascading.operation.Filter;
import cascading.operation.FilterCall;
import cascading.pipe.Each;
import cascading.pipe.Pipe;
import cascading.tap.Tap;
import cascading.tuple.Fields;
import cascading.tuple.Tuple;

import com.liveramp.cascading_ext.CascadingUtil;
import com.liveramp.cascading_ext.flow.JobRecordListener;
import com.liveramp.cascading_ext.tap.NullTap;
import com.liveramp.cascading_ext.util.DataStoreUtils;
import com.liveramp.commons.Accessors;
import com.liveramp.commons.collections.nested_map.ThreeNestedMap;
import com.liveramp.commons.collections.nested_map.TwoNestedMap;
import com.liveramp.commons.util.MultiShutdownHook;
import com.liveramp.databases.workflow_db.DatabasesImpl;
import com.liveramp.databases.workflow_db.IWorkflowDb;
import com.liveramp.databases.workflow_db.models.Application;
import com.liveramp.databases.workflow_db.models.ExecutionTag;
import com.liveramp.databases.workflow_db.models.StepAttempt;
import com.liveramp.databases.workflow_db.models.StepDependency;
import com.liveramp.databases.workflow_db.models.WorkflowAttempt;
import com.liveramp.databases.workflow_db.models.WorkflowExecution;
import com.liveramp.java_support.alerts_handler.AlertMessage;
import com.liveramp.java_support.alerts_handler.AlertsHandler;
import com.liveramp.java_support.alerts_handler.BufferingAlertsHandler;
import com.liveramp.java_support.alerts_handler.MailBuffer;
import com.liveramp.java_support.alerts_handler.configs.DefaultAlertMessageConfig;
import com.liveramp.java_support.alerts_handler.recipients.AlertRecipient;
import com.liveramp.java_support.alerts_handler.recipients.AlertSeverity;
import com.liveramp.java_support.alerts_handler.recipients.EngineeringAlertRecipient;
import com.liveramp.java_support.alerts_handler.recipients.RecipientListBuilder;
import com.liveramp.java_support.alerts_handler.recipients.RecipientUtils;
import com.liveramp.workflow.state.DbHadoopWorkflow;
import com.liveramp.workflow.state.WorkflowDbPersistenceFactory;
import com.liveramp.workflow.test.MonitoredPersistenceFactory;
import com.liveramp.workflow.types.StepStatus;
import com.liveramp.workflow.types.WorkflowAttemptStatus;
import com.liveramp.workflow.types.WorkflowExecutionStatus;
import com.liveramp.workflow2.workflow_hadoop.HadoopMultiStepAction;
import com.liveramp.workflow2.workflow_hadoop.ResourceManagers;
import com.liveramp.workflow_core.WorkflowTag;
import com.liveramp.workflow_core.alerting.BufferingAlertsHandlerFactory;
import com.liveramp.workflow_core.runner.BaseAction;
import com.liveramp.workflow_core.step.NoOp;
import com.liveramp.workflow_db_state.DbPersistence;
import com.liveramp.workflow_db_state.InitializedDbPersistence;
import com.liveramp.workflow_db_state.WorkflowQueries;
import com.liveramp.workflow_db_state.controller.ApplicationController;
import com.liveramp.workflow_db_state.controller.ExecutionController;
import com.liveramp.workflow_state.DSAction;
import com.liveramp.workflow_state.DataStoreInfo;
import com.liveramp.workflow_state.MapReduceJob;
import com.liveramp.workflow_state.StepState;
import com.liveramp.workflow_state.WorkflowRunnerNotification;
import com.liveramp.workflow_state.WorkflowStatePersistence;
import com.rapleaf.cascading_ext.datastore.DataStore;
import com.rapleaf.cascading_ext.datastore.TupleDataStore;
import com.rapleaf.cascading_ext.workflow2.Action;
import com.rapleaf.cascading_ext.workflow2.DelayedFailingAction;
import com.rapleaf.cascading_ext.workflow2.FailingAction;
import com.rapleaf.cascading_ext.workflow2.FlipAction;
import com.rapleaf.cascading_ext.workflow2.IncrementAction2;
import com.rapleaf.cascading_ext.workflow2.LockedAction;
import com.rapleaf.cascading_ext.workflow2.Step;
import com.rapleaf.cascading_ext.workflow2.UnlockWaitAction;
import com.rapleaf.cascading_ext.workflow2.WorkflowNotificationLevel;
import com.rapleaf.cascading_ext.workflow2.WorkflowRunner;
import com.rapleaf.cascading_ext.workflow2.WorkflowTestCase;
import com.rapleaf.cascading_ext.workflow2.action.NoOpAction;
import com.rapleaf.cascading_ext.workflow2.options.HadoopWorkflowOptions;
import com.rapleaf.cascading_ext.workflow2.rollback.UnlessStepsRun;
import com.rapleaf.cascading_ext.workflow2.state.HdfsCheckpointPersistence;
import com.rapleaf.cascading_ext.workflow2.state.InitializedWorkflow;
import com.rapleaf.cascading_ext.workflow2.state.WorkflowPersistenceFactory;
import com.rapleaf.cascading_ext.workflow2.test.WorkflowTestUtils;
import com.rapleaf.jack.queries.QueryOrder;

import static com.google.common.collect.Sets.newHashSet;
import static com.liveramp.commons.test.TestUtils.assertCollectionContains;
import static com.liveramp.commons.test.TestUtils.assertCollectionEquivalent;
import static com.rapleaf.cascading_ext.workflow2.test.WorkflowTestUtils.execute;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class WorkflowRunnerIT extends WorkflowTestCase {

  private WorkflowPersistenceFactory hdfsPersistenceFactory = new HdfsCheckpointPersistence(getTestRoot() + "/hdfs_root");

  private WorkflowPersistenceFactory dbPersistenceFactory = new WorkflowDbPersistenceFactory();

  @Before
  public void prepare() throws Exception {
    IncrementAction.counter = 0;
    new DatabasesImpl().getWorkflowDb().deleteAll();
  }

  private WorkflowRunner buildWfr(WorkflowPersistenceFactory persistence, Step tail) throws IOException {
    return buildWfr(persistence, newHashSet(tail));
  }

  private WorkflowRunner buildWfr(WorkflowPersistenceFactory persistence, Set<Step> tailSteps) throws IOException {
    return buildWfr(persistence, HadoopWorkflowOptions.test(), tailSteps);
  }

  private WorkflowRunner buildWfr(WorkflowPersistenceFactory persistence, HadoopWorkflowOptions opts, Set<Step> tailSteps) throws IOException {
    return new WorkflowRunner("Test Workflow", persistence, opts, tailSteps);
  }

  private InitializedWorkflow buildWf(WorkflowPersistenceFactory persistence, HadoopWorkflowOptions opts) throws IOException {
    return persistence.initialize("Test Workflow", opts);
  }

  @Test
  public void testSimple1() throws Exception {
    testSimple(hdfsPersistenceFactory);
  }

  @Test
  public void testSimple2() throws Exception {
    testSimple(dbPersistenceFactory);
  }


  public void testSimple(WorkflowPersistenceFactory persistence) throws Exception {
    Step first = new Step(new IncrementAction("first"));
    Step second = new Step(new IncrementAction("second"), first);

    buildWfr(persistence, second).run();

    assertEquals(2, IncrementAction.counter);
  }

  @Test
  public void testRetainPool() throws IOException {

    assertEquals("root.some_team", WorkflowPersistenceFactory
        .findDefaultValue(HadoopWorkflowOptions.test().addWorkflowProperties(Collections.singletonMap("mapreduce.job.queuename", "root.some_team")),
            "mapreduce.job.queuename",
            "default"
        )
    );
  }

  @Test
  public void testDescription() throws IOException {

    Step first = new Step(new IncrementAction("first"));

    WorkflowRunner wr = new WorkflowRunner("test",
        new WorkflowDbPersistenceFactory(),
        HadoopWorkflowOptions.test().setDescription("description1"),
        first
    );
    wr.run();

    IWorkflowDb workflowDb = new DatabasesImpl().getWorkflowDb();

    assertEquals("description1", workflowDb.workflowAttempts().find(wr.getPersistence().getAttemptId()).getDescription());
  }

  @Test
  public void testTagsAreCreated() throws Exception {
    String key = "k";
    String val = "v";
    WorkflowRunner wr = new WorkflowRunner("test",
        new WorkflowDbPersistenceFactory(),
        HadoopWorkflowOptions.test().addTag(WorkflowTag.of(key, val)),
        new Step(new NoOpAction("no-op"))
    );
    wr.run();
    IWorkflowDb db = new DatabasesImpl().getWorkflowDb();

    ExecutionTag tag = Iterables.getOnlyElement(db.executionTags().findByTag(key));
    assertEquals(key, tag.getTag());
    assertEquals(val, tag.getValue());
  }

  @Test
  public void testInitialStatus() throws IOException {
    IWorkflowDb workflowDb = new DatabasesImpl().getWorkflowDb();

    Step first = new Step(new IncrementAction("first"));
    WorkflowAttempt attempt = WorkflowQueries.getLatestAttempt(workflowDb.workflowExecutions().find(buildWfr(dbPersistenceFactory, first).getPersistence().getExecutionId()));
    assertEquals(WorkflowAttemptStatus.INITIALIZING.ordinal(), attempt.getStatus().intValue());

  }

  @Test
  public void testFullRestart1() throws IOException {
    testFullRestart(hdfsPersistenceFactory);
  }

  @Test
  public void testFailReturnsChain() throws IOException {

    //  TODO the case which this tries to catch hangs forever.  figure out a timer or something
    //  instead of just having the test hang

    Step zero = new Step(new NoOpAction("succeed"));
    Step one = new Step(new FailingAction("fail"));
    Step two = new Step(new NoOpAction("after"), zero, one);
    Step three = new Step(new NoOpAction("later"), two);

    try {
      execute(three);
      fail();
    } catch (Exception e) {
      // fine
    }
  }

  @Test
  public void testNotificationDestinations() throws IOException {
    IWorkflowDb workflowDb = new DatabasesImpl().getWorkflowDb();
    workflowDb.disableCaching();

    BufferingAlertsHandlerFactory factory = new BufferingAlertsHandlerFactory();

    Step one = new Step(new FailingAction("fail"));

    WorkflowRunner wr = new WorkflowRunner("test", new WorkflowDbPersistenceFactory(), HadoopWorkflowOptions.test()
        .setNotificationLevel(WorkflowNotificationLevel.ERROR)
        .setAlertsHandlerFactory(factory)
        .setAlertsHandler(factory.buildHandler(Sets.newHashSet("supplied-handler@example.com"), new MailBuffer.ListBuffer())),
        one);

    ApplicationController.addConfiguredNotifications(workflowDb,
        "test",
        "test@gmail.com",
        WorkflowNotificationLevel.ERROR
    );

    ExecutionController.addConfiguredNotifications(workflowDb,
        wr.getPersistence().getExecutionId(),
        "ben@gmail.com",
        WorkflowNotificationLevel.ERROR
    );

    try {
      wr.run();
      fail();
    } catch (Exception e) {
      // fine
    }

    DbPersistence origPersistence = (DbPersistence)wr.getPersistence();
    DbPersistence newPersistence = DbPersistence.queryPersistence(origPersistence.getAttemptId(), workflowDb);


    for (AlertsHandler alertsHandler : newPersistence.getRecipients(WorkflowRunnerNotification.DIED_UNCLEAN, factory)) {
      alertsHandler.sendAlert("Died Unclean", "Test", new EngineeringAlertRecipient(AlertSeverity.ERROR));
    }

    Multimap<String, String> recipientToSubjects = HashMultimap.create();

    DefaultAlertMessageConfig config = new DefaultAlertMessageConfig(true, Lists.newArrayList());

    for (Map.Entry<String, Set<AlertMessage>> entry : factory.getBuffer().getSentEmails().entrySet()) {
      final List<String> subjects = entry.getValue().stream().map(s -> s.getSubject(config)).collect(Collectors.toList());
      recipientToSubjects.putAll(entry.getKey(), subjects);
    }

    assertEquals(3, recipientToSubjects.keySet().size());

    assertCollectionEquivalent(Lists.newArrayList("Died Unclean", "[ERROR] [WORKFLOW] Failed: test"), recipientToSubjects.get("ben@gmail.com"));
    assertCollectionEquivalent(Lists.newArrayList("Died Unclean", "[ERROR] [WORKFLOW] Failed: test"), recipientToSubjects.get("test@gmail.com"));
    assertCollectionEquivalent(Lists.newArrayList("Died Unclean", "[ERROR] [WORKFLOW] Failed: test"), recipientToSubjects.get("supplied-handler@example.com"));

  }

  @Test
  public void testKeepRunning() throws Exception {

    final Semaphore sem = new Semaphore(0);
    final Semaphore sem2 = new Semaphore(0);

    Step one = new Step(new DelayedFailingAction("fail", sem2));
    Step two = new Step(new UnlockWaitAction("wait", sem2, sem));
    Step three = new Step(new NoOpAction("proceed"), two);

    final List<String> messages = Lists.newArrayList();

    WorkflowRunner runner = new WorkflowRunner("Test Workflow", new WorkflowDbPersistenceFactory(), HadoopWorkflowOptions.test()
        .setNotificationLevel(WorkflowNotificationLevel.ERROR)
        .setMaxConcurrentSteps(2)
        .setAlertsHandler(new AlertsHandler() {

          @Override
          public void sendAlert(AlertMessage contents, AlertRecipient recipient, AlertRecipient... ad) {
            messages.add(contents.getSubject(new DefaultAlertMessageConfig(true, Lists.newArrayList("TAG"))));
            sem.release();
          }

          @Override
          public void sendAlert(String subject, String body, AlertRecipient recipient, AlertRecipient... additionalRecipients) {
          }

          @Override
          public void sendAlert(String subject, Throwable t, AlertRecipient recipient, AlertRecipient... additionalRecipients) {
          }

          @Override
          public void sendAlert(String subject, String body, Throwable t, AlertRecipient recipient, AlertRecipient... additionalRecipients) {
          }

          @Override
          public RecipientListBuilder resolveRecipients(List<AlertRecipient> recipients) {
            return new RecipientListBuilder();
          }

        }), newHashSet(one, three));

    try {
      runner.run();
      fail();
    } catch (Exception e) {
      // fine
    }


    assertEquals(StepStatus.COMPLETED, runner.getPersistence().getStatus("proceed"));
    assertEquals(StepStatus.COMPLETED, runner.getPersistence().getStatus("wait"));
    assertEquals(StepStatus.FAILED, runner.getPersistence().getStatus("fail"));

    assertCollectionEquivalent(Lists.newArrayList(
        "[ERROR] [TAG] [WORKFLOW] Step has failed in: Test Workflow",
        "[ERROR] [TAG] [WORKFLOW] Failed: Test Workflow"),
        messages
    );

  }


  private static class EmptyMSA extends HadoopMultiStepAction {

    public EmptyMSA(String checkpointToken, String tmpRoot) {
      super(checkpointToken, tmpRoot);

      setSubSteps(Sets.<Step>newHashSet());
    }
  }

  @Test
  public void testEmptyMSA() throws Exception {

    Step step1 = new Step(new EmptyMSA("empty-msa",
        getTestRoot() + "/tmp"
    ));

    Step step2 = new Step(new NoOpAction("dep"),
        step1
    );

    new WorkflowRunner(
        "Test Workflow",
        new WorkflowDbPersistenceFactory(),
        HadoopWorkflowOptions.test(),
        Sets.<Step>newHashSet(step2)
    ).run();

    StepDependency dep = Accessors.only(new DatabasesImpl().getWorkflowDb().stepDependencies().findAll());

    StepAttempt attempt1 = dep.getStepAttempt();
    StepAttempt attempt2 = dep.getDependencyAttempt();

    assertEquals("dep", attempt1.getStepToken());
    assertEquals("empty-msa__empty-msa-placeholder", attempt2.getStepToken());

  }

  @Test
  public void testNotificationLevels() throws Exception {

    runFlow(WorkflowNotificationLevel.DEBUG, new NoOpAction("step"), Lists.newArrayList(
        "[WORKFLOW] Started: Test workflow",
        "[WORKFLOW] Succeeded: Test workflow"
    ));

    runFlow(WorkflowNotificationLevel.ERROR, new NoOpAction("step"), Lists.<String>newArrayList());

    runFlow(WorkflowNotificationLevel.INFO, new NoOpAction("step"), Lists.newArrayList(
        "[WORKFLOW] Succeeded: Test workflow"
    ));

    runFlow(WorkflowNotificationLevel.ERROR, new FailingAction("step"), Lists.newArrayList(
        "[ERROR] [WORKFLOW] Failed: Test workflow"
    ));

    runFlow(WorkflowNotificationLevel.DEBUG, new FailingAction("step"), Lists.newArrayList(
        "[WORKFLOW] Started: Test workflow",
        "[ERROR] [WORKFLOW] Failed: Test workflow"
    ));

  }

  private void runFlow(Set<WorkflowRunnerNotification> level, BaseAction toRun, List<String> expectedAlerts) throws IOException {

    SubjectAlertHandler handler = new SubjectAlertHandler();

    Step step = new Step(toRun);

    try {
      execute(step, HadoopWorkflowOptions.test()
          .setNotificationLevel(level)
          .setAlertsHandler(handler)
      );
    } catch (Exception e) {
      //  fine
    }

    assertCollectionEquivalent(expectedAlerts, handler.getSubjects());

  }


  private static class SubjectAlertHandler implements AlertsHandler {

    private final Set<String> subjects = newHashSet();

    public Set<String> getSubjects() {
      return subjects;
    }

    @Override
    public void sendAlert(AlertMessage contents, AlertRecipient recipient, AlertRecipient... ad) {
      subjects.add(contents.getSubject(new DefaultAlertMessageConfig(false, Lists.<String>newArrayList())));
    }

    @Override
    public void sendAlert(String subject, String body, AlertRecipient recipient, AlertRecipient... additionalRecipients) {
      subjects.add(subject);
    }

    @Override
    public void sendAlert(String subject, Throwable t, AlertRecipient recipient, AlertRecipient... additionalRecipients) {
      subjects.add(subject);
    }

    @Override
    public void sendAlert(String subject, String body, Throwable t, AlertRecipient recipient, AlertRecipient... additionalRecipients) {
      subjects.add(subject);
    }

    @Override
    public RecipientListBuilder resolveRecipients(List<AlertRecipient> recipients) {
      return new RecipientListBuilder();
    }
  }


  @Test
  public void testSingleAlert() throws Exception {

    Step one = new Step(new FailingAction("fail"));

    final List<String> messages = Lists.newArrayList();

    WorkflowRunner runner = new WorkflowRunner("Test Workflow", new WorkflowDbPersistenceFactory(), HadoopWorkflowOptions.test()
        .setNotificationLevel(WorkflowNotificationLevel.ERROR)
        .setAlertsHandler(new AlertsHandler() {

          @Override
          public void sendAlert(AlertMessage contents, AlertRecipient recipient, AlertRecipient... ad) {
            messages.add(contents.getSubject(new DefaultAlertMessageConfig(true, Lists.newArrayList("TAG"))));
          }

          @Override
          public void sendAlert(String subject, String body, AlertRecipient recipient, AlertRecipient... additionalRecipients) {
          }

          @Override
          public void sendAlert(String subject, Throwable t, AlertRecipient recipient, AlertRecipient... additionalRecipients) {
          }

          @Override
          public void sendAlert(String subject, String body, Throwable t, AlertRecipient recipient, AlertRecipient... additionalRecipients) {
          }

          @Override
          public RecipientListBuilder resolveRecipients(List<AlertRecipient> recipients) {
            return new RecipientListBuilder();
          }

        }), one);

    try {
      runner.run();
      fail();
    } catch (Exception e) {
      // fine
    }

    assertCollectionEquivalent(Lists.newArrayList(
        "[ERROR] [TAG] [WORKFLOW] Failed: Test Workflow"),
        messages
    );

  }


  @Test
  public void testFullRestart2() throws IOException {
    testFullRestart(dbPersistenceFactory);
  }

  public void testFullRestart(WorkflowPersistenceFactory persistence) throws IOException {

    //  test a full restart if interrupted by a failure

    AtomicInteger int1 = new AtomicInteger(0);
    AtomicInteger int2 = new AtomicInteger(0);

    Step one = new Step(new IncrementAction2("one", int1));
    Step two = new Step(new FailingAction("two"), one);
    Step three = new Step(new IncrementAction2("three", int2), two);

    WorkflowRunner run = new WorkflowRunner("Test Workflow", persistence, HadoopWorkflowOptions.test(), newHashSet(three));

    try {
      run.run();
      fail();
    } catch (Exception e) {
      //  no-op
    }

    assertEquals(1, int1.intValue());
    assertEquals(0, int2.intValue());

    one = new Step(new IncrementAction2("one", int1));
    two = new Step(new NoOpAction("two"), one);
    three = new Step(new IncrementAction2("three", int2), two);

    run = new WorkflowRunner("Test Workflow", persistence, HadoopWorkflowOptions.test(), newHashSet(three));
    run.run();

    assertEquals(1, int1.intValue());
    assertEquals(1, int2.intValue());

  }

  @Test
  public void testLoneMultiStepAction1() throws Exception {
    testLoneMultiStepAction(hdfsPersistenceFactory);
  }

  @Test
  public void testLoneMultiStepAction2() throws Exception {
    testLoneMultiStepAction(dbPersistenceFactory);
  }

  public void testLoneMultiStepAction(WorkflowPersistenceFactory factory) throws Exception {
    // lone multi
    Step s = new Step(new HadoopMultiStepAction("lone", getTestRoot(), Arrays.asList(new Step(
        new IncrementAction("blah")))));

    buildWfr(factory, s).run();

    assertEquals(1, IncrementAction.counter);
  }

  @Test
  public void testMultiInTheMiddle1() throws Exception {
    testMultiIntheMiddle(hdfsPersistenceFactory);
  }

  @Test
  public void testMultiInTheMiddle2() throws Exception {
    testMultiIntheMiddle(dbPersistenceFactory);
  }

  public void testMultiIntheMiddle(WorkflowPersistenceFactory factory) throws IOException {
    Step s = new Step(new IncrementAction("first"));
    s = new Step(new HadoopMultiStepAction("lone", getTestRoot(), Arrays.asList(new Step(new IncrementAction("blah")))),
        s);
    s = new Step(new IncrementAction("last"), s);

    buildWfr(factory, s).run();

    assertEquals(3, IncrementAction.counter);
  }

  @Test
  public void testMultiAtTheEnd() throws Exception {
    testMultiAtEnd(hdfsPersistenceFactory);
  }

  @Test
  public void testMultiAtTheEnd2() throws Exception {
    testMultiAtEnd(dbPersistenceFactory);
  }


  public void testMultiAtEnd(WorkflowPersistenceFactory factory) throws IOException {
    Step s = new Step(new IncrementAction("first"));
    s = new Step(new HadoopMultiStepAction("lone", getTestRoot(), Arrays.asList(new Step(new IncrementAction("blah")))),
        s);

    buildWfr(factory, s).run();

    assertEquals(2, IncrementAction.counter);
  }

  private static Thread run(final WorkflowRunner runner, final Wrapper<Exception> exception) {
    return new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          runner.run();
        } catch (Exception e) {
          exception.setVal(e);
        }
      }
    });
  }

  @Test
  public void testFailThenShutdown1() throws InterruptedException, IOException {
    testFailThenShutdown(hdfsPersistenceFactory);
  }

  @Test
  public void testFailThenShutdown2() throws InterruptedException, IOException {
    testFailThenShutdown(dbPersistenceFactory);
  }

  public void testFailThenShutdown(WorkflowPersistenceFactory factory) throws InterruptedException, IOException {

    Semaphore semaphore = new Semaphore(0);
    Semaphore semaphore2 = new Semaphore(0);
    AtomicBoolean didExecute = new AtomicBoolean();

    Step fail = new Step(new DelayedFailingAction("fail", semaphore));
    Step unlockFail = new Step(new UnlockWaitAction("unlock", semaphore, semaphore2));
    Step last = new Step(new FlipAction("after", didExecute), unlockFail);

    Wrapper<Exception> exception = new Wrapper<>();
    WorkflowRunner run = new WorkflowRunner("Test Workflow", factory, HadoopWorkflowOptions.test().setMaxConcurrentSteps(2),
        newHashSet(fail, last));

    WorkflowStatePersistence persistence = run.getPersistence();

    Thread t = run(run, exception);
    t.start();

    Thread.sleep(500);
    persistence.markShutdownRequested("Shutdown Requested");
    semaphore2.release();

    t.join();

    Exception failure = exception.getVal();
    assertTrue(failure.getMessage().contains("(1/1) Step fail failed with exception: failed on purpose"));

    assertEquals("Shutdown Requested", persistence.getShutdownRequest());
    assertFalse(didExecute.get());
    assertTrue(persistence.getStatus("fail") == StepStatus.FAILED);
    assertTrue(persistence.getStatus("unlock") == StepStatus.COMPLETED);
    assertTrue(persistence.getStatus("after") == StepStatus.WAITING);

  }

  @Test
  public void testShutdownThenFail1() throws InterruptedException, IOException {
    testShutdownThenFail(hdfsPersistenceFactory);
  }

  @Test
  public void testShutdownThenFail2() throws InterruptedException, IOException {
    testShutdownThenFail(dbPersistenceFactory);
  }

  public void testShutdownThenFail(WorkflowPersistenceFactory factory) throws InterruptedException, IOException {

    Semaphore semaphore = new Semaphore(0);
    AtomicBoolean didExecute = new AtomicBoolean(false);


    Step fail = new Step(new DelayedFailingAction("fail", semaphore));
    Step after = new Step(new FlipAction("after", didExecute), fail);

    Wrapper<Exception> exception = new Wrapper<Exception>();
    WorkflowRunner run = new WorkflowRunner("Test Workflow", factory, HadoopWorkflowOptions.test(), newHashSet(after));
    WorkflowStatePersistence persistence = run.getPersistence();

    Thread t = run(run, exception);
    t.start();

    Thread.sleep(500);
    persistence.markShutdownRequested("Shutdown Requested");

    semaphore.release();

    t.join();

    Exception failure = exception.getVal();
    assertTrue(failure.getMessage().contains("(1/1) Step fail failed with exception: failed on purpose"));

    assertEquals("Shutdown Requested", persistence.getShutdownRequest());
    assertFalse(didExecute.get());
    assertTrue(persistence.getStatus("fail") == StepStatus.FAILED);
    assertTrue(persistence.getStatus("after") == StepStatus.WAITING);
  }

  @Test
  public void testShutdown1() throws InterruptedException, IOException {
    testShutdown(hdfsPersistenceFactory);
  }

  @Test
  public void testShutdown12() throws InterruptedException, IOException {
    testShutdown(dbPersistenceFactory);
  }

  public void testShutdown(WorkflowPersistenceFactory factory) throws InterruptedException, IOException {

    Semaphore semaphore = new Semaphore(0);
    AtomicInteger preCounter = new AtomicInteger(0);
    AtomicInteger postConter = new AtomicInteger(0);

    Step pre = new Step(new IncrementAction2("pre", preCounter));
    Step step = new Step(new LockedAction("wait", semaphore), pre);
    Step after = new Step(new IncrementAction2("after", postConter), step);

    Wrapper<Exception> exception = new Wrapper<Exception>();
    WorkflowRunner run = new WorkflowRunner("Test Workflow", factory, HadoopWorkflowOptions.test(), newHashSet(after));

    WorkflowStatePersistence peristence = run.getPersistence();

    Thread t = run(run, exception);
    t.start();

    Thread.sleep(500);
    peristence.markShutdownRequested("Shutdown Requested");

    semaphore.release();

    t.join();

    Exception failure = exception.getVal();

    assertEquals("Shutdown requested: Test Workflow. Reason: Shutdown Requested", failure.getMessage());

    assertEquals("Shutdown Requested", peristence.getShutdownRequest());
    assertEquals(1, preCounter.get());
    assertEquals(0, postConter.get());
    assertTrue(peristence.getStatus("pre") == StepStatus.COMPLETED);
    assertTrue(peristence.getStatus("wait") == StepStatus.COMPLETED);
    assertTrue(peristence.getStatus("after") == StepStatus.WAITING);

    //  restart

    run = new WorkflowRunner("Test Workflow", factory, HadoopWorkflowOptions.test(), newHashSet(after));
    peristence = run.getPersistence();

    t = run(run, exception);

    t.start();
    semaphore.release();
    t.join();

    assertEquals(null, peristence.getShutdownRequest());
    assertEquals(1, preCounter.get());
    assertEquals(1, postConter.get());
    assertTrue(peristence.getStatus("pre") == StepStatus.SKIPPED);
    assertTrue(peristence.getStatus("wait") == StepStatus.SKIPPED);
    assertTrue(peristence.getStatus("after") == StepStatus.COMPLETED);

  }

  private static class Wrapper<T> {
    private T val;

    public T getVal() {

      if (val == null) {
        org.junit.Assert.fail("Expected value to be set!");
      }

      return val;
    }

    public void setVal(T val) {
      this.val = val;
    }
  }

  @Test
  public void testMultiInMultiEnd1() throws Exception {
    testMultiInMultiEnd(hdfsPersistenceFactory);
  }

  @Test
  public void testMultiInMultiEnd2() throws Exception {
    testMultiInMultiEnd(dbPersistenceFactory);
  }

  public void testMultiInMultiEnd(WorkflowPersistenceFactory factory) throws IOException {
    Step s = new Step(new IncrementAction("first"));
    // please, never do this in real code
    s = new Step(new HadoopMultiStepAction("depth 1", getTestRoot(), Arrays.asList(new Step(new HadoopMultiStepAction(
        "depth 2", getTestRoot(), Arrays.asList(new Step(new IncrementAction("blah"))))))), s);
    s = new Step(new IncrementAction("last"), s);

    buildWfr(factory, s).run();

    assertEquals(3, IncrementAction.counter);
  }

  @Test
  public void testMulitInMultiMiddle1() throws Exception {
    testMultiInMultiMiddle(hdfsPersistenceFactory);
  }

  @Test
  public void testMulitInMultiMiddle2() throws Exception {
    testMultiInMultiMiddle(dbPersistenceFactory);
  }

  public void testMultiInMultiMiddle(WorkflowPersistenceFactory factory) throws IOException {
    Step b = new Step(new IncrementAction("b"));
    Step innermost = new Step(new HadoopMultiStepAction("innermost", getTestRoot(), Arrays.asList(new Step(
        new IncrementAction("c")))), b);
    Step d = new Step(new IncrementAction("d"), b);

    Step a = new Step(new IncrementAction("a"));

    Step outer = new Step(new HadoopMultiStepAction("outer", getTestRoot(), Arrays.asList(b, innermost, d)), a);

    buildWfr(factory, outer).run();

    assertEquals(4, IncrementAction.counter);
  }

  @Test
  public void testDuplicateCheckpoints1() throws Exception {
    testDuplicateCheckpoints(hdfsPersistenceFactory);
  }

  @Test
  public void testDuplicateCheckpoints2() throws Exception {
    testDuplicateCheckpoints(dbPersistenceFactory);
  }

  public void testDuplicateCheckpoints(WorkflowPersistenceFactory factory) throws IOException {
    try {

      HashSet<Step> tails = newHashSet(
          new Step(new IncrementAction("a")),
          new Step(new IncrementAction("a")));

      buildWfr(factory, tails).run();

      fail("should have thrown an exception");
    } catch (IllegalArgumentException e) {
      // expected
    }
  }

  @Test
  public void testSandboxDir1() throws Exception {
    testSandboxDir(hdfsPersistenceFactory);
  }

  @Test
  public void testSandboxDir2() throws Exception {
    testSandboxDir(dbPersistenceFactory);
  }

  private void testSandboxDir(WorkflowPersistenceFactory persistence) throws Exception {

    InitializedWorkflow wf = buildWf(
        persistence,
        HadoopWorkflowOptions.test().setSandboxDir("//fake/path")
    );

    try {

      new WorkflowRunner(
          wf,
          newHashSet(fakeStep("a", "/fake/EVIL/../path"), fakeStep("b", "/path/of/fakeness"))
      ).run();

      fail("There was an invalid path!");
    } catch (Exception e) {
      //  so we can try again in this jvm
      wf.getInitializedPersistence().markWorkflowStopped();
    }

    WorkflowRunner wfr = buildWfr(persistence,
        HadoopWorkflowOptions.test().setSandboxDir("//fake/path"),
        newHashSet(fakeStep("a", "/fake/EVIL/../path"),
            fakeStep("b", "/fake/./path")));
    wfr.run();

  }

  public Step fakeStep(String checkpointToken, final String fakePath) {
    DataStore dataStore = new DataStore() {
      private String path = fakePath;

      @Override
      public String getName() {
        return "fakeDataStore";
      }

      @Override
      public Tap getTap() {
        return null;
      }

      @Override
      public String getPath() {
        return path;
      }

      @Override
      public String getRelPath() {
        return "." + path;
      }
    };
    Action action = new CreateStores(checkpointToken,
        dataStore,
        dataStore
    );
    return new Step(action);
  }

  public static class CreateStores extends Action {

    public CreateStores(String checkpoint, DataStore creates, DataStore createsTemporary) {
      super(checkpoint);
      creates(creates);
      createsTemporary(createsTemporary);
    }

    @Override
    protected void execute() throws Exception {
      // no op
    }
  }

  public static class IncrementAction extends Action {
    public IncrementAction(String checkpointToken) {
      super(checkpointToken);
    }

    public static int counter = 0;

    @Override
    public void execute() {
      counter++;
    }
  }


  @Test
  public void integrationTestCancelComplete() throws Exception {
    IWorkflowDb workflowDb = new DatabasesImpl().getWorkflowDb();

    AtomicInteger step1Count = new AtomicInteger(0);
    AtomicInteger step2Count = new AtomicInteger(0);

    Step step1 = new Step(new IncrementAction2("step1", step1Count));
    Step step2 = new Step(new IncrementAction2("step2", step2Count), step1);

    WorkflowRunner testWorkflow = buildWfr(dbPersistenceFactory, step2);
    testWorkflow.run();

    WorkflowStatePersistence origPersistence = testWorkflow.getPersistence();

    List<Application> applications = workflowDb.applications().query().name("Test Workflow").find();
    assertEquals(1, applications.size());

    assertEquals(WorkflowExecutionStatus.COMPLETE.ordinal(), workflowDb.workflowExecutions().query()
        .applicationId(Accessors.only(applications).getIntId())
        .find().iterator().next().getStatus());

    origPersistence.markStepReverted("step1");

    assertEquals(WorkflowExecutionStatus.INCOMPLETE.ordinal(), workflowDb.workflowExecutions().query()
        .applicationId(Accessors.only(applications).getIntId())
        .find().iterator().next().getStatus());


    step1 = new Step(new IncrementAction2("step1", step1Count));
    step2 = new Step(new IncrementAction2("step2", step2Count), step1);

    testWorkflow = buildWfr(dbPersistenceFactory, step2);
    testWorkflow.run();

    assertEquals(2, step1Count.get());
    assertEquals(1, step2Count.get());
    assertEquals(StepStatus.REVERTED, origPersistence.getStatus("step1"));

  }

  @Test
  public void integrationTestCancelTwoDeep() throws Exception {
    IWorkflowDb workflowDb = new DatabasesImpl().getWorkflowDb();
    workflowDb.disableCaching();

    //  complete, fail, wait
    //  skip, complete, fail
    //  cancel 1
    //  make sure it runs 1 again

    AtomicInteger step1Count = new AtomicInteger(0);
    AtomicInteger step2Count = new AtomicInteger(0);
    AtomicInteger step3Count = new AtomicInteger(0);

    Step step1 = new Step(new IncrementAction2("step1", step1Count));
    Step step2 = new Step(new FailingAction("step2"), step1);
    Step step3 = new Step(new IncrementAction2("step3", step3Count), step2);

    WorkflowRunner testWorkflow = buildWfr(dbPersistenceFactory, step3);

    try {
      testWorkflow.run();
    } catch (Exception e) {
      //  fine
    }

    WorkflowStatePersistence pers1 = testWorkflow.getPersistence();

    step1 = new Step(new IncrementAction2("step1", step1Count));
    step2 = new Step(new IncrementAction2("step2", step2Count), step1);
    step3 = new Step(new IncrementAction2("step3", step3Count), step2);

    testWorkflow = buildWfr(dbPersistenceFactory, step3);
    DbPersistence pers2 = (DbPersistence)testWorkflow.getPersistence();

    try {
      testWorkflow.run();
    } catch (Exception e) {
      //  fine
    }

    Assert.assertEquals(WorkflowExecutionStatus.COMPLETE, getExecutionStatus(workflowDb, pers2));
    assertEquals(WorkflowAttemptStatus.FINISHED, pers2.getStatus());

    pers1.markStepReverted("step1");

    Assert.assertEquals(WorkflowExecutionStatus.INCOMPLETE, getExecutionStatus(workflowDb, pers2));
    assertEquals(WorkflowAttemptStatus.FINISHED, pers2.getStatus());

    step1 = new Step(new IncrementAction2("step1", step1Count));
    step2 = new Step(new IncrementAction2("step2", step2Count), step1);
    step3 = new Step(new IncrementAction2("step3", step3Count), step2);

    testWorkflow = buildWfr(dbPersistenceFactory, step3);
    testWorkflow.run();

    Assert.assertEquals(WorkflowExecutionStatus.COMPLETE, getExecutionStatus(workflowDb, pers2));
    assertEquals(WorkflowAttemptStatus.FINISHED, pers2.getStatus());

    assertEquals(2, step1Count.get());
    assertEquals(1, step2Count.get());
    assertEquals(1, step3Count.get());

  }

  public static WorkflowExecutionStatus getExecutionStatus(IWorkflowDb workflowDb, WorkflowStatePersistence persistence) throws IOException {
    return WorkflowExecutionStatus.findByValue(workflowDb.workflowExecutions().find(persistence.getExecutionId()).getStatus());
  }

  @Test
  public void testCancelWorkflow() throws IOException {

    final IWorkflowDb workflowDb = new DatabasesImpl().getWorkflowDb();
    workflowDb.disableCaching();

    AtomicInteger step1Count = new AtomicInteger(0);
    AtomicInteger step2Count = new AtomicInteger(0);

    Step step1 = new Step(new IncrementAction2("step1", step1Count));
    Step step2 = new Step(new FailingAction("step2"), step1);

    final WorkflowRunner testWorkflow = buildWfr(dbPersistenceFactory, step2);

    getException(new Callable() {
      @Override
      public Void call() throws Exception {
        testWorkflow.run();
        return null;
      }
    });

    WorkflowStatePersistence persistence = testWorkflow.getPersistence();

    ApplicationController.cancelLatestExecution(workflowDb, "Test Workflow", null);
    WorkflowExecution ex = Accessors.first(workflowDb.workflowExecutions().findAll());

    assertEquals(WorkflowExecutionStatus.CANCELLED.ordinal(), ex.getStatus());
    assertEquals(StepStatus.COMPLETED.ordinal(), persistence.getStatus("step1").ordinal());
    assertEquals(StepStatus.FAILED.ordinal(), persistence.getStatus("step2").ordinal());

    //  restart workflow
    step1 = new Step(new IncrementAction2("step1", step1Count));
    step2 = new Step(new IncrementAction2("step2", step2Count), step1);

    WorkflowRunner restartedWorkflow = buildWfr(dbPersistenceFactory, step2);
    restartedWorkflow.run();
    WorkflowStatePersistence newPeristence = restartedWorkflow.getPersistence();

    //  nothing changed for first execution
    List<WorkflowExecution> executions = workflowDb.workflowExecutions().query().order(QueryOrder.ASC).find();
    ex = Accessors.first(executions);
    assertEquals(WorkflowExecutionStatus.CANCELLED.ordinal(), ex.getStatus());
    assertEquals(StepStatus.COMPLETED.ordinal(), persistence.getStatus("step1").ordinal());
    assertEquals(StepStatus.FAILED.ordinal(), persistence.getStatus("step2").ordinal());

    //  second one is complete
    final WorkflowExecution ex2 = Accessors.second(executions);
    assertEquals(WorkflowExecutionStatus.COMPLETE.ordinal(), ex2.getStatus());
    assertEquals(StepStatus.COMPLETED.ordinal(), newPeristence.getStatus("step1").ordinal());
    assertEquals(StepStatus.COMPLETED.ordinal(), newPeristence.getStatus("step2").ordinal());

    assertEquals(2, step1Count.get());
    assertEquals(1, step2Count.get());

    //  run a third time
    step1 = new Step(new IncrementAction2("step1", step1Count));
    step2 = new Step(new IncrementAction2("step2", step2Count), step1);

    restartedWorkflow = buildWfr(dbPersistenceFactory, step2);
    restartedWorkflow.run();

    Exception cancelNonLatest = getException(new Callable() {
      @Override
      public Void call() throws Exception {
        ExecutionController.cancelExecution(workflowDb, ex2);
        return null;
      }
    });

    assertTrue(cancelNonLatest.getMessage().startsWith("Cannot manually modify execution"));

  }

  @Test
  public void testCancelApplication() throws Exception {

    IWorkflowDb workflowDb = new DatabasesImpl().getWorkflowDb();

    Step step1 = new Step(new NoOpAction("step1"));

    WorkflowRunner restartedWorkflow = new WorkflowRunner("test",
        dbPersistenceFactory,
        HadoopWorkflowOptions.test().setAppName("test"),
        newHashSet(step1)
    );

    restartedWorkflow.run();

    ApplicationController.cancelLatestExecution(workflowDb, "test", null);
  }

  @Test
  public void integrationTestCancelIncomplete() throws Exception {

    AtomicInteger step1Count = new AtomicInteger(0);
    AtomicInteger step2Count = new AtomicInteger(0);

    Step step1 = new Step(new IncrementAction2("step1", step1Count));
    Step step2 = new Step(new FailingAction("step2"), step1);

    WorkflowRunner testWorkflow = buildWfr(dbPersistenceFactory, step2);

    try {
      testWorkflow.run();
    } catch (Exception e) {
      //  fine
    }

    testWorkflow.getPersistence().markStepReverted("step1");


    step1 = new Step(new IncrementAction2("step1", step1Count));
    step2 = new Step(new IncrementAction2("step2", step2Count), step1);

    testWorkflow = buildWfr(dbPersistenceFactory, step2);
    testWorkflow.run();

    assertEquals(2, step1Count.get());
    assertEquals(1, step2Count.get());

  }


  @Test
  public void integrationTestCancelFailure() throws Exception {

    Step step1 = new Step(new NoOpAction("step1"));
    Step step2 = new Step(new NoOpAction("step2"), step1);

    WorkflowRunner firstRun = buildWfr(dbPersistenceFactory, step2);
    firstRun.run();

    step1 = new Step(new NoOpAction("step1"));
    step2 = new Step(new NoOpAction("step2"), step1);

    WorkflowRunner secondRun = buildWfr(dbPersistenceFactory, step2);
    secondRun.run();

    assertEquals(StepStatus.COMPLETED, secondRun.getPersistence().getStatus("step1"));

    try {
      firstRun.getPersistence().markStepReverted("step1");
      fail();
    } catch (Exception e) {
      System.out.println(e.getMessage());
      assertTrue(e.getMessage().startsWith("Cannot manually modify execution"));
    }

  }

  @Test
  public void testCounters() throws IOException {

    TupleDataStore input = tupleStore("/input",
        new Fields("string")
    );

    DataStoreUtils.writeToStore(
        CascadingUtil.get(),
        input,
        new Tuple("1")
    );

    TupleDataStore output = tupleStore("/output",
        new Fields("string")
    );

    Step step = new Step(new IncrementCounter("step1", getTestRoot(), input, output));
    Step step2 = new Step(new IncrementCounter2("step2", getTestRoot(), input, output), step);

    WorkflowRunner runner = new WorkflowRunner("Test Workflow",
        new WorkflowDbPersistenceFactory(),
        HadoopWorkflowOptions.test(),
        newHashSet(step, step2)
    );

    runner.run();

    TwoNestedMap<String, String, Long> counters = runner.getPersistence().getFlatCounters();

    assertEquals(1L, counters.get("CUSTOM_COUNTER", "NAME").longValue());

    assertEquals(1L, counters.get("CUSTOM_COUNTER2", "NAME").longValue());

    TaskCounter mapIn = TaskCounter.MAP_INPUT_RECORDS;
    assertEquals(2L, counters.get(mapIn.getClass().getName(), mapIn.name()).longValue());

  }

  @Test
  public void testCountersOnRestart() throws IOException {


    TupleDataStore input = tupleStore("/input",
        new Fields("string")
    );

    DataStoreUtils.writeToStore(
        CascadingUtil.get(),
        input,
        new Tuple("1")
    );

    TupleDataStore output = tupleStore("/output",
        new Fields("string")
    );

    Step step = new Step(new IncrementCounter("step1", getTestRoot(), input, output));
    Step step2 = new Step(new FailingAction("step2"), step);

    WorkflowRunner runner = new WorkflowRunner("Test Workflow",
        new WorkflowDbPersistenceFactory(),
        HadoopWorkflowOptions.test(),
        newHashSet(step2)
    );

    try {
      runner.run();
    } catch (Exception e) {
      // expected
    }

    step = new Step(new IncrementCounter("step1", getTestRoot(), input, output));
    step2 = new Step(new IncrementCounter2("step2", getTestRoot(), input, output), step);

    runner = new WorkflowRunner("Test Workflow",
        new WorkflowDbPersistenceFactory(),
        HadoopWorkflowOptions.test(),
        newHashSet(step2)
    );

    runner.run();

    //  test flat

    TwoNestedMap<String, String, Long> counters = runner.getPersistence().getFlatCounters();

    assertEquals(1L, counters.get("CUSTOM_COUNTER", "NAME").longValue());

    assertEquals(1L, counters.get("CUSTOM_COUNTER2", "NAME").longValue());

    TaskCounter mapIn = TaskCounter.MAP_INPUT_RECORDS;
    assertEquals(2L, counters.get(mapIn.getClass().getName(), mapIn.name()).longValue());

    //  test by step

    ThreeNestedMap<String, String, String, Long> countersByStep = runner.getPersistence().getCountersByStep();
    assertEquals(1L, countersByStep.get("step1", mapIn.getClass().getName(), mapIn.name()).longValue());

  }

  @Test
  public void testCurrentStepCounters() throws IOException {

    TupleDataStore input = tupleStore("input",
        new Fields("string")
    );

    DataStoreUtils.writeToStore(
        CascadingUtil.get(),
        input,
        new Tuple("1")
    );

    Step step1 = new Step(new JobAndCheckCounter("step1", input));
    Step step2 = new Step(new JobAndCheckCounter("step2", input), step1);

    execute(step2);

  }

  private static class JobAndCheckCounter extends Action {

    private final DataStore store;

    public JobAndCheckCounter(String checkpointToken,
                              DataStore input) {
      super(checkpointToken);
      this.store = input;
      readsFrom(store);
    }

    @Override
    protected void execute() throws Exception {
      Pipe pipe = new Pipe("pipe");
      completeWithProgress(buildFlow().connect(store.getTap(), new NullTap(), pipe));
      assertEquals(Long.valueOf(1), getCurrentStepCounters().get("org.apache.hadoop.mapreduce.TaskCounter", "MAP_INPUT_RECORDS"));
    }
  }

  @Test
  public void testDatastoreDeps() throws Exception {
    CascadingUtil util = CascadingUtil.get();

    TupleDataStore store1 = tupleStore("store1", new Fields("test_type"));
    DataStoreUtils.writeToStore(util, store1, new Tuple("test"));

    TupleDataStore store2 = tupleStore("store2", new Fields("test_type"));
    TupleDataStore store3 = tupleStore("store3", new Fields("test_type"));

    Step step1 = new Step(new CopyStore("step1", store1, store2));
    new WorkflowRunner("workflow1", new WorkflowDbPersistenceFactory(), HadoopWorkflowOptions.test(), step1).run();

    Step step3 = new Step(new CopyStore("step2", store1, store3));
    WorkflowRunner runner = new WorkflowRunner("workflow2", new WorkflowDbPersistenceFactory(), HadoopWorkflowOptions.test(), step3);
    runner.run();

    WorkflowStatePersistence persistence = runner.getPersistence();

    StepState step2 = persistence.getStepStates().get("step2");
    Multimap<DSAction, DataStoreInfo> states = step2.getDatastores();

    assertEquals(1, states.get(DSAction.CREATES).size());
    assertEquals(1, states.get(DSAction.READS_FROM).size());

  }

  @Test
  public void testTaskStatistics() throws Exception {

    TupleDataStore input = tupleStore("input",
        new Fields("string")
    );

    DataStoreUtils.writeToStore(
        CascadingUtil.get(),
        input,
        new Tuple("1")
    );

    WorkflowRunner stat = execute(new Step(new StatAction("stat", input)));

    StepState state = stat.getPersistence().getStepStates().get("stat");
    MapReduceJob job = Accessors.only(state.getMrJobsByID().values());

    //  unfortunately we can't get real values locally, but prove not fail and not null
    assertEquals(0L, job.getTaskSummary().getAvgMapDuration().longValue());
    assertNotNull(job.getTaskSummary().getTaskFailures());

  }

  @Test
  public void testSimultaneousConcurrent() throws IOException {

    //  simple -- confirm we are not calling getStepStatuses for each step

    Set<Step> heads = newHashSet();
    for (int i = 0; i < 50; i++) {
      heads.add(new Step(new DelayAction("step-" + i)));
    }

    Step tail = new Step(new NoOpAction("tail"), heads);

    WorkflowRunner runner = new WorkflowRunner(
        getClass().getName(),
        new MonitoredPersistenceFactory(new WorkflowDbPersistenceFactory()),
        HadoopWorkflowOptions.test()
            .setStepPollInterval(2000)
            //  concurrency 200
            .setMaxConcurrentSteps(200),
        tail
    );
    runner.run();

    MonitoredPersistenceFactory.MonitoredPersistence monitoredPersistence =
        (MonitoredPersistenceFactory.MonitoredPersistence)runner.getPersistence();

    //  three times through the loop, once checking if we had step failures
    assertEquals(7, monitoredPersistence.getGetStepStatusCalls());

  }


  @Test
  public void testInitializeBeforePrepare() throws IOException {

    AtomicLong workflowID = new AtomicLong();

    //  initialize the workflow with a name and scope to get an execution ID
    InitializedWorkflow workflow = new WorkflowDbPersistenceFactory().initialize(
        HadoopWorkflowOptions.test()
            .setUniqueIdentifier("1")
            .setAppName("name")
    );

    //  verify that it shows up as running after initialization
    assertTrue(ApplicationController.isRunning(new DatabasesImpl().getWorkflowDb(), "name", "1"));

    //  prove we can use it to do stuff when creating steps
    Step step = new Step(new SetStep(
        "step",
        workflowID,
        workflow.getInitializedPersistence().getExecutionId())
    );

    //  then run the workflow
    WorkflowRunner runner = new WorkflowRunner(workflow, Sets.<Step>newHashSet(step));
    runner.run();

    assertEquals(runner.getPersistence().getExecutionId(), workflowID.get());

  }

  @Test
  public void testStepCreationFailure() throws IOException, InterruptedException {

    //  initialize the workflow with a name and scope to get an execution ID
    DbHadoopWorkflow workflow = new WorkflowDbPersistenceFactory().initialize(
        "Test Workflow",
        HadoopWorkflowOptions.test()
            .setUniqueIdentifier("1")
    );

    //  pretend we failed before getting to new WorkflowRunner().run()
    //  we can't really test jvm shutdown behavior, so run the shutdown hook manually
    MultiShutdownHook hook = workflow.getShutdownHook();
    hook.start();
    hook.join();

    InitializedDbPersistence dbInitialized = workflow.getInitializedPersistence();

    //  verify that the shutdown hook failed the attempt
    assertEquals(WorkflowExecutionStatus.INCOMPLETE.ordinal(), dbInitialized.getExecution().getStatus());
    assertEquals(WorkflowAttemptStatus.FAILED.ordinal(), dbInitialized.getAttempt().getStatus().intValue());

    //  try again
    workflow = new WorkflowDbPersistenceFactory().initialize(
        "Test Workflow",
        HadoopWorkflowOptions.test()
            .setUniqueIdentifier("1")
    );

    Step step = new Step(new NoOpAction("no-op"));

    WorkflowRunner runner = new WorkflowRunner(workflow, Sets.newHashSet(step));
    runner.run();

    dbInitialized = workflow.getInitializedPersistence();

    //  verify that the shutdown hook failed the attempt
    assertEquals(WorkflowExecutionStatus.COMPLETE.ordinal(), dbInitialized.getExecution().getStatus());
    assertEquals(WorkflowAttemptStatus.FINISHED.ordinal(), dbInitialized.getAttempt().getStatus().intValue());
    assertEquals(StepStatus.COMPLETED, runner.getPersistence().getStepStatuses().get("no-op"));

  }

  @Test
  public void testMarkStepComplete() throws IOException, InterruptedException {

    //  1) standard -- step fails, mark it as complete, resume

    Semaphore failLock = new Semaphore(0);

    Step step = new Step(new DelayedFailingAction("fail", failLock));

    WorkflowRunner execute = new WorkflowRunner(WorkflowRunnerIT.class, HadoopWorkflowOptions.test(), step);

    Wrapper<Exception> exception = new Wrapper<Exception>();
    Thread t = run(execute, exception);
    t.start();

    IWorkflowDb db = new DatabasesImpl().getWorkflowDb();

    try {

      ApplicationController.manuallyCompleteStep(db,
          WorkflowRunnerIT.class.getName(),
          null, "fail"
      );

      fail();

    } catch (Exception e) {
      failLock.release();
    }

    t.join();

    WorkflowStatePersistence persistence = execute.getPersistence();
    persistence.markStepManuallyCompleted("fail");

    assertEquals(StepStatus.MANUALLY_COMPLETED, persistence.getStatus("fail"));

    step = new Step(new NoOp<>("fail"));

    BufferingAlertsHandler.MessageBuffer buffer = new BufferingAlertsHandler.MessageBuffer();
    BufferingAlertsHandler handler = new BufferingAlertsHandler(buffer, RecipientUtils.of("test@test.com"));

    WorkflowRunner newRunner = new WorkflowRunner(WorkflowRunnerIT.class,
        HadoopWorkflowOptions.test()
            .setAlertsHandler(handler),
        step
    );

    newRunner.run();

    WorkflowStatePersistence newPersistence = newRunner.getPersistence();

    assertEquals(WorkflowExecutionStatus.COMPLETE.ordinal(),
        db.workflowExecutions().find(newPersistence.getExecutionId()).getStatus());

    assertEquals(WorkflowAttemptStatus.FINISHED,
        newPersistence.getStatus());

    assertEquals(StepStatus.SKIPPED, newPersistence.getStatus("fail"));

    DefaultAlertMessageConfig config = new DefaultAlertMessageConfig(true, Lists.newArrayList());

    assertTrue(buffer.getSentAlerts().stream().map(alertMessage -> alertMessage.getSubject(config)).collect(Collectors.toSet()).contains(
        "[WORKFLOW] Succeeded: com.liveramp.workflow.WorkflowRunnerIT"
    ));

  }


  @Test
  public void testSimpleRollback() throws IOException {

    List<String> revertList = Lists.newArrayList();

    Step step1 = new Step(new ActionWithRollback(
        "step1",
        revertList)
    );

    Step step2 = new Step(new ActionWithRollback(
        "step2",
        revertList),
        step1
    );

    Step step3 = new Step(new FailingAction("step3"),
        step2
    );

    BufferingAlertsHandler.MessageBuffer buffer = new BufferingAlertsHandler.MessageBuffer();
    BufferingAlertsHandler handler = new BufferingAlertsHandler(buffer, RecipientUtils.of("test@test.com"));

    WorkflowRunner runner = new WorkflowRunner(WorkflowRunnerIT.class,
        HadoopWorkflowOptions.test()
            .setAlertsHandler(handler)
            .setRollBackOnFailure(true),
        step3
    );

    try {
      runner.run();
      fail();
    } catch (Exception e) {
      //  expected
      assertTrue(e.getMessage().startsWith("Failed: com.liveramp.workflow.WorkflowRunnerIT"));
    }

    //  both step rollbacks happened, and in reverse order
    assertEquals(Lists.newArrayList("step2", "step1"), revertList);

    WorkflowStatePersistence persistence = runner.getPersistence();

    //  all steps are rolled back
    Map<String, StepStatus> statuses = persistence.getStepStatuses();
    assertEquals(StepStatus.ROLLED_BACK, statuses.get("step1"));
    assertEquals(StepStatus.ROLLED_BACK, statuses.get("step2"));
    assertEquals(StepStatus.ROLLED_BACK, statuses.get("step3"));

    //  execution itself is cancelled
    IWorkflowDb workflowDB = new DatabasesImpl().getWorkflowDb();
    WorkflowExecution execution = workflowDB.workflowExecutions().find(persistence.getExecutionId());
    assertEquals(WorkflowExecutionStatus.CANCELLED.getValue(), execution.getStatus());

    DefaultAlertMessageConfig config = new DefaultAlertMessageConfig(true, Lists.newArrayList());

    Set<String> messages = buffer.getSentAlerts().stream().map(alertMessage -> alertMessage.getSubject(config)).collect(Collectors.toSet());

    System.out.println(messages);

    assertCollectionContains(messages, "[WORKFLOW] Started: com.liveramp.workflow.WorkflowRunnerIT (ROLLBACK)");
    assertCollectionContains(messages, "[WORKFLOW] Succeeded: com.liveramp.workflow.WorkflowRunnerIT (ROLLBACK)");

  }

  @Test
  public void testSimpleHDFSRollback() throws IOException {

    List<String> revertList = Lists.newArrayList();

    Step step1 = new Step(new ActionWithRollback(
        "step1",
        revertList)
    );

    Step step2 = new Step(new ActionWithRollback(
        "step2",
        revertList),
        step1
    );

    Step step3 = new Step(new FailingAction("step3"),
        step2
    );

    WorkflowRunner runner = new WorkflowRunner(WorkflowRunnerIT.class,
        new HdfsCheckpointPersistence(getTestRoot() + "/checkpoints"),
        HadoopWorkflowOptions.test()
            .setRollBackOnFailure(true),
        step3
    );

    try {
      runner.run();
      fail();
    } catch (Exception e) {
      //  expected
      assertTrue(e.getMessage().startsWith("Failed: com.liveramp.workflow.WorkflowRunnerIT"));
    }

    //  both step rollbacks happened, and in reverse order
    assertEquals(Lists.newArrayList("step2", "step1"), revertList);

    step1 = new Step(new NoOp("step1"));

    step2 = new Step(new NoOp("step2"), step1);

    runner = new WorkflowRunner(WorkflowRunnerIT.class,
        new HdfsCheckpointPersistence(getTestRoot() + "/checkpoints"),
        HadoopWorkflowOptions.test()
            .setRollBackOnFailure(true),
        step2
    );

    runner.run();

    WorkflowStatePersistence persistence = runner.getPersistence();
    Map<String, StepStatus> statuses = persistence.getStepStatuses();
    assertEquals(StepStatus.COMPLETED, statuses.get("step1"));
    assertEquals(StepStatus.COMPLETED, statuses.get("step2"));

  }

  @Test
  public void testFailedRollback() throws IOException {

    Step step1 = new Step(new FailOnRollback(
        "step1")
    );

    Step step2 = new Step(new FailingAction("step2"),
        step1
    );

    BufferingAlertsHandler.MessageBuffer buffer = new BufferingAlertsHandler.MessageBuffer();
    BufferingAlertsHandler handler = new BufferingAlertsHandler(buffer, RecipientUtils.of("test@test.com"));

    WorkflowRunner runner = new WorkflowRunner(WorkflowRunnerIT.class,
        HadoopWorkflowOptions.test()
            .setAlertsHandler(handler)
            .setRollBackOnFailure(true),
        step2
    );

    try {
      runner.run();
      fail();
    } catch (Exception e) {
      //  expected
      assertTrue(e.getMessage().startsWith("Failed: com.liveramp.workflow.WorkflowRunnerIT"));
    }

    WorkflowStatePersistence persistence = runner.getPersistence();

    //  all steps are rolled back
    Map<String, StepStatus> statuses = persistence.getStepStatuses();
    assertEquals(StepStatus.ROLLBACK_FAILED, statuses.get("step1"));
    assertEquals(StepStatus.ROLLED_BACK, statuses.get("step2"));

    //  execution itself is cancelled
    IWorkflowDb workflowDB = new DatabasesImpl().getWorkflowDb();
    WorkflowExecution execution = workflowDB.workflowExecutions().find(persistence.getExecutionId());
    assertEquals(WorkflowExecutionStatus.CANCELLED.getValue(), execution.getStatus());

    DefaultAlertMessageConfig config = new DefaultAlertMessageConfig(true, Lists.newArrayList());
    Set<String> subjects = buffer.getSentAlerts().stream().map(alertMessage -> alertMessage.getSubject(config)).collect(Collectors.toSet());

    //  make sure we get notified about rollbacks
    assertCollectionContains(subjects, "[WORKFLOW] Started: com.liveramp.workflow.WorkflowRunnerIT (ROLLBACK)");
    assertCollectionContains(subjects, "[ERROR] [WORKFLOW] Failed: com.liveramp.workflow.WorkflowRunnerIT (ROLLBACK)");


    //  even though the rollback failed, the next attempt will start from scratch and work

    step1 = new Step(new NoOp("step1"));

    step2 = new Step(new NoOp("step2"), step1);


    runner = new WorkflowRunner(WorkflowRunnerIT.class,
        HadoopWorkflowOptions.test()
            .setRollBackOnFailure(true),
        step2
    );

    runner.run();

    persistence = runner.getPersistence();
    statuses = persistence.getStepStatuses();
    assertEquals(StepStatus.COMPLETED, statuses.get("step1"));
    assertEquals(StepStatus.COMPLETED, statuses.get("step2"));

  }

  //  expect rollback to be blocked
  @Test
  public void testConditionalRollback1() throws IOException {

    Step step1 = new Step(new NoOp("step1"));
    Step step2 = new Step(new FailingAction("step2"), step1);
    Step step3 = new Step(new NoOp("step3"), step2);

    WorkflowRunner runner = new WorkflowRunner(WorkflowRunnerIT.class,
        HadoopWorkflowOptions.test()
            .setRollBackBehavior(new UnlessStepsRun("step2")),
        step3
    );

    try {
      runner.run();
      fail();
    } catch (Exception e) {
      //  expected
    }

    assertEquals(StepStatus.COMPLETED, runner.getPersistence().getStatus("step1"));
    assertEquals(StepStatus.FAILED, runner.getPersistence().getStatus("step2"));

  }

  //  expect rollback to be happen
  @Test
  public void testConditionalRollback2() throws IOException {

    Step step1 = new Step(new NoOp("step1"));
    Step step2 = new Step(new FailingAction("step2"), step1);
    Step step3 = new Step(new NoOp("step3"), step2);

    WorkflowRunner runner = new WorkflowRunner(WorkflowRunnerIT.class,
        HadoopWorkflowOptions.test()
            .setRollBackBehavior(new UnlessStepsRun("step3")),
        step3
    );

    try {
      runner.run();
      fail();
    } catch (Exception e) {
      //  expected
    }

    assertEquals(StepStatus.ROLLED_BACK, runner.getPersistence().getStatus("step1"));
    assertEquals(StepStatus.ROLLED_BACK, runner.getPersistence().getStatus("step2"));

  }


  @Test
  public void testTestRollback() throws IOException {

    List<String> rollbackList = Lists.newArrayList();

    Step step1 = new Step(new ActionWithRollback("step1", rollbackList));
    Step step2 = new Step(new ActionWithRollback("step2", rollbackList), step1);

    WorkflowTestUtils.executeAndRollback(Sets.newHashSet(step2), ResourceManagers.dbResourceManager());

    assertCollectionEquivalent(Lists.newArrayList("step1", "step2"), rollbackList);

  }

  @Test
  public void testStatusCallback() throws IOException {

    WorkflowRunner step = execute(new ActionWithCallback("step"));
    assertEquals("Status0", step.getPersistence().getStepStates().get("step").getStatusMessage());

  }

  private static class ActionWithCallback extends Action {

    public ActionWithCallback(String checkpointToken) {
      super(checkpointToken);
    }

    @Override
    protected void execute() throws Exception {
      setStatusCallback(new StatusCallback() {
        @Override
        public String updateStatus() {
          return "Status0";
        }
      });
    }
  }

  private static class ActionWithRollback extends Action {

    private final List<String> revertList;

    public ActionWithRollback(String checkpointToken,
                              List<String> revertList) {
      super(checkpointToken);
      this.revertList = revertList;
    }

    @Override
    protected void execute() throws Exception {
      //  no op
    }

    @Override
    protected void rollback() throws Exception {
      revertList.add(getActionId().resolve());
    }

  }

  private static class FailOnRollback extends Action {

    public FailOnRollback(String checkpointToken) {
      super(checkpointToken);
    }

    @Override
    protected void execute() throws Exception {
      //  no op
    }

    @Override
    protected void rollback() throws Exception {
      throw new RuntimeException("Failing on purpose!");
    }
  }


  //  3) try complete while execution is running.  fail.

  private static class SetStep extends Action {

    private final AtomicLong value;
    private final long executionID;

    public SetStep(String checkpointToken,
                   AtomicLong value,
                   long executionID) {
      super(checkpointToken);
      this.value = value;
      this.executionID = executionID;
    }

    @Override
    protected void execute() throws Exception {
      this.value.set(executionID);
    }
  }

  public static class DelayAction extends Action {

    public DelayAction(String checkpointToken) {
      super(checkpointToken);
    }

    @Override
    protected void execute() throws Exception {
      Thread.sleep(1000);
    }
  }

  public static class StatAction extends Action {

    private final TupleDataStore input;

    public StatAction(String checkpointToken, TupleDataStore input) {
      super(checkpointToken);
      this.input = input;
    }

    @Override
    protected void execute() throws Exception {
      completeWithProgress(buildFlow().connect(
          input.getTap(),
          new NullTap(),
          new Each(new Pipe("pipe"), new TempDelay())
      ));
    }

    private static class TempDelay extends BaseOperation implements Filter {

      @Override
      public boolean isRemove(FlowProcess flowProcess, FilterCall filterCall) {
        try {
          Thread.sleep(10);
        } catch (InterruptedException e) {
          //  wait
        }
        return false;
      }
    }
  }

  public static class CopyStore extends Action {

    private final DataStore input;
    private final DataStore output;

    public CopyStore(String checkpointToken,
                     DataStore input,
                     DataStore output) {
      super(checkpointToken);
      this.input = input;
      this.output = output;

      readsFrom(input);
      creates(output);
    }

    @Override
    protected void execute() throws Exception {
      completeWithProgress(buildFlow().connect(input.getTap(), output.getTap(), new Pipe("flow")));
    }
  }


  private static void hideComplete(Flow f) {
    f.complete();
  }

  public static class IncrementCounter2 extends Action {

    private final TupleDataStore in;
    private final TupleDataStore out;

    public IncrementCounter2(String checkpointToken, String tmpRoot,
                             TupleDataStore in,
                             TupleDataStore out) {
      super(checkpointToken, tmpRoot);

      this.in = in;
      this.out = out;

      readsFrom(in);
      creates(out);

    }

    @Override
    protected void execute() throws Exception {

      Pipe pipe = new Pipe("input");
      pipe = new Each(pipe, new Count());

      Flow flow = CascadingUtil.get().getFlowConnector().connect(in.getTap(), out.getTap(), pipe);
      flow.addStepListener(new JobRecordListener(getPersister(), true));
      hideComplete(flow);

      //  make sure counter from previous step is accessible
      assertEquals(1L, (long)getFlatCounters().get("CUSTOM_COUNTER", "NAME"));

    }

    private static class Count extends BaseOperation implements Filter {
      @Override
      public boolean isRemove(FlowProcess flowProcess, FilterCall filterCall) {
        flowProcess.increment("CUSTOM_COUNTER2", "NAME", 1);
        flowProcess.increment("OTHER_COUNTER2", "NAME", 1);
        return false;
      }
    }
  }


  public static class IncrementCounter extends Action {

    private final TupleDataStore in;
    private final TupleDataStore out;

    public IncrementCounter(String checkpointToken, String tmpRoot,
                            TupleDataStore in,
                            TupleDataStore out) {
      super(checkpointToken, tmpRoot);

      this.in = in;
      this.out = out;

      readsFrom(in);
      creates(out);
    }

    @Override
    protected void execute() throws Exception {

      Pipe pipe = new Pipe("input");
      pipe = new Each(pipe, new Count());
      completeWithProgress(buildFlow().connect(in.getTap(), out.getTap(), pipe));

    }

    private static class Count extends BaseOperation implements Filter {
      @Override
      public boolean isRemove(FlowProcess flowProcess, FilterCall filterCall) {
        flowProcess.increment("CUSTOM_COUNTER", "NAME", 1);
        flowProcess.increment("OTHER_COUNTER", "NAME", 1);
        return false;
      }
    }
  }




}