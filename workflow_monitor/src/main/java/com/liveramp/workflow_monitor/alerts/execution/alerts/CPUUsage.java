package com.liveramp.workflow_monitor.alerts.execution.alerts;

import java.time.Duration;

import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liveramp.commons.collections.map.MultimapBuilder;
import com.liveramp.commons.collections.nested_map.TwoNestedMap;
import com.liveramp.databases.workflow_db.models.MapreduceJob;
import com.liveramp.workflow_monitor.alerts.execution.JobThresholdAlert;
import com.liveramp.workflow_monitor.alerts.execution.thresholds.GreaterThan;
import com.liveramp.workflow_state.WorkflowRunnerNotification;

import static com.liveramp.workflow_core.WorkflowConstants.WORKFLOW_ALERT_RECOMMENDATIONS;

public class CPUUsage extends JobThresholdAlert {
  private static final Logger LOG = LoggerFactory.getLogger(CPUUsage.class);

  private static final double CPU_USED_RATIO = 2.0;

  private static final long MIN_TIME_ALERT_THRESHOLD = Duration.ofHours(3).toMillis();
  private static final long TASK_FORGIVENESS = 5 * 1000;

  static final String PREAMBLE = " of allocated CPU time was used. ";

  private static final Multimap<String, String> COUNTERS = new MultimapBuilder<String, String>()
      .put(JOB_COUNTER_GROUP, VCORES_MAPS)
      .put(JOB_COUNTER_GROUP, VCORES_REDUCES)
      .put(TASK_COUNTER_GROUP, CPU_MILLISECONDS)
      .get();

  public CPUUsage() {
    super(CPU_USED_RATIO, WorkflowRunnerNotification.PERFORMANCE, COUNTERS, new GreaterThan());
  }

  @Override
  protected Double calculateStatistic(MapreduceJob job, TwoNestedMap<String, String, Long> counters) {
    Long mapAllocatedCore = get(JOB_COUNTER_GROUP, VCORES_MAPS, counters);
    Long reduceAllocatedCore = get(JOB_COUNTER_GROUP, VCORES_REDUCES, counters);

    Long launchedMaps = get(JOB_COUNTER_GROUP, LAUNCHED_MAPS, counters);
    Long launchedReduces = get(JOB_COUNTER_GROUP, LAUNCHED_REDUCES, counters);

    Long cpuMillis = counters.get(TASK_COUNTER_GROUP, CPU_MILLISECONDS);
    long allocatedMillis = mapAllocatedCore + reduceAllocatedCore;

    if (allocatedMillis == 0 || cpuMillis == null) {
      return null;
    }

    //  ignore small startup time CPU factors so we don't alert on tiny jobs
    if (cpuMillis < MIN_TIME_ALERT_THRESHOLD) {
      LOG.debug("Skipping alert for < " + MIN_TIME_ALERT_THRESHOLD + " ms: " + job.getJobIdentifier());
      return null;
    }

    Long forgivenCPU = (launchedMaps + launchedReduces) * TASK_FORGIVENESS;

    return (cpuMillis.doubleValue() - forgivenCPU) / ((double)allocatedMillis);

  }

  @Override
  protected String getMessage(double value, MapreduceJob job, TwoNestedMap<String, String, Long> counters) {
    return asPercent(value) + PREAMBLE + WORKFLOW_ALERT_RECOMMENDATIONS.get("CPUUsage");
  }
}
