package com.rapleaf.cascading_ext.workflow2;

public class IncrementAction extends Action {
  public IncrementAction(String checkpointToken) {
    super(checkpointToken);
  }

  public static int counter = 0;

  @Override
  public void execute() {
    counter++;
  }
}
