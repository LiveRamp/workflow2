
namespace rb    Liveramp.Types.Workflow
namespace java  com.liveramp.workflow.types

enum WorkflowExecutionStatus {
  INCOMPLETE = 0;
  COMPLETE = 1;
  CANCELLED = 2;
}

enum WorkflowAttemptStatus {
  RUNNING = 0;
  FAIL_PENDING = 1;
  SHUTDOWN_PENDING = 2;
  FAILED = 3;
  FINISHED = 4;
  SHUTDOWN = 5;
  INITIALIZING = 6;
}

enum StepStatus {
  WAITING = 0;
  RUNNING = 1;
  COMPLETED = 2;
  FAILED = 3;
  SKIPPED = 4;
  REVERTED = 5;
  MANUALLY_COMPLETED = 6;
  ROLLING_BACK = 7;
  ROLLED_BACK = 8;
  ROLLBACK_FAILED = 9;
}

enum ExecutorStatus {
  RUNNING = 0;
  STOPPED = 1;
}
