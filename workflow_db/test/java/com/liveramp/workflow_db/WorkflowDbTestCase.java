package com.liveramp.workflow_db;

import org.apache.log4j.Level;
import org.junit.Before;

import com.liveramp.databases.workflow_db.DatabasesImpl;

public class WorkflowDbTestCase {

  @Before
  public void deleteFixtures() throws Exception {
    new DatabasesImpl().getWorkflowDb().deleteAll();
  }
}
