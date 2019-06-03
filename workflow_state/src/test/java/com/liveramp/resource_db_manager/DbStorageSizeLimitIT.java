package com.liveramp.resource_db_manager;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.liveramp.databases.workflow_db.DatabasesImpl;
import com.liveramp.databases.workflow_db.IWorkflowDb;
import com.liveramp.databases.workflow_db.models.ResourceRoot;
import com.liveramp.workflow2.workflow_state.resources.BaseDbStorage;
import com.liveramp.cascading_ext.resource.Storage;
import com.liveramp.workflow2.workflow_state.resources.DbResourceManager;
import com.liveramp.workflow2.workflow_state.resources.DbStorage;

import static org.junit.Assert.assertTrue;

public class DbStorageSizeLimitIT extends ResourceDbManagerTestCase {

  private final IWorkflowDb workflowDb = new DatabasesImpl().getWorkflowDb();
  private Storage.Factory<ResourceRoot> dbStorage;

  @Before
  public void setup() throws IOException {
    this.dbStorage = new DbStorage.Factory(new DbResourceManager.WorkflowDbFactory.Default());
  }

  @Test
  public void testUnderSizeLimit() throws IOException {
    DbStorage.Factory dbStorage = new DbStorage.Factory(new DbResourceManager.WorkflowDbFactory.Default());
    ResourceRoot resourceRoot = workflowDb.resourceRoots().create()
        .setName("resource_root")
        .setUpdatedAt(System.currentTimeMillis())
        .setCreatedAt(System.currentTimeMillis());
    resourceRoot.save();

    Storage storage = dbStorage.forResourceRoot(resourceRoot);

    byte[] largeObject = new byte[BaseDbStorage.MAX_OBJECT_SIZE / 9];
    storage.store("someObject", largeObject);
  }

  @Test
  public void testOverSizeLimit() throws IOException {
    ResourceRoot resourceRoot = workflowDb.resourceRoots().create()
        .setName("resource_root")
        .setCreatedAt(System.currentTimeMillis())
        .setUpdatedAt(System.currentTimeMillis());
    resourceRoot.save();

    Storage storage = dbStorage.forResourceRoot(resourceRoot);

    byte[] largeObject = new byte[BaseDbStorage.MAX_OBJECT_SIZE];
    try {
      storage.store("someObject", largeObject);
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().startsWith("Resource size over limit"));
    }
  }
}
