package com.liveramp.workflow_core.runner;

import java.util.Set;

import com.google.common.collect.Multimap;

import com.liveramp.workflow_state.DSAction;
import com.liveramp.workflow_state.DataStoreInfo;
import com.liveramp.workflow_state.IStep;

public class BaseMultiStep<Config> implements ExecutionNode<Config>{

//  public BaseMultiStep<Config>()

  @Override
  public Set<ExecutionNode<Config>> getDependencies() {
    return null;
  }
}
