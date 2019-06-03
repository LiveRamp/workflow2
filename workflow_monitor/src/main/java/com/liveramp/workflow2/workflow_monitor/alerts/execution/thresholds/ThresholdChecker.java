package com.liveramp.workflow2.workflow_monitor.alerts.execution.thresholds;

public interface ThresholdChecker {
  boolean isAlert(Double threshold, Double value);
}
