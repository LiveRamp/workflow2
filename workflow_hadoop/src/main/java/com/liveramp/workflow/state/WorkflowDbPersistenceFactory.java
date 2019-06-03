package com.liveramp.workflow.state;

import com.liveramp.cascading_ext.resource.ResourceManager;
import com.liveramp.commons.util.MultiShutdownHook;
import com.liveramp.databases.workflow_db.IWorkflowDb;
import com.liveramp.workflow_core.JVMState;
import com.liveramp.workflow_db_state.CoreWorkflowDbPersistenceFactory;
import com.liveramp.workflow_db_state.InitializedDbPersistence;
import com.rapleaf.cascading_ext.workflow2.Step;
import com.rapleaf.cascading_ext.workflow2.options.HadoopWorkflowOptions;

import java.util.function.Supplier;

public class WorkflowDbPersistenceFactory extends CoreWorkflowDbPersistenceFactory<Step, HadoopWorkflowOptions,
    DbHadoopWorkflow> {
  public WorkflowDbPersistenceFactory() {
    super(new JVMState());
  }

  public WorkflowDbPersistenceFactory(Supplier<IWorkflowDb> supplier) {
    super(new JVMState(), supplier);
  }

  @Override
  public DbHadoopWorkflow construct(String workflowName, HadoopWorkflowOptions options,
                                    InitializedDbPersistence initializedDbPersistence,
                                    ResourceManager manager, MultiShutdownHook hook) {
    return new DbHadoopWorkflow(workflowName, options, initializedDbPersistence, this, manager, hook);
  }
}
