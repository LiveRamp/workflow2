package com.rapleaf.cascading_ext.workflow2.strategy;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import com.liveramp.commons.collections.properties.OverridableProperties;
import com.liveramp.workflow.types.StepStatus;
import com.liveramp.workflow_core.WorkflowEnums;
import com.liveramp.workflow_core.runner.BaseStep;
import com.liveramp.workflow_state.WorkflowStatePersistence;

public class RollbackStrategy<Config> implements WorkflowStrategy<Config> {

  @Override
  public void markStepStart(WorkflowStatePersistence persistence, String token) throws IOException {
    persistence.markStepRollingBack(token);
  }

  @Override
  public void markStepCompleted(WorkflowStatePersistence persistence, String token) throws IOException {
    persistence.markStepRolledBack(token);
  }

  @Override
  public void markStepFailure(WorkflowStatePersistence persistence, String checkpointToken, Throwable e) throws IOException {
    persistence.markStepRollbackFailure(checkpointToken, e);
  }

  @Override
  public Set<StepStatus> getFailureStatuses() {
    return WorkflowEnums.FAILURE_ROLLBACK_STATUSES;
  }

  @Override
  public Set<StepStatus> getNonBlockingStatuses() {
    return WorkflowEnums.NON_BLOCKING_ROLLBACK_STATUSES;
  }

  @Override
  public Set<StepStatus> getCompleteStatuses() {
    return WorkflowEnums.COMPLETE_ROLLBACK_STATUSES;
  }

  @Override
  public Set<BaseStep<Config>> getUpstreamSteps(DirectedGraph<BaseStep<Config>, DefaultEdge> dependencyGraph, BaseStep<Config> baseStep) {
    Set<BaseStep<Config>> upstreamSteps = Sets.newHashSet();
    upstreamSteps.addAll(dependencyGraph.incomingEdgesOf(baseStep).stream().map(dependencyGraph::getEdgeSource).collect(Collectors.toList()));
    return upstreamSteps;
  }

  @Override
  public Set<BaseStep<Config>> getDownstreamSteps(DirectedGraph<BaseStep<Config>, DefaultEdge> dependencyGraph, BaseStep<Config> baseStep) {
    Set<BaseStep<Config>> downstreamSteps = Sets.newHashSet();
    downstreamSteps.addAll(dependencyGraph.outgoingEdgesOf(baseStep).stream().map(dependencyGraph::getEdgeTarget).collect(Collectors.toList()));
    return downstreamSteps;
  }

  @Override
  public void run(BaseStep<Config> step, OverridableProperties properties) {
    step.rollback(properties);
  }

  @Override
  public void markAttemptStart(WorkflowStatePersistence persistence) throws IOException {
    persistence.markRollbackStarted();
  }

}
