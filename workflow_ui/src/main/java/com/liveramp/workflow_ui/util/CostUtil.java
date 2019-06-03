package com.liveramp.workflow_ui.util;

import org.json.JSONException;
import org.json.JSONObject;

import com.liveramp.commons.collections.nested_map.TwoNestedMap;
import com.liveramp.workflow_core.constants.YarnConstants;
import com.liveramp.workflow_ui.servlet.ClusterConstants;

public class CostUtil {

  private static final double CPU_WEIGHT = 0.5;
  private static final double MEM_WEIGHT = 0.5;

  public static final ApplicationStatistic DEFAULT_COST_ESTIMATE = new YarnComputationCostStatistic();

  public static class MRComputationCostStatistic implements ApplicationStatistic {
    @Override
    public JSONObject deriveStats(TwoNestedMap<String, String, Long> counters) throws JSONException {
      return new JSONObject().put("estimated_cost", CostUtil.getCost(counters));
    }
  }

  public static class YarnComputationCostStatistic implements ApplicationStatistic {

    @Override
    public JSONObject deriveStats(TwoNestedMap<String, String, Long> counters) throws JSONException {
      return new JSONObject().put("estimated_cost", CostUtil.getYarnCost(counters));
    }
  }

  public static double getYarnCost(TwoNestedMap<String, String, Long> counters) {
    return getYarnCost(
        counters.get(YarnConstants.YARN_GROUP, YarnConstants.YARN_VCORE_SECONDS),
        counters.get(YarnConstants.YARN_GROUP, YarnConstants.YARN_MB_SECONDS)
    );
  }

  public static double getYarnCost(Long secondsVcore, Long secondsMB) {
    return computeDollarCost(pad(secondsVcore)*1000, pad(secondsMB)*1000);
  }


  public static double getCost(TwoNestedMap<String, String, Long> counters) {
    return getCost(
        counters.get(ClusterConstants.MR2_GROUP, ClusterConstants.VCORE_MAP),
        counters.get(ClusterConstants.MR2_GROUP, ClusterConstants.VCORE_RED),
        counters.get(ClusterConstants.MR2_GROUP, ClusterConstants.MB_MAP),
        counters.get(ClusterConstants.MR2_GROUP, ClusterConstants.MB_RED)
    );
  }

  public static double getCost(Long vcoreMap, Long vcoreRed, Long mbMap, Long mbReduce) {
    return computeDollarCost(pad(vcoreMap) + pad(vcoreRed), pad(mbMap) + pad(mbReduce));
  }

  private static long pad(Long value) {
    if (value == null) {
      return 0;
    }

    return value;
  }

  /*
  This computes the cost in terms of memory/vcore percentage use.
  */
  private static double computeDollarCost(Long cpu, Long mem) {
    return CPU_WEIGHT * ClusterConstants.CPU_MS_COST * cpu + MEM_WEIGHT * ClusterConstants.MB_MS_COST * mem;
  }


}
