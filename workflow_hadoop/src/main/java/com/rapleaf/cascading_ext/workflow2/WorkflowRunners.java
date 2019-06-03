package com.rapleaf.cascading_ext.workflow2;

import java.io.IOException;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import com.liveramp.workflow.state.DbHadoopWorkflow;
import com.liveramp.workflow.state.WorkflowDbPersistenceFactory;
import com.liveramp.workflow_db_state.InitializedDbPersistence;
import com.liveramp.workflow_state.InitializedPersistence;
import com.rapleaf.cascading_ext.workflow2.options.HadoopWorkflowOptions;
import com.rapleaf.cascading_ext.workflow2.state.HadoopWorkflow;
import com.rapleaf.cascading_ext.workflow2.state.HdfsCheckpointPersistence;
import com.rapleaf.cascading_ext.workflow2.state.HdfsInitializedPersistence;
import com.rapleaf.cascading_ext.workflow2.state.InitializedWorkflow;
import com.rapleaf.cascading_ext.workflow2.state.WorkflowPersistenceFactory;

//  TODO if this officially covers all cases, should deprecate other constructors (or delete this comment if it's too annoying)
public class WorkflowRunners {

  public static void dbRun(
      String workflowName,
      HadoopWorkflowOptions options,
      WorkflowBuilder<InitializedDbPersistence, DbHadoopWorkflow> constructor
  ) throws IOException {
    WorkflowRunners.run(new WorkflowDbPersistenceFactory(), workflowName, options, constructor, new NoOp<>());
  }

  public static void hdfsRun(
      String workflowName,
      String hdfsPath,
      HadoopWorkflowOptions options,
      WorkflowBuilder<HdfsInitializedPersistence, HadoopWorkflow> constructor
  ) throws IOException {
    WorkflowRunners.run(new HdfsCheckpointPersistence(hdfsPath), workflowName, options, constructor, new NoOp<>());
  }


  public static <INITIALIZED extends InitializedPersistence, WORKFLOW extends InitializedWorkflow<Step, INITIALIZED, HadoopWorkflowOptions>> void run(
      WorkflowPersistenceFactory<Step, INITIALIZED, HadoopWorkflowOptions, WORKFLOW> persistenceFactory,
      String workflowName,
      HadoopWorkflowOptions options,
      WorkflowBuilder<INITIALIZED, WORKFLOW> constructor,
      PostRunCallback<INITIALIZED, WORKFLOW> callback
  ) throws IOException {


    WORKFLOW initialized = null;

    try {
      initialized = persistenceFactory.initialize(workflowName, options);
      Set<Step> steps = constructor.apply(initialized);

      WorkflowRunner runner = new WorkflowRunner(initialized, steps);
      runner.run();

      callback.accept(initialized);

    } finally {

      if (initialized != null) {
        INITIALIZED persistence = initialized.getInitializedPersistence();
        persistence.markWorkflowStopped();
        persistence.shutdown();
      }

    }

  }

  public interface WorkflowBuilder<INITIALIZED extends InitializedPersistence, WORKFLOW extends InitializedWorkflow<Step, INITIALIZED, HadoopWorkflowOptions>>
      extends Function<WORKFLOW, Set<Step>> {}

  public interface PostRunCallback<INITIALIZED extends InitializedPersistence, WORKFLOW extends InitializedWorkflow<Step, INITIALIZED, HadoopWorkflowOptions>>
      extends Consumer<WORKFLOW> {}

  public static class NoOp<INITIALIZED extends InitializedPersistence, WORKFLOW extends InitializedWorkflow<Step, INITIALIZED, HadoopWorkflowOptions>> implements PostRunCallback<INITIALIZED, WORKFLOW>{
    @Override
    public void accept(WORKFLOW initialized) {
      //  nope
    }
  }

}
