package com.liveramp.workflow_ui.servlet;

import java.util.Map;

import org.json.JSONObject;

import com.liveramp.databases.workflow_db.IDatabases;
import com.liveramp.workflow_db_state.WorkflowQueries;
import com.liveramp.workflow_ui.util.CostUtil;

public class CostServlet implements JSONServlet.Processor {

  @Override
  public JSONObject getData(IDatabases workflowDb, Map<String, String> parameters) throws Exception {
    return CostUtil.DEFAULT_COST_ESTIMATE.deriveStats(WorkflowQueries.getFlatCounters(
        workflowDb.getWorkflowDb(),
        Long.parseLong(parameters.get("id"))
    ));
  }
}
