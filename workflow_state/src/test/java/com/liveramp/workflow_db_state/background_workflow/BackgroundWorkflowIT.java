package com.liveramp.workflow_db_state.background_workflow;

import java.io.IOException;
import java.io.Serializable;
import java.time.Duration;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liveramp.cascading_ext.resource.ReadResource;
import com.liveramp.cascading_ext.resource.Resource;
import com.liveramp.cascading_ext.resource.ResourceManager;
import com.liveramp.cascading_ext.resource.WriteResource;
import com.liveramp.commons.Accessors;
import com.liveramp.commons.test.WaitUntil;
import com.liveramp.databases.workflow_db.DatabasesImpl;
import com.liveramp.databases.workflow_db.IWorkflowDb;
import com.liveramp.workflow2.workflow_state.resources.DbResourceManager;
import com.liveramp.workflow.types.StepStatus;
import com.liveramp.workflow.types.WorkflowAttemptStatus;
import com.liveramp.workflow_core.CoreOptions;
import com.liveramp.workflow_core.background_workflow.AlwaysStart;
import com.liveramp.workflow_core.background_workflow.BackgroundAction;
import com.liveramp.workflow_core.background_workflow.BackgroundStep;
import com.liveramp.workflow_core.background_workflow.BackgroundWorkflowPreparer;
import com.liveramp.workflow_core.background_workflow.BackgroundWorkflowSubmitter;
import com.liveramp.workflow_core.background_workflow.MultiStep;
import com.liveramp.workflow_core.background_workflow.PreconditionFunction;
import com.liveramp.workflow_db_state.WorkflowDbStateTestCase;
import com.liveramp.workflow_state.WorkflowStatePersistence;
import com.liveramp.workflow_state.background_workflow.BackgroundWorkflowExecutor;
import com.liveramp.workflow_state.background_workflow.ErrorReporter;
import com.rapleaf.cascading_ext.workflow2.state.InitializedWorkflow;

import static org.junit.Assert.assertEquals;

public class BackgroundWorkflowIT extends WorkflowDbStateTestCase {
  private static final Logger LOG = LoggerFactory.getLogger(BackgroundWorkflowIT.class);

  @Test
  public void testSimple() throws IOException, InterruptedException {

    //  the context is the serialized "input" to the workflow.  BackgroundActions have to deserialize this input
    //  on the executors to know what to do
    TestWorkflowContext context = new TestWorkflowContext("1");

    //  build and submit the workflow we care about.  the client process can exit after this point
    WorkflowStatePersistence persistence = submitTest(context);

    //  just assert that the persistence has the information we expect it to have
    assertEquals(context, persistence.getContext("action1"));

    //  this would normally run as some standalone monitored worker.  this is a process which looks for runnable
    //  steps and runs them as they become startable.
    BackgroundWorkflowExecutor executor = startExecutor(persistence.getName(), "worker1");

    //  verify that the workflow succeeded
    waitUntilStatus(persistence, WorkflowAttemptStatus.FINISHED);

    //  check that fancy sub steps work
    assertEquals(StepStatus.COMPLETED, persistence.getStatus("action1"));
    assertEquals(StepStatus.COMPLETED, persistence.getStatus("action2__sub1"));
    assertEquals(StepStatus.COMPLETED, persistence.getStatus("action2__sub2"));
    assertEquals(StepStatus.COMPLETED, persistence.getStatus("action3"));

    //  cleanup
    executor.shutdown();
  }

  public WorkflowStatePersistence submitTest(TestWorkflowContext context) throws IOException {

    BackgroundWorkflow initialized = BackgroundWorkflowPreparer.initialize(
        "test",
        new BackgroundPersistenceFactory(),
        CoreOptions.test()
    );

    InitializedWorkflow.ScopedContext rootScope = initialized.getRootScope();

    BackgroundStep action1 = new BackgroundStep(
        rootScope,
        "action1",
        TestAction.class,
        context
    );

    BackgroundStep action2 = new MultiStep<>(
        rootScope,
        "action2",
        context,
        new SimpleAssembly(context),
        Sets.newHashSet(action1)
    );

    BackgroundStep action3 = new BackgroundStep(
        rootScope,
        "action3",
        TestAction.class,
        context,
        Duration.ZERO,
        Sets.newHashSet(action2)
    );

    //  this is executed by the launcher process.  it can exit afterwards.  or hang around.  doesn't really matter.
    WorkflowStatePersistence persistence = BackgroundWorkflowSubmitter.submit(
        initialized,
        Lists.newArrayList(action3)
    );

    return persistence;
  }

  private void waitUntilStatus(WorkflowStatePersistence persistence, WorkflowAttemptStatus status) throws InterruptedException {
    WaitUntil.orDie(() -> {
      try {
        return persistence.getStatus() == status;
      } catch (IOException e) {
        LOG.error("Berk", e);
        return false;
      }
    });
  }



  public WorkflowStatePersistence submitWorkflowClaiming2() throws IOException {

    BackgroundWorkflow initialized = BackgroundWorkflowPreparer.initialize(
        "test",
        new BackgroundPersistenceFactory(),
        CoreOptions.test()
    );

    TestWorkflowContext context = new TestWorkflowContext("1");
    InitializedWorkflow.ScopedContext rootScope = initialized.getRootScope();

    SIMPLE_PRECONDITION_ALLOW = false;

    BackgroundStep action1 = new BackgroundStep(
        rootScope,
        "action1",
        NoOp.class,
        context
    );

    BackgroundStep action2 = new BackgroundStep(
        rootScope,
        "action2",
        BlockingClass.class,
        context,
        Duration.ZERO,
        Sets.newHashSet(action1)
    );


    //  this is executed by the launcher process.  it can exit afterwards.  or hang around.  doesn't really matter.
    WorkflowStatePersistence persistence = BackgroundWorkflowSubmitter.submit(
        initialized,
        Lists.newArrayList(action2)
    );

    return persistence;
  }


  //run an executor process which runs workflows with a specific name
  public BackgroundWorkflowExecutor startExecutor(String workflowName, String executorName){

    BackgroundWorkflowExecutor executor = new BackgroundWorkflowExecutor(
        Lists.newArrayList(workflowName),
        1000,
        10,
        new ErrorReporter.InMemoryReporter(),
        executorName
    );

    executor.start();

    return executor;
  }

  @Test
  public void testClaiming2() throws IOException, InterruptedException {

    WorkflowStatePersistence persistence = submitWorkflowClaiming2();
    BackgroundWorkflowExecutor executor = startExecutor(persistence.getName(), "test-worker1");

    //  verify that step 1 succeeds
    WaitUntil.orDie(() -> {
      try {
        return persistence.getStatus("action1") == StepStatus.COMPLETED;
      } catch (IOException e) {
        return false;
      }
    });

    //  kill the first executor
    executor.shutdown();

    //  the second action is still waiting, and the workflow is still 'running'
    assertEquals(StepStatus.WAITING, persistence.getStatus("action2"));
    assertEquals(WorkflowAttemptStatus.RUNNING, persistence.getStatus());

    //  start up a new executor to pick up the work
    BackgroundWorkflowExecutor executor2 = startExecutor(persistence.getName(), "test-worker2");

    //  unblock the second action and wait for it to finish
    SIMPLE_PRECONDITION_ALLOW = true;
    WaitUntil.orDie(() -> {
      try {
        return persistence.getStatus("action2") == StepStatus.COMPLETED;
      } catch (IOException e) {
        return false;
      }
    });

    //  verify that the expected workers ran each step
    IWorkflowDb workflowDb = new DatabasesImpl().getWorkflowDb();
    assertEquals(
        "test-worker1",
        Accessors.only(workflowDb.stepAttempts().findByStepToken("action1")).getBackgroundStepAttemptInfo().getBackgroundWorkflowExecutorInfo().getHost()
    );
    assertEquals(
        "test-worker2",
        Accessors.only(workflowDb.stepAttempts().findByStepToken("action2")).getBackgroundStepAttemptInfo().getBackgroundWorkflowExecutorInfo().getHost()
    );


    executor2.shutdown();
  }

  public WorkflowStatePersistence submitFailing() throws IOException {
    BackgroundWorkflow initialized = BackgroundWorkflowPreparer.initialize(
        "test",
        new BackgroundPersistenceFactory(),
        CoreOptions.test()
    );

    InitializedWorkflow.ScopedContext rootScope = initialized.getRootScope();

    TestWorkflowContext context = new TestWorkflowContext("A");

    BackgroundStep action1 = new BackgroundStep(
        rootScope,
        "action1",
        NoOp.class,
        context
    );

    BackgroundStep action2 = new BackgroundStep(
        rootScope,
        "action2",
        FailingAction.class,
        context,
        Duration.ZERO,
        Sets.newHashSet(action1)
    );


    BackgroundStep action3 = new BackgroundStep(
        rootScope,
        "action3",
        NoOp.class,
        context,
        Duration.ZERO,
        Sets.newHashSet(action2)
    );

    //  this is executed by the launcher process.  it can exit afterwards.  or hang around.  doesn't really matter.
    WorkflowStatePersistence persistence = BackgroundWorkflowSubmitter.submit(
        initialized,
        Lists.newArrayList(action3)
    );

    return persistence;
  }

  @Test
  public void testFailing() throws IOException, InterruptedException {

    WorkflowStatePersistence persistence = submitFailing();

    BackgroundWorkflowExecutor executor = startExecutor(persistence.getName(), "worker1");

    waitUntilStatus(persistence, WorkflowAttemptStatus.FAILED);

    //  here we expect the first step to pass, the second to fail, and the last to
    assertEquals(StepStatus.COMPLETED, persistence.getStatus("action1"));
    assertEquals(StepStatus.FAILED, persistence.getStatus("action2"));
    assertEquals(StepStatus.WAITING, persistence.getStatus("action3"));

    executor.shutdown();
  }

  @Test
  public void testPreconditionBlock() throws IOException, InterruptedException {

    BackgroundWorkflow initialized = BackgroundWorkflowPreparer.initialize(
        "test",
        new BackgroundPersistenceFactory(),
        CoreOptions.test()
    );

    InitializedWorkflow.ScopedContext rootScope = initialized.getRootScope();

    TestWorkflowContext context = new TestWorkflowContext("A");

    BackgroundStep action1 = new BackgroundStep(
        rootScope,
        "action1",
        BlockingClass.class,
        context,
        Duration.ofSeconds(1),
        Sets.newHashSet()
    );

    //  this is executed by the launcher process.  it can exit afterwards.  or hang around.  doesn't really matter.
    WorkflowStatePersistence persistence = BackgroundWorkflowSubmitter.submit(
        initialized,
        Lists.newArrayList(action1)
    );

    BackgroundWorkflowExecutor executor = new BackgroundWorkflowExecutor(
        Lists.newArrayList("test"),
        1000,
        10,
        new ErrorReporter.InMemoryReporter(),
        "test-worker"
    );

    executor.start();

    //  hang out for a bit, make sure the executor has been able to run
    Thread.sleep(5000);

    assertEquals(StepStatus.WAITING, persistence.getStatus("action1"));

    SIMPLE_PRECONDITION_ALLOW = true;

    waitUntilStatus(persistence, WorkflowAttemptStatus.FINISHED);

    executor.shutdown();
  }

  public WorkflowStatePersistence submitWorkflowUsingResources() throws IOException {
    BackgroundWorkflow initialized = BackgroundWorkflowPreparer.initialize(
        "test",
        new BackgroundPersistenceFactory(),
        CoreOptions.test()
            .setResourceManagerFactory(DbResourceManager.Factory.class)
    );

    InitializedWorkflow.ScopedContext rootScope = initialized.getRootScope();
    ResourceManager manager = initialized.getManager();

    Resource<String> resource = manager.emptyResource("test-resource");
    ContextWithResources context = new ContextWithResources(resource);

    BackgroundStep action1 = new BackgroundStep(
        rootScope,
        "action1",
        BackgroundResourceProducer.class,
        context,
        Duration.ofSeconds(1),
        Sets.newHashSet()
    );

    BackgroundStep action2 = new BackgroundStep(
        rootScope,
        "action2",
        BackgroundResourceConsumer.class,
        context,
        Duration.ofSeconds(1),
        Sets.newHashSet(action1)
    );

    return BackgroundWorkflowSubmitter.submit(
        initialized,
        Lists.newArrayList(action2)
    );
  }

  @Test
  public void testResources() throws IOException, InterruptedException, InstantiationException, IllegalAccessException {

    WorkflowStatePersistence persistence = submitWorkflowUsingResources();

    BackgroundWorkflowExecutor executor = startExecutor(persistence.getName(), "worker1");

    waitUntilStatus(persistence, WorkflowAttemptStatus.FINISHED);

    executor.shutdown();

  }


  public static class ContextWithResources implements Serializable {
    private final Resource<String> resource;

    public ContextWithResources(Resource<String> resource) {
      this.resource = resource;
    }
  }

  public static class BackgroundResourceConsumer extends BackgroundAction<ContextWithResources> {
    private ReadResource<String> resource;

    public BackgroundResourceConsumer() {
      super(new AlwaysStart<>());
    }

    @Override
    public void initializeInternal(ContextWithResources contextWithResources) {
      resource = readsFrom(contextWithResources.resource);
    }

    @Override
    protected void execute() throws Exception {
      assertEquals("value", get(resource));
    }
  }

  public static class BackgroundResourceProducer extends BackgroundAction<ContextWithResources> {
    private WriteResource<String> resource;

    public BackgroundResourceProducer() {
      super(new AlwaysStart<>());
    }

    @Override
    public void initializeInternal(ContextWithResources testWorkflowContext) {
      resource = creates(testWorkflowContext.resource);
    }

    @Override
    protected void execute() throws Exception {
      set(resource, "value");
    }
  }


  public static class SimpleAssembly implements MultiStep.StepAssembly<Void, TestWorkflowContext> {

    private final TestWorkflowContext sharedContext;

    SimpleAssembly(TestWorkflowContext sharedContext) {
      this.sharedContext = sharedContext;
    }

    @Override
    public Set<BackgroundStep> constructTails(InitializedWorkflow.ScopedContext context) {

      BackgroundStep step = new BackgroundStep(
          context,
          "sub1",
          NoOp.class,
          sharedContext
      );

      BackgroundStep step2 = new BackgroundStep(
          context,
          "sub2",
          NoOp.class,
          sharedContext,
          Duration.ZERO,
          Sets.newHashSet(step)
      );

      return Sets.newHashSet(step2);
    }
  }


  public static class TestWorkflowContext implements Serializable {
    private String someVariable;

    TestWorkflowContext(String variable) {
      this.someVariable = variable;
    }

    public String getSomeVariable() {
      return someVariable;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      TestWorkflowContext that = (TestWorkflowContext)o;

      return someVariable != null ? someVariable.equals(that.someVariable) : that.someVariable == null;
    }

    @Override
    public int hashCode() {
      return someVariable != null ? someVariable.hashCode() : 0;
    }
  }


  //  avoid needing to set up some database or w/e
  private static boolean SIMPLE_PRECONDITION_ALLOW = false;

  private static final class SimplePrecondition implements PreconditionFunction<TestWorkflowContext> {
    @Override
    public Boolean apply(TestWorkflowContext testWorkflowContext) {
      return SIMPLE_PRECONDITION_ALLOW;
    }
  }

  public static class BlockingClass extends BackgroundAction<TestWorkflowContext> {

    public BlockingClass() {
      super(new SimplePrecondition());
    }

    @Override
    public void initializeInternal(TestWorkflowContext testWorkflowContext) {
    }

    @Override
    protected void execute() throws Exception {
      LOG.info("Executing " + getActionId());
    }
  }

  public static class NoOp extends BackgroundAction<TestWorkflowContext> {

    public NoOp() {
      super(new AlwaysStart<>());
    }

    @Override
    public void initializeInternal(TestWorkflowContext testWorkflowContext) {
    }

    @Override
    protected void execute() throws Exception {
      LOG.info("Running Action " + getActionId().resolve());
    }

  }

  public static class FailingAction extends BackgroundAction<TestWorkflowContext> {

    public FailingAction() {
      super(new AlwaysStart<>());
    }

    @Override
    public void initializeInternal(TestWorkflowContext testWorkflowContext) {
    }

    @Override
    protected void execute() throws Exception {
      throw new RuntimeException("berk");
    }
  }

  public static class TestAction extends BackgroundAction<TestWorkflowContext> {

    private String variable;

    public TestAction() {
      super(new AlwaysStart<>());
    }

    @Override
    public void initializeInternal(TestWorkflowContext testWorkflowContext) {
      this.variable = testWorkflowContext.someVariable;
    }

    @Override
    protected void execute() throws Exception {
      LOG.info(variable);
    }

  }

}
