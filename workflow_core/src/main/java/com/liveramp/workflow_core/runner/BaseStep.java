package com.liveramp.workflow_core.runner;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import com.liveramp.commons.collections.nested_map.TwoNestedMap;
import com.liveramp.commons.collections.properties.OverridableProperties;
import com.liveramp.workflow_state.DSAction;
import com.liveramp.workflow_state.DataStoreInfo;
import com.liveramp.workflow_state.IStep;


/**
 * Provides the base class for a workflow "step," the fundamental unit
 * of progress for the frameworks that execute composite work graphs.
 * Each workflow step contains an action ({@link BaseAction}) that provides
 * the actual work to be done.  The step supplies the node in the workflow
 * graph, while the action supplies the code to be executed.
 *
 * Additionally, a step may have a set of children (see {@link #addChild})
 * and a set of dependencies (supplied during construction).  The workflow
 * controls the execution of both children and dependencies.
 *
 * @param <Config>
 *   The underlying configuration for this step.  Typically, this will be
 *   a {@link Collection} of key-value pairs.  The actual configuration
 *   object may be given to the underlying actions, which may choose to
 *   alter their behavior based on the configuration values.
 */
public class BaseStep<Config> implements IStep<BaseStep<Config>>  {

  private final BaseAction<Config> action;
  private final Set<BaseStep<Config>> dependencies;
  private final Set<BaseStep<Config>> children;

  public BaseStep(BaseAction<Config> action) {
    this(action, Lists.newArrayList());
  }

  public BaseStep(BaseAction<Config> action, BaseStep<Config> dependency) {
    this(action, Lists.newArrayList(dependency));
  }

  public BaseStep(BaseAction<Config> action, BaseStep<Config> dependency1, BaseStep<Config> dependency2) {
    this(action, Lists.newArrayList(dependency1, dependency2));
  }

  public BaseStep(BaseAction<Config> action, BaseStep<Config> dependency1, BaseStep<Config> dependency2, BaseStep<Config> dependency3) {
    this(action, Lists.newArrayList(dependency1, dependency2, dependency3));
  }

  public BaseStep(BaseAction<Config> action, BaseStep<Config>... dependencies) {
    this(action, Arrays.asList(dependencies));
  }

  public BaseStep(BaseAction<Config> action, Collection<? extends BaseStep<Config>> dependencies) {
    this.action = action;
    children = new HashSet<BaseStep<Config>>();
    this.dependencies = new HashSet<>(dependencies);
    if (this.dependencies.contains(null)) {
      throw new NullPointerException("null cannot be a dependency for a step!");
    }
    for (BaseStep dependency : this.dependencies) {
      dependency.addChild(this);
    }
  }

  private void addChild(BaseStep child) {
    if (!child.getDependencies().contains(this)) {
      throw new RuntimeException("child (" + child + ") does not depend on this (" + this + ")");
    }
    children.add(child);
  }

  @Override
  public Set<BaseStep<Config>> getChildren() {
    return Collections.unmodifiableSet(children);
  }

  @Override
  public Set<BaseStep<Config>> getDependencies() {
    return Collections.unmodifiableSet(dependencies);
  }

  public BaseAction getAction() {
    return action;
  }

  @Override
  public Multimap<DSAction, DataStoreInfo> getDataStores() {
    return action.getAllDataStoreInfo();
  }

  @Override
  public String getCheckpointToken() {
    return getAction().fullId();
  }

  @Override
  public String getActionClass() {
    return getAction().getClass().getName();
  }

  public String getSimpleCheckpointToken() {
    return getAction().getActionId().getRelativeName();
  }

  @Override
  public String toString() {
    return "Step " + getCheckpointToken() + " " + action + " deps=" + dependencies;
  }

  public Callable<TwoNestedMap<String, String, Long>> getCountersFuture() throws IOException {
    return new Callable<TwoNestedMap<String, String, Long>>() {
      @Override
      public TwoNestedMap<String, String, Long> call() throws Exception {
        return action.getCurrentStepCounters();
      }
    };
  }

  public Callable<BaseAction.DurationInfo> getDurationFuture() {
    return new Callable<BaseAction.DurationInfo>() {
      @Override
      public BaseAction.DurationInfo call() throws Exception {
        return action.getDurationInfo();
      }
    };
  }

  public void run(OverridableProperties properties) {
    action.internalExecute(properties);
  }

  public void rollback(OverridableProperties properties){
    action.internalRollback(properties);
  }

}
