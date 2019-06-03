package com.liveramp.workflow_core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class WorkflowConstants {

  public static final String WORKFLOW_EMAIL_SUBJECT_TAG = "WORKFLOW";
  public static final String ERROR_EMAIL_SUBJECT_TAG = "ERROR";

  public static final String JOB_PRIORITY_PARAM = "mapred.job.priority";
  public static final String JOB_POOL_PARAM = "mapreduce.job.queuename";

  public static final Map WORKFLOW_ALERT_SHORT_DESCRIPTIONS = Collections.unmodifiableMap(new HashMap() {
    private static final long serialVersionUID = 1L;

    {
      put("ShortMaps", "Maps take <1min on average.");
      put("ShortReduces", "Reduces take <1min on average.");
      put("GCTime", "Over 25% of time spent in GC.");
      put("CPUUsage", "CPU time over 3 hours.");
      put("OutputPerMapTask", "Map tasks output over 5GB post-serialization.");
    }
  });

  public static final Map WORKFLOW_ALERT_RECOMMENDATIONS = Collections.unmodifiableMap(new HashMap() {
    private static final long serialVersionUID = 1L;

    {
      put("ShortMaps", "Consider batching more aggressively.");
      put("ShortReduces", "Consider batching more aggressively.");
      put("GCTime", "This can be triggered by excessive object creation or insufficient heap size. " +
          "Try to reduce object instantiations or increase task heap sizes.");
      put("CPUUsage", "This is probably due to high Garbage Collection time, " +
          "unless the job is doing explicit multi-threading. + " +
          "If CPU time is due to GC, try to reduce either object creation or increase memory. " +
          "If you are explicitly multi-threading, please increase set mapreduce.map.cpu.vcores or mapreduce.reduce.cpu.vcores accordingly.");
      put("OutputPerMapTask", "Reading spills which are this large can cause machine performance problems; please increase the number of " +
          "map tasks this data is spread over.");
      put("InputPerReduceTask", "Merging spills which are this large can cause performance problems; please increase the number of redcue tasks " +
          "this data is spread across.");
    }
  });
}
