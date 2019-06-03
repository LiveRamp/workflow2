package com.rapleaf.cascading_ext.workflow2;

import java.util.concurrent.atomic.AtomicInteger;

public class IncrementAction2 extends Action {

  private final AtomicInteger counter;

  public IncrementAction2(String checkpointToken, AtomicInteger counter) {
    super(checkpointToken);
    this.counter = counter;
  }

  @Override
  public void execute() {
    counter.incrementAndGet();
  }

}
