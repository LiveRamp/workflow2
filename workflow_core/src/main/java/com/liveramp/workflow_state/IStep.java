package com.liveramp.workflow_state;

import java.util.Set;

import com.google.common.collect.Multimap;

public interface IStep<Dep> {

  public Multimap<DSAction, DataStoreInfo> getDataStores();

  public String getCheckpointToken();

  public String getActionClass();

  public Set<Dep> getDependencies();

  public Set<Dep> getChildren();

}
