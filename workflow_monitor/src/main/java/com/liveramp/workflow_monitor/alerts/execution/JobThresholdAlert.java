package com.liveramp.workflow_monitor.alerts.execution;

import java.io.IOException;
import java.text.DecimalFormat;

import com.google.common.collect.Multimap;

import com.liveramp.commons.collections.nested_map.TwoNestedMap;
import com.liveramp.databases.workflow_db.IDatabases;
import com.liveramp.databases.workflow_db.models.MapreduceJob;
import com.liveramp.databases.workflow_db.models.StepAttempt;
import com.liveramp.workflow_monitor.alerts.execution.alert.AlertMessage;
import com.liveramp.workflow_monitor.alerts.execution.thresholds.ThresholdChecker;
import com.liveramp.workflow_state.WorkflowRunnerNotification;

import static com.liveramp.workflow_core.WorkflowConstants.WORKFLOW_ALERT_SHORT_DESCRIPTIONS;

public abstract class JobThresholdAlert extends MapreduceJobAlertGenerator {

  private final WorkflowRunnerNotification notification;
  private final double threshold;
  private ThresholdChecker thresholdCheck;

  protected static final DecimalFormat df = new DecimalFormat("##.##");

  protected JobThresholdAlert(double threshold,
                              WorkflowRunnerNotification notification,
                              Multimap<String, String> countersToFetch,
                              ThresholdChecker thresholdCheck) {
    super(countersToFetch);
    this.notification = notification;
    this.threshold = threshold;
    this.thresholdCheck = thresholdCheck;
  }

  protected String asPercent(double fraction) {
    return df.format(fraction * 100) + "%";
  }

  @Override
  public AlertMessage generateAlert(StepAttempt stepAttempt, MapreduceJob job, TwoNestedMap<String, String, Long> counters, IDatabases db) throws IOException {

    Double value = calculateStatistic(job, counters);
    if (value != null && thresholdCheck.isAlert(threshold, value)) {

      String message = "Step: " +
          stepAttempt.getStepToken() + "\n" +
          "Job name: " +
          job.getJobName() + "\n" +
          "Tracker link: " +
          job.getTrackingUrl() + "\n\n" +
          getMessage(value, job, counters) + "\n" +
          "Threshold: " + WORKFLOW_ALERT_SHORT_DESCRIPTIONS.get(this.getClass().getName());

      return AlertMessage.createAlertMessage(this.getClass().getName(), message, notification, job, db);
    }

    return null;
  }

  protected abstract Double calculateStatistic(MapreduceJob job, TwoNestedMap<String, String, Long> counters);

  protected abstract String getMessage(double value, MapreduceJob job, TwoNestedMap<String, String, Long> counters);

}
