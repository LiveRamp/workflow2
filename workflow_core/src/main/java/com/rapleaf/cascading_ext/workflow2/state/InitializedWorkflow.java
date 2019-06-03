package com.rapleaf.cascading_ext.workflow2.state;

import java.util.List;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import com.liveramp.cascading_ext.resource.ResourceManager;
import com.liveramp.commons.collections.list.ListBuilder;
import com.liveramp.commons.util.MultiShutdownHook;
import com.liveramp.workflow_core.BaseWorkflowOptions;
import com.liveramp.workflow_state.IStep;
import com.liveramp.workflow_state.InitializedPersistence;
import com.liveramp.workflow_state.WorkflowStatePersistence;

public class InitializedWorkflow<S extends IStep, INITIALIZED extends InitializedPersistence, OPTS extends BaseWorkflowOptions> {

  private final String workflowName;
  private final OPTS options;
  private final INITIALIZED persistence;
  private final WorkflowPersistenceFactory<S, INITIALIZED, ?, ?> factory;
  private final ResourceManager manager;
  private final MultiShutdownHook hook;

  private final ScopedContext rootScope;

  public InitializedWorkflow(String workflowName,
                             OPTS options,
                             INITIALIZED reservedPersistence,
                             WorkflowPersistenceFactory<S, INITIALIZED, ?, ?> factory,
                             ResourceManager manager,
                             MultiShutdownHook hook) {

    this.workflowName = workflowName;
    this.options = options;
    this.persistence = reservedPersistence;
    this.factory = factory;
    this.manager = manager;
    this.hook = hook;

    this.rootScope = new ScopedContext(
        new ScopedName(Lists.newArrayList())
    );

  }

  public ResourceManager getManager() {
    return manager;
  }

  public String getWorkflowName() {
    return workflowName;
  }

  public OPTS getOptions() {
    return options;
  }

  public INITIALIZED getInitializedPersistence() {
    return persistence;
  }

  public WorkflowStatePersistence prepare(DirectedGraph<S, DefaultEdge> steps) {
    return factory.prepare(persistence, steps);
  }

  public MultiShutdownHook getShutdownHook() {
    return hook;
  }

  public ScopedContext getRootScope() {
    return this.rootScope;
  }

  public static class ScopedContext {

    private final ScopedName scopedName;

    private ScopedContext(ScopedName scopedName) {
      this.scopedName = scopedName;
    }

    public ScopedContext scope(String scope) {
      return new ScopedContext(
          scopedName.scope(scope)
      );
    }

    public ScopedName getScopedName() {
      return scopedName;
    }

  }

  public static class ScopedName {
    private static final String SEPARATOR = "__";


    private final List<String> currentName;

    private ScopedName(List<String> name) {
      this.currentName = name;
    }

    public ScopedName scope(String scope) {

      if (scope.contains(SEPARATOR)) {
        throw new IllegalArgumentException("Step token cannot include nesting separator");
      }

      return new ScopedName(new ListBuilder<String>()
          .addAll(currentName)
          .add(scope)
          .get());
    }

    public String getName() {
      return StringUtils.join(currentName, SEPARATOR);
    }

  }

}
