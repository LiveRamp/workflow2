package com.liveramp.workflow_core.background_workflow;

import java.io.Serializable;
import java.time.Duration;
import java.util.Set;

import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import com.liveramp.workflow_state.DSAction;
import com.liveramp.workflow_state.DataStoreInfo;
import com.liveramp.workflow_state.IStep;
import com.rapleaf.cascading_ext.workflow2.state.InitializedWorkflow;

//  just enforce that all steps are background steps
public class BackgroundStep implements IStep<BackgroundStep> {

  private final Serializable context;
  private final InitializedWorkflow.ScopedContext scopedContext;
  private final Class<? extends BackgroundAction<? extends Serializable>> actionClass;
  private final Duration prereqCheckCooldown;

  private final Set<BackgroundStep> dependencies;
  private final Set<BackgroundStep> children;

  public <Context extends Serializable> BackgroundStep(
      InitializedWorkflow.ScopedContext scopedContext,
      String checkpointToken,
      Class<? extends BackgroundAction<Context>> actionClass,
      Context context) {
    this(scopedContext, checkpointToken, actionClass, context, Duration.ofMinutes(5), Sets.newHashSet());
  }

  public <Context extends Serializable> BackgroundStep(
      InitializedWorkflow.ScopedContext scopedContext,
      String checkpointToken,
      Class<? extends BackgroundAction<Context>> actionClass,
      Context context,
      Duration prereqCheckCooldown,
      Set<BackgroundStep> dependencies) {

    this.scopedContext = scopedContext.scope(checkpointToken);
    this.actionClass = actionClass;
    this.context = context;
    this.dependencies = dependencies;
    this.prereqCheckCooldown = prereqCheckCooldown;
    this.children = Sets.newHashSet();

    if (this.dependencies.contains(null)) {
      throw new NullPointerException("null cannot be a dependency for a step!");
    }

    if(context == null){
      throw new IllegalArgumentException();
    }

    for (BackgroundStep dependency : this.dependencies) {
      dependency.addChild(this);
    }
  }

  protected InitializedWorkflow.ScopedContext getScopedContext() {
    return this.scopedContext;
  }

  private transient BackgroundAction<? extends Serializable> instantiatedAction;

  private BackgroundAction<? extends Serializable> instantiate() {
    try {

      if (instantiatedAction == null) {
        instantiatedAction = actionClass.newInstance();
        instantiatedAction.initializeContext(context);
      }

      return instantiatedAction;

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public Duration getPrereqCheckCooldown() {
    return prereqCheckCooldown;
  }

  public PreconditionFunction<? extends Serializable> getPrecondition() {
    return instantiate().getAllowExecute();
  }

  @Override
  public Multimap<DSAction, DataStoreInfo> getDataStores() {
    return instantiate().getAllDataStoreInfo();
  }

  @Override
  public String getCheckpointToken() {
    return scopedContext.getScopedName().getName();
  }

  @Override
  public String getActionClass() {
    return actionClass.getName();
  }

  @Override
  public Set<BackgroundStep> getDependencies() {
    return dependencies;
  }

  @Override
  public Set getChildren() {
    return children;
  }

  public void addChild(BackgroundStep child) {
    this.children.add(child);
  }

  public void build() {
  }

  public Serializable getContext() {
    return context;
  }

  @Override
  public String toString() {
    return "BackgroundStep{" +
        "scopedContext=" + scopedContext +
        '}';
  }
}
