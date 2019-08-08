package com.rapleaf.cascading_ext.workflow2;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Sets;

import com.liveramp.cascading_ext.CascadingUtil;
import com.liveramp.cascading_ext.megadesk.IStoreReaderLocker;
import com.liveramp.workflow.backpressure.FlowSubmissionController;
import com.liveramp.workflow.state.WorkflowDbPersistenceFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liveramp.workflow2.workflow_hadoop.TmpDirFilter;
import com.liveramp.workflow_state.InitializedPersistence;
import com.rapleaf.cascading_ext.workflow2.options.HadoopWorkflowOptions;
import com.rapleaf.cascading_ext.workflow2.state.InitializedWorkflow;
import com.rapleaf.cascading_ext.workflow2.state.WorkflowPersistenceFactory;

/**
 * Use {@link WorkflowRunners} instead.
 * This is deprecated for public (outside of this package) usage due to connection leaks.
 */
@Deprecated
public final class WorkflowRunner extends BaseWorkflowRunner<WorkflowRunner.ExecuteConfig> {
  private static final Logger LOG = LoggerFactory.getLogger(WorkflowRunner.class);


  public WorkflowRunner(Class klass, HadoopWorkflowOptions options, Step tail) throws IOException {
    this(klass, new WorkflowDbPersistenceFactory(), options, tail);
  }

  public WorkflowRunner(Class klass, HadoopWorkflowOptions options, Set<Step> tailSteps) throws IOException {
    this(klass, new WorkflowDbPersistenceFactory(), options, tailSteps);
  }

  // This constructor requires that the given options contain an AppType for generating the workflow name
  public WorkflowRunner(HadoopWorkflowOptions options, Step tail) throws IOException {
    this(new WorkflowDbPersistenceFactory(), options, tail);
  }

  // This constructor requires that the given options contain an AppType for generating the workflow name
  public WorkflowRunner(HadoopWorkflowOptions options, Set<Step> tailSteps) throws IOException {
    this(new WorkflowDbPersistenceFactory(), options, tailSteps);
  }

  public <K extends InitializedPersistence, W extends InitializedWorkflow<Step, K, HadoopWorkflowOptions>> WorkflowRunner(String workflowName, WorkflowPersistenceFactory<Step, K, HadoopWorkflowOptions, W> persistence, HadoopWorkflowOptions options, Step tail) throws IOException {
    this(workflowName, persistence, options, Sets.newHashSet(tail));
  }


  public <K extends InitializedPersistence, W extends InitializedWorkflow<Step, K, HadoopWorkflowOptions>> WorkflowRunner(Class klass, WorkflowPersistenceFactory<Step, K, HadoopWorkflowOptions, W> persistence, HadoopWorkflowOptions options, final Step first, Step... rest) throws IOException {
    this(klass.getName(), persistence, options, combine(first, rest));
  }

  private static HashSet<Step> combine(final Step first, Step... rest) {
    HashSet<Step> s = new HashSet<>(Arrays.asList(rest));
    s.add(first);
    return s;
  }

  public <K extends InitializedPersistence, W extends InitializedWorkflow<Step, K, HadoopWorkflowOptions>> WorkflowRunner(
      Class klass,
      WorkflowPersistenceFactory<Step, K, HadoopWorkflowOptions, W> persistence,
      HadoopWorkflowOptions options,
      Set<Step> tailSteps) throws IOException {
    this(klass.getName(), persistence, options, tailSteps);
  }

  // This constructor requires that the given options contain an AppType for generating the workflow name
  public <K extends InitializedPersistence,
      W extends InitializedWorkflow<Step, K, HadoopWorkflowOptions>> WorkflowRunner(
      WorkflowPersistenceFactory<Step, K, HadoopWorkflowOptions, W> persistence,
      HadoopWorkflowOptions options,
      Set<Step> tailSteps) throws IOException {
    this(persistence.initialize(options), tailSteps);
  }

  // This constructor requires that the given options contain an AppType for generating the workflow name
  public <K extends InitializedPersistence, W extends InitializedWorkflow<Step, K, HadoopWorkflowOptions>> WorkflowRunner(
      WorkflowPersistenceFactory<Step, K, HadoopWorkflowOptions, W> persistence,
      HadoopWorkflowOptions options,
      Step tail) throws IOException {
    this(persistence, options, Sets.newHashSet(tail));
  }

  public <
      K extends InitializedPersistence,
      W extends InitializedWorkflow<Step, K, HadoopWorkflowOptions>> WorkflowRunner(
      String workflowName, WorkflowPersistenceFactory<Step, K, HadoopWorkflowOptions, W> persistenceFactory,
      HadoopWorkflowOptions options,
      Set<Step> tailSteps) throws IOException {

    this(persistenceFactory.initialize(workflowName, options), tailSteps);
  }

  public <K extends InitializedPersistence,
      W extends InitializedWorkflow<Step, K, HadoopWorkflowOptions>> WorkflowRunner(InitializedWorkflow<Step, K, HadoopWorkflowOptions> initializedData, Step tail) throws IOException {
    this(initializedData, Sets.newHashSet(tail));
  }

  public <K extends InitializedPersistence, W extends InitializedWorkflow<Step, K, HadoopWorkflowOptions>> WorkflowRunner(
      W initializedData,
      Set<Step> tailSteps) throws IOException {

    super(initializedData, tailSteps,
        new ExecuteConfig(
            initializedData.getOptions().getLockProvider().create(),
            initializedData.getOptions().getFlowSubmissionController(),
            initializedData.getOptions().getCascadingUtil(),
            initializedData.getOptions().getTmpDirFilter(),
            initializedData.getOptions().getRuntimePropertiesBuilder(),
            initializedData.getOptions().isFailOnCounterFetch()
        ),
        new OnShutdown<ExecuteConfig>() {
          @Override
          public void shutdown(ExecuteConfig context) {
            LOG.info("Shutting down lock provider");
            context.getLockProvider().shutdown();
          }
        },
        new OnStepRunnerStart() {
          @Override
          public void onStart() {
            initializedData.getOptions().getCascadingUtil().getJobConf();
          }
        }
    );

  }

  public static class ExecuteConfig {
    private IStoreReaderLocker lockProvider;
    private FlowSubmissionController submissionController;
    private CascadingUtil cascadingUtil;
    private TmpDirFilter tmpDirFilter;
    private RuntimePropertiesBuilder runtimePropertiesBuilder;
    private boolean failOnCounterFetch;

    public ExecuteConfig(IStoreReaderLocker lockProvider, FlowSubmissionController submissionController, CascadingUtil cascadingUtil, TmpDirFilter tmpDirFilter, RuntimePropertiesBuilder runtimePropertiesBuilder, boolean failOnCounterFetch) {
      this.lockProvider = lockProvider;
      this.submissionController = submissionController;
      this.cascadingUtil = cascadingUtil;
      this.tmpDirFilter = tmpDirFilter;
      this.runtimePropertiesBuilder = runtimePropertiesBuilder;
      this.failOnCounterFetch = failOnCounterFetch;
    }

    public ExecuteConfig(IStoreReaderLocker lockProvider, CascadingUtil cascadingUtil) {
      this.lockProvider = lockProvider;
      this.submissionController = new FlowSubmissionController.SubmitImmediately();
      this.cascadingUtil = cascadingUtil;
    }

    public IStoreReaderLocker getLockProvider() {
      return lockProvider;
    }

    public FlowSubmissionController getSubmissionController() {
      return submissionController;
    }

    public CascadingUtil getCascadingUtil() {
      return cascadingUtil;
    }

    public TmpDirFilter getTmpDirFilter(){
      return tmpDirFilter;
    }

    public RuntimePropertiesBuilder getRuntimePropertiesBuilder() {
      return runtimePropertiesBuilder;
    }

    public boolean isFailOnCounterFetch() {
      return failOnCounterFetch;
    }
  }

}
