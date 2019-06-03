package com.liveramp.workflow_core.background_workflow;

import java.io.IOException;

import com.liveramp.workflow_core.BaseWorkflowOptions;
import com.liveramp.workflow_state.InitializedPersistence;
import com.rapleaf.cascading_ext.workflow2.state.InitializedWorkflow;
import com.rapleaf.cascading_ext.workflow2.state.WorkflowPersistenceFactory;

public class BackgroundWorkflowPreparer {
  public static <
      Initialized extends InitializedPersistence,
      Opts extends BaseWorkflowOptions<Opts>,
      Workflow extends InitializedWorkflow<BackgroundStep, Initialized, Opts>> Workflow initialize(
      String name,
      WorkflowPersistenceFactory<BackgroundStep, Initialized, Opts, Workflow> factory,
      Opts options
  ) throws IOException {
    return factory.initialize(name, options);
  }
}
