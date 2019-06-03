package com.liveramp.workflow_db_state;

import org.junit.Before;

import com.liveramp.databases.workflow_db.DatabasesImpl;

public class WorkflowDbStateTestCase {

  @Before
  public void deleteFixtures() throws Exception {
    new DatabasesImpl().getWorkflowDb().deleteAll();
  }
}
