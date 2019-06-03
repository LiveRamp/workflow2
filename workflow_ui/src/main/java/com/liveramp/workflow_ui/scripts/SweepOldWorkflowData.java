package com.liveramp.workflow_ui.scripts;

import java.io.IOException;
import java.util.List;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liveramp.commons.Accessors;
import com.liveramp.databases.workflow_db.DatabasesImpl;
import com.liveramp.databases.workflow_db.IWorkflowDb;
import com.liveramp.databases.workflow_db.models.MapreduceCounter;
import com.liveramp.databases.workflow_db.models.MapreduceJob;
import com.liveramp.databases.workflow_db.models.StepAttempt;
import com.liveramp.databases.workflow_db.models.WorkflowAttempt;
import com.liveramp.databases.workflow_db.models.WorkflowExecution;
import com.rapleaf.jack.queries.Deletions;
import com.rapleaf.jack.queries.GenericQuery;
import com.rapleaf.jack.queries.Index;
import com.rapleaf.jack.queries.IndexHints;
import com.rapleaf.jack.queries.QueryOrder;
import com.rapleaf.jack.queries.Records;

public class SweepOldWorkflowData {
  private static final Logger LOG = LoggerFactory.getLogger(SweepOldWorkflowData.class);

  public static void main(String[] args) throws IOException {

    DateTime window = DateTime.now().minusMonths(10);
    LOG.info("Sweeping counters from jobs which finished before: " + window);

    IWorkflowDb db = new DatabasesImpl().getWorkflowDb();

    sweep(window.getMillis(), db);

  }

  private static void sweepDetachedCounters(IWorkflowDb db) throws IOException {

    //  while oldest counter's mapreduce job doesn't exist

    int deletedCounters = 0;

    while (true) {
      MapreduceCounter counter = Accessors.only(db.createQuery().from(MapreduceCounter.TBL)
          .orderBy(MapreduceCounter.ID, QueryOrder.ASC)
          .limit(1)
          .fetch().getModels(
              MapreduceCounter.TBL,
              db.getDatabases())
      );

      if(isDetached(counter)){
        db.mapreduceCounters().delete(counter);
      }else{
        LOG.info("Deleted " + deletedCounters + " detached counters");
        break;
      }

      if(deletedCounters % 1000 == 0){
        LOG.info("Deleted "+deletedCounters+" detached counters");
      }

      //  in case we mess up something
      if (deletedCounters++ > 1000000) {
        throw new RuntimeException("More than 1,000,000 counters deleted detached from mapreduce jobs");
      }

    }

  }

  private static boolean isDetached(MapreduceCounter counter) throws IOException {

    MapreduceJob mapreduceJob = counter.getMapreduceJob();

    if(mapreduceJob == null){
      return true;
    }

    StepAttempt step = mapreduceJob.getStepAttempt();
    if(step == null){
      return true;
    }

    WorkflowAttempt attempt = step.getWorkflowAttempt();
    if(attempt == null){
      return true;
    }

    WorkflowExecution execution = attempt.getWorkflowExecution();
    if(execution == null){
      return true;
    }

    return false;
  }


  public static void sweep(long executionsBefore, IWorkflowDb db) throws IOException {

    sweepDetachedCounters(db);

    DateTime monthAgo = DateTime.now().minusMonths(3);

    //  safety to make sure this doesn't get called on anything less than a month ago
    if (executionsBefore > monthAgo.toDate().getTime()) {
      throw new IllegalArgumentException();
    }

    //  get oldest mapreduce counter
    List<Long> counter = db.createQuery().from(MapreduceCounter.TBL)
        .orderBy(MapreduceCounter.ID, QueryOrder.ASC)
        .limit(1)
        .select(MapreduceCounter.ID)
        .fetch()
        .getLongs(MapreduceCounter.ID);

    if (counter.isEmpty()) {
      return;
    }

    Long oldestCounter = Accessors.only(counter);

    LOG.info("Oldest counter:" + oldestCounter);

    MapreduceJob job = db.mapreduceCounters().find(oldestCounter)
        .getMapreduceJob();

    StepAttempt step = job
        .getStepAttempt();

    WorkflowAttempt attempt = step
        .getWorkflowAttempt();

    WorkflowExecution oldestExecution = attempt
        .getWorkflowExecution();

    LOG.info("Found oldest execution with counters: " + oldestExecution);

    Long beginSearch = oldestExecution.getStartTime();

    DateTime sweepDay = new DateTime(beginSearch)
        .withTimeAtStartOfDay();

    while (sweepDay.toDate().getTime() < executionsBefore) {

      //  get executions between sweep date and next
      DateTime nextDay = sweepDay.plusDays(1)
          .withTimeAtStartOfDay();

      LOG.info("Deleting counters between " + sweepDay + " and " + nextDay);

      Records jobs = getMapreduceJobsInWindow(db, sweepDay.getMillis(), nextDay.getMillis())
          .fetch();

      for (Long jobID : jobs.getLongs(MapreduceJob.ID)) {
        LOG.info("Deleting counters for job: " + jobID);

        Deletions delete = db.createDeletion().from(MapreduceCounter.TBL)
            .where(MapreduceCounter.MAPREDUCE_JOB_ID.equalTo(jobID.intValue()))
            .execute();
        LOG.info("Deleted " + delete.getDeletedRowCount() + " counters");

      }

      //  TODO in the future it may be worth sweeping other tables, but counters are the bulk of the savings for now
      //  workflow_executions
      //  workflow_attempts
      //  step attempts
      //  mapreduce_jobs
      //  step_attempts
      //  mapreduce_job_task_exceptions
      //  step_dependencies
      //  workflow_attempt_configured_notifications

      sweepDay = nextDay;

    }

  }

  private static GenericQuery getMapreduceJobsInWindow(IWorkflowDb db, Long startWindow, Long endWindow) {
    return db.createQuery().from(WorkflowExecution.TBL.with(IndexHints.force(Index.of("start_time_idx"))))
        .where(
            WorkflowExecution.START_TIME.greaterThanOrEqualTo(startWindow),
            WorkflowExecution.START_TIME.lessThan(endWindow))
        .innerJoin(WorkflowAttempt.TBL)
        .on(WorkflowAttempt.WORKFLOW_EXECUTION_ID.equalTo(WorkflowExecution.ID.as(Integer.class)))
        .innerJoin(StepAttempt.TBL)
        .on(StepAttempt.WORKFLOW_ATTEMPT_ID.equalTo(WorkflowAttempt.ID.as(Integer.class)))
        .innerJoin(MapreduceJob.TBL)
        .on(MapreduceJob.STEP_ATTEMPT_ID.equalTo(StepAttempt.ID))
        .select(MapreduceJob.ID);
  }

}
