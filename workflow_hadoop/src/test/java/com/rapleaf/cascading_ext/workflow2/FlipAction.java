package com.rapleaf.cascading_ext.workflow2;

import java.util.concurrent.atomic.AtomicBoolean;

public class FlipAction extends Action {

  private final AtomicBoolean val;

  public FlipAction(String checkpointToken, AtomicBoolean val) {
    super(checkpointToken);
    this.val = val;
  }

  @Override
  public void execute() {
    val.set(true);
  }
}
