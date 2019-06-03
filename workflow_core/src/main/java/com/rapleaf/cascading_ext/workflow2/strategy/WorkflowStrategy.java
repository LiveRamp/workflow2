package com.rapleaf.cascading_ext.workflow2.strategy;

import java.io.IOException;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import com.liveramp.commons.collections.properties.OverridableProperties;
import com.liveramp.workflow.types.StepStatus;
import com.liveramp.workflow_core.runner.BaseStep;
import com.liveramp.workflow_state.WorkflowStatePersistence;

public interface WorkflowStrategy<Config> {

  void markStepStart(WorkflowStatePersistence persistence, String token) throws IOException;

  void markStepCompleted(WorkflowStatePersistence persistence, String token) throws IOException;

  void markStepFailure(WorkflowStatePersistence persistence,
                       String checkpointToken,
                       Throwable e) throws IOException;

  Set<StepStatus> getFailureStatuses();

  Set<StepStatus> getNonBlockingStatuses();

  Set<StepStatus> getCompleteStatuses();

  Set<BaseStep<Config>> getDownstreamSteps(DirectedGraph<BaseStep<Config>, DefaultEdge> dependencyGraph,
                                           BaseStep<Config> baseStep);

  Set<BaseStep<Config>> getUpstreamSteps(DirectedGraph<BaseStep<Config>, DefaultEdge> dependencyGraph,
                                         BaseStep<Config> baseStep);


  void run(BaseStep<Config> step, OverridableProperties properties);

  void markAttemptStart(WorkflowStatePersistence persistence) throws IOException;


}
