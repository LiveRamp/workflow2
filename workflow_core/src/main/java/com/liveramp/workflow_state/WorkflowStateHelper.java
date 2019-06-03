package com.liveramp.workflow_state;

import java.io.IOException;
import java.util.stream.Stream;

import com.liveramp.commons.state.TaskFailure;

public class WorkflowStateHelper {

  public static Stream<TaskFailure> getTaskFailures(WorkflowStatePersistence state) throws IOException {
    return state.getStepStates().values().stream()
        .flatMap(s -> s.getMrJobsByID().values().stream())
        .map(s -> s.getTaskSummary())
        .flatMap(ts -> ts.getTaskFailures().stream());
  }

}
