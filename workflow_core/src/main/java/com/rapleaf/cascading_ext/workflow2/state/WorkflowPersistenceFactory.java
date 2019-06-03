package com.rapleaf.cascading_ext.workflow2.state;

import java.io.IOException;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liveramp.cascading_ext.resource.ResourceDeclarer;
import com.liveramp.cascading_ext.resource.ResourceManager;
import com.liveramp.commons.util.MultiShutdownHook;
import com.liveramp.workflow_core.BaseWorkflowOptions;
import com.liveramp.java_support.RunJarUtil;
import com.liveramp.workflow_core.StepStateManager;
import com.liveramp.workflow_core.WorkflowConstants;
import com.liveramp.workflow_core.info.WorkflowInfo;
import com.liveramp.workflow_core.info.WorkflowInfoConsumer;
import com.liveramp.workflow_core.info.WorkflowInfoImpl;
import com.liveramp.workflow_state.IStep;
import com.liveramp.workflow_state.InitializedPersistence;
import com.liveramp.workflow_state.WorkflowStatePersistence;

public abstract class WorkflowPersistenceFactory<
    S extends IStep,
    INITIALIZED extends InitializedPersistence,
    OPTS extends BaseWorkflowOptions<OPTS>,
    WORKFLOW extends InitializedWorkflow<S, INITIALIZED, OPTS>> {
  private static final Logger LOG = LoggerFactory.getLogger(WorkflowPersistenceFactory.class);

  private final StepStateManager<S> manager;

  protected WorkflowPersistenceFactory(StepStateManager<S> manager) {
    this.manager = manager;
  }

  protected StepStateManager<S> getManager() {
    return manager;
  }

  public synchronized WORKFLOW initialize(OPTS options) throws IOException {
    return initialize(options.getAppName(), options);
  }


  public WORKFLOW initialize(String appName, OPTS options) throws IOException {

    if(appName == null){
      throw new IllegalArgumentException();
    }

    if(options.getAppName() != null && !appName.equals(options.getAppName())){
      throw new IllegalArgumentException("Provided name "+appName+"conflicts with options name "+options.getAppName());
    }

    RunJarUtil.ScmInfo scmInfo = RunJarUtil.getRemoteAndCommit();

    final INITIALIZED initialized = initializeInternal(
        appName,
        options,
        options.getHostnameProvider().getHostname(),
        System.getProperty("user.name"),
        findDefaultValue(options, WorkflowConstants.JOB_POOL_PARAM, "default"),
        findDefaultValue(options, WorkflowConstants.JOB_PRIORITY_PARAM, "NORMAL"),
        System.getProperty("user.dir"),
        RunJarUtil.getLaunchJarName(),
        scmInfo.getGitRemote(),
        scmInfo.getRevision()
    );

    MultiShutdownHook hook = new MultiShutdownHook(appName);

    if(manager.isLive()) {
      hook.add(new MultiShutdownHook.Hook() {
        @Override
        public void onShutdown() throws Exception {
          LOG.info("Invoking workflow shutdown hook");
          initialized.markWorkflowStopped();
        }
      });
    }

    ResourceDeclarer resourceDeclarer = options.getResourceManager();

    ResourceManager resourceManager = resourceDeclarer.create(
        initialized.getExecutionId(),
        initialized.getClass().getName()
    );

    WorkflowInfo info = new WorkflowInfoImpl(initialized, options);
    for (WorkflowInfoConsumer workflowInfoConsumer : options.getWorkflowInfoConsumers()) {
      workflowInfoConsumer.accept(info);
    }

    Runtime.getRuntime().addShutdownHook(hook);

    return construct(appName, options, initialized, resourceManager, hook);
  }

  //  force implementers to override to avoid generics overload
  public abstract WORKFLOW construct(String workflowName,
                                        OPTS options,
                                        INITIALIZED initialized,
                                        ResourceManager manager,
                                        MultiShutdownHook hook);

  public static String findDefaultValue(BaseWorkflowOptions options, String property, String defaultValue) {

    Object value = options.getConfiguredProperty(property);


    if (value != null) {
      return value.toString();
    }

    //  only really expect in tests
    return defaultValue;
  }


  public abstract INITIALIZED initializeInternal(String name,
                                                 OPTS options,
                                                 String host,
                                                 String username,
                                                 String pool,
                                                 String priority,
                                                 String launchDir,
                                                 String launchJar,
                                                 String remote,
                                                 String implementationBuild
  ) throws IOException;

  public abstract WorkflowStatePersistence prepare(INITIALIZED persistence, DirectedGraph<S, DefaultEdge> flatSteps);

}
