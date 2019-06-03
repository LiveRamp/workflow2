package com.liveramp.workflow.backpressure;

import org.apache.hadoop.conf.Configuration;

import cascading.flow.Flow;

public interface FlowSubmissionController {

  //Returns a cleanup callback
  Runnable blockUntilSubmissionAllowed(Configuration flowConfig);

  class SubmitImmediately implements FlowSubmissionController {

    @Override
    public Runnable blockUntilSubmissionAllowed(Configuration flowConfig) {
      //Don't block, don't cleanup
      return () -> {};
    }
  }

  interface SubmissionSemaphore {

    void blockUntilShareIsAvailable(long timeoutMillseconds);

    void releaseShare();

  }

}
