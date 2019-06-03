package com.liveramp.workflow.state;

import com.liveramp.cascading_ext.resource.ResourceManager;
import com.liveramp.commons.util.MultiShutdownHook;
import com.liveramp.workflow_db_state.InitializedDbPersistence;
import com.rapleaf.cascading_ext.workflow2.Step;
import com.rapleaf.cascading_ext.workflow2.options.HadoopWorkflowOptions;
import com.rapleaf.cascading_ext.workflow2.state.InitializedWorkflow;
import com.rapleaf.cascading_ext.workflow2.state.WorkflowPersistenceFactory;

public class DbHadoopWorkflow extends InitializedWorkflow<Step, InitializedDbPersistence, HadoopWorkflowOptions> {
  public DbHadoopWorkflow(String workflowName, HadoopWorkflowOptions options, InitializedDbPersistence reservedPersistence, WorkflowPersistenceFactory<Step, InitializedDbPersistence, HadoopWorkflowOptions, ?> factory, ResourceManager manager, MultiShutdownHook hook) {
    super(workflowName, options, reservedPersistence, factory, manager, hook);
  }
}
