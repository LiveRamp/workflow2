package com.liveramp.workflow2.workflow_state.resources;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liveramp.cascading_ext.resource.Storage;
import com.liveramp.databases.workflow_db.IWorkflowDb;
import com.liveramp.databases.workflow_db.models.ResourceRoot;

public class DbStorage implements Storage {

  private final IWorkflowDb workflowDb;
  private static final Logger LOG = LoggerFactory.getLogger(DbStorage.class);
  private final BaseDbStorage baseDbStorage;
  private ResourceRoot root;

  DbStorage(IWorkflowDb workflowDb, ResourceRoot root) {
    this.workflowDb = workflowDb;
    this.root = root;

    this.baseDbStorage = new BaseDbStorage(root);
  }

  public synchronized ResourceRoot getRoot() {
    return root;
  }

  @Override
  public <T> void store(String name, T object) {
    baseDbStorage.store(name, object, workflowDb);
  }

  @Override
  public <T> T retrieve(String name) {
    return baseDbStorage.retrieve(name, workflowDb);
  }

  @Override
  public boolean isStored(String name) {
    return baseDbStorage.isStored(name, workflowDb);
  }

  public static class Factory implements Storage.Factory<ResourceRoot> {

    private final DbResourceManager.WorkflowDbFactory workflowDbFactory;

    public Factory(DbResourceManager.WorkflowDbFactory workflowDbFactory) {
      this.workflowDbFactory = workflowDbFactory;
    }

    @Override
    public DbStorage forResourceRoot(ResourceRoot resourceRoot) throws IOException {
      return new DbStorage(workflowDbFactory.create(), resourceRoot);

    }

  }
}
