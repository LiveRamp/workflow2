package com.rapleaf.cascading_ext.workflow2;

import java.util.Collection;

import com.liveramp.workflow_core.runner.BaseAction;
import com.liveramp.workflow_core.runner.BaseStep;

public final class Step extends BaseStep<WorkflowRunner.ExecuteConfig> {

  @SafeVarargs
  public Step(BaseAction<WorkflowRunner.ExecuteConfig> action, BaseStep<WorkflowRunner.ExecuteConfig>... dependencies) {
    super(action, dependencies);
  }

  public Step(BaseAction<WorkflowRunner.ExecuteConfig> action, Collection<? extends BaseStep<WorkflowRunner.ExecuteConfig>> dependencies) {
    super(action, dependencies);
  }
}
