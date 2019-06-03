package com.liveramp.workflow_monitor.alerts.execution.alerts;

import com.liveramp.commons.collections.map.MultimapBuilder;
import com.liveramp.commons.collections.nested_map.TwoNestedMap;
import com.liveramp.databases.workflow_db.models.MapreduceJob;
import com.liveramp.workflow_monitor.alerts.execution.JobThresholdAlert;
import com.liveramp.workflow_monitor.alerts.execution.thresholds.GreaterThan;
import com.liveramp.workflow_state.WorkflowRunnerNotification;

public class KilledTasks extends JobThresholdAlert {

  //  don't alert if 5/10 are killed, not really important
  private static final int MIN_TASKS = 100;

  private static final String GROUP = "org.apache.hadoop.mapreduce.JobCounter";
  private static final String KILLED_MAPS = "NUM_KILLED_MAPS";
  private static final String KILLED_REDUCES = "NUM_KILLED_REDUCES";
  private static final String LAUNCHED_MAPS = "TOTAL_LAUNCHED_MAPS";
  private static final String LAUNCHED_REDUCES = "TOTAL_LAUNCHED_REDUCES";

  public KilledTasks() {
    super(
        .5,
        WorkflowRunnerNotification.PERFORMANCE,
        new MultimapBuilder<String, String>()
            .put(GROUP, KILLED_MAPS)
            .put(GROUP, KILLED_REDUCES)
            .put(GROUP, LAUNCHED_MAPS)
            .put(GROUP, LAUNCHED_REDUCES)
            .get(),
        new GreaterThan());
  }


  @Override
  protected Double calculateStatistic(MapreduceJob job, TwoNestedMap<String, String, Long> counters) {

    long killed = get(GROUP, KILLED_MAPS, counters) + get(GROUP, KILLED_REDUCES, counters);
    long launched = get(GROUP, LAUNCHED_MAPS, counters) + get(GROUP, LAUNCHED_REDUCES, counters);

    if (launched < MIN_TASKS) {
      return null;
    }

    return ((double)killed) / ((double)launched);

  }

  @Override
  protected String getMessage(double value, MapreduceJob job, TwoNestedMap<String, String, Long> counters) {
    return asPercent(value) + " of launched tasks were killed.  This may indicate heavy contention and under-allocated pools.";
  }
}
