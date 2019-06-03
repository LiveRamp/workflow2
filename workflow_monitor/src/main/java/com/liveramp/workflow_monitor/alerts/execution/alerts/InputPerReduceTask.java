package com.liveramp.workflow_monitor.alerts.execution.alerts;

import com.google.common.collect.Multimap;

import com.liveramp.commons.collections.map.MultimapBuilder;
import com.liveramp.commons.collections.nested_map.TwoNestedMap;
import com.liveramp.databases.workflow_db.models.MapreduceJob;
import com.liveramp.java_support.ByteUnit;
import com.liveramp.workflow_monitor.alerts.execution.JobThresholdAlert;
import com.liveramp.workflow_monitor.alerts.execution.thresholds.GreaterThan;
import com.liveramp.workflow_state.WorkflowRunnerNotification;

import static com.liveramp.workflow_core.WorkflowConstants.WORKFLOW_ALERT_RECOMMENDATIONS;

public class InputPerReduceTask extends JobThresholdAlert {

  private static final long BYTES_PER_REDUCE = ByteUnit.GIBIBYTES.toBytes(20);

  protected static final Multimap<String, String> REQUIRED_COUNTERS = new MultimapBuilder<String, String>()
      .put(TASK_COUNTER_GROUP, MAP_OUTPUT_MATERIALIZED_BYTES)
      .put(JOB_COUNTER_GROUP, LAUNCHED_REDUCES)
      .get();

  public InputPerReduceTask() {
    super(BYTES_PER_REDUCE, WorkflowRunnerNotification.PERFORMANCE, REQUIRED_COUNTERS, new GreaterThan());
  }

  @Override
  protected Double calculateStatistic(MapreduceJob job, TwoNestedMap<String, String, Long> counters) {

    Long mapOutputBytes = counters.get(TASK_COUNTER_GROUP, MAP_OUTPUT_MATERIALIZED_BYTES);
    Long launchedReduces = counters.get(JOB_COUNTER_GROUP, LAUNCHED_REDUCES);

    if(launchedReduces != null && mapOutputBytes != null){
      return mapOutputBytes.doubleValue() / launchedReduces.doubleValue();
    }

    return null;
  }

  @Override
  protected String getMessage(double value, MapreduceJob job, TwoNestedMap<String, String, Long> counters) {

    return "Reduce tasks in this job are reading on average " +
        df.format(value / ((double)ByteUnit.GIGABYTES.toBytes(1))) + "GB post-serialization. " +
        WORKFLOW_ALERT_RECOMMENDATIONS.get("InputPerReduceTask");

  }
}
