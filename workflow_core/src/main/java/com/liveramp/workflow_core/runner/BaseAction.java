package com.liveramp.workflow_core.runner;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liveramp.cascading_ext.resource.ReadResource;
import com.liveramp.cascading_ext.resource.ReadResourceContainer;
import com.liveramp.cascading_ext.resource.Resource;
import com.liveramp.cascading_ext.resource.ResourceManager;
import com.liveramp.cascading_ext.resource.WriteResource;
import com.liveramp.cascading_ext.resource.WriteResourceContainer;
import com.liveramp.commons.collections.nested_map.ThreeNestedMap;
import com.liveramp.commons.collections.nested_map.TwoNestedMap;
import com.liveramp.commons.collections.properties.NestedProperties;
import com.liveramp.commons.collections.properties.OverridableProperties;
import com.liveramp.commons.concurrent.NamedThreadFactory;
import com.liveramp.java_support.concurrent.DaemonThreadFactory;
import com.liveramp.java_support.workflow.ActionId;
import com.liveramp.workflow_state.DSAction;
import com.liveramp.workflow_state.DataStoreInfo;
import com.liveramp.workflow_state.StepState;
import com.liveramp.workflow_state.WorkflowStatePersistence;


/**
 * Provides the base class for a workflow "action," an executable unit
 * that runs within a {@link BaseStep} object in composite work graphs.
 * Each workflow step contains an action that provides
 * the actual work to be done.  The step supplies the node in the workflow
 * graph, while the action supplies the code to be executed.
 *
 * Additionally, a step may have a set of children (see {@see #addChild})
 * and a set of dependencies (supplied during construction).  The workflow
 * controls the execution of both children and dependencies.
 *
 * @param <Config> The underlying configuration for this action.  Typically, this will be a {@link
 * Resource} that holds a special object.  The actual configuration object may be specified through
 * {@link #setOptionObjects}.  The action's implementation may choose to alter its behavior based on
 * the configuration values.
 */

public abstract class BaseAction<Config> {

  private static final Logger LOG = LoggerFactory.getLogger(BaseAction.class);

  private final ActionId actionId;

  private OverridableProperties stepProperties;
  private OverridableProperties combinedProperties;

  private final Map<Resource, ReadResourceContainer> readResources = Maps.newHashMap();
  private final Map<Resource, WriteResourceContainer> writeResources = Maps.newHashMap();

  private ResourceManager resourceManager;

  private transient WorkflowStatePersistence persistence;
  private Config config;

  private ScheduledExecutorService statusCallbackExecutor;
  private StatusCallback statusCallback;

  public BaseAction(String checkpointToken) {
    this(checkpointToken, Maps.newHashMap());
  }

  public BaseAction(String checkpointToken, Map<Object, Object> properties) {
    this.actionId = new ActionId(checkpointToken);
    this.stepProperties = new NestedProperties(properties, false);
  }

  public ActionId getActionId() {
    return actionId;
  }

  public String fullId() {
    return actionId.resolve();
  }

  protected WorkflowStatePersistence getPersistence() {
    return persistence;
  }

  protected OverridableProperties getCombinedProperties() {
    return combinedProperties;
  }

  protected OverridableProperties getStepProperties() {
    return stepProperties;
  }

  protected Config getConfig() {
    return config;
  }

  //  before workflow runs
  protected void initialize(Config context) {
    //  default no op
  }

  //  when step runs, before Action execute
  protected void preExecute() throws Exception {
    //  default no op
  }

  //  action work, implemented by end-user
  protected abstract void execute() throws Exception;

  //  attempt to immediately stop execution of this action.  expect the result to be a
  //  FAILED step
  public void stop() throws InterruptedException {

  }

  protected void rollback() throws Exception {

  }

  //  after execute, either fail or succeed
  protected void postExecute() {
    //  default no op
  }

  //  inputs and outputs of the action
  public Multimap<DSAction, DataStoreInfo> getAllDataStoreInfo() {
    return HashMultimap
        .create(); // TODO make Action stop storing DataStore and store DataStoreInfo in this class
  }

  //  not really public : / make package private after cleanup
  public final void setOptionObjects(WorkflowStatePersistence persistence,
      ResourceManager resourceManager,
      Config context) {
    this.persistence = persistence;
    this.config = context;
    this.resourceManager = resourceManager;

    initialize(context);
  }

  //  resource actions

  protected <T> ReadResource<T> readsFrom(Resource<T> resource) {
    ReadResourceContainer<T> container = new ReadResourceContainer<T>();
    readResources.put(resource, container);
    return container;
  }

  protected <T> WriteResource<T> creates(Resource<T> resource) {
    WriteResourceContainer<T> container = new WriteResourceContainer<T>();
    writeResources.put(resource, container);
    return container;
  }


  protected <T> T get(ReadResource<T> resource) {
    return (T) resourceManager.read(resource);
  }

  protected <T, R extends WriteResource<T>> void set(R resource, T value) {
    resourceManager.write(resource, value);
  }

  protected interface StatusCallback {

    String updateStatus();
  }

  protected void setStatusCallback(StatusCallback callback) throws IOException {
    statusCallback = callback;
  }


  //  not really public : /
  public final void internalExecute(OverridableProperties parentProperties) {

    ScheduledExecutorService callbackExecutor = null;

    try {

      //  only create a new thread if we have a callback we want to use
      if (hasStatusCallback()) {
        callbackExecutor = Executors.newSingleThreadScheduledExecutor(
            new DaemonThreadFactory(new NamedThreadFactory("status-callback-thread")));
        callbackExecutor.scheduleAtFixedRate(() -> {
          try {
            setStatusMessage(statusCallback.updateStatus());
          } catch (IOException e) {
            LOG.error("Failed to update status: ", e);
          }
        }, 0L, 30L, TimeUnit.SECONDS);
      }

      combinedProperties = stepProperties.override(parentProperties);

      preExecute();

      LOG.info("Setting read resources");
      for (Resource resource : readResources.keySet()) {
        readResources.get(resource).setResource(resourceManager.getReadPermission(resource));
      }

      LOG.info("Setting write resources");
      for (Resource resource : writeResources.keySet()) {
        writeResources.get(resource).setResource(resourceManager.getWritePermission(resource));
      }

      execute();

      //  update status one last time
      if (hasStatusCallback()) {
        setStatusMessage(statusCallback.updateStatus());
      }

    } catch (Throwable t) {
      LOG.error("Action " + fullId() + " failed due to Throwable", t);
      throw wrapRuntimeException(t);
    } finally {

      if (callbackExecutor != null) {
        callbackExecutor.shutdown();
      }

      postExecute();
    }
  }

  private boolean hasStatusCallback() {
    return statusCallback != null;
  }

  protected final void internalRollback(OverridableProperties properties) {

    try {

      combinedProperties = stepProperties.override(properties);

      rollback();

    } catch (Throwable t) {
      LOG.error("Rollback of action " + fullId() + " failed due to Throwable", t);
      throw wrapRuntimeException(t);
    }
  }

  private static RuntimeException wrapRuntimeException(Throwable t) {
    return (t instanceof RuntimeException) ? (RuntimeException) t : new RuntimeException(t);
  }

  /**
   * Set an application-specific status message to display. This will be
   * visible in the logs as well as through the UI. This is a great place to
   * report progress or choices.
   */
  protected void setStatusMessage(String statusMessage) throws IOException {
    LOG.info("Status Message: " + statusMessage);
    persistence.markStepStatusMessage(fullId(), statusMessage);
  }

  protected String getStatusMessage() throws IOException {
    return persistence.getStatusMessage(fullId());
  }

  /**
   * Same as {@link #setStatusMessage(String)} but only logs failures
   * doesn't rethrow.
   */
  protected void setStatusMessageSafe(String message) {
    try {
      setStatusMessage(message);
    } catch (Exception e) {
      LOG.warn("Couldn't set status message.", e);
    }
  }

  protected Optional<String> getStatusMessageSafe() {
    try {
      return Optional.of(getStatusMessage());
    } catch (Exception e) {
      LOG.warn("Couldn't get status message.", e);
      return Optional.empty();
    }
  }

  protected long getCurrentExecutionId() throws IOException {
    return persistence.getExecutionId();
  }

  protected TwoNestedMap<String, String, Long> getFlatCounters() throws IOException {
    return persistence.getFlatCounters();
  }

  protected ThreeNestedMap<String, String, String, Long> getAllCountersByStep() throws IOException {
    return persistence.getCountersByStep();
  }

  //  TODO package-private after class cleanup
  public TwoNestedMap<String, String, Long> getCurrentStepCounters() throws IOException {
    ThreeNestedMap<String, String, String, Long> allCounters = persistence.getCountersByStep();
    return allCounters.get(getActionId().resolve());
  }

  //  TODO package-private after class cleanup
  public DurationInfo getDurationInfo() throws IOException {
    Map<String, StepState> stepStatuses = persistence.getStepStates();
    StepState stepState = stepStatuses.get(getActionId().resolve());
    return new DurationInfo(stepState.getStartTimestamp(), stepState.getEndTimestamp());
  }

  public static class DurationInfo {

    private final long startTime;
    private final long endTime;

    public DurationInfo(long startTime, long endTime) {
      this.startTime = startTime;
      this.endTime = endTime;
    }

    public long getStartTime() {
      return startTime;
    }

    public long getEndTime() {
      return endTime;
    }

  }


  @Override
  public String toString() {
    return getClass().getSimpleName() + " [checkpointToken=" + getActionId().getRelativeName()
        + "]";
  }

}
