package com.liveramp.workflow_state.background_workflow;

import java.io.IOException;
import java.io.Serializable;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.liveramp.cascading_ext.resource.ResourceDeclarerFactory;
import com.liveramp.cascading_ext.resource.ResourceManager;
import com.liveramp.commons.Accessors;
import com.liveramp.commons.collections.list.ListBuilder;
import com.liveramp.commons.collections.nested_map.TwoNestedMap;
import com.liveramp.commons.collections.properties.NestedProperties;
import com.liveramp.databases.workflow_db.DatabasesImpl;
import com.liveramp.databases.workflow_db.IWorkflowDb;
import com.liveramp.databases.workflow_db.models.BackgroundAttemptInfo;
import com.liveramp.databases.workflow_db.models.BackgroundStepAttemptInfo;
import com.liveramp.databases.workflow_db.models.BackgroundWorkflowExecutorInfo;
import com.liveramp.databases.workflow_db.models.StepAttempt;
import com.liveramp.databases.workflow_db.models.StepDependency;
import com.liveramp.databases.workflow_db.models.WorkflowAttempt;
import com.liveramp.databases.workflow_db.models.WorkflowExecution;
import com.liveramp.java_support.functional.Fns8;
import com.liveramp.workflow.types.ExecutorStatus;
import com.liveramp.workflow.types.StepStatus;
import com.liveramp.workflow.types.WorkflowAttemptStatus;
import com.liveramp.workflow_core.WorkflowEnums;
import com.liveramp.workflow_core.background_workflow.BackgroundAction;
import com.liveramp.workflow_core.background_workflow.PreconditionFunction;
import com.liveramp.workflow_db_state.DbPersistence;
import com.liveramp.workflow_db_state.InitializedDbPersistence;
import com.liveramp.workflow_state.WorkflowStatePersistence;
import com.rapleaf.jack.AttributesWithId;
import com.rapleaf.jack.queries.Column;
import com.rapleaf.jack.queries.Updates;

public class BackgroundWorkflowExecutor {
  private static final Logger LOG = LoggerFactory.getLogger(BackgroundWorkflowExecutor.class);

  private static final long DEFAULT_HEARTBEAT_TIMEOUT = Duration.ofMinutes(15).toMillis();
  private static final long DEFAULT_HEARTBEAT_INTERVAL = Duration.ofSeconds(15).toMillis();

  private final List<String> runnableWorkflowNames;

  private enum Status {
    RUNNING,
    SHUTDOWN_GRACEFUL,
    SHUTDOWN_HARD;

    public boolean isShutdown() {
      return this == Status.SHUTDOWN_GRACEFUL || this == Status.SHUTDOWN_HARD;
    }

  }

  private Status status = Status.RUNNING;

  private final long pollDelay;
  private final int maxConcurrentSteps;
  private final ErrorReporter errorReporter;
  private final long heartbeatTimeoutMs;
  private final long heartbeatIntervalMs;

  private final String hostName;

  private final Thread pollerThread;

  public BackgroundWorkflowExecutor(List<String> runnableWorkflowNames,
                                    long pollDelayMs,
                                    int maxConcurrentSteps,
                                    ErrorReporter errorReporter,
                                    String hostName) {
    this(workflowDbNonCaching(), runnableWorkflowNames, pollDelayMs, DEFAULT_HEARTBEAT_TIMEOUT, DEFAULT_HEARTBEAT_INTERVAL, maxConcurrentSteps, errorReporter, hostName);
  }

  public BackgroundWorkflowExecutor(IWorkflowDb db,
                                    List<String> runnableWorkflowNames,
                                    long pollDelayMs,
                                    long heartbeatTimeoutMs,
                                    long heartbeatIntervalMs,
                                    int maxConcurrentSteps,
                                    ErrorReporter errorReporter,
                                    String hostName) {
    this.runnableWorkflowNames = runnableWorkflowNames;
    this.pollDelay = pollDelayMs;
    this.maxConcurrentSteps = maxConcurrentSteps;
    this.heartbeatTimeoutMs = heartbeatTimeoutMs;
    this.heartbeatIntervalMs = heartbeatIntervalMs;
    this.hostName = hostName;
    this.errorReporter = errorReporter;
    this.pollerThread = new Thread(new Runner(db));
  }

  public static IWorkflowDb workflowDbNonCaching() {
    IWorkflowDb workflowDb = new DatabasesImpl().getWorkflowDb();
    workflowDb.disableCaching();
    return workflowDb;
  }

  public void shutdown() throws InterruptedException {
    shutdown(false);
  }

  public void shutdown(boolean shouldInterrupt) throws InterruptedException {
    triggerShutdown(shouldInterrupt);
    pollerThread.join();
  }

  private void triggerShutdown(boolean hard) {
    if (hard) {
      status = Status.SHUTDOWN_HARD;
    } else {
      status = Status.SHUTDOWN_GRACEFUL;
    }
    pollerThread.interrupt();
  }

  public void start() {
    this.pollerThread.start();
  }

  public boolean claimStep(IWorkflowDb db, Long workerID, Long backgroundStepInfoId) throws IOException {

    Updates execute = db.createUpdate().table(BackgroundStepAttemptInfo.TBL)
        .where(BackgroundStepAttemptInfo.ID.equalTo(backgroundStepInfoId))
        .where(BackgroundStepAttemptInfo.BACKGROUND_WORKFLOW_EXECUTOR_INFO_ID.isNull())
        .set(BackgroundStepAttemptInfo.BACKGROUND_WORKFLOW_EXECUTOR_INFO_ID, workerID.intValue())
        .execute();

    return execute.getMatchedRowCount() > 0;
  }

  private class Heartbeat implements Runnable {

    private final long workerID;

    public Heartbeat(long workerID) {
      this.workerID = workerID;
    }

    @Override
    public void run() {
      //noinspection InfiniteLoopStatement
      IWorkflowDb workflowDb = new DatabasesImpl().getWorkflowDb();
      workflowDb.disableCaching();

      try {

        while (!Thread.interrupted()) {

          Thread.sleep(heartbeatIntervalMs);

          BackgroundWorkflowExecutorInfo workerInfo = workflowDb
              .backgroundWorkflowExecutorInfos()
              .find(workerID);

          long currentTime = System.currentTimeMillis();
          long lastHeartbeat = workerInfo.getLastHeartbeat();

          long timeElapsed = currentTime - lastHeartbeat;
          if (timeElapsed > heartbeatTimeoutMs) {
            throw new IOException("Elapsed time " + timeElapsed + " longer than timeout: " + heartbeatIntervalMs + ".  Shutting down hard.");
          }

          workerInfo
              .setLastHeartbeat(currentTime)
              .save();

        }

      } catch (InterruptedException e) {
        LOG.info("Heartbeat thread killed");
      } catch (Exception e) {

        try {
          BackgroundWorkflowExecutor.this.triggerShutdown(true);
        } finally {
          sendError("Worker heartbeat encountered exception", e);
        }

      } finally {

        try {
          workflowDb.backgroundWorkflowExecutorInfos().find(workerID)
              .setStatus(ExecutorStatus.STOPPED.ordinal())
              .save();
        } catch (Exception e) {
          sendError("Error marking executor dead", e);
        }

      }
    }

  }


  private void sendError(String title, Throwable error) {
    sendError(title, ExceptionUtils.getStackTrace(error));
  }

  private void sendError(String title, String message) {
    errorReporter.reportError(
        title, message, hostName, "application:background_workflow_executor"
    );
  }

  private BackgroundWorkflowExecutorInfo createInfo(IWorkflowDb workflowDb, String hostname) throws IOException {
    return workflowDb.backgroundWorkflowExecutorInfos().create(
        hostname, ExecutorStatus.RUNNING.ordinal(), System.currentTimeMillis()
    );
  }

  private class RunningStepManager {

    private final Map<Long, BackgroundAction> runningActions = Maps.newHashMap();
    //  this is obnoxious but I can't find a better way to interrupt running threads
    private final Map<Long, Future> runningThreads = Maps.newHashMap();

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public int getRunningStepCount() {
      return runningThreads.size();
    }

    public synchronized void submit(LiveAttemptCache cache,
                                    WorkflowExecution.Attributes executionAttributes,
                                    WorkflowAttempt.Attributes attemptAttributes,
                                    BackgroundAttemptInfo.Attributes backgroundInfo,
                                    StepAttempt.Attributes step,
                                    BackgroundAction action,
                                    Serializable context) {

      runningThreads.put(
          step.getId(),
          executorService.submit(new StepRunnable(cache, executionAttributes, attemptAttributes, backgroundInfo, step, action, context))
      );

      runningActions.put(
          step.getId(),
          action
      );

    }

    public void drainAll() throws InterruptedException {
      LOG.info("Draining workers");
      executorService.shutdown();
      LOG.info("Waiting for workers to terminate");
      executorService.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS);
    }

    public synchronized void reapComplete() {

      List<Long> toRemove = Lists.newArrayList();
      for (Map.Entry<Long, Future> entry : runningThreads.entrySet()) {
        if (entry.getValue().isDone()) {
          toRemove.add(entry.getKey());
        }
      }

      for (Long step : toRemove) {
        runningActions.remove(step);
        runningThreads.remove(step);
      }

    }

    public synchronized void stopAll() {

      //  try to gracefully stop everything using explicit action
      for (BackgroundAction action : runningActions.values()) {
        try {
          action.stop();
        } catch (InterruptedException e) {
          LOG.error("Exception stopping action " + action.getActionId(), e);
        }
      }

      //  interrupt threads otherwise
      for (Future future : runningThreads.values()) {
        future.cancel(true);
      }

    }

  }

  private class Runner implements Runnable {

    private final IWorkflowDb workflowDb;

    public Runner(IWorkflowDb workflowDb) {
      this.workflowDb = workflowDb;
    }

    public void run() {

      LiveAttemptCache cache = new LiveAttemptCache();
      RunningStepManager stepManager = new RunningStepManager();

      Thread heartbeatThread = null;

      try {

        BackgroundWorkflowExecutorInfo info = createInfo(workflowDb, hostName);
        LOG.info("Created background worker info: " + info);
        long workerID = info.getId();
        MDC.put("background_workflow_executor_id", Long.toString(workerID));

        heartbeatThread = new Thread(new Heartbeat(
            workerID
        ));
        heartbeatThread.start();

        while (true) {

          if (status.isShutdown()) {
            LOG.info("Poller thread received shutdown request");
            shutdownResources(cache, stepManager, heartbeatThread);
            break;
          }

          stepManager.reapComplete();

          //  this check isn't strictly necessary (there's another check internally) but we can avoid unnecessary
          if (stepManager.getRunningStepCount() < maxConcurrentSteps) {

            TwoNestedMap<WorkflowExecution.Attributes, WorkflowAttempt.Attributes, BackgroundAttemptInfo.Attributes> incompleteAttempts = getIncompleteAttempts(workflowDb);
            Map<StepAttempt.Attributes, BackgroundStepAttemptInfo.Attributes> allSteps = getIncompleteSteps(workflowDb, incompleteAttempts
                .entrySet().stream().map(entry -> entry.getK2().getIntId())
                .collect(Collectors.toSet())
            );

            //  TODO we can cache the dependency model for an attempt to reduce db churn
            List<StepDependency.Attributes> dependencies = getStepDependencies(workflowDb, allSteps);
            Map<Long, Long> activeWorkers = getAllWorkersForSteps(workflowDb, allSteps.keySet().stream()
                .map(AttributesWithId::getId).collect(Collectors.toList())
            );


            for (Map.Entry<Long, AttemptState> attemptEntry : splitStepsByAttempt(incompleteAttempts, allSteps, dependencies).entrySet()) {

              Long workflowAttemptID = attemptEntry.getKey();
              AttemptState state = attemptEntry.getValue();

              MDC.put("workflow_execution_id", Integer.toString(state.attributes.getWorkflowExecutionId()));
              MDC.put("workflow_attempt_id", Long.toString(state.attributes.getId()));
              if (state.executionAttributes.getScopeIdentifier() != null) {
                MDC.put("workflow_scope", state.executionAttributes.getScopeIdentifier());
              }

              //noinspection ConstantConditions
              switch (WorkflowAttemptStatus.findByValue(state.attributes.getStatus())) {

                case SHUTDOWN_PENDING:
                  // TODO add support.  tricky synchronization
                case RUNNING:
                case FAIL_PENDING:

                  long currentTime = System.currentTimeMillis();

                  Map<StepAttempt.Attributes, BackgroundStepAttemptInfo.Attributes> startableSteps = state.getStartableSteps();
                  Map<StepAttempt.Attributes, BackgroundStepAttemptInfo.Attributes> runningSteps = state.getRunningSteps();

                  for (Map.Entry<StepAttempt.Attributes, BackgroundStepAttemptInfo.Attributes> startable : startableSteps.entrySet()) {

                    if (stepManager.getRunningStepCount() < maxConcurrentSteps) {
                      attemptStep(workflowDb, workerID, stepManager, cache, state.backgroundInfo, attemptEntry, currentTime, startable.getValue(), startable.getKey());
                    }
                  }

                  //  nothing is running and nothing can launch.  so we mark this as ded.
                  if (runningSteps.isEmpty() && startableSteps.isEmpty()) {
                    cache.getPersistence(workflowAttemptID).markWorkflowStopped();
                  }

                  //  in case an executor died, we don't want to silently block on it forever
                  cleanupDeadExecutors(workerID, heartbeatTimeoutMs, cache, allSteps, activeWorkers, workflowAttemptID);

                  break;

              }

              MDC.remove("workflow_execution_id");
              MDC.remove("workflow_scope");
              MDC.remove("workflow_attempt_id");

            }

            //  TODO more efficient algorithm --
            //  1) batch up things we can potentially start
            //  2) as threads become free, try to start them
            //  3) if the start fails b/c it's already started, take the next one
            //  4) when we run out of work to do, run a new query

          }

          try {
            Thread.sleep(pollDelay);
          } catch (InterruptedException e) {
            //  this is probably fine, just a shutdown
            LOG.info("Poll thread interrupted");

          }


        }


      } catch (IOException e) {

        //  I'm not totally sure that failing out here is right, but just do it for now
        sendError("Error running executor poller", e);

        try {
          status = Status.SHUTDOWN_GRACEFUL;
          shutdownResources(cache, stepManager, heartbeatThread);
        } catch (Exception e1) {
          sendError("Error draining on error", e1);
        }

        throw new RuntimeException(e);
      }
    }

    private void cleanupDeadExecutors(long thisExecutorID, long heartbeatTimeoutMs, LiveAttemptCache cache, Map<StepAttempt.Attributes, BackgroundStepAttemptInfo.Attributes> allSteps, Map<Long, Long> activeWorkers, Long workflowAttemptID) throws IOException {
      for (Map.Entry<StepAttempt.Attributes, BackgroundStepAttemptInfo.Attributes> step : allSteps.entrySet()) {

        //  if step is not complete
        StepAttempt.Attributes stepAttempt = step.getKey();
        if (StepStatus.findByValue(stepAttempt.getStepStatus()) == StepStatus.RUNNING) {

          //  if it has an executor
          Integer executorID = step.getValue().getBackgroundWorkflowExecutorInfoId();
          if (executorID != null) {

            //  we let the heartbeat thread take care of it if the issue is happening locally
            if (executorID.longValue() != thisExecutorID) {

              //  if the worker is dead
              long lastHearbeat = executorID.longValue();
              if (activeWorkers.get(lastHearbeat) < System.currentTimeMillis() + heartbeatTimeoutMs) {

                //  mark it as failed
                String msg = "Failing step " + stepAttempt.getStepToken() + "because worker " + executorID + " has not heartbeat since " + new Date(lastHearbeat);
                LOG.warn(msg);

                cache.getPersistence(workflowAttemptID).markStepFailed(
                    stepAttempt.getStepToken(),
                    new Exception(msg)
                );

              }

            }
          }

        }

      }
    }

    private List<StepDependency.Attributes> getStepDependencies(IWorkflowDb workflowDb, Map<StepAttempt.Attributes, BackgroundStepAttemptInfo.Attributes> allSteps) throws IOException {
      return workflowDb.createQuery().from(StepAttempt.TBL)
          .where(StepAttempt.ID.in(allSteps.keySet().stream().map(AttributesWithId::getId).collect(Collectors.toList())))
          .innerJoin(StepDependency.TBL)
          .on(StepDependency.STEP_ATTEMPT_ID.equalTo(StepAttempt.ID))
          .fetch()
          .stream().map(record -> record.getAttributes(StepDependency.TBL))
          .collect(Collectors.toList());
    }

    private Map<StepAttempt.Attributes, BackgroundStepAttemptInfo.Attributes> getIncompleteSteps(IWorkflowDb workflowDb, Set<Integer> workflowAttemptIDs) throws IOException {
      return workflowDb.createQuery().from(StepAttempt.TBL)
          .where(StepAttempt.WORKFLOW_ATTEMPT_ID.in(workflowAttemptIDs))
          .innerJoin(BackgroundStepAttemptInfo.TBL)
          .on(BackgroundStepAttemptInfo.STEP_ATTEMPT_ID.equalTo(StepAttempt.ID))
          .select(new ListBuilder<Column>()
              .addAll(StepAttempt.TBL.getAllColumns())
              .add(BackgroundStepAttemptInfo.ID)
              .add(BackgroundStepAttemptInfo.NEXT_EXECUTE_CHECK)
              .add(BackgroundStepAttemptInfo.EXECUTE_CHECK_COOLDOWN_SECONDS)
              .add(BackgroundStepAttemptInfo.BACKGROUND_WORKFLOW_EXECUTOR_INFO_ID)
              .get())
          .fetch().stream()
          .collect(Collectors.toMap(
              record -> record.getAttributes(StepAttempt.TBL),
              record -> record.getAttributes(BackgroundStepAttemptInfo.TBL)
          ));
    }

    private Map<Long, Long> getAllWorkersForSteps(IWorkflowDb workflowDb, Collection<Long> stepIDs) throws IOException {
      return workflowDb.createQuery().from(StepAttempt.TBL)
          .where(StepAttempt.ID.in(stepIDs))
          .innerJoin(BackgroundStepAttemptInfo.TBL)
          .on(BackgroundStepAttemptInfo.STEP_ATTEMPT_ID.equalTo(StepAttempt.ID))
          .innerJoin(BackgroundWorkflowExecutorInfo.TBL)
          .on(BackgroundStepAttemptInfo.BACKGROUND_WORKFLOW_EXECUTOR_INFO_ID.equalTo(BackgroundWorkflowExecutorInfo.ID.as(Integer.class)))
          .select(BackgroundWorkflowExecutorInfo.ID, BackgroundWorkflowExecutorInfo.LAST_HEARTBEAT)
          .distinct()
          .fetch().stream().collect(
              Collectors.toMap(
                  record -> record.get(BackgroundWorkflowExecutorInfo.ID),
                  record -> record.get(BackgroundWorkflowExecutorInfo.LAST_HEARTBEAT)));
    }

    private TwoNestedMap<WorkflowExecution.Attributes, WorkflowAttempt.Attributes, BackgroundAttemptInfo.Attributes> getIncompleteAttempts(IWorkflowDb workflowDb) throws IOException {
      return workflowDb.createQuery()
          .from(WorkflowExecution.TBL)
          .where(WorkflowExecution.NAME.in(Sets.newHashSet(runnableWorkflowNames)))
          .innerJoin(WorkflowAttempt.TBL)
          .on(WorkflowAttempt.WORKFLOW_EXECUTION_ID.equalTo(WorkflowExecution.ID.as(Integer.class)))
          .where(WorkflowAttempt.STATUS.in(WorkflowEnums.EXECUTING_ATTEMPT_STATUSES), WorkflowAttempt.LAST_HEARTBEAT.equalTo(0L))
          .innerJoin(BackgroundAttemptInfo.TBL)
          .on(BackgroundAttemptInfo.WORKFLOW_ATTEMPT_ID.equalTo(WorkflowAttempt.ID))
          .select(WorkflowExecution.TBL, WorkflowAttempt.TBL, BackgroundAttemptInfo.TBL)
          .fetch()
          .stream()
          .collect(Fns8.toTwoNestedMap(
              record -> record.getAttributes(WorkflowExecution.TBL),
              record -> record.getAttributes(WorkflowAttempt.TBL),
              record -> record.getAttributes(BackgroundAttemptInfo.TBL)));
    }

    private void attemptStep(IWorkflowDb workflowDb, Long workerID, RunningStepManager manager, LiveAttemptCache cache, BackgroundAttemptInfo.Attributes backgroundInfo, Map.Entry<Long, AttemptState> attemptEntry, long currentTime, BackgroundStepAttemptInfo.Attributes info, StepAttempt.Attributes step) throws IOException {
      if (info.getNextExecuteCheck() <= currentTime) {
        String actionClass = step.getActionClass();


        try {

          BackgroundAction action = (BackgroundAction)Class.forName(actionClass).newInstance();

          Serializable context = (Serializable)SerializationUtils.deserialize(Accessors.only(workflowDb.createQuery().from(BackgroundStepAttemptInfo.TBL)
              .where(BackgroundStepAttemptInfo.ID.equalTo(info.getId()))
              .select(BackgroundStepAttemptInfo.SERIALIZED_CONTEXT)
              .select(Sets.newHashSet(BackgroundStepAttemptInfo.SERIALIZED_CONTEXT))
              .fetch()).get(BackgroundStepAttemptInfo.SERIALIZED_CONTEXT));

          PreconditionFunction allowExecute = action.getAllowExecute();
          Boolean canExecute = (Boolean)allowExecute.apply(context);

          if (canExecute) {
            //  if false, someone else got to it first -- this is fine, carry on
            if (claimStep(workflowDb, workerID, info.getId())) {
              AttemptState attrs = attemptEntry.getValue();


              manager.submit(
                  cache,
                  attrs.executionAttributes,
                  attrs.attributes,
                  backgroundInfo,
                  step,
                  action,
                  context
              );

            }
          } else {
            workflowDb.createUpdate().table(BackgroundStepAttemptInfo.TBL)
                .where(BackgroundStepAttemptInfo.ID.equalTo(info.getId()))
                .set(BackgroundStepAttemptInfo.NEXT_EXECUTE_CHECK, info.getExecuteCheckCooldownSeconds() * 1000 + currentTime)
                .execute();
          }
        } catch (IllegalAccessException | ClassNotFoundException | InstantiationException e) {
          //  send an error, but don't fail out, might make some migrations less painful
          sendError("Error instantiating provided class " + actionClass, e);
        }
      }
    }


  }

  private void shutdownResources(LiveAttemptCache cache, RunningStepManager stepManager, Thread heartbeatThread) throws IOException {
    try {

      if (status == Status.SHUTDOWN_GRACEFUL) {
        stepManager.drainAll();
      }

      if (status == Status.SHUTDOWN_HARD) {
        stepManager.stopAll();
      }

      if (heartbeatThread != null) {
        LOG.info("Interrupting heartbeat");
        heartbeatThread.interrupt();
        heartbeatThread.join();
      }

      LOG.info("Shutting down cache");
      cache.shutdown();

    } catch (InterruptedException e) {
      LOG.error("Interrupted waiting for shutdown, exiting now");
    }
  }


  private class StepRunnable implements Runnable {
    private final LiveAttemptCache cache;
    private final WorkflowExecution.Attributes executionAttributes;
    private final WorkflowAttempt.Attributes workflowAttempt;
    private final BackgroundAttemptInfo.Attributes backgroundInfo;
    private final StepAttempt.Attributes step;
    private final BackgroundAction action;
    private final Serializable context;

    public StepRunnable(LiveAttemptCache cache, WorkflowExecution.Attributes executionAttributes, WorkflowAttempt.Attributes attemptAttributes, BackgroundAttemptInfo.Attributes backgroundInfo, StepAttempt.Attributes step, BackgroundAction action, Serializable context) {
      this.cache = cache;
      this.executionAttributes = executionAttributes;
      this.workflowAttempt = attemptAttributes;
      this.backgroundInfo = backgroundInfo;
      this.step = step;
      this.action = action;
      this.context = context;
    }

    @Override
    public void run() {

      //  mark running
      WorkflowStatePersistence persistence = cache.getPersistence(workflowAttempt.getId());
      String token = step.getStepToken();

      try {

        MDC.put("workflow_execution_id", Integer.toString(workflowAttempt.getWorkflowExecutionId()));
        MDC.put("workflow_attempt_id", Long.toString(workflowAttempt.getId()));
        if (executionAttributes.getScopeIdentifier() != null) {
          MDC.put("workflow_scope", executionAttributes.getScopeIdentifier());
        }

        persistence.markStepRunning(token);

        try {

          action.initializeContext(context);

          List<String> nameParts = Lists.newArrayList(token.split("__"));

          action.getActionId()
              .setParentPrefix(StringUtils.join(nameParts.subList(0, nameParts.size() - 1), "__"))
              .setRelativeName(Accessors.last(nameParts));

          action.setOptionObjects(
              persistence,
              cache.getManager(workflowAttempt, backgroundInfo),
              null // TODO flow submission controller + store locker.  important but only once we have the hadoopy version.
          );

          //  TODO finish creating property models in migration, use here
          action.internalExecute(new NestedProperties(Collections.emptyMap(), true));

        } catch (Exception e) {
          LOG.error("Execution of step " + token + " failed!", e);
          persistence.markStepFailed(token, e);
        }

        persistence.markStepCompleted(token);

      } catch (IOException ie) {
        sendError("Internal error running step " + token, ie);
        BackgroundWorkflowExecutor.this.triggerShutdown(true);
      } finally {
        MDC.remove("workflow_execution_id");
        MDC.remove("workflow_scope");
        MDC.remove("workflow_attempt_id");
      }

    }
  }

  private class LiveAttemptCache {

    Map<Long, WorkflowStatePersistence> cachedPersistences = Maps.newHashMap();
    Map<Long, ResourceManager> cachedManagers = Maps.newHashMap();

    public synchronized ResourceManager getManager(WorkflowAttempt.Attributes attempt, BackgroundAttemptInfo.Attributes info) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {

      long attemptId = attempt.getId();
      if (!cachedManagers.containsKey(attemptId)) {
        String factoryClass = info.getResourceManagerFactory();
        ResourceDeclarerFactory factory = (ResourceDeclarerFactory)Class.forName(factoryClass).newInstance();
        cachedManagers.put(attemptId, factory.create().create(attempt.getWorkflowExecutionId(), InitializedDbPersistence.class.getName()));
      }

      return cachedManagers.get(attemptId);

    }

    public synchronized WorkflowStatePersistence getPersistence(Long workflowAttemptID) {
      if (!cachedPersistences.containsKey(workflowAttemptID)) {
        cachedPersistences.put(workflowAttemptID, DbPersistence.queryPersistence(workflowAttemptID, new DatabasesImpl().getWorkflowDb()));
      }
      return cachedPersistences.get(workflowAttemptID);
    }

    public synchronized void shutdown() throws IOException {
      for (WorkflowStatePersistence persistence : cachedPersistences.values()) {
        persistence.shutdown();
      }
    }

  }

  private Map<Long, AttemptState> splitStepsByAttempt(
      TwoNestedMap<WorkflowExecution.Attributes, WorkflowAttempt.Attributes, BackgroundAttemptInfo.Attributes> attempts,
      Map<StepAttempt.Attributes, BackgroundStepAttemptInfo.Attributes> attemptSteps,
      List<StepDependency.Attributes> dependencies) {

    Multimap<Long, StepAttempt.Attributes> stepAttemptsByWA = HashMultimap.create();

    Map<Long, StepAttempt.Attributes> stepsByID = Maps.newHashMap();
    for (Map.Entry<StepAttempt.Attributes, BackgroundStepAttemptInfo.Attributes> entry : attemptSteps.entrySet()) {
      stepsByID.put(entry.getKey().getId(), entry.getKey());
    }

    for (StepAttempt.Attributes attributes : attemptSteps.keySet()) {
      stepAttemptsByWA.put((long)attributes.getWorkflowAttemptId(), attributes);
    }

    Multimap<Long, StepDependency.Attributes> dependenciesByWA = HashMultimap.create();

    for (StepDependency.Attributes dependency : dependencies) {
      dependenciesByWA.put(
          (long)stepsByID.get(dependency.getStepAttemptId()).getWorkflowAttemptId(),
          dependency
      );
    }


    Map<Long, AttemptState> attemptStates = Maps.newHashMap();

    for (TwoNestedMap.Entry<WorkflowExecution.Attributes, WorkflowAttempt.Attributes, BackgroundAttemptInfo.Attributes> entry : attempts.entrySet()) {

      WorkflowAttempt.Attributes attempt = entry.getK2();

      Map<StepAttempt.Attributes, BackgroundStepAttemptInfo.Attributes> scopedInfos = Maps.newHashMap();
      for (StepAttempt.Attributes stepAttempt : stepAttemptsByWA.get(attempt.getId())) {
        scopedInfos.put(stepAttempt, attemptSteps.get(stepAttempt));
      }

      DirectedGraph<Long, DefaultEdge> graph = new SimpleDirectedGraph<>(DefaultEdge.class);

      for (StepAttempt.Attributes attributes : stepAttemptsByWA.get(attempt.getId())) {
        graph.addVertex(attributes.getId());
      }

      for (StepDependency.Attributes edge : dependenciesByWA.get(attempt.getId())) {
        graph.addEdge(edge.getStepAttemptId(), edge.getDependencyAttemptId());
      }

      attemptStates.put(attempt.getId(), new AttemptState(entry.getK1(), attempt, entry.getValue(), scopedInfos, graph));

    }


    return attemptStates;

  }

  private class AttemptState {

    private final WorkflowExecution.Attributes executionAttributes;
    private final WorkflowAttempt.Attributes attributes;
    private final BackgroundAttemptInfo.Attributes backgroundInfo;
    private final Map<StepAttempt.Attributes, BackgroundStepAttemptInfo.Attributes> incompleteSteps;
    private final Map<Long, StepAttempt.Attributes> stepsByID;
    private final DirectedGraph<Long, DefaultEdge> graph;

    public AttemptState(WorkflowExecution.Attributes execution,
                        WorkflowAttempt.Attributes attributes,
                        BackgroundAttemptInfo.Attributes backgroundInfo,
                        Map<StepAttempt.Attributes, BackgroundStepAttemptInfo.Attributes> incompleteSteps,
                        DirectedGraph<Long, DefaultEdge> graph) {

      Map<Long, StepAttempt.Attributes> steps = Maps.newHashMap();
      for (StepAttempt.Attributes step : incompleteSteps.keySet()) {
        steps.put(step.getId(), step);
      }

      this.executionAttributes = execution;
      this.attributes = attributes;
      this.backgroundInfo = backgroundInfo;
      this.incompleteSteps = incompleteSteps;
      this.stepsByID = steps;
      this.graph = graph;
    }

    public Map<StepAttempt.Attributes, BackgroundStepAttemptInfo.Attributes> getRunningSteps() {

      Map<StepAttempt.Attributes, BackgroundStepAttemptInfo.Attributes> running = Maps.newHashMap();
      for (Map.Entry<StepAttempt.Attributes, BackgroundStepAttemptInfo.Attributes> entry : incompleteSteps.entrySet()) {
        if (entry.getKey().getStepStatus() == StepStatus.RUNNING.getValue()) {
          running.put(entry.getKey(), entry.getValue());
        }
      }

      return running;
    }

    public Map<StepAttempt.Attributes, BackgroundStepAttemptInfo.Attributes> getStartableSteps() {

      Map<StepAttempt.Attributes, BackgroundStepAttemptInfo.Attributes> startable = Maps.newHashMap();
      for (Map.Entry<StepAttempt.Attributes, BackgroundStepAttemptInfo.Attributes> entry : incompleteSteps.entrySet()) {
        if (isStartable(entry.getKey())) {
          startable.put(entry.getKey(), entry.getValue());
        }
      }

      return startable;

    }

    private boolean isStartable(StepAttempt.Attributes step) {

      if (step.getStepStatus() != StepStatus.WAITING.ordinal()) {
        return false;
      }

      for (DefaultEdge dependency : graph.outgoingEdgesOf(step.getId())) {
        StepAttempt.Attributes attributes = stepsByID.get(graph.getEdgeTarget(dependency));
        if (!WorkflowEnums.NON_BLOCKING_STEP_STATUSES.contains(StepStatus.findByValue(attributes.getStepStatus()))) {
          return false;
        }
      }

      return true;

    }

  }

}
