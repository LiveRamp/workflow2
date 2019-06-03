package com.liveramp.workflow_core;

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Sets;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liveramp.workflow_core.runner.BaseMultiStepAction;
import com.liveramp.workflow_core.runner.BaseStep;
import com.liveramp.workflow_state.IStep;

public class WorkflowUtil {
  private static final Logger LOG = LoggerFactory.getLogger(com.liveramp.workflow_core.WorkflowUtil.class);

  public static <Type extends IStep> void removeRedundantEdges(DirectedGraph<Type, DefaultEdge> graph) {
    for (Type step : graph.vertexSet()) {
      Set<Type> firstDegDeps = new HashSet<>();
      Set<Type> secondPlusDegDeps = new HashSet<>();
      for (DefaultEdge edge : graph.outgoingEdgesOf(step)) {
        Type depStep = graph.getEdgeTarget(edge);
        firstDegDeps.add(depStep);
        getDepsRecursive(depStep, secondPlusDegDeps, graph);
      }

      for (Type firstDegDep : firstDegDeps) {
        if (secondPlusDegDeps.contains(firstDegDep)) {
          LOG.debug("Found a redundant edge from " + step.getCheckpointToken()
              + " to " + firstDegDep.getCheckpointToken());
          graph.removeAllEdges(step, firstDegDep);
        }
      }
    }
  }

  private static <Type extends IStep> void getDepsRecursive(Type step, Set<Type> deps, DirectedGraph<Type, DefaultEdge> graph) {
    for (DefaultEdge edge : graph.outgoingEdgesOf(step)) {
      Type s = graph.getEdgeTarget(edge);
      boolean isNew = deps.add(s);
      if (isNew) {
        getDepsRecursive(s, deps, graph);
      }
    }
  }


  public static void setCheckpointPrefixes(Set<? extends BaseStep> tailSteps) {
    Set<String> explored = Sets.newHashSet();
    for (BaseStep tailStep : tailSteps) {
      setCheckpointPrefixes(tailStep, "", explored);
    }
  }

  private static <T> void setCheckpointPrefixes(BaseStep<T> step, String prefix, Set<String> explored) {

    step.getAction().getActionId().setParentPrefix(prefix);
    String resolved = step.getCheckpointToken();

    if (explored.contains(resolved)) {
      return;
    }

    if(step.getAction() instanceof BaseMultiStepAction){
      BaseMultiStepAction<T> msa = (BaseMultiStepAction) step.getAction();

      for (BaseStep<T> tail : msa.getTailSteps()) {
        setCheckpointPrefixes(tail, resolved + "__", explored);
      }

    }

    for (BaseStep dep: step.getDependencies()) {
      setCheckpointPrefixes(dep, prefix, explored);
    }

  }

}
