package com.liveramp.workflow2.workflow_state.resources;

import java.io.IOException;

import com.liveramp.databases.workflow_db.IWorkflowDb;
import com.liveramp.databases.workflow_db.models.ResourceRoot;
import com.liveramp.cascading_ext.resource.RootDeterminer;

public class DbStorageRootDeterminer implements RootDeterminer<ResourceRoot> {

  private final IWorkflowDb workflowDb;

  public DbStorageRootDeterminer(IWorkflowDb workflowDb) {
    this.workflowDb = workflowDb;
  }

  @Override
  public synchronized ResourceRoot getResourceRoot(long version, String versionType) throws IOException {
    return BaseDbStorageRootDeterminer.getResourceRoot(version, versionType, workflowDb);
  }
}
