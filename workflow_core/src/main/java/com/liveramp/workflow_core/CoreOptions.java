package com.liveramp.workflow_core;

import java.util.Map;

import com.google.common.collect.Maps;

import com.liveramp.cascading_ext.resource.ResourceManager;
import com.liveramp.commons.collections.properties.NestedProperties;
import com.liveramp.commons.collections.properties.OverridableProperties;
import com.liveramp.java_support.alerts_handler.LoggingAlertsHandler;
import com.liveramp.workflow_core.alerting.BufferingAlertsHandlerFactory;
import com.rapleaf.cascading_ext.workflow2.TrackerURLBuilder;
import com.rapleaf.cascading_ext.workflow2.WorkflowNotificationLevel;
import com.rapleaf.cascading_ext.workflow2.options.FixedHostnameProvider;

public class CoreOptions extends BaseWorkflowOptions<CoreOptions> {

  protected CoreOptions(OverridableProperties defaultProperties) {
    super(defaultProperties);
  }

  protected CoreOptions(OverridableProperties defaultProperties,
                                Map<Object, Object> systemProperties) {
    super(defaultProperties, systemProperties);
  }

  public static void configureTest(BaseWorkflowOptions opts) {
    opts.setMaxConcurrentSteps(1)
        .setAlertsHandler(new LoggingAlertsHandler())
        .setNotificationLevel(WorkflowNotificationLevel.DEBUG)
        .setStepPollInterval(100)
        .setUrlBuilder(new TrackerURLBuilder.None())
        .setHostnameProvider(new FixedHostnameProvider())
        .setAlertsHandlerFactory(new BufferingAlertsHandlerFactory())
        .setResourceManagerFactory(ResourceManager.NotImplementedFactory.class);
  }

  public static CoreOptions test() {
    CoreOptions opts = new CoreOptions(new NestedProperties(Maps.newHashMap(), false));
    configureTest(opts);
    return opts;
  }


}
