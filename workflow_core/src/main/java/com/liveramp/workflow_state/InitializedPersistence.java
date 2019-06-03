package com.liveramp.workflow_state;

import java.io.IOException;

//  public methods after a workflow is initialized
public interface InitializedPersistence {

  //  should these belong somewhere else?  only really implemented in db-backed one
  long getExecutionId() throws IOException;
  long getAttemptId() throws IOException;

  //  indicate that the workflow is stopped
  public void markWorkflowStopped() throws IOException;

  //  terminate all resources. no state cleanup is done before terminating.
  public void shutdown() throws IOException;

}
