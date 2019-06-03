package com.rapleaf.cascading_ext.workflow2.rollback;

import java.io.IOException;
import java.util.Set;

import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liveramp.workflow.types.StepStatus;
import com.liveramp.workflow_core.WorkflowEnums;
import com.liveramp.workflow_state.WorkflowStatePersistence;

public class UnlessStepsRun implements RollbackBehavior {
  private static final Logger LOG = LoggerFactory.getLogger(UnlessStepsRun.class);

  private final Set<String> steps = Sets.newHashSet();

  public UnlessStepsRun(String... blockingSteps){
    this(Sets.newHashSet(blockingSteps));
  }

  public UnlessStepsRun(Set<String> blockingSteps) {
    steps.addAll(blockingSteps);
  }

  @Override
  public boolean rollbackOnException(WorkflowStatePersistence state) throws IOException {

    for (String step : steps) {
      StepStatus status = state.getStatus(step);
      if (status == null) {
        throw new RuntimeException("Unrecognized step token: " + step);
      }

      //  if the step is complete, was manually marked complete, or succeeded in a previous attempt,
      //  don't attempt a rollback
      if (WorkflowEnums.NON_BLOCKING_STEP_STATUSES.contains(status) ||
          WorkflowEnums.FAILURE_STATUSES.contains(status)) {
        LOG.info("Blocking workflow rollback because step "+step+" had status: "+status);
        return false;
      }

    }

    return true;

  }
}
