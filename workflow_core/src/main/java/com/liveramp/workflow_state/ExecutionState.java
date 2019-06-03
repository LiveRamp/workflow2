package com.liveramp.workflow_state;

public class ExecutionState {

  private final Long startTime;
  private final Long endTime;
  private final String appName;
  private final int numAttempts;

  public ExecutionState(Long startTime, Long endTime, String appName, int numAttempts) {
    this.startTime = startTime;
    this.endTime = endTime;
    this.appName = appName;
    this.numAttempts = numAttempts;
  }

  public Long getStartTime() {
    return startTime;
  }

  public Long getEndTime() {
    return endTime;
  }

  public String getAppName() {
    return appName;
  }


  public int getNumAttempts() {
    return numAttempts;
  }

}
