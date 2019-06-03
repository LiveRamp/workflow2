package com.liveramp.workflow_core.step;

import com.liveramp.workflow_core.runner.BaseAction;

public class NoOp<Config> extends BaseAction<Config> {

  public NoOp(String checkpointToken) {
    super(checkpointToken);
  }

  @Override
  protected void execute() throws Exception {
    //  no op
  }
}
