package com.liveramp.workflow_monitor.alerts.execution.alerts;

import com.google.common.collect.Multimap;

import com.liveramp.commons.collections.map.MultimapBuilder;
import com.liveramp.commons.collections.nested_map.TwoNestedMap;
import com.liveramp.databases.workflow_db.models.MapreduceJob;
import com.liveramp.workflow_monitor.alerts.execution.JobThresholdAlert;
import com.liveramp.workflow_monitor.alerts.execution.thresholds.GreaterThan;
import com.liveramp.workflow_state.WorkflowRunnerNotification;

public class NearMemoryLimit extends JobThresholdAlert {

  protected static final double MEMORY_WARN_THRESHOLD = .95;

  protected static final Multimap<String, String> REQUIRED_COUNTERS = new MultimapBuilder<String, String>()
      .put(JOB_COUNTER_GROUP, MILLIS_MAPS)
      .put(JOB_COUNTER_GROUP, MILLIS_REDUCES)
      .put(JOB_COUNTER_GROUP, LAUNCHED_MAPS)
      .put(JOB_COUNTER_GROUP, LAUNCHED_REDUCES)
      .put(JOB_COUNTER_GROUP, MB_MAPS)
      .put(JOB_COUNTER_GROUP, MB_REDUCES)
      .put(TASK_COUNTER_GROUP, MEM_BYTES)
      .get();

  public NearMemoryLimit() {
    super(MEMORY_WARN_THRESHOLD, WorkflowRunnerNotification.PERFORMANCE, REQUIRED_COUNTERS, new GreaterThan());
  }

  @Override
  protected Double calculateStatistic(MapreduceJob job, TwoNestedMap<String, String, Long> counters) {

    Long allTime = get(JOB_COUNTER_GROUP, MILLIS_MAPS, counters) + get(JOB_COUNTER_GROUP, MILLIS_REDUCES, counters);
    Long totalTasks = get(JOB_COUNTER_GROUP, LAUNCHED_MAPS, counters) + get(JOB_COUNTER_GROUP, LAUNCHED_REDUCES, counters);
    Long allocatedMem = get(JOB_COUNTER_GROUP, MB_MAPS, counters) + get(JOB_COUNTER_GROUP, MB_REDUCES, counters);
    Long memoryMB = get(TASK_COUNTER_GROUP, MEM_BYTES, counters) / (1024 * 1024);

    if (memoryMB == 0 || allTime == 0 || totalTasks == 0 || allocatedMem == 0) {
      return null;
    }

    Double averageTaskMemory = memoryMB.doubleValue() / totalTasks.doubleValue();
    Long occupiedMemory = (long)(allTime * averageTaskMemory);

    return occupiedMemory.doubleValue() / allocatedMem.doubleValue();

  }

  @Override
  protected String getMessage(double value, MapreduceJob job, TwoNestedMap<String, String, Long> counters) {
    return asPercent(value) + " of total allocated memory is in use.  This is likely to cause heavy Garbage Collection, and " +
        "potentially cause OutOfMemoryErrors.  Either increase heap size or decrease heap usage to bring this below " + asPercent(MEMORY_WARN_THRESHOLD);
  }
}
