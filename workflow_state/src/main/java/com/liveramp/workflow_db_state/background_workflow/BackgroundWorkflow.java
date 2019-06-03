package com.liveramp.workflow_db_state.background_workflow;

import com.liveramp.cascading_ext.resource.ResourceManager;
import com.liveramp.commons.util.MultiShutdownHook;
import com.liveramp.workflow_core.CoreOptions;
import com.liveramp.workflow_core.background_workflow.BackgroundStep;
import com.liveramp.workflow_db_state.InitializedDbPersistence;
import com.rapleaf.cascading_ext.workflow2.state.InitializedWorkflow;
import com.rapleaf.cascading_ext.workflow2.state.WorkflowPersistenceFactory;

public class BackgroundWorkflow extends InitializedWorkflow<
    BackgroundStep,
    InitializedDbPersistence,
    CoreOptions> {

  public BackgroundWorkflow(String workflowName, CoreOptions options, InitializedDbPersistence reservedPersistence,
                            WorkflowPersistenceFactory<BackgroundStep, InitializedDbPersistence, CoreOptions, ?> factory,
                            ResourceManager manager, MultiShutdownHook hook) {
    super(workflowName, options, reservedPersistence, factory, manager, hook);
  }

}
