package com.liveramp.workflow_ui.servlet;

import java.util.Map;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.joda.time.LocalDate;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liveramp.databases.workflow_db.IDatabases;
import com.liveramp.workflow_core.constants.YarnConstants;
import com.liveramp.workflow_ui.util.CostUtil;
import com.liveramp.workflow_ui.util.QueryUtil;

public class ClusterUsageServlet implements JSONServlet.Processor {

  private static final Multimap<String, String> COUNTERS_TO_RECORD = HashMultimap.create();

  static {
    COUNTERS_TO_RECORD.put(ClusterConstants.MR2_GROUP, ClusterConstants.VCORE_MAP);
    COUNTERS_TO_RECORD.put(ClusterConstants.MR2_GROUP, ClusterConstants.VCORE_RED);

    COUNTERS_TO_RECORD.put(ClusterConstants.MR2_GROUP, ClusterConstants.MB_MAP);
    COUNTERS_TO_RECORD.put(ClusterConstants.MR2_GROUP, ClusterConstants.MB_RED);

    COUNTERS_TO_RECORD.put(YarnConstants.YARN_GROUP, YarnConstants.YARN_VCORE_SECONDS);
    COUNTERS_TO_RECORD.put(YarnConstants.YARN_GROUP, YarnConstants.YARN_MB_SECONDS);
  }

  @Override
  public JSONObject getData(IDatabases workflowDb, Map<String, String> parameters) throws Exception {
    return QueryUtil.getCountersPerApplication(
        workflowDb.getWorkflowDb(),
        COUNTERS_TO_RECORD,
        CostUtil.DEFAULT_COST_ESTIMATE,
        new LocalDate(Long.parseLong((parameters.get("started_after")))),
        new LocalDate(Long.parseLong((parameters.get("started_before"))))
    );
  }
}
