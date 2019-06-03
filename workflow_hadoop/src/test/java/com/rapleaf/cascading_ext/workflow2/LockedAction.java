package com.rapleaf.cascading_ext.workflow2;

import java.util.concurrent.Semaphore;

public class LockedAction extends Action {

  private final Semaphore semaphore;

  public LockedAction(String checkpointToken, Semaphore semaphore) {
    super(checkpointToken);
    this.semaphore = semaphore;
  }

  @Override
  protected void execute() throws Exception {
    semaphore.acquire();
  }
}
