package com.liveramp.workflow2.workflow_state.resources;

import java.io.IOException;

import com.liveramp.databases.workflow_db.IWorkflowDb;
import com.liveramp.databases.workflow_db.models.ResourceRoot;
import com.liveramp.cascading_ext.resource.RootDeterminer;
import com.rapleaf.jack.transaction.ITransactor;

public class TransactorStorageRootDeterminer implements RootDeterminer<ResourceRoot> {

  private final ITransactor<IWorkflowDb> workflowDbTransactor;

  public TransactorStorageRootDeterminer(ITransactor<IWorkflowDb> workflowDbTransactor) {
    this.workflowDbTransactor = workflowDbTransactor;
  }

  @Override
  public synchronized ResourceRoot getResourceRoot(long version, String versionType) throws IOException {
    return workflowDbTransactor.query(db -> BaseDbStorageRootDeterminer.getResourceRoot(version, versionType, db));
  }
}
