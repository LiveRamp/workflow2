package com.rapleaf.cascading_ext.workflow2;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liveramp.commons.collections.properties.NestedProperties;
import com.liveramp.commons.collections.properties.OverridableProperties;
import com.liveramp.commons.util.MultiShutdownHook;
import com.liveramp.workflow.types.StepStatus;
import com.liveramp.workflow_core.alerting.AlertsHandlerFactory;
import com.liveramp.workflow_core.runner.BaseStep;
import com.liveramp.workflow_state.WorkflowStatePersistence;
import com.rapleaf.cascading_ext.workflow2.rollback.SuccessCallback;
import com.rapleaf.cascading_ext.workflow2.strategy.WorkflowStrategy;

import static com.liveramp.workflow_core.WorkflowConstants.JOB_POOL_PARAM;
import static com.liveramp.workflow_core.WorkflowConstants.JOB_PRIORITY_PARAM;

public class StepExecutor<Config> {
  private static final Logger LOG = LoggerFactory.getLogger(StepExecutor.class);


  /**
   * semaphore used to control the max number of running components
   */
  private final Semaphore semaphore;

  /**
   * components that haven't yet been started
   */
  private final Set<StepRunner> pendingSteps = new HashSet<StepRunner>();
  /**
   * components that have been started and not yet finished
   */
  private final Set<StepRunner> runningSteps = new HashSet<StepRunner>();
  /**
   * started and completed successfully
   */
  private final Set<StepRunner> completedSteps = new HashSet<StepRunner>();

  //  set this if something fails in a step (outside user-code) so we don't keep trying to start steps
  private List<Exception> internalErrors = new CopyOnWriteArrayList<Exception>();

  private final DirectedGraph<BaseStep<Config>, DefaultEdge> dependencyGraph;

  private final WorkflowStrategy<Config> strategy;
  private final WorkflowStatePersistence persistence;

  private final int stepPollInterval;
  private final int maxConcurrentSteps;
  private final OverridableProperties parentProperties;

  private final MultiShutdownHook hook;

  private final BaseWorkflowRunner.OnStepRunnerStart onStart;

  private final NotificationManager notifications;

  private final List<SuccessCallback> successCallbacks;

  private final AlertsHandlerFactory alertsHandlerFactory;

  public StepExecutor(WorkflowStrategy<Config> strategy,
                      WorkflowStatePersistence persistence,
                      int maxConcurrentSteps,
                      int stepPollInterval,
                      DirectedGraph<BaseStep<Config>, DefaultEdge> dependencyGraph,
                      OverridableProperties parentProperties,
                      MultiShutdownHook hook,
                      AlertsHandlerFactory alertsHandlerFactory,
                      BaseWorkflowRunner.OnStepRunnerStart onStart,
                      List<SuccessCallback> successCallbacks,
                      NotificationManager notifications) {

    this.semaphore = new Semaphore(maxConcurrentSteps);

    this.maxConcurrentSteps = maxConcurrentSteps;
    this.stepPollInterval = stepPollInterval;
    this.parentProperties = parentProperties;
    this.strategy = strategy;
    this.persistence = persistence;
    this.dependencyGraph = dependencyGraph;
    this.hook = hook;
    this.onStart = onStart;
    this.successCallbacks = successCallbacks;
    this.notifications = notifications;
    this.alertsHandlerFactory = alertsHandlerFactory;

  }

  private boolean shouldKeepStartingSteps() throws IOException {
    return persistence.getShutdownRequest() == null && internalErrors.isEmpty();
  }

  public void clearFinishedSteps() throws IOException {
    {
      Iterator<StepExecutor<Config>.StepRunner> iter = runningSteps.iterator();
      while (iter.hasNext()) {
        StepExecutor.StepRunner cr = iter.next();
        //LOG.info("Checking persistence for " + cr.step.getCheckpointToken());
        switch (persistence.getStatus(cr.step.getCheckpointToken())) {
          case COMPLETED:
          case SKIPPED:
            completedSteps.add(cr);
            iter.remove();
            break;
          case FAILED:
            iter.remove();
            break;
        }
      }
    }
  }

  private List<StepRunner> getStartableSteps() throws IOException {

    Map<String, StepStatus> stepStatuses = persistence.getStepStatuses();

    List<StepRunner> allStartable = Lists.newArrayList();
    for (StepRunner cr : pendingSteps) {

      if (allDependenciesCompleted(stepStatuses, cr.getStep(), dependencyGraph)) {
        allStartable.add(cr);
      }
    }
    return allStartable;
  }


  public boolean existUnblockedSteps() throws IOException {
    Queue<BaseStep<Config>> explore = Lists.newLinkedList();

    Map<String, StepStatus> allStatuses = persistence.getStepStatuses();

    //  get failed steps
    for (BaseStep<Config> step : dependencyGraph.vertexSet()) {
      if (strategy.getFailureStatuses().contains(allStatuses.get(step.getCheckpointToken()))) {
        explore.add(step);
      }
    }

    Set<String> blockedSteps = getAllDownstreamSteps(dependencyGraph, explore);

    //  if any pending steps are not in this set, they can still plausibly run
    for (StepExecutor.StepRunner pendingStep : pendingSteps) {
      if (!blockedSteps.contains(pendingStep.getStep().getCheckpointToken())) {
        return true;
      }
    }

    return false;

  }

  public Set<String> getAllDownstreamSteps(DirectedGraph<BaseStep<Config>, DefaultEdge> dependencyGraph,
                                           Collection<BaseStep<Config>> baseSteps) {

    Set<String> blockedSteps = Sets.newHashSet();
    //  get any part of the graph depending on failed steps

    Queue<BaseStep<Config>> queue = Lists.newLinkedList(baseSteps);

    while (!queue.isEmpty()) {
      BaseStep<Config> step = queue.poll();
      if (!blockedSteps.contains(step.getCheckpointToken())) {
        blockedSteps.add(step.getCheckpointToken());

        queue.addAll(strategy.getDownstreamSteps(dependencyGraph, step));
      }
    }

    return blockedSteps;
  }

  public boolean allDependenciesCompleted(Map<String, StepStatus> statuses, BaseStep<Config> step, DirectedGraph<BaseStep<Config>, DefaultEdge> dependencyGraph) throws IOException {

    for (BaseStep<Config> dep : strategy.getUpstreamSteps(dependencyGraph, step)) {
      if (!strategy.getNonBlockingStatuses().contains(statuses.get(dep.getCheckpointToken()))) {
        return false;
      }
    }

    return true;
  }

  public void doRun() throws IOException {

    // Notify
    LOG.info(notifications.getStartSubject());
    // Note: start email after web server so that UI is functional
    notifications.sendStartEmail();

    strategy.markAttemptStart(persistence);


    for (BaseStep<Config> step : dependencyGraph.vertexSet()) {
      StepRunner runner = new StepRunner(step, persistence);
      pendingSteps.add(runner);
    }

    MultiShutdownHook.Hook onShutdown = new MultiShutdownHook.Hook() {
      @Override
      public void onShutdown() throws Exception {
        LOG.info("Marking running steps as failed");
        for (StepRunner runningStep : runningSteps) {

          strategy.markStepFailure(
              persistence,
              runningStep.step.getCheckpointToken(),
              new RuntimeException("Workflow process killed!")
          );

        }
      }
    };

    this.hook.add(onShutdown);

    try {
      while (pendingSteps.size() > 0 && shouldKeepStartingSteps() && existUnblockedSteps()) {
        // process any completed/failed steps
        clearFinishedSteps();

        // acquire semaphore so we don't do any polling until there are free permits
        semaphore.acquire();

        //  release the permit so it can be taken by a step
        semaphore.release();

        // check if there are any startable steps
        List<StepRunner> startableSteps = getStartableSteps();

        //  start each of them.  if we block for a while waiting for a free permit, that's fine
        for (StepRunner startableStep : startableSteps) {

          semaphore.acquire();

          //  we only check for shutdown requests here because we know that we do have a runnable step already
          if (!shouldKeepStartingSteps()) {
            LOG.info("Exiting early because of internal error or shutdown request");
            semaphore.release();
            break;
          }

          // start one startable
          runningSteps.add(startableStep);
          pendingSteps.remove(startableStep);
          startableStep.start();

          // note that we explicitly don't release the semaphore here. this is
          // because the actual step runner thread will release it when it's
          // complete (or failed).

        }

        //  if there was nothing to do this time, block for a while before trying again
        //  otherwise, we may have blocked for a while, and now want to poll immediately
        if (startableSteps.isEmpty()) {
          Thread.sleep(stepPollInterval);
        }
      }
    } catch (InterruptedException e) {
      LOG.warn("Interrupted waiting to acquire semaphore.", e);
      runningSteps.forEach(StepRunner::stop);
    } finally {
      LOG.info("Removing shutdown hook");
      this.hook.remove(onShutdown);
    }

    // acquire all the permits on the semaphore. this will guarantee that zero
    // components are running.
    try {
      semaphore.acquire(maxConcurrentSteps);
    } catch (InterruptedException e) {
      runningSteps.forEach(StepRunner::stop);
      try {
        // acquire all the permits on the semaphore. Again, this will guarantee that no
        // step is running, which means that they exited after receiving the interrupt.
        semaphore.acquire(maxConcurrentSteps);
      } catch (InterruptedException ex) {
        //should never happen; thread has already been interrupted
        throw new RuntimeException("Interrupted waiting for running steps to finish", e);
      }
      throw new RuntimeException("Interrupted waiting for running steps to complete!", e);
    }

    // there are now no running steps. clear the finished ones again in
    // case someone failed.
    clearFinishedSteps();

    persistence.markWorkflowStopped();

    // if there are any failures, then the workflow failed. throw an exception.
    if (isFailPending(persistence)) {
      String failureMessage = notifications.buildStepsFailureMessage();
      notifications.sendFailureEmail(failureMessage);
      throw new RuntimeException(notifications.getFailureSubject() + "\n" + failureMessage);
    }

    //  something internal to WorkflowRunner failed.
    if (!internalErrors.isEmpty()) {
      LOG.error("WorkflowRunner has encountered an internal error");
      notifications.sendInternalErrorMessage(internalErrors);
      throw new RuntimeException(notifications.getFailureSubject() + " internal WorkflowRunner error");
    }

    // nothing failed, but if there are steps that haven't been executed, it's
    // because someone shut down the workflow.
    if (pendingSteps.size() > 0) {
      String reason = notifications.getReasonForShutdownRequest();
      notifications.sendShutdownEmail(reason);
      throw new RuntimeException(notifications.getShutdownSubject(reason));
    }

    // Notify success
    notifications.sendSuccessEmail();

    LOG.info(notifications.getSuccessSubject());

    for (SuccessCallback successCallback : successCallbacks) {
      try {
        successCallback.onSuccess(persistence);
      } catch (Exception e) {
        LOG.error("Error calling success callback: ", e);
      }
    }

  }

  public boolean isFailPending(WorkflowStatePersistence persistence) throws IOException {

    for (Map.Entry<String, StepStatus> entry : persistence.getStepStatuses().entrySet()) {
      if (strategy.getFailureStatuses().contains(entry.getValue())) {
        return true;
      }
    }

    return false;

  }


  /**
   * StepRunner keeps track of some extra state for each component, as
   * well as manages the actual execution thread. Note that it is itself *not*
   * a Thread.
   */
  public final class StepRunner {
    public final BaseStep<Config> step;
    private final WorkflowStatePersistence state;
    public Thread thread;

    public StepRunner(BaseStep<Config> c, WorkflowStatePersistence state) {
      this.step = c;
      this.state = state;
    }

    private OverridableProperties buildInheritedProperties() throws IOException {
      NestedProperties.Builder uiPropertiesBuilder = new NestedProperties.Builder();
      String priority = persistence.getPriority();
      String pool = persistence.getPool();

      if (priority != null) {
        uiPropertiesBuilder.setProperty(JOB_PRIORITY_PARAM, priority);
      }

      if (pool != null) {
        uiPropertiesBuilder.setProperty(JOB_POOL_PARAM, pool);
      }

      return uiPropertiesBuilder.build().override(parentProperties);
    }

    public void start() {

      onStart.onStart();
      Runnable r = new Runnable() {
        @Override
        public void run() {
          String stepToken = step.getCheckpointToken();
          try {
            if (strategy.getNonBlockingStatuses().contains(state.getStatus(stepToken))) {
              LOG.info("Step " + stepToken + " was executed successfully in a prior run. Skipping.");
            } else {

              strategy.markStepStart(persistence, stepToken);

              LOG.info("Executing step " + stepToken);
              strategy.run(step, buildInheritedProperties());

              strategy.markStepCompleted(persistence, stepToken);
            }
          } catch (Throwable e) {

            LOG.error("Step " + stepToken + " failed!", e);

            try {
              strategy.markStepFailure(persistence, stepToken, e);

              //  only alert about this specific step failure if we aren't about to fail
              if (existUnblockedSteps() || runningSteps.size() > 1) {
                notifications.sendStepFailureEmail(stepToken);
              }

            } catch (Exception e2) {
              LOG.error("Could not update step " + stepToken + " to failed! ", e2);
              internalErrors.add(e2);
            }


          } finally {
            semaphore.release();
          }
        }
      };
      thread = new Thread(r, "Step Runner for " + step.getCheckpointToken());
      thread.start();
    }

    public void stop() {
      thread.interrupt();
    }

    public BaseStep<Config> getStep() {
      return step;
    }

  }

}
