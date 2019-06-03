package com.liveramp.workflow_core.background_workflow;

import java.io.Serializable;

public class AlwaysStart<Context extends Serializable> implements PreconditionFunction<Context> {
  @Override
  public Boolean apply(Context context) {
    return true;
  }
}
