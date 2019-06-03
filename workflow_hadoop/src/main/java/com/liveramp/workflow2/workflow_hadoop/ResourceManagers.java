package com.liveramp.workflow2.workflow_hadoop;

import java.io.IOException;

import com.liveramp.cascading_ext.resource.InMemoryResourceManager;
import com.liveramp.cascading_ext.resource.ResourceDeclarer;
import com.liveramp.cascading_ext.resource.ResourceDeclarerContainer;
import com.liveramp.cascading_ext.resource.ResourceManager;
import com.liveramp.cascading_ext.resource.RootManager;
import com.liveramp.databases.workflow_db.IWorkflowDb;
import com.liveramp.workflow2.workflow_state.resources.TransactorResourceManager;
import com.rapleaf.jack.transaction.ITransactor;

/**
 * All ResourceManagers returned by methods of this class use DbPersistence for checkpoints
 */
public class ResourceManagers {

  public static ResourceDeclarer defaultResourceManager() throws IOException {
    return TransactorResourceManager.create(new TransactorResourceManager.TransactorFactory.Default());
  }

  public static ResourceDeclarer defaultResourceManager(ITransactor<IWorkflowDb> transactor) throws IOException {
    return TransactorResourceManager.create(new TransactorResourceManager.TransactorFactory.Static(transactor));
  }

  public static ResourceDeclarer notImplemented() {
    return new ResourceManager.NotImplemented();
  }

  public static ResourceDeclarer dbResourceManager() throws IOException {
    return TransactorResourceManager.create(new TransactorResourceManager.TransactorFactory.Default());
  }

  public static ResourceDeclarer hdfsResourceManager(String workflowRoot) throws IOException {
    return new ResourceDeclarerContainer<>(
        new ResourceDeclarerContainer.MethodNameTagger(),
        new RootManager<>(
            new HdfsStorageRootDeterminer(workflowRoot),
            new HdfsStorage.Factory())
    );
  }

  public static ResourceDeclarer inMemoryResourceManager() throws IOException {
    return InMemoryResourceManager.create();
  }

}
