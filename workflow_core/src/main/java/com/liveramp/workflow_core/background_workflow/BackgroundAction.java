package com.liveramp.workflow_core.background_workflow;

import java.io.Serializable;
import java.util.function.Function;

import com.liveramp.workflow_core.runner.BaseAction;

public abstract class BackgroundAction<Context extends Serializable> extends BaseAction<Void> {

  private final PreconditionFunction<Context> allowExecute;

  public BackgroundAction(PreconditionFunction<Context> allowExecute) {
    super(null);
    this.allowExecute = allowExecute;
  }

  //  it would be nice to make this a Context, but I can't figure out how to make the casting work
  public void initializeContext(Serializable context){
    initializeInternal((Context) context);
  }

  public abstract void initializeInternal(Context context);


  public PreconditionFunction<Context> getAllowExecute() {
    return allowExecute;
  }


}
