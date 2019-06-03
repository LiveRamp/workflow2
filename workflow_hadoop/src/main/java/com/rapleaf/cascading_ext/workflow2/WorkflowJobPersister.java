package com.rapleaf.cascading_ext.workflow2;

import java.io.IOException;
import java.util.List;

import com.liveramp.cascading_ext.flow.JobPersister;
import com.liveramp.commons.collections.nested_map.TwoNestedMap;
import com.liveramp.commons.state.LaunchedJob;
import com.liveramp.commons.state.TaskSummary;
import com.liveramp.workflow_state.WorkflowStatePersistence;

public class WorkflowJobPersister implements JobPersister {

  private final WorkflowStatePersistence persistence;
  private final String checkpoint;
  private final List<CounterVerifier> verifiers;

  public interface CounterVerifier {
    public void verify(TwoNestedMap<String, String, Long> toRecord);
  }

  public WorkflowJobPersister(WorkflowStatePersistence persistence,
                              String checkpoint,
                              List<CounterVerifier> verifiers) {
    this.persistence = persistence;
    this.checkpoint = checkpoint;
    this.verifiers = verifiers;
  }

  @Override
  public void onRunning(LaunchedJob launchedJob) throws IOException {
    persistence.markStepRunningJob(
        checkpoint,
        launchedJob.getJobId(),
        launchedJob.getJobName(),
        launchedJob.getTrackingURL()
    );
  }

  @Override
  public void onTaskInfo(String jobID, TaskSummary summary) throws IOException {
    persistence.markJobTaskInfo(checkpoint, jobID, summary);
  }

  @Override
  public void onCounters(String jobID, TwoNestedMap<String, String, Long> counters) throws IOException {

    TwoNestedMap<String, String, Long> toRecord = new TwoNestedMap<>();
    for (String group : counters.key1Set()) {
      for (String name : counters.key2Set(group)) {
        toRecord.put(group, name, counters.get(group, name));
      }
    }

    for (CounterVerifier verifier : verifiers) {
      verifier.verify(toRecord);
    }

    persistence.markJobCounters(checkpoint, jobID, counters);
  }

}
