package com.liveramp.workflow_ui.servlet;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.liveramp.commons.Accessors;
import com.liveramp.databases.workflow_db.IDatabases;
import com.liveramp.databases.workflow_db.IWorkflowDb;
import com.liveramp.databases.workflow_db.models.Application;
import com.liveramp.databases.workflow_db.models.ConfiguredNotification;
import com.liveramp.workflow_db_state.WorkflowQueries;
import com.liveramp.workflow_db_state.json.WorkflowJSON;

public class ApplicationQueryServlet implements JSONServlet.Processor{
  @Override
  public JSONObject getData(IDatabases databases, Map<String, String> parameters) throws Exception {

    String name = parameters.get("name");
    IWorkflowDb workflowDb = databases.getWorkflowDb();
    Application application = Accessors.only(workflowDb.applications().findByName(name));

    JSONArray notifications = new JSONArray();
    for (ConfiguredNotification.Attributes attributes : WorkflowQueries.getApplicationNotifications(workflowDb, application.getId())) {
      notifications.put(WorkflowJSON.toJSON(attributes));
    }

    return new JSONObject()
        .put("application", WorkflowJSON.toJSON(application.getAttributes()))
        .put("configured_notifications", notifications);
  }
}
