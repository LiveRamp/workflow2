package com.rapleaf.cascading_ext.workflow2;

import java.util.concurrent.Semaphore;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;

public class DelayedFailingAction extends Action {
  private static final Logger LOG = LoggerFactory.getLogger(DelayedFailingAction.class);

  private final Semaphore semaphore;

  public DelayedFailingAction(String checkpointToken, Semaphore sem) {
    super(checkpointToken);
    this.semaphore = sem;
  }

  @Override
  public void execute() throws InterruptedException {
    LOG.info("Acquiring wait lock");
    semaphore.acquire();
    LOG.info("Throwing failure!");
    throw new RuntimeException("failed on purpose");
  }
}
