package com.liveramp.workflow_db_state;

import com.liveramp.cascading_ext.resource.ResourceManager;
import com.liveramp.commons.util.MultiShutdownHook;
import com.liveramp.workflow_core.CoreOptions;
import com.liveramp.workflow_core.runner.BaseStep;
import com.rapleaf.cascading_ext.workflow2.state.InitializedWorkflow;
import com.rapleaf.cascading_ext.workflow2.state.WorkflowPersistenceFactory;

public class DbWorkflow extends InitializedWorkflow<BaseStep, InitializedDbPersistence, CoreOptions>{
  public DbWorkflow(String workflowName, CoreOptions options, InitializedDbPersistence reservedPersistence, WorkflowPersistenceFactory<BaseStep, InitializedDbPersistence, CoreOptions, ?> factory, ResourceManager manager, MultiShutdownHook hook) {
    super(workflowName, options, reservedPersistence, factory, manager, hook);
  }
}
