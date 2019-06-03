package com.rapleaf.cascading_ext.workflow2.rollback;

import java.io.IOException;

import com.liveramp.workflow_state.WorkflowStatePersistence;

public interface RollbackBehavior {

  boolean rollbackOnException(WorkflowStatePersistence state) throws IOException;

  class Unconditional implements RollbackBehavior {

    private final boolean rollback;

    public Unconditional(boolean rollback){
      this.rollback = rollback;
    }

    @Override
    public boolean rollbackOnException(WorkflowStatePersistence state) {
      return rollback;
    }
  }


}
