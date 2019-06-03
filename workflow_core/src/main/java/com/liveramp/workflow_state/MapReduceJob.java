package com.liveramp.workflow_state;

import java.util.List;

import com.liveramp.commons.state.LaunchedJob;
import com.liveramp.commons.state.TaskSummary;

public class MapReduceJob {

  private List<Counter> counters;
  private LaunchedJob job;
  private TaskSummary taskSummary;

  public static class Counter {
    private final String group;
    private final String name;
    private final Long value;

    public Counter(String group, String name, Long value) {
      this.group = group;
      this.name = name;
      this.value = value;
    }

    public String getGroup() {
      return group;
    }

    public String getName() {
      return name;
    }

    public Long getValue() {
      return value;
    }
  }

  public MapReduceJob(LaunchedJob job,
                      TaskSummary taskSummary,
                      List<Counter> counters) {
    this.job = job;
    this.taskSummary = taskSummary;
    this.counters = counters;
  }

  public void setTaskSummary(TaskSummary taskSummary) {
    this.taskSummary = taskSummary;
  }

  public void setCounters(List<Counter> counters){
    this.counters = counters;
  }

  public LaunchedJob getJob() {
    return job;
  }

  public List<Counter> getCounters() {
    return counters;
  }

  public TaskSummary getTaskSummary() {
    return taskSummary;
  }
}
