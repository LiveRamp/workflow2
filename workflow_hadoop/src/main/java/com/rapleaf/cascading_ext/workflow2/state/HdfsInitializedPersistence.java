package com.rapleaf.cascading_ext.workflow2.state;

import java.io.IOException;
import java.util.Random;
import java.util.Set;

import org.apache.hadoop.fs.FileSystem;

import com.liveramp.java_support.alerts_handler.AlertsHandler;
import com.liveramp.workflow_state.InitializedPersistence;
import com.liveramp.workflow_state.WorkflowRunnerNotification;

public class HdfsInitializedPersistence implements InitializedPersistence {

  private final Long executionID;
  private final String name;
  private String priority;
  private String pool;
  private final String host;
  private final String username;
  private final AlertsHandler handler;
  private final Set<WorkflowRunnerNotification> configuredNotifications;
  private final FileSystem fs;

  private long attempt;

  public HdfsInitializedPersistence(Long executionID,
                                    String name,
                                    String priority,
                                    String pool,
                                    String host,
                                    String username,
                                    AlertsHandler handler,
                                    Set<WorkflowRunnerNotification> configuredNotifications,
                                    FileSystem fs) {
    this.executionID = executionID;
    this.name = name;
    this.priority = priority;
    this.pool = pool;
    this.host = host;
    this.username = username;
    this.handler = handler;
    this.configuredNotifications = configuredNotifications;
    this.fs = fs;
    this.attempt = Math.abs(new Random().nextLong());
  }

  @Override
  public long getExecutionId() throws IOException {
    return executionID;
  }

  @Override
  public long getAttemptId() throws IOException {
    return attempt;
  }

  @Override
  public void markWorkflowStopped() throws IOException {
    //  no op
  }

  @Override
  public void shutdown() throws IOException {
    fs.close();
  }

  public FileSystem getFs() {
    return fs;
  }

  public String getName() {
    return name;
  }

  public String getPriority() {
    return priority;
  }

  public String getPool() {
    return pool;
  }

  public String getHost() {
    return host;
  }

  public String getUsername() {
    return username;
  }

  public AlertsHandler getHandler() {
    return handler;
  }

  public Set<WorkflowRunnerNotification> getConfiguredNotifications() {
    return configuredNotifications;
  }

  public void setPriority(String priority) {
    this.priority = priority;
  }

  public void setPool(String pool) {
    this.pool = pool;
  }
}
