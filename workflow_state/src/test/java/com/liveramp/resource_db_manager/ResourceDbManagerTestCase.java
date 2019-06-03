package com.liveramp.resource_db_manager;

import org.junit.Before;

import com.liveramp.databases.workflow_db.DatabasesImpl;

public class ResourceDbManagerTestCase  {

  @Before
  public void deleteFixtures() throws Exception {
    new DatabasesImpl().getWorkflowDb().deleteAll();
  }

}
