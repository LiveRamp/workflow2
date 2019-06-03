package com.liveramp.workflow_ui.servlet;

import java.util.Map;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.joda.time.LocalDate;
import org.json.JSONObject;

import com.liveramp.databases.workflow_db.IDatabases;
import com.liveramp.databases.workflow_db.IWorkflowDb;
import com.liveramp.workflow_core.constants.YarnConstants;
import com.liveramp.workflow_ui.util.CostUtil;
import com.liveramp.workflow_ui.util.QueryUtil;

public class AppCostHistoryServlet implements JSONServlet.Processor {

  private static final Multimap<String, String> COST_COUNTERS = HashMultimap.create();

  static {
    COST_COUNTERS.put(ClusterConstants.MR2_GROUP, ClusterConstants.VCORE_MAP);
    COST_COUNTERS.put(ClusterConstants.MR2_GROUP, ClusterConstants.VCORE_RED);

    COST_COUNTERS.put(ClusterConstants.MR2_GROUP, ClusterConstants.MB_MAP);
    COST_COUNTERS.put(ClusterConstants.MR2_GROUP, ClusterConstants.MB_RED);

    COST_COUNTERS.put(YarnConstants.YARN_GROUP, YarnConstants.YARN_VCORE_SECONDS);
    COST_COUNTERS.put(YarnConstants.YARN_GROUP, YarnConstants.YARN_MB_SECONDS);
  }

  @Override
  public JSONObject getData(IDatabases workflowDb, Map<String, String> parameters) throws Exception {
    IWorkflowDb workflow = workflowDb.getWorkflowDb();

    return QueryUtil.getCountersForApp(
        workflow,
        parameters.get("name"),
        COST_COUNTERS,
        new LocalDate(Long.parseLong((parameters.get("started_after")))),
        new LocalDate(Long.parseLong((parameters.get("started_before")))),
        CostUtil.DEFAULT_COST_ESTIMATE
    );
  }
}
