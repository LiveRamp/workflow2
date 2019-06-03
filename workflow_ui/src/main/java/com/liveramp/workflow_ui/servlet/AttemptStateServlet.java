package com.liveramp.workflow_ui.servlet;

import java.util.Map;

import org.json.JSONObject;

import com.liveramp.databases.workflow_db.IDatabases;
import com.liveramp.databases.workflow_db.IWorkflowDb;
import com.liveramp.workflow_db_state.DbPersistence;
import com.liveramp.workflow_db_state.json.WorkflowJSON;

public class AttemptStateServlet implements JSONServlet.Processor {
  @Override
  public JSONObject getData(IDatabases databases, Map<String, String> parameters) throws Exception {
    IWorkflowDb workflowDb = databases.getWorkflowDb();
    return WorkflowJSON.getDbJSONState(workflowDb, DbPersistence.queryPersistence(Long.parseLong(parameters.get("workflow_attempt_id")), workflowDb));
  }
}
