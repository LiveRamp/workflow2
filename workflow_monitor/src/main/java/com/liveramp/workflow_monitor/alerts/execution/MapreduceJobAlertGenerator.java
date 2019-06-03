package com.liveramp.workflow_monitor.alerts.execution;

import java.io.IOException;
import java.util.Map;

import com.google.common.collect.Multimap;

import com.liveramp.commons.collections.nested_map.TwoNestedMap;
import com.liveramp.databases.workflow_db.IDatabases;
import com.liveramp.databases.workflow_db.models.MapreduceJob;
import com.liveramp.databases.workflow_db.models.StepAttempt;
import com.liveramp.workflow_monitor.alerts.execution.alert.AlertMessage;

public abstract class MapreduceJobAlertGenerator {

  public static final String TASK_COUNTER_GROUP = "org.apache.hadoop.mapreduce.TaskCounter";
  public static final String JOB_COUNTER_GROUP = "org.apache.hadoop.mapreduce.JobCounter";

  public static final String GC_TIME_MILLIS = "GC_TIME_MILLIS";
  public static final String MILLIS_MAPS = "MILLIS_MAPS";
  public static final String MILLIS_REDUCES = "MILLIS_REDUCES";

  public static final String VCORES_MAPS = "VCORES_MILLIS_MAPS";
  public static final String VCORES_REDUCES = "VCORES_MILLIS_REDUCES";

  public static final String MEM_BYTES = "PHYSICAL_MEMORY_BYTES";
  public static final String LAUNCHED_MAPS = "TOTAL_LAUNCHED_MAPS";
  public static final String LAUNCHED_REDUCES = "TOTAL_LAUNCHED_REDUCES";

  public static final String MB_MAPS = "MB_MILLIS_MAPS";
  public static final String MB_REDUCES = "MB_MILLIS_REDUCES";

  public static final String MAP_OUTPUT_MATERIALIZED_BYTES = "MAP_OUTPUT_MATERIALIZED_BYTES";

  public static final String CPU_MILLISECONDS = "CPU_MILLISECONDS";

  private final Multimap<String, String> countersToFetch;

  protected MapreduceJobAlertGenerator(Multimap<String, String> countersToFetch) {
    this.countersToFetch = countersToFetch;
  }

  public abstract AlertMessage generateAlert(StepAttempt step, MapreduceJob job, TwoNestedMap<String, String, Long> counters, IDatabases db) throws IOException;

  public Multimap<String, String> getCountersToFetch() {
    return countersToFetch;
  }

  //  helpers

  protected Long get(String group, String name, TwoNestedMap<String, String, Long> counters) {
    if (counters.containsKey(group, name)) {
      return counters.get(group, name);
    }
    return 0L;
  }

  protected boolean containsAll(TwoNestedMap<String, String, Long> available, Multimap<String, String> required) {
    for (Map.Entry<String, String> entry : required.entries()) {
      if (!available.containsKey(entry.getKey(), entry.getValue())) {
        return false;
      }
    }
    return true;
  }

}
