package com.liveramp.workflow_core.background_workflow;

import java.io.Serializable;
import java.util.Set;

import com.rapleaf.cascading_ext.workflow2.WorkflowDiagram;

public class BackgroundUnwrapper implements WorkflowDiagram.GraphUnwrapper<BackgroundStep, MultiStep> {

  @Override
  public MultiStep getMultiNode(BackgroundStep node) {

    if (node instanceof MultiStep) {
      return (MultiStep)node;
    }

    return null;
  }

  @Override
  public Set<BackgroundStep> getMultiSubSteps(MultiStep multi) {
    return multi.getSubSteps();
  }

  @Override
  public Set<BackgroundStep> getTailSteps(MultiStep multi) {
    return multi.getTails();
  }

  @Override
  public Set<BackgroundStep> getHeadSteps(MultiStep multi) {
    return multi.getHeads();
  }
}
