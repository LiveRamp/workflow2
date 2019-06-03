package com.liveramp.workflow_db_state;

import com.liveramp.databases.workflow_db.DatabasesImpl;
import com.liveramp.databases.workflow_db.IDatabases;

public class ThreadLocalWorkflowDb extends ThreadLocal<IDatabases> {
  @Override
  protected synchronized IDatabases initialValue() {
    DatabasesImpl db = new DatabasesImpl();
    db.getWorkflowDb().disableCaching();
    return db;
  }
}
