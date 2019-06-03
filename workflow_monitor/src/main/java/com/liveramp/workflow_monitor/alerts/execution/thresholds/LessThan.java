package com.liveramp.workflow_monitor.alerts.execution.thresholds;

public class LessThan implements ThresholdChecker {
  public boolean isAlert(Double threshold, Double value) {
    return value < threshold;
  }
}
