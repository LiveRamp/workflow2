package com.rapleaf.cascading_ext.workflow2;

import java.util.Set;

import com.liveramp.commons.collections.nested_map.ThreeNestedMap;
import com.liveramp.commons.collections.nested_map.TwoNestedMap;
import com.rapleaf.cascading_ext.workflow2.options.HadoopWorkflowOptions;

public interface WorkflowRunnable {

  Set<Step> getSteps() throws Exception;

  HadoopWorkflowOptions getOptions() throws Exception;

  class CounterData {
  }

  void postWorkflow(TwoNestedMap<String, String, Long> flatCounters, ThreeNestedMap<String, String, String, Long> countersByStep) throws Exception;

}
