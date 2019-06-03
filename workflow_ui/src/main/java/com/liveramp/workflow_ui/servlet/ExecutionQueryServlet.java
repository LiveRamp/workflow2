package com.liveramp.workflow_ui.servlet;

import java.util.Map;

import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liveramp.databases.workflow_db.IDatabases;
import com.liveramp.databases.workflow_db.IWorkflowDb;
import com.liveramp.databases.workflow_db.models.WorkflowAttempt;
import com.liveramp.databases.workflow_db.models.WorkflowExecution;
import com.liveramp.workflow_db_state.jack.JackUtil;
import com.liveramp.workflow_db_state.json.WorkflowJSON;
import com.liveramp.workflow_ui.util.QueryUtil;

public class ExecutionQueryServlet implements JSONServlet.Processor {
  private static final Logger LOG = LoggerFactory.getLogger(ExecutionQueryServlet.class);
  public static final Integer QUERY_LIMIT = 100;

  @Override
  public JSONObject getData(IDatabases databases, Map<String, String> parameters) throws Exception {
    IWorkflowDb workflowDb = databases.getWorkflowDb();

    boolean details = Boolean.parseBoolean(parameters.get("details"));
    String processStatus = parameters.get("process_status");
    int queryLimit = getQueryLimit(parameters );

    JSONArray rows = new JSONArray();

    Multimap<WorkflowExecution, WorkflowAttempt> results = QueryUtil.queryWorkflowExecutions(
        databases,
        parameters,
        queryLimit + 1
    );

    for (WorkflowExecution execution : Iterables.limit(JackUtil.sortDescending(results.keySet()), queryLimit)) {
      JSONObject obj = WorkflowJSON.toJSON(workflowDb, details, processStatus, execution, results.get(execution));
      if (obj != null) {
        rows.put(obj);
      }
    }

    return new JSONObject()
        .put("values", rows)
        .put("truncated", results.size() > queryLimit)
        .put("limit", queryLimit);
  }

  public static int getQueryLimit(Map<String, String> parameters) {

    String limit = parameters.get("limit");
    if(limit != null){
      return Integer.parseInt(limit);
    }

    return QUERY_LIMIT;
  }

}
