package com.liveramp.workflow2.workflow_ui.workflow_ui;

import org.junit.Before;

/**
 * Unit test for simple App.
 */
public abstract class WorkflowUITestCase {

  @Before
  public void setUp() throws Exception {
    new com.liveramp.databases.workflow_db.DatabasesImpl().getWorkflowDb().deleteAll();
  }
}
