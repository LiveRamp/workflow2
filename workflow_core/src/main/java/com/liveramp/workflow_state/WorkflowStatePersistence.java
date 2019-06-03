package com.liveramp.workflow_state;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.liveramp.commons.collections.nested_map.ThreeNestedMap;
import com.liveramp.commons.collections.nested_map.TwoNestedMap;
import com.liveramp.commons.state.TaskSummary;
import com.liveramp.java_support.alerts_handler.AlertsHandler;
import com.liveramp.workflow.types.StepStatus;
import com.liveramp.workflow.types.WorkflowAttemptStatus;
import com.liveramp.workflow_core.alerting.AlertsHandlerFactory;

//  TODO somehow split up the interfaces here.  dunno how.
public interface WorkflowStatePersistence extends InitializedPersistence{

  //  user-defined-action triggered
  public void markStepStatusMessage(String stepToken, String newMessage) throws IOException;
  public void markStepRunningJob(String stepToken, String jobId, String jobName, String trackingURL) throws IOException;
  public void markJobCounters(String stepToken, String jobId, TwoNestedMap<String, String, Long> values) throws IOException;
  public void markJobTaskInfo(String stepToken, String jobId, TaskSummary info) throws IOException;

  //  StepRunner
  public void markStepRunning(String stepToken) throws IOException;
  public void markStepFailed(String stepToken, Throwable t) throws IOException;
  public void markStepCompleted(String stepToken) throws IOException;

  public void markWorkflowStarted() throws IOException;

  //  StepRunner
  public void markStepRollingBack(String stepToken) throws IOException;
  public void markStepRollbackFailure(String stepToken, Throwable t) throws IOException;
  public void markStepRolledBack(String stepToken) throws IOException;

  public void markRollbackStarted() throws IOException;

  //  UI / manually controlled
  public void markStepReverted(String stepToken) throws IOException;
  public void markStepManuallyCompleted(String stepToken) throws IOException;
  public void markPool(String pool) throws IOException;
  public void markPriority(String priority) throws IOException;
  public void markShutdownRequested(String reason) throws IOException;

  //  get* methods probably don't need to be quarantined to here.  can be split out?
  public StepStatus getStatus(String stepToken) throws IOException;
  public Serializable getContext(String stepToken) throws IOException;
  public String getStatusMessage(String stepToken) throws IOException;
  public Map<String, StepStatus> getStepStatuses() throws IOException;
  public Map<String, StepState> getStepStates() throws IOException;
  public String getShutdownRequest() throws IOException;
  public String getPriority() throws IOException;
  public String getPool() throws IOException;
  public String getName() throws IOException;
  public String getScopeIdentifier() throws IOException;
  public String getId() throws IOException;
  public WorkflowAttemptStatus getStatus() throws IOException;
  public ExecutionState getExecutionState() throws IOException;
  public List<AlertsHandler> getRecipients(WorkflowRunnerNotification notification, AlertsHandlerFactory factory) throws IOException;
  
  //  should these belong somewhere else?  only really implemented in db-backed one
  public ThreeNestedMap<String, String, String, Long> getCountersByStep() throws IOException;
  public TwoNestedMap<String, String, Long> getFlatCounters() throws IOException;

}
