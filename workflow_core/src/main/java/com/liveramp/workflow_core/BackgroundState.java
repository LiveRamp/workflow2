package com.liveramp.workflow_core;

import java.io.Serializable;

import com.liveramp.workflow_core.background_workflow.BackgroundStep;

public class BackgroundState implements StepStateManager<BackgroundStep> {

  @Override
  public boolean isLive() {
    return false;
  }

  @Override
  public Integer getPrereqCheckCooldown(BackgroundStep step) {
    return (int) (step.getPrereqCheckCooldown().toMillis()/1000);
  }

  @Override
  public Serializable getStepContext(BackgroundStep step) {
    return step.getContext();
  }
}
