package com.liveramp.workflow2.workflow_state.resources;

import java.io.IOException;

import com.liveramp.cascading_ext.resource.Storage;
import com.liveramp.databases.workflow_db.IWorkflowDb;
import com.liveramp.databases.workflow_db.models.ResourceRoot;
import com.rapleaf.jack.transaction.ITransactor;

public class TransactorStorage implements Storage {

  private final ITransactor<IWorkflowDb> workflowDbTransactor;
  private final ResourceRoot root;

  private final BaseDbStorage baseDbStorage;

  TransactorStorage(ITransactor<IWorkflowDb> workflowDbTransactor, ResourceRoot root) {
    this.workflowDbTransactor = workflowDbTransactor;
    this.root = root;

    this.baseDbStorage = new BaseDbStorage(root);
  }

  @Override
  public synchronized <T> void store(String name, T object) {
    workflowDbTransactor.executeAsTransaction(db -> baseDbStorage.store(name, object, db));
  }

  @Override
  public synchronized <T> T retrieve(String name) {
    return workflowDbTransactor.query(db -> baseDbStorage.retrieve(name, db));
  }

  @Override
  public synchronized boolean isStored(String name) {
    return workflowDbTransactor.query(db -> baseDbStorage.isStored(name, db));
  }

  public synchronized ResourceRoot getRoot() {
    return root;
  }

  public static class Factory implements Storage.Factory<ResourceRoot> {

    private final TransactorResourceManager.TransactorFactory transactorFactory;

    public Factory(TransactorResourceManager.TransactorFactory workflowDbFactory) {
      this.transactorFactory = workflowDbFactory;
    }

    @Override
    public TransactorStorage forResourceRoot(ResourceRoot resourceRoot) throws IOException {
      return new TransactorStorage(transactorFactory.create(), resourceRoot);
    }
  }
}
