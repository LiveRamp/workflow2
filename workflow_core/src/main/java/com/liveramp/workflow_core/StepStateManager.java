package com.liveramp.workflow_core;

import java.io.Serializable;

import com.liveramp.workflow_core.background_workflow.BackgroundStep;
import com.liveramp.workflow_core.runner.BaseStep;

public interface StepStateManager<S> {

  boolean isLive();

  Serializable getStepContext(S step);

  Integer getPrereqCheckCooldown(S step);

}
