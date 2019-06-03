package com.liveramp.workflow_core.background_workflow;

import java.io.Serializable;
import java.util.Set;

import com.google.common.collect.Multimap;
import org.apache.commons.lang.NotImplementedException;

import com.liveramp.workflow_state.DSAction;
import com.liveramp.workflow_state.DataStoreInfo;
import com.rapleaf.cascading_ext.workflow2.WorkflowDiagram;
import com.rapleaf.cascading_ext.workflow2.state.InitializedWorkflow;

public class MultiStep<Config, Context extends Serializable> extends BackgroundStep {

  private final StepAssembly<Config, Context> constructor;
  private Set<BackgroundStep> subSteps;

  public MultiStep(
      InitializedWorkflow.ScopedContext scope,
      String stepName,
      Context context,
      StepAssembly<Config, Context> constructor,
      Set<BackgroundStep> dependencies) {
    super(scope, stepName, null, context, null, dependencies);

    this.constructor = constructor;
  }

  @Override
  public Multimap<DSAction, DataStoreInfo> getDataStores() {
    throw new NotImplementedException();
  }

  @Override
  public String getActionClass() {
    throw new NotImplementedException();
  }

  public interface StepAssembly<Config, Context extends Serializable> {
    Set<BackgroundStep> constructTails(InitializedWorkflow.ScopedContext context);
  }

  public Set<BackgroundStep> getSubSteps() {
    return subSteps;
  }

  public Set<BackgroundStep> getTails(){
    return WorkflowDiagram.getTails(subSteps);
  }

  public Set<BackgroundStep> getHeads(){
    return WorkflowDiagram.getHeads(subSteps);
  }

  public void build() {
    Set<BackgroundStep> subSteps = WorkflowDiagram.getSubStepsFromTails(constructor.constructTails(getScopedContext()));

    for (BackgroundStep subStep : subSteps) {
      if(subStep instanceof MultiStep) {
        MultiStep<Config, Context> childMulti = (MultiStep<Config, Context>) subStep;
        childMulti.build();
      }
    }

    this.subSteps = subSteps;
  }

}
