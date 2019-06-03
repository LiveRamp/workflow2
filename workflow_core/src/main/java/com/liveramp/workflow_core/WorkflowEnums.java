package com.liveramp.workflow_core;

import java.util.EnumSet;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import com.liveramp.workflow.types.StepStatus;
import com.liveramp.workflow.types.WorkflowAttemptStatus;

import static com.liveramp.workflow.types.StepStatus.COMPLETED;
import static com.liveramp.workflow.types.StepStatus.FAILED;
import static com.liveramp.workflow.types.StepStatus.MANUALLY_COMPLETED;
import static com.liveramp.workflow.types.StepStatus.REVERTED;
import static com.liveramp.workflow.types.StepStatus.ROLLBACK_FAILED;
import static com.liveramp.workflow.types.StepStatus.ROLLED_BACK;
import static com.liveramp.workflow.types.StepStatus.ROLLING_BACK;
import static com.liveramp.workflow.types.StepStatus.RUNNING;
import static com.liveramp.workflow.types.StepStatus.SKIPPED;
import static com.liveramp.workflow.types.StepStatus.WAITING;

public class WorkflowEnums {

  private static final Set<WorkflowAttemptStatus> LIVE_ATTEMPT_STATUS = Sets.newHashSet(
      WorkflowAttemptStatus.RUNNING,
      WorkflowAttemptStatus.FAIL_PENDING,
      WorkflowAttemptStatus.SHUTDOWN_PENDING,
      WorkflowAttemptStatus.INITIALIZING
  );

  private static final Set<WorkflowAttemptStatus> EXECUTING_ATTEMPT_STATUS = Sets.newHashSet(
      WorkflowAttemptStatus.RUNNING,
      WorkflowAttemptStatus.FAIL_PENDING,
      WorkflowAttemptStatus.SHUTDOWN_PENDING
  );

  public static final Set<Integer> LIVE_ATTEMPT_STATUSES = Sets.newHashSet();
  public static final Set<Integer> EXECUTING_ATTEMPT_STATUSES = Sets.newHashSet();

  static {
    for (WorkflowAttemptStatus status : LIVE_ATTEMPT_STATUS) {
      LIVE_ATTEMPT_STATUSES.add(status.ordinal());
    }
    for (WorkflowAttemptStatus status : EXECUTING_ATTEMPT_STATUS) {
      EXECUTING_ATTEMPT_STATUSES.add(status.ordinal());
    }
  }

  //  for forward execution

  public static final Set<StepStatus> COMPLETED_STATUSES = EnumSet.of(
      COMPLETED, MANUALLY_COMPLETED
  );

  public static final Set<StepStatus> NON_BLOCKING_STEP_STATUSES = EnumSet.of(
      COMPLETED, SKIPPED, MANUALLY_COMPLETED
  );

  public static final Set<StepStatus> FAILURE_STATUSES = EnumSet.of(
    FAILED
  );

  //  for rollback execution

  public static final Set<StepStatus> NON_BLOCKING_ROLLBACK_STATUSES = EnumSet.of(
      WAITING,
      ROLLED_BACK,
      REVERTED
  );

  public static final Set<StepStatus> FAILURE_ROLLBACK_STATUSES = EnumSet.of(
    ROLLBACK_FAILED
  );

  public static final Set<StepStatus> COMPLETE_ROLLBACK_STATUSES = EnumSet.of(
      ROLLED_BACK,
      REVERTED
  );

  //  etc

  public static final Set<Integer> NON_BLOCKING_STEP_STATUS_IDS = Sets.newHashSet();

  static {
    for (StepStatus stepStatus : NON_BLOCKING_STEP_STATUSES) {
      NON_BLOCKING_STEP_STATUS_IDS.add(stepStatus.ordinal());
    }
  }

  public final static Multimap<StepStatus, StepStatus> VALID_STEP_STATUS_TRANSITIONS = HashMultimap.create();

  static {

    VALID_STEP_STATUS_TRANSITIONS.putAll(WAITING,
        Lists.newArrayList(RUNNING, MANUALLY_COMPLETED));

    VALID_STEP_STATUS_TRANSITIONS.putAll(RUNNING,
        Lists.newArrayList(COMPLETED, FAILED, MANUALLY_COMPLETED));

    //  failed b/c of shutdown hook ordering
    VALID_STEP_STATUS_TRANSITIONS.putAll(COMPLETED,
        Lists.newArrayList(REVERTED, FAILED, ROLLING_BACK));

    VALID_STEP_STATUS_TRANSITIONS.putAll(REVERTED,
        Lists.newArrayList(MANUALLY_COMPLETED));

    VALID_STEP_STATUS_TRANSITIONS.putAll(FAILED,
        Lists.newArrayList(MANUALLY_COMPLETED));

    VALID_STEP_STATUS_TRANSITIONS.putAll(MANUALLY_COMPLETED,
        Lists.newArrayList(REVERTED));

    VALID_STEP_STATUS_TRANSITIONS.putAll(FAILED,
        Lists.newArrayList(ROLLING_BACK));

    VALID_STEP_STATUS_TRANSITIONS.putAll(ROLLING_BACK,
        Lists.newArrayList(ROLLED_BACK, ROLLBACK_FAILED));

  }

}
