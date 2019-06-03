package com.liveramp.workflow_ui.servlet;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liveramp.commons.collections.CountingMap;
import com.liveramp.commons.collections.list.ListBuilder;
import com.liveramp.commons.collections.nested_map.ThreeNestedMap;
import com.liveramp.databases.workflow_db.IDatabases;
import com.liveramp.databases.workflow_db.models.MapreduceCounter;
import com.liveramp.databases.workflow_db.models.MapreduceJob;
import com.liveramp.databases.workflow_db.models.StepAttempt;
import com.liveramp.databases.workflow_db.models.WorkflowAlert;
import com.liveramp.databases.workflow_db.models.WorkflowAlertMapreduceJob;
import com.liveramp.databases.workflow_db.models.WorkflowAttempt;
import com.liveramp.databases.workflow_db.models.WorkflowExecution;
import com.rapleaf.jack.queries.Column;
import com.rapleaf.jack.queries.GenericQuery;
import com.rapleaf.jack.queries.Index;
import com.rapleaf.jack.queries.IndexHints;
import com.rapleaf.jack.queries.Record;

import static com.liveramp.workflow_ui.servlet.ClusterConstants.MR2_GROUP;
import static com.rapleaf.jack.queries.AggregatedColumn.SUM;

public class ClusterAppAlerts implements JSONServlet.Processor {
  private static final Logger LOG = LoggerFactory.getLogger(ClusterAppAlerts.class);

  @Override
  public JSONObject getData(IDatabases workflowDb, Map<String, String> parameters) throws Exception {

    LOG.info("Starting");

    //  within window

    long startWindow = Long.parseLong(parameters.get("start_time"));
    long endWindow = Long.parseLong(parameters.get("end_time"));
    String pool = parameters.get("pool");

    //  all mapreduce jobs in a step finishing in past day:
    //  mapreduce_job_id - [launched maps/reduces] - count

    GenericQuery counters = windowMapreduceJobs(workflowDb, startWindow, endWindow)
        .innerJoin(MapreduceCounter.TBL)
        .on(MapreduceCounter.MAPREDUCE_JOB_ID.equalTo(MapreduceJob.ID.as(Integer.class)))
        .where(MapreduceCounter.GROUP.equalTo(MR2_GROUP), MapreduceCounter.NAME.in("TOTAL_LAUNCHED_MAPS", "TOTAL_LAUNCHED_REDUCES"))
        .innerJoin(WorkflowAttempt.TBL)
        .on(StepAttempt.WORKFLOW_ATTEMPT_ID.equalTo(WorkflowAttempt.ID.as(Integer.class)))
        .innerJoin(WorkflowExecution.TBL)
        .on(WorkflowAttempt.WORKFLOW_EXECUTION_ID.equalTo(WorkflowExecution.ID.as(Integer.class)))
        .groupBy(WorkflowExecution.NAME)
        .select(WorkflowExecution.NAME, SUM(MapreduceCounter.VALUE));

    if(pool != null){
      counters = counters.where(WorkflowAttempt.POOL.startsWith(pool));
    }

    CountingMap<String> tasksPerApp = new CountingMap<>();

    for (Record record : counters.fetch()) {

      tasksPerApp.increment(
          record.getString(WorkflowExecution.NAME),
          record.getLong(SUM(MapreduceCounter.VALUE))
      );

    }

    LOG.info("Got counters for " + tasksPerApp.get().size() + " apps");

    //  get counters for jobs with alerts
    GenericQuery alertJobCounters = windowMapreduceJobs(workflowDb, startWindow, endWindow)
        .innerJoin(WorkflowAlertMapreduceJob.TBL)
        .on(WorkflowAlertMapreduceJob.MAPREDUCE_JOB_ID.equalTo(MapreduceJob.ID))
        .innerJoin(WorkflowAlert.TBL)
        .on(WorkflowAlert.ID.equalTo(WorkflowAlertMapreduceJob.WORKFLOW_ALERT_ID))
        .innerJoin(MapreduceCounter.TBL)
        .on(MapreduceCounter.MAPREDUCE_JOB_ID.equalTo(MapreduceJob.ID.as(Integer.class)))
        .where(MapreduceCounter.GROUP.equalTo(MR2_GROUP), MapreduceCounter.NAME.in("TOTAL_LAUNCHED_MAPS", "TOTAL_LAUNCHED_REDUCES"))
        .groupBy(MapreduceJob.ID, WorkflowAlert.ALERT_CLASS)
        .select(new ListBuilder<Column>()
            .add(MapreduceJob.ID)
            .add(SUM(MapreduceCounter.VALUE))
            .get());

    Map<Long, Long> alertJobToTasks = Maps.newHashMap();

    for (Record record : alertJobCounters.fetch()) {
      //  little weird, but grouping on alert class keeps it from multiplying the count for the job,
      //  which is all we really care about
      alertJobToTasks.put(
          record.getLong(MapreduceJob.ID),
          record.getLong(SUM(MapreduceCounter.VALUE))
      );
    }

    LOG.info("Found " + alertJobToTasks.size() + " jobs with alerts");

    //  get counters for jobs with alerts
    GenericQuery alertJobInfo = windowMapreduceJobs(workflowDb, startWindow, endWindow)
        .innerJoin(WorkflowAlertMapreduceJob.TBL)
        .on(WorkflowAlertMapreduceJob.MAPREDUCE_JOB_ID.equalTo(MapreduceJob.ID))
        .innerJoin(WorkflowAlert.TBL)
        .on(WorkflowAlert.ID.equalTo(WorkflowAlertMapreduceJob.WORKFLOW_ALERT_ID))
        .innerJoin(WorkflowAttempt.TBL)
        .on(StepAttempt.WORKFLOW_ATTEMPT_ID.equalTo(WorkflowAttempt.ID.as(Integer.class)))
        .innerJoin(WorkflowExecution.TBL)
        .on(WorkflowAttempt.WORKFLOW_EXECUTION_ID.equalTo(WorkflowExecution.ID.as(Integer.class)))
        .select(
            new ListBuilder<Column>()
                .addAll(WorkflowAlert.TBL.getAllColumns())
                .add(MapreduceJob.ID)
                .add(WorkflowAttempt.ID)
                .add(WorkflowAttempt.POOL)
                .add(MapreduceJob.JOB_NAME)
                .add(StepAttempt.STEP_TOKEN)
                .add(MapreduceJob.TRACKING_URL)
                .add(WorkflowExecution.NAME)
                .get()
        );

    if(pool != null){
      alertJobInfo = alertJobInfo.where(WorkflowAttempt.POOL.startsWith(pool));
    }

    //  app, alert, job, alert info
    ThreeNestedMap<String, String, Long, WorkflowAlert.Attributes> alertsForApp = new ThreeNestedMap<>();
    Map<Long, Long> mapreduceJobToAttempt = Maps.newHashMap();
    Map<Long, String> mapreduceJobToName = Maps.newHashMap();
    Map<Long, String> mapreduceJobToURL = Maps.newHashMap();
    Map<Long, String> mapreduceJobToStep = Maps.newHashMap();

    for (Record record : alertJobInfo.fetch()) {
      Long jobID = record.getLong(MapreduceJob.ID);

      WorkflowAlert.Attributes alert = record.getAttributes(WorkflowAlert.TBL);

      alertsForApp.put(record.getString(WorkflowExecution.NAME), alert.getAlertClass(), jobID, alert);
      mapreduceJobToName.put(jobID, record.getString(MapreduceJob.JOB_NAME));
      mapreduceJobToStep.put(jobID, record.getString(StepAttempt.STEP_TOKEN));
      mapreduceJobToURL.put(jobID, record.getString(MapreduceJob.TRACKING_URL));
      mapreduceJobToAttempt.put(jobID, record.getLong(WorkflowAttempt.ID));

    }

    LOG.info("Found " + alertsForApp.size() + " alerted jobs");

    List<AppAlert> alertList = Lists.newArrayList();

    for (String app : alertsForApp.key1Set()) {
      Long totalAppTasks = tasksPerApp.get().get(app);

      if(totalAppTasks == null){
        totalAppTasks = 0L;
      }

      for (String alertName : alertsForApp.get(app).key1Set()) {

        List<AlertInfo> alerts = Lists.newArrayList();
        long alertTasks = 0;

        for (Long job : alertsForApp.get(app, alertName).keySet()) {
          Long jobTasks = alertJobToTasks.get(job);

          if(jobTasks != null) {
            alertTasks += jobTasks;
          }else{
            LOG.warn("No data for job: "+jobTasks);
          }

          alerts.add(new AlertInfo(
              alertsForApp.get(app, alertName, job),
              mapreduceJobToStep.get(job),
              mapreduceJobToName.get(job),
              mapreduceJobToURL.get(job),
              mapreduceJobToAttempt.get(job)
          ));
        }

        alertList.add(new AppAlert(app, alertName, alertTasks, totalAppTasks, alerts));
      }

    }

    Gson gson = new Gson();
    return new JSONObject()
        .put("alerts", alertList.stream().map(appAlert -> {
              try {
                return new JSONObject(gson.toJson(appAlert));
              } catch (JSONException e) {
                throw new RuntimeException(e);
              }
            })
                .collect(Collectors.toList())
        );
  }

  private static class AlertInfo {
    private final WorkflowAlert.Attributes attributes;
    private final String stepToken;
    private final String jobName;
    private final String jobURL;
    private final Long workflowAttemptId;

    private AlertInfo(WorkflowAlert.Attributes attributes, String stepToken, String jobName, String jobURL, Long workflowAttemptId) {
      this.attributes = attributes;
      this.stepToken = stepToken;
      this.jobName = jobName;
      this.jobURL = jobURL;
      this.workflowAttemptId = workflowAttemptId;
    }
  }

  private static class AppAlert {

    private final String app;
    private final String alertName;

    private final long alertTasks;
    private final long totalTasks;
    private final List<AlertInfo> alerts;

    private AppAlert(String app, String alertName, long alertTasks, long totalTasks, List<AlertInfo> alerts) {
      this.app = app;
      this.alertName = alertName;
      this.alertTasks = alertTasks;
      this.totalTasks = totalTasks;
      this.alerts = alerts;
    }

  }

  private GenericQuery windowMapreduceJobs(IDatabases workflowDb, long startWindow, long endWindow) {
    return workflowDb.getWorkflowDb().createQuery().from(StepAttempt.TBL.with(IndexHints.force(Index.of("index_step_attempts_on_end_time"))))
        .where(StepAttempt.END_TIME.greaterThanOrEqualTo(startWindow), StepAttempt.END_TIME.lessThan(endWindow))
        .innerJoin(MapreduceJob.TBL)
        .on(MapreduceJob.STEP_ATTEMPT_ID.equalTo(StepAttempt.ID));
  }


  public static void main(String[] args) {

    DateTime end = DateTime.now();
    long endTime = end.toDate().getTime();

    DateTime start = end.minusHours(1);
    long startTime = start.toDate().getTime();

    System.out.println(endTime);
    System.out.println(startTime);

  }

}
