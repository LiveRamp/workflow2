package com.rapleaf.cascading_ext.workflow2;

import java.util.concurrent.Semaphore;

public class UnlockWaitAction extends Action {

  private final Semaphore toUnlock;
  private final Semaphore toAwait;


  public UnlockWaitAction(String checkpointToken, Semaphore toUnlock,
                          Semaphore toAwait) {
    super(checkpointToken);
    this.toUnlock = toUnlock;
    this.toAwait = toAwait;
  }

  @Override
  protected void execute() throws Exception {
    toUnlock.release();
    toAwait.acquire();
  }
}
