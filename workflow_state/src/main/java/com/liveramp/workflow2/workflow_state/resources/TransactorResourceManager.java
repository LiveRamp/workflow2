package com.liveramp.workflow2.workflow_state.resources;

import java.io.IOException;

import com.liveramp.cascading_ext.resource.ResourceDeclarer;
import com.liveramp.cascading_ext.resource.ResourceDeclarerContainer;
import com.liveramp.cascading_ext.resource.RootManager;
import com.liveramp.databases.workflow_db.DatabasesImpl;
import com.liveramp.databases.workflow_db.IWorkflowDb;
import com.rapleaf.jack.transaction.ITransactor;

public class TransactorResourceManager {

  public static ResourceDeclarer create(TransactorFactory factory) throws IOException {
    return new ResourceDeclarerContainer<>(
        new ResourceDeclarerContainer.MethodNameTagger(),
        new RootManager<>(
            new TransactorStorageRootDeterminer(factory.create()),
            transactorStorage(factory))
    );
  }

  public static TransactorStorage.Factory transactorStorage(TransactorFactory transactor) {
    return new TransactorStorage.Factory(transactor);
  }

  public interface TransactorFactory {
    ITransactor<IWorkflowDb> create();

    public static class Default implements TransactorFactory {
      @Override
      public ITransactor<IWorkflowDb> create() {
        return new DatabasesImpl().getWorkflowDbTransactor().get();
      }
    }

    public static class Static implements TransactorFactory {
      private final ITransactor<IWorkflowDb> workflowDbTransactor;

      public Static(ITransactor<IWorkflowDb> workflowDbTransactor) {
        this.workflowDbTransactor = workflowDbTransactor;
      }

      @Override
      public ITransactor<IWorkflowDb> create() {
        return workflowDbTransactor;
      }
    }
  }
}
