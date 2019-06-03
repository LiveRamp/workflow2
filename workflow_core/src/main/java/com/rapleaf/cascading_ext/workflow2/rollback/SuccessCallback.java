package com.rapleaf.cascading_ext.workflow2.rollback;

import com.liveramp.workflow_state.WorkflowStatePersistence;

public interface SuccessCallback {
  void onSuccess(WorkflowStatePersistence state);
}
