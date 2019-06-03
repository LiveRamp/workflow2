package com.rapleaf.cascading_ext.workflow2.counter.verifier;

import cascading.tap.BaseTemplateTap;

import com.liveramp.commons.collections.nested_map.TwoNestedMap;
import com.rapleaf.cascading_ext.workflow2.WorkflowJobPersister;

public class TemplateTapFiles implements WorkflowJobPersister.CounterVerifier {
  @Override
  public void verify(TwoNestedMap<String, String, Long> toRecord) {

    Long opened = toRecord.get("cascading.tap.BaseTemplateTap$Counters", "Paths_Opened");
    Long closed = toRecord.get("cascading.tap.BaseTemplateTap$Counters", "Paths_Closed");

    if(opened != null && closed != null){
      if(opened.longValue() != closed.longValue()){
        throw new RuntimeException("Job contains a TemplateTap which did not close all output files!");
      }
    }

  }
}
