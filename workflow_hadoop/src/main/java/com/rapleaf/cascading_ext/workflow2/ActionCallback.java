package com.rapleaf.cascading_ext.workflow2;

import java.io.IOException;

public interface ActionCallback {

  public void construct(Action.ConstructContext context);

  public void prepare(Action.PreExecuteContext context) throws IOException;

  public class Default implements ActionCallback {

    @Override
    public void construct(Action.ConstructContext context) {
      //  no op
    }

    @Override
    public void prepare(Action.PreExecuteContext context) throws IOException {
      // no op
    }
  }
}
