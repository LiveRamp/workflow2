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

public class ExecuteStrategy<Config> implements WorkflowStrategy<Config> {

  @Override
  public void markStepStart(WorkflowStatePersistence persistence, String token) throws IOException {
    persistence.markStepRunning(token);
  }

  @Override
  public void markStepCompleted(WorkflowStatePersistence persistence, String token) throws IOException {
    persistence.markStepCompleted(token);
  }

  @Override
  public void markStepFailure(WorkflowStatePersistence persistence, String checkpointToken, Throwable e) throws IOException {
    persistence.markStepFailed(checkpointToken, e);
  }

  @Override
  public Set<StepStatus> getFailureStatuses() {
    return WorkflowEnums.FAILURE_STATUSES;
  }

  @Override
  public Set<StepStatus> getNonBlockingStatuses() {
    return WorkflowEnums.NON_BLOCKING_STEP_STATUSES;
  }

  @Override
  public Set<StepStatus> getCompleteStatuses() {
    return WorkflowEnums.COMPLETED_STATUSES;
  }

  @Override
  public Set<BaseStep<Config>> getDownstreamSteps(DirectedGraph<BaseStep<Config>, DefaultEdge> dependencyGraph, BaseStep<Config> baseStep) {
    Set<BaseStep<Config>> downstreamSteps = Sets.newHashSet();
    downstreamSteps.addAll(dependencyGraph.incomingEdgesOf(baseStep).stream().map(dependencyGraph::getEdgeSource).collect(Collectors.toList()));
    return downstreamSteps;
  }

  @Override
  public Set<BaseStep<Config>> getUpstreamSteps(DirectedGraph<BaseStep<Config>, DefaultEdge> dependencyGraph, BaseStep<Config> baseStep) {
    Set<BaseStep<Config>> upstreamSteps = Sets.newHashSet();
    upstreamSteps.addAll(dependencyGraph.outgoingEdgesOf(baseStep).stream().map(dependencyGraph::getEdgeTarget).collect(Collectors.toList()));
    return upstreamSteps;
  }

  @Override
  public void run(BaseStep<Config> step, OverridableProperties properties) {
    step.run(properties);
  }

  @Override
  public void markAttemptStart(WorkflowStatePersistence persistence) throws IOException {
    persistence.markWorkflowStarted();
  }

}
