package com.liveramp.workflow_ui.servlet;

import java.util.Map;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.joda.time.LocalDate;
import org.json.JSONException;
import org.json.JSONObject;

import com.liveramp.commons.collections.nested_map.TwoNestedMap;
import com.liveramp.databases.workflow_db.IDatabases;
import com.liveramp.workflow_ui.util.ApplicationStatistic;
import com.liveramp.workflow_ui.util.QueryUtil;

//  TODO consolidate with hdfs io servlet
public class ShuffleIOServlet implements JSONServlet.Processor{

  private static final Multimap<String, String> COUNTERS_TO_RECORD = HashMultimap.create();

  static {
    COUNTERS_TO_RECORD.put("org.apache.hadoop.mapreduce.FileSystemCounter", "FILE_BYTES_READ");
    COUNTERS_TO_RECORD.put("org.apache.hadoop.mapreduce.FileSystemCounter", "FILE_BYTES_WRITTEN");
  }

  private static final ApplicationStatistic NO_OP = new ApplicationStatistic(){
    @Override
    public JSONObject deriveStats(TwoNestedMap<String, String, Long> counters) throws JSONException {
      // no op for now (dunno what a good NN aggregate cost function is right now)
      return new JSONObject();
    }
  };

  @Override
  public JSONObject getData(IDatabases workflowDb, Map<String, String> parameters) throws Exception {
    return QueryUtil.getCountersPerApplication(
        workflowDb.getWorkflowDb(),
        COUNTERS_TO_RECORD,
        NO_OP,
        new LocalDate(Long.parseLong((parameters.get("started_after")))),
        new LocalDate(Long.parseLong((parameters.get("started_before"))))
    );
  }
}
