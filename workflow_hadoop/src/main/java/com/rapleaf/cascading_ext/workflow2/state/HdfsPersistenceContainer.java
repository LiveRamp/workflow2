package com.rapleaf.cascading_ext.workflow2.state;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang.NotImplementedException;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liveramp.cascading_ext.FileSystemHelper;
import com.liveramp.commons.collections.nested_map.ThreeNestedCountingMap;
import com.liveramp.commons.collections.nested_map.ThreeNestedMap;
import com.liveramp.commons.collections.nested_map.TwoNestedCountingMap;
import com.liveramp.commons.collections.nested_map.TwoNestedMap;
import com.liveramp.commons.state.LaunchedJob;
import com.liveramp.commons.state.TaskSummary;
import com.liveramp.java_support.alerts_handler.AlertsHandler;
import com.liveramp.workflow.types.StepStatus;
import com.liveramp.workflow.types.WorkflowAttemptStatus;
import com.liveramp.workflow2.workflow_hadoop.CheckpointUtil;
import com.liveramp.workflow_core.alerting.AlertsHandlerFactory;
import com.liveramp.workflow_state.ExecutionState;
import com.liveramp.workflow_state.MapReduceJob;
import com.liveramp.workflow_state.StepState;
import com.liveramp.workflow_core.WorkflowEnums;
import com.liveramp.workflow_state.WorkflowRunnerNotification;
import com.liveramp.workflow_state.WorkflowStatePersistence;
import com.liveramp.workflow_db_state.json.WorkflowJSON;

public class HdfsPersistenceContainer implements WorkflowStatePersistence {
  private static final Logger LOG = LoggerFactory.getLogger(HdfsPersistenceContainer.class);

  private final HdfsInitializedPersistence initializedPersistence;

  private final String checkpointDir;
  private final boolean deleteCheckpointsOnSuccess;
  private final FileSystem fs;

  private final Map<String, StepState> statuses;
  private String shutdownReason;

  private final String id;

  private final long attempt;

  public HdfsPersistenceContainer(String checkpointDir,
                                  boolean deleteOnSuccess,
                                  String id,
                                  Map<String, StepState> statuses,
                                  HdfsInitializedPersistence initializedPersistence) {

    this.checkpointDir = checkpointDir;
    this.deleteCheckpointsOnSuccess = deleteOnSuccess;
    this.fs = FileSystemHelper.getFileSystemForPath(checkpointDir);

    this.id = id;
    this.statuses = statuses;

    this.initializedPersistence = initializedPersistence;

    this.attempt = Math.abs(new Random().nextLong());
  }


  @Override
  public void markShutdownRequested(String reason) {
    shutdownReason = WorkflowJSON.getShutdownReason(reason);
  }

  @Override
  public void markWorkflowStopped() {

    if (allStepsSucceeded() && shutdownReason == null) {
      try {
        if (deleteCheckpointsOnSuccess) {
          clearCheckpoints();
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

  }

  @Override
  public void shutdown() throws IOException {
    initializedPersistence.shutdown();
    fs.close();
  }

  private void clearCheckpoints() throws IOException {
    LOG.debug("Deleting checkpoints in checkpoint dir " + checkpointDir);
    CheckpointUtil.clearCheckpoints(fs, new Path(checkpointDir));
  }

  private boolean allStepsSucceeded() {

    for (Map.Entry<String, StepState> stepStatuses : statuses.entrySet()) {
      if (!WorkflowEnums.NON_BLOCKING_STEP_STATUSES.contains(stepStatuses.getValue().getStatus())) {
        return false;
      }
    }

    return true;
  }


  @Override
  public StepStatus getStatus(String stepToken) throws IOException {
    return getState(stepToken).getStatus();
  }

  @Override
  public Serializable getContext(String stepToken) throws IOException {
    return null;
  }

  @Override
  public String getStatusMessage(String stepToken) throws IOException {
    return getState(stepToken).getStatusMessage();
  }

  @Override
  public Map<String, StepStatus> getStepStatuses() throws IOException {
    Map<String, StepStatus> statuses = Maps.newHashMap();
    for (Map.Entry<String, StepState> entry : getStepStates().entrySet()) {
      statuses.put(entry.getKey(), entry.getValue().getStatus());
    }
    return statuses;
  }

  private StepState getState(String stepToken) {

    if (!statuses.containsKey(stepToken)) {
      throw new RuntimeException("Unknown step " + stepToken + "!");
    }

    return statuses.get(stepToken);
  }

  @Override
  public Map<String, StepState> getStepStates() {
    return statuses;
  }

  @Override
  public String getShutdownRequest() {
    return shutdownReason;
  }

  @Override
  public String getPriority() {
    return initializedPersistence.getPriority();
  }

  @Override
  public String getPool() {
    return initializedPersistence.getPool();
  }

  @Override
  public String getName() {
    return initializedPersistence.getName();
  }

  @Override
  public String getScopeIdentifier() throws IOException {
    return null;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public WorkflowAttemptStatus getStatus() throws IOException {
    throw new NotImplementedException();
  }

  @Override
  public ExecutionState getExecutionState() throws IOException {
    throw new NotImplementedException();
  }

  @Override
  public List<AlertsHandler> getRecipients(WorkflowRunnerNotification notification, AlertsHandlerFactory factory) throws IOException {
    if (initializedPersistence.getConfiguredNotifications().contains(notification)) {
      return Lists.newArrayList(initializedPersistence.getHandler());
    }
    return Lists.newArrayList();
  }

  @Override
  public ThreeNestedMap<String, String, String, Long> getCountersByStep() throws IOException {

    ThreeNestedCountingMap<String, String, String> counters = new ThreeNestedCountingMap<>(0L);
    for (Map.Entry<String, StepState> step : statuses.entrySet()) {
      for (Map.Entry<String, MapReduceJob> jobEntry : step.getValue().getMrJobsByID().entrySet()) {
        for (MapReduceJob.Counter counter : jobEntry.getValue().getCounters()) {
          counters.incrementAndGet(step.getKey(), counter.getGroup(), counter.getName(), counter.getValue());
        }
      }
    }

    return counters;
  }

  @Override
  public TwoNestedMap<String, String, Long> getFlatCounters() {

    TwoNestedCountingMap<String, String> counters = new TwoNestedCountingMap<>(0L);
    for (Map.Entry<String, StepState> step : statuses.entrySet()) {
      for (Map.Entry<String, MapReduceJob> jobEntry : step.getValue().getMrJobsByID().entrySet()) {
        for (MapReduceJob.Counter counter : jobEntry.getValue().getCounters()) {
          counters.incrementAndGet(counter.getGroup(), counter.getName(), counter.getValue());
        }
      }
    }

    return counters;

  }

  @Override
  public long getExecutionId() throws IOException {
    return initializedPersistence.getExecutionId();
  }

  @Override
  public long getAttemptId() throws IOException {
    return attempt;
  }

  @Override
  public void markStepRunning(String stepToken) throws IOException {
    getState(stepToken)
        .setStatus(StepStatus.RUNNING)
        .setStartTimestamp(System.currentTimeMillis());
  }

  @Override
  public void markStepFailed(String stepToken, Throwable e) throws IOException {

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    e.printStackTrace(pw);

    getState(stepToken)
        .setFailureMessage(e.getMessage())
        .setFailureTrace(sw.toString())
        .setStatus(StepStatus.FAILED)
        .setEndTimestamp(System.currentTimeMillis());

  }

  @Override
  public void markStepCompleted(String stepToken) throws IOException {
    LOG.info("Writing out checkpoint token for " + stepToken);
    String tokenPath = checkpointDir + "/" + stepToken;
    if (!fs.createNewFile(new Path(tokenPath))) {
      throw new IOException("Couldn't create checkpoint file " + tokenPath);
    }
    LOG.debug("Done writing checkpoint token for " + stepToken);

    getState(stepToken)
        .setStatus(StepStatus.COMPLETED)
        .setEndTimestamp(System.currentTimeMillis());
  }

  @Override
  public void markStepReverted(String stepToken) throws IOException {
    throw new NotImplementedException();
  }

  @Override
  public void markStepManuallyCompleted(String stepToken) throws IOException {
    throw new NotImplementedException();
  }

  @Override
  public void markStepStatusMessage(String stepToken, String newMessage) {
    getState(stepToken)
        .setStatusMessage(newMessage);
  }

  @Override
  public void markStepRunningJob(String stepToken, String jobId, String jobName, String trackingURL) {

    Set<String> knownJobs = Sets.newHashSet();
    StepState stepState = getState(stepToken);

    for (String existingId : stepState.getMrJobsByID().keySet()) {
      knownJobs.add(existingId);
    }

    if (!knownJobs.contains(jobId)) {
      stepState.addMrjob(new MapReduceJob(new LaunchedJob(jobId, jobName, trackingURL), null, Lists.<MapReduceJob.Counter>newArrayList()));
    }

  }

  @Override
  public void markJobCounters(String stepToken, String jobId, TwoNestedMap<String, String, Long> values) throws IOException {
    getState(stepToken).getMrJobsByID().get(jobId).setCounters(values.entrySet().stream().map(
        entry -> new MapReduceJob.Counter(entry.getK1(), entry.getK2(), entry.getValue())).collect(Collectors.toList())
    );
  }

  @Override
  public void markJobTaskInfo(String stepToken, String jobId, TaskSummary info) throws IOException {
    getState(stepToken).getMrJobsByID().get(jobId).setTaskSummary(info);
  }

  @Override
  public void markWorkflowStarted() throws IOException {
  }

  @Override
  public void markStepRollingBack(String stepToken) throws IOException {
    getState(stepToken)
        .setStatus(StepStatus.ROLLING_BACK);
  }

  @Override
  public void markStepRollbackFailure(String stepToken, Throwable t) throws IOException {
    getState(stepToken)
        .setStatus(StepStatus.ROLLBACK_FAILED);
  }

  @Override
  public void markStepRolledBack(String stepToken) throws IOException {
    getState(stepToken)
        .setStatus(StepStatus.ROLLED_BACK);

  }

  @Override
  public void markRollbackStarted() throws IOException {
    LOG.info("Rolling back workflow, clearing checkpoints");
    clearCheckpoints();
  }

  @Override
  public void markPool(String pool) {
    this.initializedPersistence.setPool(pool);
  }

  @Override
  public void markPriority(String priority) {
    this.initializedPersistence.setPriority(priority);
  }

}
