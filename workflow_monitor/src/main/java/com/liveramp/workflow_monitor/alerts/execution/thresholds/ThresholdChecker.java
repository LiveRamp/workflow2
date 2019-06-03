package com.liveramp.workflow_monitor.alerts.execution.thresholds;

public interface ThresholdChecker {
  boolean isAlert(Double threshold, Double value);
}
