package com.liveramp.workflow_core.info;

import java.io.IOException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liveramp.java_support.alerts_handler.AlertsHandler;
import com.liveramp.workflow_core.BaseWorkflowOptions;
import com.liveramp.workflow_state.InitializedPersistence;

public class WorkflowInfoImpl implements WorkflowInfo {

  private final long executionId;
  private final long attemptId;
  private final Optional<String> appName;
  private final Optional<String> scopeId;
  private final Optional<String> description;
  private final Optional<String> hostname;
  private final Optional<AlertsHandler> alertsHandler;

  private static Logger LOG = LoggerFactory.getLogger(WorkflowInfoImpl.class);
  private final String persistenceClassName;

  public WorkflowInfoImpl(InitializedPersistence persistence, BaseWorkflowOptions options) {
    try {
      this.executionId = persistence.getExecutionId();
      this.attemptId = persistence.getAttemptId();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    this.appName = Optional.ofNullable(options.getAppName());
    this.scopeId = Optional.ofNullable(options.getScopeIdentifier());
    this.description = Optional.ofNullable(options.getDescription());
    this.hostname = options.getHostnameProvider() != null ?
        Optional.ofNullable(options.getHostnameProvider().getHostname()) :
        Optional.empty();
    this.alertsHandler = Optional.ofNullable(options.getAlertsHandler());
    this.persistenceClassName = persistence.getClass().getName();
  }

  @Override
  public long getExecutionId() {
    return this.executionId;
  }

  @Override
  public long getAttemptId() {
    return this.attemptId;
  }

  @Override
  public Optional<String> getAppName() {
    return this.appName;
  }

  @Override
  public Optional<String> getScopeIdentifier() {
    return this.scopeId;
  }

  @Override
  public Optional<String> getDescription() {
    return this.description;
  }

  @Override
  public Optional<String> getHostname() {
    return this.hostname;
  }

  @Override
  public Optional<AlertsHandler> getAlertsHandler() {
    return this.alertsHandler;
  }

  @Override
  public String getPersistenceClassName() {
    return this.persistenceClassName;
  }
}
