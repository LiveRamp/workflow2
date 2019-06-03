package com.liveramp.workflow_ui.servlet;

import java.util.Map;

import com.google.common.collect.Lists;
import org.json.JSONArray;
import org.json.JSONObject;

import com.liveramp.databases.workflow_db.IDatabases;
import com.liveramp.workflow_state.WorkflowRunnerNotification;

public class AvailableNotificationServlet implements JSONServlet.Processor{
  @Override
  public JSONObject getData(IDatabases workflowDb, Map<String, String> parameters) throws Exception {
    return new JSONObject().put("values", new JSONArray(Lists.newArrayList(WorkflowRunnerNotification.values())));
  }
}
