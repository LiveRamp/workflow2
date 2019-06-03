package com.liveramp.workflow_ui.util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONObject;
import org.junit.Test;

import com.liveramp.commons.Accessors;
import com.liveramp.databases.workflow_db.DatabasesImpl;
import com.liveramp.databases.workflow_db.IWorkflowDb;
import com.liveramp.databases.workflow_db.models.Application;
import com.liveramp.databases.workflow_db.models.ApplicationCounterSummary;
import com.liveramp.databases.workflow_db.models.MapreduceJob;
import com.liveramp.databases.workflow_db.models.StepAttempt;
import com.liveramp.databases.workflow_db.models.WorkflowAttempt;
import com.liveramp.databases.workflow_db.models.WorkflowExecution;
import com.liveramp.workflow.types.WorkflowExecutionStatus;
import com.liveramp.workflow.types.StepStatus;
import com.liveramp.workflow_ui.WorkflowUITestCase;
import com.liveramp.workflow_ui.servlet.ClusterConstants;
import com.rapleaf.jack.queries.where_operators.EqualTo;
import com.rapleaf.jack.queries.where_operators.IsNull;

import static org.junit.Assert.assertEquals;

public class SummarizerIT extends WorkflowUITestCase {
  private static final DateTimeFormatter FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

  @Test
  public void testSummarizer() throws Exception {

    long date14 = FORMAT.parseMillis("2016-06-14 12:00:00");
    long date13 = FORMAT.parseMillis("2016-06-13 12:00:00");

    final LocalDate localDate13 = FORMAT.parseLocalDate("2016-06-13 00:00:00");
    final LocalDate localDate14 = FORMAT.parseLocalDate("2016-06-14 00:00:00");
    final LocalDate localDate15 = FORMAT.parseLocalDate("2016-06-15 00:00:00");
    final LocalDate localDate16 = FORMAT.parseLocalDate("2016-06-16 00:00:00");
    final LocalDate localDate17 = FORMAT.parseLocalDate("2016-06-17 00:00:00");

    final IWorkflowDb workflowDb = new DatabasesImpl().getWorkflowDb();

    Application app = workflowDb.applications().create("Test Workflow");
    WorkflowExecution execution = workflowDb.workflowExecutions().create(null,
        "Test Workflow",
        null,
        WorkflowExecutionStatus.COMPLETE.ordinal(),
        date13,
        date14,
        app.getIntId(),
        null
    );
    WorkflowAttempt attempt = workflowDb.workflowAttempts().create(execution.getIntId(), "user", "HIGH", "default", "localhost");
    StepAttempt step = workflowDb.stepAttempts().create(attempt.getIntId(), "step",
        date13,
        date14,
        StepStatus.COMPLETED.ordinal(),
        null,
        null,
        "class",
        null
    );
    MapreduceJob mrJob = workflowDb.mapreduceJobs().create(step.getId(), "job1", "jobname", "url", null, null, null, null, null, null, null, null, null, null, null, null);

    workflowDb.mapreduceCounters().create(mrJob.getIntId(), ClusterConstants.MR2_GROUP, ClusterConstants.VCORE_MAP, 1);
    workflowDb.mapreduceCounters().create(mrJob.getIntId(), ClusterConstants.MR2_GROUP, ClusterConstants.VCORE_RED, 2);
    workflowDb.mapreduceCounters().create(mrJob.getIntId(), ClusterConstants.MR2_GROUP, ClusterConstants.MB_MAP, 1);
    workflowDb.mapreduceCounters().create(mrJob.getIntId(), ClusterConstants.MR2_GROUP, ClusterConstants.MB_RED, 1);

    final Multimap<String, String> countersToSummarize = HashMultimap.create();
    countersToSummarize.put(ClusterConstants.MR2_GROUP, ClusterConstants.VCORE_MAP);
    countersToSummarize.put(ClusterConstants.MR2_GROUP, ClusterConstants.VCORE_RED);
    countersToSummarize.put(ClusterConstants.MR2_GROUP, ClusterConstants.MB_MAP);
    countersToSummarize.put(ClusterConstants.MR2_GROUP, ClusterConstants.MB_RED);

    Summarizer.summarizeApplicationCounters(countersToSummarize, workflowDb, 7, localDate14);

    //  4 for this app, 4 counters * 7 cluster summaries
    assertEquals(32, workflowDb.applicationCounterSummaries().findAll().size());

    ApplicationCounterSummary summary = Accessors.only(workflowDb.applicationCounterSummaries().query()
        .whereGroup(new EqualTo<>(ClusterConstants.MR2_GROUP))
        .whereName(new EqualTo<>(ClusterConstants.VCORE_MAP))
        .whereApplicationId(new EqualTo<>(app.getIntId()))
        .whereDate(new EqualTo<>(localDate14.toDate().getTime()))
        .find());

    assertEquals(1L, summary.getValue().longValue());

    ApplicationCounterSummary summary2 = Accessors.only(workflowDb.applicationCounterSummaries().query()
        .whereGroup(new EqualTo<>(ClusterConstants.MR2_GROUP))
        .whereName(new EqualTo<>(ClusterConstants.VCORE_RED))
        .whereApplicationId(new EqualTo<>(app.getIntId()))
        .whereDate(new EqualTo<>(localDate14.toDate().getTime()))
        .find());

    assertEquals(2L, summary2.getValue().longValue());

    ApplicationCounterSummary clusterSummary = Accessors.only(workflowDb.applicationCounterSummaries().query()
        .whereGroup(new EqualTo<>(ClusterConstants.MR2_GROUP))
        .whereName(new EqualTo<>(ClusterConstants.VCORE_MAP))
        .whereApplicationId(new IsNull<Integer>())
        .whereDate(new EqualTo<>(localDate14.toDate().getTime()))
        .find());

    assertEquals(1L, clusterSummary.getValue().longValue());

    ApplicationCounterSummary prevClusterSummary = Accessors.only(workflowDb.applicationCounterSummaries().query()
        .whereGroup(new EqualTo<>(ClusterConstants.MR2_GROUP))
        .whereName(new EqualTo<>(ClusterConstants.VCORE_MAP))
        .whereApplicationId(new IsNull<Integer>())
        .whereDate(new EqualTo<>(localDate13.toDate().getTime()))
        .find());

    assertEquals(0L, prevClusterSummary.getValue().longValue());

    //  run on same day (assert idempotent)
    Summarizer.summarizeApplicationCounters(countersToSummarize, workflowDb, 7, localDate14);

    //  run again for next day.  asert no records
    Summarizer.summarizeApplicationCounters(countersToSummarize, workflowDb, 7, localDate15);

    ApplicationCounterSummary nextClusterSummary = Accessors.only(workflowDb.applicationCounterSummaries().query()
        .whereGroup(new EqualTo<>(ClusterConstants.MR2_GROUP))
        .whereName(new EqualTo<>(ClusterConstants.VCORE_MAP))
        .whereApplicationId(new IsNull<Integer>())
        .whereDate(new EqualTo<>(localDate15.toDate().getTime()))
        .find());

    assertEquals(0L, nextClusterSummary.getValue().longValue());

    //  same as before + 2 cluster summaries
    assertEquals(32+4, workflowDb.applicationCounterSummaries().findAll().size());


    //  test query util using the summaries

    JSONObject results2 = QueryUtil.getCountersPerApplication(workflowDb, countersToSummarize, CostUtil.DEFAULT_COST_ESTIMATE, localDate14, localDate15);
    assertEquals(2L, results2.getJSONObject("CLUSTER_TOTAL").getJSONObject("counters").getInt("org.apache.hadoop.mapreduce.JobCounter.VCORES_MILLIS_REDUCES"));
    assertEquals(2L, results2.getJSONObject("Test Workflow").getJSONObject("counters").getInt("org.apache.hadoop.mapreduce.JobCounter.VCORES_MILLIS_REDUCES"));

    JSONObject results3 = QueryUtil.getCountersPerApplication(workflowDb, countersToSummarize, CostUtil.DEFAULT_COST_ESTIMATE, localDate13, localDate14);
    assertEquals(0L, results3.getJSONObject("CLUSTER_TOTAL").getJSONObject("counters").getInt("org.apache.hadoop.mapreduce.JobCounter.VCORES_MILLIS_REDUCES"));

    //  should be nothing in this window
    JSONObject results4 = QueryUtil.getCountersPerApplication(workflowDb, countersToSummarize, CostUtil.DEFAULT_COST_ESTIMATE, localDate15, localDate16);
    assertEquals(0L, results4.getJSONObject("CLUSTER_TOTAL").getJSONObject("counters").getInt("org.apache.hadoop.mapreduce.JobCounter.VCORES_MILLIS_REDUCES"));

    JSONObject results5 = QueryUtil.getCountersPerApplication(workflowDb, countersToSummarize, CostUtil.DEFAULT_COST_ESTIMATE, localDate16, localDate17);
    assertEquals(0L, results5.getJSONObject("CLUSTER_TOTAL").getJSONObject("counters").getInt("org.apache.hadoop.mapreduce.JobCounter.VCORES_MILLIS_REDUCES"));
    
  }

}