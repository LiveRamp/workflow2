package com.rapleaf.cascading_ext.workflow2.flow_closure;

import java.util.Properties;

import cascading.flow.Flow;
import cascading.pipe.Pipe;
import cascading.tap.Tap;

public interface FlowRunner {
  Flow complete(Properties properties, String name, Tap source, Tap sink, Pipe tail);
}
