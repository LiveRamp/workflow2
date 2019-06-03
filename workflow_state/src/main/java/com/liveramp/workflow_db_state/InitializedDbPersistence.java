package com.liveramp.workflow_db_state;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liveramp.databases.workflow_db.IWorkflowDb;
import com.liveramp.databases.workflow_db.models.WorkflowAttempt;
import com.liveramp.databases.workflow_db.models.WorkflowExecution;
import com.liveramp.java_support.alerts_handler.AlertsHandler;
import com.liveramp.workflow.types.WorkflowAttemptStatus;
import com.liveramp.workflow.types.WorkflowExecutionStatus;
import com.liveramp.workflow_core.WorkflowEnums;
import com.liveramp.workflow_state.InitializedPersistence;

import static com.liveramp.workflow_db_state.DbPersistence.HEARTBEAT_INTERVAL;

public class InitializedDbPersistence implements InitializedPersistence {

  private static final Logger LOG = LoggerFactory.getLogger(InitializedDbPersistence.class);

  private final Object lock = new Object();
  private final IWorkflowDb db;
  private final long workflowAttemptId;
  private final Thread heartbeatThread;
  private final AlertsHandler providedHandler;

  public InitializedDbPersistence(long workflowAttemptId, IWorkflowDb db, boolean isLive, AlertsHandler providedHandler) {
    this.db = db;
    this.db.disableCaching();
    this.workflowAttemptId = workflowAttemptId;

    if (isLive) {
      this.heartbeatThread = new Thread(new Heartbeat());
      this.heartbeatThread.setDaemon(true);
      this.heartbeatThread.start();
      this.providedHandler = providedHandler;
    } else {
      this.heartbeatThread = null;
      this.providedHandler = null;
    }
  }

  @Override
  public long getExecutionId() throws IOException {
    synchronized (lock) {
      return getExecution().getId();
    }
  }

  @Override
  public long getAttemptId() throws IOException {
    synchronized (lock) {
      return workflowAttemptId;
    }
  }

  //  this method is carefully not synchronized, because we don't want a deadlock with the heartbeat waiting to heartbeat.
  @Override
  public void markWorkflowStopped() throws IOException {

    synchronized (lock) {

      LOG.info("Marking workflow stopped");

      WorkflowAttempt attempt = getAttempt();

      if (WorkflowEnums.LIVE_ATTEMPT_STATUSES.contains(attempt.getStatus())) {

        attempt.setEndTime(System.currentTimeMillis());

        if (attempt.getStatus() == WorkflowAttemptStatus.INITIALIZING.ordinal()) {
          LOG.info("Workflow initialized without executing, assuming failure");
          attempt.setStatus(WorkflowAttemptStatus.FAILED.ordinal());
        } else {

          if (WorkflowQueries.workflowComplete(getExecution())) {
            LOG.info("Marking execution as complete");
            save(getExecution()
                .setStatus(WorkflowExecutionStatus.COMPLETE.ordinal())
                .setEndTime(System.currentTimeMillis())
            );
          }

          LOG.info("Stopping attempt: " + attempt);
          if (attempt.getStatus() == WorkflowAttemptStatus.FAIL_PENDING.ordinal()) {
            attempt.setStatus(WorkflowAttemptStatus.FAILED.ordinal());
          } else if (attempt.getStatus() == WorkflowAttemptStatus.SHUTDOWN_PENDING.ordinal()) {
            attempt.setStatus(WorkflowAttemptStatus.SHUTDOWN.ordinal());
          } else {
            attempt.setStatus(WorkflowAttemptStatus.FINISHED.ordinal());
          }

        }
      } else {
        LOG.info("Attempt was already stopped (via shutdown hook probably)");
      }

      LOG.info("Marking status of attempt " + attempt.getId() + " as " + attempt.getStatus());

      save(attempt);

    }

    killHeartbeat();

  }

  private void killHeartbeat() {
    if (heartbeatThread != null) {
      heartbeatThread.interrupt();
      try {
        heartbeatThread.join();
      } catch (InterruptedException e) {
        LOG.error("Failed to interrupt heartbeat thread!");
      }
    }
  }

  @Override
  public void shutdown() {

    killHeartbeat();

    synchronized (lock) {
      try {
        db.close();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }


  public WorkflowAttempt getAttempt() throws IOException {
    synchronized (lock) {
      return db.workflowAttempts().find(workflowAttemptId);
    }
  }

  public WorkflowExecution getExecution() throws IOException {
    synchronized (lock) {
      return getAttempt().getWorkflowExecution();
    }
  }

  public IWorkflowDb getDb() {
    return db;
  }

  protected Object getLock() {
    return lock;
  }

  protected AlertsHandler getProvidedHandler() {
    return providedHandler;
  }

  protected void save(WorkflowExecution execution) throws IOException {
    synchronized (lock) {
      db.workflowExecutions().save(execution);
    }
  }

  protected void save(WorkflowAttempt attempt) throws IOException {
    synchronized (lock) {
      db.workflowAttempts().save(attempt);
    }
  }

  private class Heartbeat implements Runnable {
    @Override
    public void run() {
      //noinspection InfiniteLoopStatement
      while (!Thread.interrupted()) {
        try {
          Thread.sleep(HEARTBEAT_INTERVAL);
          heartbeat();
        } catch (InterruptedException e) {
          LOG.info("Heartbeat thread killed");
          return;
        }
      }
    }
  }

  private void heartbeat() {
    synchronized (lock) {
      try {
        save(getAttempt()
            .setLastHeartbeat(System.currentTimeMillis())
        );
      } catch (IOException e) {
        LOG.error("Failed to record heartbeat: ", e);
        throw new RuntimeException(e);
      }
    }
  }

}