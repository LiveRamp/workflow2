package com.liveramp.workflow_state;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import com.liveramp.workflow.types.StepStatus;

public class StepState {

  //  immutable
  private final String actionClass;
  private final String stepId;

  //  required
  private StepStatus status;

  private String statusMessage;
  private String failureMessage;
  private String failureTrace;

  private long startTimestamp;
  private long endTimestamp;
  private final Set<String> stepDependencies;

  private final Multimap<DSAction, DataStoreInfo> datastores;

  private Map<String, MapReduceJob> mrJobsByID = Maps.newHashMap();

  public StepState(String stepId,
                   StepStatus status,
                   String actionClass,
                   Set<String> dependencies,
                   Multimap<DSAction, DataStoreInfo> datastores) {
    this.stepId = stepId;
    this.status = status;
    this.actionClass = actionClass;
    this.statusMessage = "";
    this.stepDependencies = dependencies;
    this.datastores = datastores;
  }

  public StepState setStatus(StepStatus status) {
    this.status = status;
    return this;
  }

  public StepState setStatusMessage(String statusMessage) {
    this.statusMessage = statusMessage;
    return this;
  }

  public StepState setFailureMessage(String failureMessage) {
    this.failureMessage = failureMessage;
    return this;
  }

  public StepState setFailureTrace(String failureTrace) {
    this.failureTrace = failureTrace;
    return this;
  }

  public StepState setStartTimestamp(long startTimestamp) {
    this.startTimestamp = startTimestamp;
    return this;
  }

  public StepState setEndTimestamp(long endTime) {
    this.endTimestamp = endTime;
    return this;
  }

  public StepState addMrjob(MapReduceJob job) {
    this.mrJobsByID.put(job.getJob().getJobId(), job);
    return this;
  }

  public StepStatus getStatus() {
    return status;
  }

  public String getStepId() {
    return stepId;
  }

  public long getStartTimestamp() {
    return startTimestamp;
  }

  public long getEndTimestamp() {
    return endTimestamp;
  }

  public String getFailureMessage() {
    return failureMessage;
  }

  public String getFailureTrace() {
    return failureTrace;
  }

  public String getStatusMessage() {
    return statusMessage;
  }

  public String getActionClass() {
    return actionClass;
  }

  public Map<String, MapReduceJob> getMrJobsByID() {
    return mrJobsByID;
  }

  public Set<String> getStepDependencies() {
    return stepDependencies;
  }

  public Multimap<DSAction, DataStoreInfo> getDatastores() {
    return datastores;
  }
}
