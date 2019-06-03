package com.liveramp.workflow_ui.util;

import org.json.JSONException;
import org.json.JSONObject;

import com.liveramp.commons.collections.nested_map.TwoNestedMap;

public interface ApplicationStatistic {
  public JSONObject deriveStats(TwoNestedMap<String, String, Long> counters) throws JSONException;
}
