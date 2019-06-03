package com.liveramp.workflow_monitor;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liveramp.workflow_monitor.alerts.execution.ExecutionAlerter;

public class WorkflowMonitor {
  private static final Logger LOG = LoggerFactory.getLogger(WorkflowMonitor.class);

  private static final long FIVE_MINUTES = Duration.ofMinutes(5).toMillis();

  public static void monitor(List<ExecutionAlerter> executionAlerters) throws IOException, URISyntaxException, InterruptedException {

    while (true) {
      LOG.info("Starting to generate alerts");
      for (ExecutionAlerter alerter : executionAlerters) {
        alerter.generateAlerts();
      }

      LOG.info("Sleeping for " + FIVE_MINUTES + "ms");
      Thread.sleep(FIVE_MINUTES);
    }

  }

}

