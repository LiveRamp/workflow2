package com.liveramp.workflow2.workflow_hadoop;

import java.io.IOException;
import java.util.Collection;

import org.apache.hadoop.fs.FileSystem;

import com.liveramp.workflow_core.runner.BaseMultiStepAction;
import com.liveramp.workflow_core.runner.BaseStep;
import com.rapleaf.cascading_ext.workflow2.FsActionContext;
import com.rapleaf.cascading_ext.workflow2.WorkflowRunner;

public class HadoopMultiStepAction extends BaseMultiStepAction<WorkflowRunner.ExecuteConfig> {

  private final FsActionContext context;

  public HadoopMultiStepAction(String checkpointToken, String tmpRoot) {
    this(checkpointToken, tmpRoot, null);
  }

  public HadoopMultiStepAction(String checkpointToken, String tmpRoot, Collection<? extends BaseStep<WorkflowRunner.ExecuteConfig>> steps) {
    super(checkpointToken, steps);
    this.context = new FsActionContext(tmpRoot, checkpointToken);
  }

  public final String getTmpRoot() {
    return context.getTmpRoot();
  }

  protected FileSystem getFS() throws IOException {
    return context.getFS();
  }

}
