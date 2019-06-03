package com.liveramp.workflow_monitor.alerts.execution;

import org.junit.Before;

import com.liveramp.databases.workflow_db.DatabasesImpl;

public abstract class WorkflowMonitorTestCase {

  @Before
  public void deleteFixtures() throws Exception {
    new DatabasesImpl().getWorkflowDb().deleteAll();
  }

}
