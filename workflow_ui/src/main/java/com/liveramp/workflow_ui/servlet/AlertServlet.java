package com.liveramp.workflow_ui.servlet;

import java.time.Duration;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.liveramp.commons.collections.nested_map.TwoKeyTuple;
import com.liveramp.commons.collections.nested_map.TwoNestedCountingMap;
import com.liveramp.databases.workflow_db.IDatabases;
import com.liveramp.databases.workflow_db.models.MapreduceJob;
import com.liveramp.databases.workflow_db.models.StepAttempt;
import com.liveramp.databases.workflow_db.models.WorkflowAlert;
import com.liveramp.databases.workflow_db.models.WorkflowAlertMapreduceJob;
import com.liveramp.databases.workflow_db.models.WorkflowAttempt;
import com.liveramp.databases.workflow_db.models.WorkflowExecution;
import com.rapleaf.jack.queries.GenericQuery;
import com.rapleaf.jack.queries.Record;

import static com.liveramp.workflow_core.WorkflowConstants.WORKFLOW_ALERT_SHORT_DESCRIPTIONS;
import static com.rapleaf.jack.queries.AggregatedColumn.COUNT;

public class AlertServlet implements JSONServlet.Processor {

  private static int MAX_DURATION_IN_DAYS = 1;

  @Override
  public JSONObject getData(IDatabases workflowDb, Map<String, String> parameters) throws Exception {
    Long startRange = Long.parseLong(parameters.get("started_after"));
    Long endRange = Long.parseLong(parameters.get("started_before"));
    String appName = parameters.get("name");
    if ((endRange - startRange) > Duration.ofDays(MAX_DURATION_IN_DAYS).toMillis()) {
      startRange = endRange - Duration.ofDays(MAX_DURATION_IN_DAYS).toMillis();
    }
    JSONObject data = new JSONObject();

    GenericQuery getBadMrJobsQuery = workflowDb.getWorkflowDb().createQuery()
        .from(MapreduceJob.TBL)
        .leftJoin(WorkflowAlertMapreduceJob.TBL)
        .on(WorkflowAlertMapreduceJob.MAPREDUCE_JOB_ID.equalTo(MapreduceJob.ID))
        .leftJoin(StepAttempt.TBL)
        .on(StepAttempt.ID.equalTo(MapreduceJob.STEP_ATTEMPT_ID.as(Long.class)))
        .leftJoin(WorkflowAttempt.TBL)
        .on(WorkflowAttempt.ID.equalTo(StepAttempt.WORKFLOW_ATTEMPT_ID.as(Long.class)))
        .leftJoin(WorkflowExecution.TBL)
        .on(WorkflowExecution.ID.equalTo(WorkflowAttempt.WORKFLOW_EXECUTION_ID.as(Long.class)))
        .where(WorkflowExecution.START_TIME.greaterThan(startRange),
            WorkflowExecution.START_TIME.lessThan(endRange),
            WorkflowExecution.NAME.equalTo(appName)
        )
        .select(WorkflowExecution.NAME, COUNT(MapreduceJob.ID));

    double jobCount = getBadMrJobsQuery.fetch()
        .get(0).get(COUNT(MapreduceJob.ID)).doubleValue();

    TwoNestedCountingMap<String, String> mapreduceAlertCounts = new TwoNestedCountingMap<>(0L);
    GenericQuery q2 = workflowDb.getWorkflowDb().createQuery()
        .from(MapreduceJob.TBL)
        .innerJoin(WorkflowAlertMapreduceJob.TBL)
        .on(WorkflowAlertMapreduceJob.MAPREDUCE_JOB_ID.equalTo(MapreduceJob.ID))
        .innerJoin(WorkflowAlert.TBL)
        .on(WorkflowAlert.ID.equalTo(WorkflowAlertMapreduceJob.WORKFLOW_ALERT_ID))
        .innerJoin(StepAttempt.TBL)
        .on(StepAttempt.ID.equalTo(MapreduceJob.STEP_ATTEMPT_ID.as(Long.class)))
        .innerJoin(WorkflowAttempt.TBL)
        .on(WorkflowAttempt.ID.equalTo(StepAttempt.WORKFLOW_ATTEMPT_ID.as(Long.class)))
        .innerJoin(WorkflowExecution.TBL)
        .on(WorkflowExecution.ID.equalTo(WorkflowAttempt.WORKFLOW_EXECUTION_ID.as(Long.class)))
        .where(
            WorkflowExecution.START_TIME.greaterThan(startRange),
            WorkflowExecution.START_TIME.lessThan(endRange))
        .select(COUNT(WorkflowAlertMapreduceJob.ID), WorkflowExecution.NAME,
            WorkflowAlert.ALERT_CLASS, StepAttempt.STEP_TOKEN)
        .groupBy(WorkflowExecution.NAME, WorkflowAlert.ALERT_CLASS, StepAttempt.STEP_TOKEN, MapreduceJob.TRACKING_URL);

    for (Record r : q2.fetch()) {
      if (!r.get(WorkflowExecution.NAME).equals(appName)) {
        continue;
      }
      String alert = r.getString(WorkflowAlert.ALERT_CLASS).replaceAll(".*\\.", "");
      String step = r.getString(StepAttempt.STEP_TOKEN);
      mapreduceAlertCounts.incrementAndGet(alert, step, r.get(COUNT(WorkflowAlertMapreduceJob.ID)).longValue());
    }

    JSONArray jsonArray = new JSONArray();

    for (TwoKeyTuple<String, String> alertStep : mapreduceAlertCounts.key12Set()) {
      JSONObject object = new JSONObject();
      object.put("stepName", alertStep.getK2());
      object.put("alertName", alertStep.getK1());
      object.put("alertCount", mapreduceAlertCounts.get(alertStep));
      object.put("alertsPerJob", mapreduceAlertCounts.get(alertStep) / jobCount);
      jsonArray.put(object);
    }

    data.put("list", jsonArray);
    return data.put("descriptions", new JSONObject(WORKFLOW_ALERT_SHORT_DESCRIPTIONS));
  }
}
