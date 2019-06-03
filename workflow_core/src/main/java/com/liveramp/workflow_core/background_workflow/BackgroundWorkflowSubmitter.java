package com.liveramp.workflow_core.background_workflow;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;

import com.google.common.collect.Sets;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import com.liveramp.workflow_core.BackgroundState;
import com.liveramp.workflow_core.BaseWorkflowOptions;
import com.liveramp.workflow_state.InitializedPersistence;
import com.liveramp.workflow_state.WorkflowStatePersistence;
import com.rapleaf.cascading_ext.workflow2.WorkflowDiagram;
import com.rapleaf.cascading_ext.workflow2.state.InitializedWorkflow;
import com.rapleaf.cascading_ext.workflow2.state.WorkflowPersistenceFactory;

public class BackgroundWorkflowSubmitter {

  public static <Initialized extends InitializedPersistence, Opts extends BaseWorkflowOptions<Opts>> WorkflowStatePersistence submit(
      InitializedWorkflow<BackgroundStep, Initialized, Opts> workflow,
      Collection<BackgroundStep> tails) throws IOException {

    //  recursively instantiate all MSs
    for (BackgroundStep step : WorkflowDiagram.getSubStepsFromTails(tails)) {
      step.build();
    }

    DirectedGraph<BackgroundStep, DefaultEdge> graph = WorkflowDiagram.dependencyGraphFromTailSteps(
        new BackgroundUnwrapper(),
        Sets.newHashSet(tails)
    );

    WorkflowDiagram.verifyUniqueCheckpointTokens(graph.vertexSet());
    WorkflowStatePersistence prepared = workflow.prepare(graph);

    //  it's party time
    prepared.markWorkflowStarted();

    return prepared;
  }

}
