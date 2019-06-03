package com.rapleaf.cascading_ext.workflow2;

public class FailingAction extends Action {
  public FailingAction(String checkpointToken) {
    super(checkpointToken);
  }

  @Override
  public void execute() {
    throw new RuntimeException("failed on purpose");
  }
}
