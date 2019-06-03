package com.liveramp.workflow.formatting;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.EdgeReversedGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

import com.liveramp.workflow_core.runner.BaseStep;
import com.liveramp.workflow_state.StepState;
import com.liveramp.workflow_state.WorkflowStatePersistence;

public class TimeFormatting {

  private static class Node {

    private Map<String, Node> children = Maps.newLinkedHashMap();
    private BaseStep terminal;

    public void insert(LinkedList<String> tokens, BaseStep step) {
      if (tokens.isEmpty()) {
        terminal = step;
      } else {
        String head = tokens.pop();
        if (!children.containsKey(head)) {
          children.put(head, new Node());
        }
        children.get(head).insert(tokens, step);
      }
    }

    public void print(String prefix, StringBuilder builder, Map<String, StepState> statuses) throws IOException {

      if(terminal != null){
        StepState state = statuses.get(terminal.getCheckpointToken());
        String duration = DurationFormatUtils.formatDurationWords(state.getEndTimestamp() - state.getStartTimestamp(), true, true);
        builder.append(duration).append(" (start: ").append(state.getStartTimestamp()).append(" end: ").append(state.getEndTimestamp()).append(")");
      }

      for (String key : children.keySet()) {
        builder.append("\n").append(prefix).append(key).append(":");
        children.get(key).print(prefix + "  ", builder, statuses);
      }
    }

  }

  public static <T> String getFormattedTimes(DirectedGraph<BaseStep<T>, DefaultEdge> g, WorkflowStatePersistence persistence) throws IOException {

    TopologicalOrderIterator<BaseStep<T>, DefaultEdge> orderIterator =
        new TopologicalOrderIterator<>(new EdgeReversedGraph<>(g));

    Node root = new Node();

    while (orderIterator.hasNext()) {
      BaseStep next = orderIterator.next();
      LinkedList<String> strings = Lists.newLinkedList(Arrays.asList(next.getCheckpointToken().split("__")));
      root.insert(strings, next);
    }

    StringBuilder toS = new StringBuilder();


    Map<String, StepState> statuses = persistence.getStepStates();
    root.print("", toS, statuses);

    return toS.toString();

  }
}
