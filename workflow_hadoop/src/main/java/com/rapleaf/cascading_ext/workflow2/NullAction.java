package com.rapleaf.cascading_ext.workflow2;

public final class NullAction extends Action {
  public NullAction(String checkpointToken) {
    super(checkpointToken);
  }
  
  @Override
  public void execute() {}
}
