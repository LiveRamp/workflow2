package com.liveramp.workflow2.workflow_state.resources;

import java.io.IOException;

import com.liveramp.cascading_ext.resource.ResourceDeclarer;
import com.liveramp.cascading_ext.resource.ResourceDeclarerContainer;
import com.liveramp.cascading_ext.resource.ResourceDeclarerFactory;
import com.liveramp.cascading_ext.resource.RootManager;
import com.liveramp.databases.workflow_db.DatabasesImpl;
import com.liveramp.databases.workflow_db.IWorkflowDb;

public class DbResourceManager {

  public static class Factory implements ResourceDeclarerFactory {
    @Override
    public ResourceDeclarer create() throws IOException {
      return DbResourceManager.create(new WorkflowDbFactory.Default());
    }
  }

  public static ResourceDeclarer create(WorkflowDbFactory factory) throws IOException {
    return new ResourceDeclarerContainer<>(
        new ResourceDeclarerContainer.MethodNameTagger(),
        new RootManager<>(
            new DbStorageRootDeterminer(factory.create()),
            dbStorage(factory))
    );
  }

  public static ResourceDeclarer create() throws IOException {
    return create(new WorkflowDbFactory.Default());
  }

  public static DbStorage.Factory dbStorage(WorkflowDbFactory factory) {
    return new DbStorage.Factory(factory);
  }

  public interface WorkflowDbFactory {
    IWorkflowDb create();

    class Default implements WorkflowDbFactory {
      @Override
      public IWorkflowDb create() {
        IWorkflowDb workflowDb = new DatabasesImpl().getWorkflowDb();
        workflowDb.disableCaching();
        return workflowDb;
      }
    }

  }
}
