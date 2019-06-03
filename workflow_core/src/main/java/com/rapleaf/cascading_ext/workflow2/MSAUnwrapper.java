package com.rapleaf.cascading_ext.workflow2;

import java.util.Set;

import com.liveramp.workflow_core.runner.BaseMultiStepAction;
import com.liveramp.workflow_core.runner.BaseStep;

public class MSAUnwrapper<Config> implements WorkflowDiagram.GraphUnwrapper<BaseStep<Config>, BaseMultiStepAction<Config>> {

  @Override
  public BaseMultiStepAction<Config> getMultiNode(BaseStep<Config> node) {
    if(node.getAction() instanceof BaseMultiStepAction){
      return (BaseMultiStepAction<Config>) node.getAction();
    }
    return null;
  }

  @Override
  public Set<BaseStep<Config>> getMultiSubSteps(BaseMultiStepAction<Config> multi) {
    return multi.getSubSteps();
  }

  @Override
  public Set<BaseStep<Config>> getTailSteps(BaseMultiStepAction<Config> multi) {
    return multi.getTailSteps();
  }

  @Override
  public Set<BaseStep<Config>> getHeadSteps(BaseMultiStepAction<Config> multi) {
    return multi.getHeadSteps();
  }

}
