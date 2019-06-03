package com.rapleaf.cascading_ext.workflow2.state;

import com.liveramp.cascading_ext.resource.ResourceManager;
import com.liveramp.commons.util.MultiShutdownHook;
import com.rapleaf.cascading_ext.workflow2.Step;
import com.rapleaf.cascading_ext.workflow2.options.HadoopWorkflowOptions;

public class HadoopWorkflow extends InitializedWorkflow<Step, HdfsInitializedPersistence, HadoopWorkflowOptions> {
  public HadoopWorkflow(String workflowName, HadoopWorkflowOptions options, HdfsInitializedPersistence reservedPersistence, WorkflowPersistenceFactory<Step, HdfsInitializedPersistence, HadoopWorkflowOptions, ?> factory, ResourceManager manager, MultiShutdownHook hook) {
    super(workflowName, options, reservedPersistence, factory, manager, hook);
  }
}
