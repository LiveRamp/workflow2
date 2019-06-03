package com.liveramp.workflow_db_state.background_workflow;


import com.liveramp.cascading_ext.resource.ResourceManager;
import com.liveramp.commons.util.MultiShutdownHook;
import com.liveramp.workflow_core.BackgroundState;
import com.liveramp.workflow_core.CoreOptions;
import com.liveramp.workflow_core.background_workflow.BackgroundStep;
import com.liveramp.workflow_db_state.CoreWorkflowDbPersistenceFactory;
import com.liveramp.workflow_db_state.InitializedDbPersistence;

public class BackgroundPersistenceFactory extends CoreWorkflowDbPersistenceFactory<BackgroundStep, CoreOptions, BackgroundWorkflow> {

  public BackgroundPersistenceFactory() {
    super(new BackgroundState());
  }

  @Override
  public BackgroundWorkflow construct(String workflowName, CoreOptions options,
                                      InitializedDbPersistence initializedDbPersistence,
                                      ResourceManager manager, MultiShutdownHook hook) {
    return new BackgroundWorkflow(workflowName, options, initializedDbPersistence, this, manager, hook);
  }

}
