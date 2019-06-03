package com.rapleaf.cascading_ext.workflow2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import com.google.common.base.Joiner;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import com.liveramp.workflow_state.IStep;


public class WorkflowDiagram {

  public interface GraphUnwrapper<Base extends IStep<Base>, Multi> {
    Multi getMultiNode(Base node);

    Set<Base> getMultiSubSteps(Multi multi);

    Set<Base> getTailSteps(Multi multi);

    Set<Base> getHeadSteps(Multi multi);
  }


  public static <Base extends IStep<Base>, Multi> DirectedGraph<Base, DefaultEdge> dependencyGraphFromTailSteps(
      GraphUnwrapper<Base, Multi> expander,
      Set<Base> tailSteps) {
    verifyNoOrphanedTailSteps(expander, tailSteps);
    return dependencyGraphFromTailStepsNoVerification(expander, tailSteps);
  }


  private static <Base extends IStep<Base>, Multi> DirectedGraph<Base, DefaultEdge> dependencyGraphFromTailStepsNoVerification(
      GraphUnwrapper<Base, Multi> expander,
      Set<Base> tailSteps) {

    Set<Base> tailsAndDependencies = addDependencies(tailSteps);
    DirectedGraph<Base, DefaultEdge> dependencyGraph = createGraph(expander, tailsAndDependencies);

    Queue<Base> multiSteps = new LinkedList<>(filterMultiStep(expander, tailsAndDependencies));

    // now, proceed through each MultiStepAction node in the dependency graph,
    // recursively flattening it out and merging it into the current dependency
    // graph.

    while (!multiSteps.isEmpty()) {
      Base s = multiSteps.poll();
      // If the dependency graph doesn't contain the vertex, then we've already
      // processed it
      if (dependencyGraph.containsVertex(s)) {

        Multi multiNode = expander.getMultiNode(s);
        pullUpSubstepsAndAdjustCheckpointTokens(expander, dependencyGraph, multiSteps, multiNode);

        // now that the dep graph contains the unwrapped multistep *and* the
        // original multistep, let's move the edges to the unwrapped stuff so that
        // we can remove the multistep.

        copyIncomingEdges(expander, dependencyGraph, s, multiNode);

        copyOutgoingEdges(expander, dependencyGraph, s, multiNode);

        // finally, the multistep's vertex should be removed.
        dependencyGraph.removeVertex(s);
      }
    }

    return dependencyGraph;
  }

  private static <Base extends IStep<Base>, Multi> DirectedGraph<Base, DefaultEdge> createGraph(
      GraphUnwrapper<Base, Multi> graphUnwrapper,
      Set<Base> tailsAndDependencies) {
    DirectedGraph<Base, DefaultEdge> dependencyGraph = new SimpleDirectedGraph<>(DefaultEdge.class);
    for (Base step : tailsAndDependencies) {
      addStepAndDependencies(graphUnwrapper, dependencyGraph, step);
    }
    return dependencyGraph;
  }

  public static <Base extends IStep<Base>, Multi> void verifyNoOrphanedTailSteps(GraphUnwrapper<Base, Multi> unwrapper, Set<Base> tailSteps) {
    Set<Base> orphans = getOrphanedTailSteps(unwrapper, tailSteps);
    if (orphans.size() != 0) {
      throw new RuntimeException("Orphaned tail steps:\n" + Joiner.on("\n").join(orphans));
    }
  }

  public static <Base extends IStep<Base>, Multi> Set<Base> reachableSteps(GraphUnwrapper<Base, Multi> unwrapper, Set<Base> steps) {
    Set<Base> reachableSteps = new HashSet<>();
    Stack<Base> toProcess = new Stack<>();
    toProcess.addAll(steps);
    while (!toProcess.isEmpty()) {
      Base step = toProcess.pop();
      if (!reachableSteps.contains(step)) {
        reachableSteps.add(step);
        toProcess.addAll(step.getChildren());
        toProcess.addAll(step.getDependencies());
      }
    }
    return reachableSteps;
  }

  static <Base extends IStep<Base>, Multi> Set<Base> getOrphanedTailSteps(GraphUnwrapper<Base, Multi> unwrapper, Set<Base> tailSteps) {
    return getOrphanedTailSteps(
        unwrapper,
        dependencyGraphFromTailStepsNoVerification(unwrapper, tailSteps),
        reachableSteps(unwrapper, tailSteps)
    );
  }

  private static <Base extends IStep<Base>, Multi> Set<Base> getOrphanedTailSteps(GraphUnwrapper<Base, Multi> unwrapper, DirectedGraph<Base, DefaultEdge> dependencyGraph, Set<Base> allSteps) {
    Set<Base> tailSteps = new HashSet<>();
    for (Base step : allSteps) {

      if (unwrapper.getMultiNode(step) == null) {
        for (Base child : step.getChildren()) {
          if (!dependencyGraph.containsVertex(child) && unwrapper.getMultiNode(child) == null) {
            tailSteps.add(child);
          }
        }
      }

    }
    return tailSteps;
  }

  public static <T> void verifyUniqueCheckpointTokens(Iterable<? extends IStep> steps) {
    Set<String> tokens = new HashSet<>();
    for (IStep step : steps) {
      String token = step.getCheckpointToken();
      if (tokens.contains(token)) {
        throw new IllegalArgumentException(step.toString() + " has a non-unique checkpoint token!");
      }
      tokens.add(token);
    }
  }

  private static <Base extends IStep<Base>, Multi> Set<Base> addDependencies(Set<Base> steps) {
    Queue<Base> toProcess = new LinkedList<>(steps);
    Set<Base> visited = new HashSet<>();
    while (!toProcess.isEmpty()) {
      Base step = toProcess.poll();
      if (!visited.contains(step)) {
        visited.add(step);
        toProcess.addAll(step.getDependencies());
      }
    }
    return visited;
  }

  private static <Base extends IStep<Base>, Multi> Set<Base> filterMultiStep(GraphUnwrapper<Base, Multi> unwrapper, Iterable<Base> steps) {
    Set<Base> multiSteps = new HashSet<>();
    for (Base step : steps) {

      Multi multi = unwrapper.getMultiNode(step);
      if (multi != null) {
        multiSteps.add(step);
      }

    }
    return multiSteps;
  }

  private static <Base extends IStep<Base>, Multi> void copyOutgoingEdges(GraphUnwrapper<Base, Multi> unwrapper, DirectedGraph<Base, DefaultEdge> dependencyGraph, Base s, Multi msa) {
    // next, the head steps of this multistep, which are naturally dependent
    // upon nothing, should depend on all the dependencies of the multistep
    for (DefaultEdge dependedUponEdge : dependencyGraph.outgoingEdgesOf(s)) {
      Base dependedUpon = dependencyGraph.getEdgeTarget(dependedUponEdge);
      for (Base headStep : unwrapper.getHeadSteps(msa)) {
        dependencyGraph.addVertex(headStep);
        dependencyGraph.addVertex(dependedUpon);
        dependencyGraph.addEdge(headStep, dependedUpon);
      }
    }
  }

  private static <Base extends IStep<Base>, Multi> void copyIncomingEdges(GraphUnwrapper<Base, Multi> unwrapper, DirectedGraph<Base, DefaultEdge> dependencyGraph, Base s, Multi msa) {
    // anyone who was dependent on this multistep should instead be
    // dependent on the tail steps of the multistep
    for (DefaultEdge dependsOnThis : dependencyGraph.incomingEdgesOf(s)) {
      Base edgeSource = dependencyGraph.getEdgeSource(dependsOnThis);
      for (Base tailStep : unwrapper.getTailSteps(msa)) {
        dependencyGraph.addVertex(tailStep);
        dependencyGraph.addEdge(edgeSource, tailStep);
      }
    }
  }

  private static <Base extends IStep<Base>, Multi> void pullUpSubstepsAndAdjustCheckpointTokens(
      GraphUnwrapper<Base, Multi> unwrapper,
      DirectedGraph<Base, DefaultEdge> dependencyGraph,
      Queue<Base> multiSteps,
      Multi msa) {
    // take all the substeps out of the multistep and put them into the top
    // level dependency graph, making sure to add their dependencies.
    // this is certain to visit some vertices repeatedly, and could probably
    // be done more elegantly with a tail-first traversal.
    // TODO, perhaps?

    for (Base substep : unwrapper.getMultiSubSteps(msa)) {

      // if we encounter another multistep, put it on the queue to be expanded
      Multi multiNode = unwrapper.getMultiNode(substep);
      if (multiNode != null) {
        multiSteps.add(substep);
      }

      addStepAndDependencies(unwrapper, dependencyGraph, substep);
    }
  }

  private static <Base extends IStep<Base>, Multi> void addStepAndDependencies(
      GraphUnwrapper<Base, Multi> unwrapper,
      DirectedGraph<Base, DefaultEdge> dependencyGraph,
      Base step) {
    dependencyGraph.addVertex(step);
    for (Base dep : step.getDependencies()) {
      dependencyGraph.addVertex(dep);
      dependencyGraph.addEdge(step, dep);
    }
  }

  public static <Dep extends IStep<Dep>> Set<Dep> getSubStepsFromTails(Collection<? extends Dep> tails) {
    Set<Dep> steps = new HashSet<>(tails);
    List<Dep> queue = new ArrayList<>(tails);
    int index = 0;
    while (index < queue.size()) {
      Dep curStep = queue.get(index);
      Set<Dep> deps = curStep.getDependencies();
      for (Dep curDep : deps) {
        if (!steps.contains(curDep)) {
          steps.add(curDep);
          queue.add(curDep);
        }
      }
      index++;
    }
    return steps;
  }

  public static <Dep extends IStep<Dep>> Set<Dep> getHeads(Collection<? extends Dep> steps) {
    Set<Dep> heads = new HashSet<>();
    for (Dep s : steps) {
      if (s.getDependencies().isEmpty()) {
        heads.add(s);
      }
    }
    return heads;
  }

  public static <Dep extends IStep<Dep>> Set<Dep> getTails(Collection<? extends Dep> steps){
    Set<Dep> possibleTails = new HashSet<>(steps);
    for (Dep s : steps) {
      possibleTails.removeAll(s.getDependencies());
    }
    return possibleTails;
  }


}
