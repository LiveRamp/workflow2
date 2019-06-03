package com.liveramp.workflow_ui.servlet;

import java.util.List;

import com.google.common.collect.Lists;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liveramp.commons.collections.nested_map.TwoNestedMap;

public class RenderInfo {

  private static final Logger LOG = LoggerFactory.getLogger(RenderInfo.class);

  public static final List<StatServlet.DerivedCounter> DERIVED_COUNTERS = Lists.newArrayList(

      new StatServlet.DerivedCounter() {
        @Override
        public String getName() {
          return "TOTAL_MB_MILLIS";
        }

        @Override
        public Long getDerivedValue(TwoNestedMap<String, String, Long> counters) {
          return safeGet(counters, "org.apache.hadoop.mapreduce.JobCounter", "MB_MILLIS_MAPS") +
              safeGet(counters, "org.apache.hadoop.mapreduce.JobCounter", "MB_MILLIS_REDUCES");
        }
      },

      new StatServlet.DerivedCounter() {
        @Override
        public String getName() {
          return "TOTAL_VCORES_MILLIS";
        }

        @Override
        public Long getDerivedValue(TwoNestedMap<String, String, Long> counters) {
          return safeGet(counters, "org.apache.hadoop.mapreduce.JobCounter", "VCORES_MILLIS_MAPS") +
              safeGet(counters, "org.apache.hadoop.mapreduce.JobCounter", "VCORES_MILLIS_REDUCES");
        }
      },

      new StatServlet.DerivedCounter() {
        @Override
        public String getName() {
          return "NON_LOCAL_MAPS";
        }

        @Override
        public Long getDerivedValue(TwoNestedMap<String, String, Long> counters) {
          return
              safeGet(counters, "org.apache.hadoop.mapreduce.JobCounter", "TOTAL_LAUNCHED_MAPS") -
                  safeGet(counters, "org.apache.hadoop.mapreduce.JobCounter", "RACK_LOCAL_MAPS") -
                  safeGet(counters, "org.apache.hadoop.mapreduce.JobCounter", "DATA_LOCAL_MAPS");
        }
      },


      new StatServlet.DerivedCounter() {
        @Override
        public String getName() {
          return "GC_TIME_PERCENT";
        }

        @Override
        public Long getDerivedValue(TwoNestedMap<String, String, Long> counters) {

          long millisTotal = safeGet(counters, "org.apache.hadoop.mapreduce.JobCounter", "MILLIS_MAPS") + safeGet(counters, "org.apache.hadoop.mapreduce.JobCounter", "MILLIS_REDUCES");
          if (millisTotal == 0) {
            return 0L;
          }

          double millisDouble = (double)millisTotal;
          double gcTime = (double)safeGet(counters, "org.apache.hadoop.mapreduce.TaskCounter", "GC_TIME_MILLIS");

          return (long)((gcTime * 100.0 / millisDouble));

        }
      },

      new StatServlet.DerivedCounter() {
        @Override
        public String getName() {
          return "PERCENT_ALLOCATED_HEAP_USED";
        }

        @Override
        public Long getDerivedValue(TwoNestedMap<String, String, Long> counters) {
          long totalTasks = safeGet(counters, "org.apache.hadoop.mapreduce.JobCounter", "TOTAL_LAUNCHED_MAPS")
              + safeGet(counters, "org.apache.hadoop.mapreduce.JobCounter", "TOTAL_LAUNCHED_REDUCES");

          if (totalTasks == 0L) {
            return 0L;
          }

          long millisMaps = safeGet(counters, "org.apache.hadoop.mapreduce.JobCounter", "MILLIS_MAPS");
          long millisReduces = safeGet(counters, "org.apache.hadoop.mapreduce.JobCounter", "MILLIS_REDUCES");

          long physMemBytes = safeGet(counters, "org.apache.hadoop.mapreduce.TaskCounter", "PHYSICAL_MEMORY_BYTES");

          double physMemMB = (double)physMemBytes / (1024.0 * 1024.0);

          double occupiedMemoryMB =
              ((millisMaps + millisReduces) * physMemMB / totalTasks);


          long allocatedMemoryMB = safeGet(counters, "org.apache.hadoop.mapreduce.JobCounter", "MB_MILLIS_MAPS")
              + safeGet(counters, "org.apache.hadoop.mapreduce.JobCounter", "MB_MILLIS_REDUCES");

          if (allocatedMemoryMB == 0L) {
            return 0L;
          }
          return (long)(100.0 * occupiedMemoryMB / allocatedMemoryMB);

        }
      }


  );

  private static long safeGet(TwoNestedMap<String, String, Long> counters, String group, String name) {
    if (!counters.containsKey(group, name)) {
      return 0L;
    }
    return counters.get(group, name);
  }

  public static final JSONObject DEFAULT_EXPAND = getExpandCounters();


  private static JSONObject getExpandCounters() {
    try {
      return new JSONObject()
          .put("org.apache.hadoop.mapreduce.FileSystemCounter.HDFS_BYTES_WRITTEN", true)
          .put("org.apache.hadoop.mapreduce.FileSystemCounter.HDFS_BYTES_READ", true)
          .put("Duration.Total", true)
          .put("YarnStats.MB_SECONDS", true)
          .put("YarnStats.VCORES_SECONDS", true)
          .put("Derived.PERCENT_ALLOCATED_HEAP_USED", true);
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
  }

}
