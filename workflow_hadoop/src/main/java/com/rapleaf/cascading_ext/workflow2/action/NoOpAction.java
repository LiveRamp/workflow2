package com.rapleaf.cascading_ext.workflow2.action;

import com.rapleaf.cascading_ext.workflow2.Action;


public class NoOpAction extends Action {

  public NoOpAction(String checkpointToken) {
    super(checkpointToken);
  }

  public NoOpAction(String checkpointToken, String tmpRoot) {
    super(checkpointToken, tmpRoot);
  }

  @Override
  protected void execute() throws Exception {
    // Deliberately do nothing.
  }
}
