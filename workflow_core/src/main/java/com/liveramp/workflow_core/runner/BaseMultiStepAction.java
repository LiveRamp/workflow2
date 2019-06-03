package com.liveramp.workflow_core.runner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import org.jetbrains.annotations.NotNull;

import com.liveramp.cascading_ext.resource.ReadResource;
import com.liveramp.cascading_ext.resource.WriteResource;
import com.liveramp.commons.collections.nested_map.TwoNestedCountingMap;
import com.liveramp.commons.collections.nested_map.TwoNestedMap;
import com.liveramp.workflow_core.step.NoOp;
import com.liveramp.workflow_state.IStep;
import com.rapleaf.cascading_ext.workflow2.WorkflowDiagram;

public class BaseMultiStepAction<Config> extends BaseAction<Config> {

  private Collection<? extends BaseStep<Config>> steps;


  public BaseMultiStepAction(String checkpointToken) {
    this(checkpointToken, null);
  }

  public BaseMultiStepAction(String checkpointToken, Collection<? extends BaseStep<Config>> steps) {
    super(checkpointToken);
    setSubSteps(steps);
  }

  /**
   * This method will never actually get called, since MultiStepAction is just a
   * placeholder for the workflow planner to expand later.
   */
  @Override
  public final void execute() {
    throw new IllegalStateException("planner error: method should never be called!");
  }

  public final void setSubSteps(Collection<? extends BaseStep<Config>> steps) {
    Set<String> tokens = new HashSet<>();
    if (steps == null) {
      return;
    }
    for (BaseStep<Config> s : steps) {
      if (tokens.contains(s.getSimpleCheckpointToken())) {
        throw new IllegalArgumentException("Substep checkpoint token " + s.getSimpleCheckpointToken()
            + " is used more than once in " + this);
      }
      tokens.add(s.getSimpleCheckpointToken());
    }

    //  all hell will break loose if there are no steps in the MSA, once it gets decomposed into steps (it will get spliced from the dep graph)
    //  give a placeholder so we propagate dependencies forward
    if (steps.isEmpty()) {
      this.steps = Sets.newHashSet(new BaseStep<Config>(new NoOp("empty-msa-placeholder")));
    } else {
      this.steps = steps;
    }

  }

  protected final void setSubStepsFromTail(BaseStep<Config> tail) {
    setSubStepsFromTails(Collections.singleton(tail));
  }

  protected final void setSubStepsFromTails(BaseStep<Config> tail1) {
    setSubStepsFromTails(Arrays.asList(tail1));
  }

  protected final void setSubStepsFromTails(BaseStep<Config> tail1, BaseStep<Config> tail2) {
    setSubStepsFromTails(Arrays.asList(tail1, tail2));
  }

  protected final void setSubStepsFromTails(BaseStep<Config>... tails) {
    setSubStepsFromTails(Arrays.asList(tails));
  }

  public Set<BaseStep<Config>> getSubSteps() {
    verifyStepsAreSet();
    return new HashSet<>(steps);
  }

  protected final void setSubStepsFromTails(Collection<? extends BaseStep<Config>> tails) {
    setSubSteps(WorkflowDiagram.getSubStepsFromTails(tails));
  }

  public Set<BaseStep<Config>> getHeadSteps() {
    verifyStepsAreSet();
    return WorkflowDiagram.getHeads(steps);
  }

  public Set<BaseStep<Config>> getTailSteps() {
    verifyStepsAreSet();
    return WorkflowDiagram.getTails(steps);
  }

  private void verifyStepsAreSet() {
    if (steps == null) {
      throw new RuntimeException(
          "Steps in a multi-step action must be set before thay can be used!");
    }
  }

  @Override
  public TwoNestedMap<String, String, Long> getCurrentStepCounters() throws IOException {
    TwoNestedCountingMap<String, String> map = new TwoNestedCountingMap<>(0L);
    for (BaseStep<Config> step : steps) {
      map.incrementAll(step.getAction().getCurrentStepCounters());
    }
    return map;
  }

  @Override
  public BaseAction.DurationInfo getDurationInfo() throws IOException {

    long minStart = Long.MAX_VALUE;
    long maxEnd = Long.MIN_VALUE;

    for (BaseStep<Config> step : steps) {
      BaseAction.DurationInfo durationInfo = step.getAction().getDurationInfo();
      minStart = Math.min(minStart, durationInfo.getStartTime());
      maxEnd = Math.max(maxEnd, durationInfo.getEndTime());
    }

    return new BaseAction.DurationInfo(minStart, maxEnd);
  }

  protected <T> T get(ReadResource<T> resource) {
    throw new RuntimeException("Cannot get a resource in a multistep action!");
  }

  protected <T, R extends WriteResource<T>> void set(R resource, T value) {
    throw new RuntimeException("Cannot set a resource in a multistep action!");
  }

  @Override
  protected void setStatusCallback(StatusCallback callback) throws IOException {
    throw new RuntimeException("Cannot set a status message in a multistep action!");
  }

  @Override
  protected void setStatusMessage(String message) {
    throw new RuntimeException("Cannot set a status message in a multistep action!");
  }
}
