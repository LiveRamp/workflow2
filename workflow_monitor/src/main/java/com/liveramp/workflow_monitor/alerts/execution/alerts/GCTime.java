package com.liveramp.workflow_monitor.alerts.execution.alerts;

import com.google.common.collect.Multimap;

import com.liveramp.commons.collections.map.MultimapBuilder;
import com.liveramp.commons.collections.nested_map.TwoNestedMap;
import com.liveramp.databases.workflow_db.models.MapreduceJob;
import com.liveramp.workflow_monitor.alerts.execution.JobThresholdAlert;
import com.liveramp.workflow_monitor.alerts.execution.thresholds.GreaterThan;
import com.liveramp.workflow_state.WorkflowRunnerNotification;

import static com.liveramp.workflow_core.WorkflowConstants.WORKFLOW_ALERT_RECOMMENDATIONS;

public class GCTime extends JobThresholdAlert {

  public static final double GC_FRACTION_THRESHOLD = .25;

  //  unclear why, but there seems to be some fixed startup GC time we probably want to ignore in tiny jobs
  private static final long GC_STARTUP_TIME = 3000;

  static final String PREAMBLE = " of processing time was spent in Garbage Collection. ";

  private static final Multimap<String, String> REQUIRED_COUNTERS = new MultimapBuilder<String, String>()
      .put(TASK_COUNTER_GROUP, GC_TIME_MILLIS)
      .put(JOB_COUNTER_GROUP, MILLIS_MAPS)
      .put(JOB_COUNTER_GROUP, MILLIS_REDUCES).get();

  public GCTime() {
    super(GC_FRACTION_THRESHOLD, WorkflowRunnerNotification.PERFORMANCE, REQUIRED_COUNTERS, new GreaterThan());
  }

  @Override
  protected Double calculateStatistic(MapreduceJob job, TwoNestedMap<String, String, Long> counters) {

    Long gcTime = counters.get(TASK_COUNTER_GROUP, GC_TIME_MILLIS);

    Long launchedMaps = get(JOB_COUNTER_GROUP, LAUNCHED_MAPS, counters);
    Long launchedReduces = get(JOB_COUNTER_GROUP, LAUNCHED_REDUCES, counters);

    Long totalForgivenGC = (launchedMaps + launchedReduces) * GC_STARTUP_TIME;

    if (gcTime == null) {
      return null;
    }

    Long allTime = get(JOB_COUNTER_GROUP, MILLIS_MAPS, counters) + get(JOB_COUNTER_GROUP, MILLIS_REDUCES, counters);

    return (gcTime.doubleValue() - totalForgivenGC) / allTime.doubleValue();
  }

  @Override
  protected String getMessage(double value, MapreduceJob job, TwoNestedMap<String, String, Long> counters) {
    return asPercent(value) + PREAMBLE + WORKFLOW_ALERT_RECOMMENDATIONS.get("GCTime");
  }
}
