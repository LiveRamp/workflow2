package com.liveramp.workflow_core;

import java.io.Serializable;

import org.apache.commons.lang.NotImplementedException;

import com.liveramp.workflow_core.runner.BaseStep;

public class JVMState<S extends BaseStep<S>> implements StepStateManager<S> {

  @Override
  public boolean isLive() {
    return true;
  }

  @Override
  public Serializable getStepContext(S step) {
    return null;
  }

  @Override
  public Integer getPrereqCheckCooldown(S step) {
    throw new NotImplementedException();
  }

}
