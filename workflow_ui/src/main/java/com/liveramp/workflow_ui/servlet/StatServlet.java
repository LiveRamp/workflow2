package com.liveramp.workflow_ui.servlet;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.liveramp.commons.collections.list.ListBuilder;
import com.liveramp.commons.collections.nested_map.ThreeNestedCountingMap;
import com.liveramp.commons.collections.nested_map.TwoNestedMap;
import com.liveramp.databases.workflow_db.IDatabases;
import com.liveramp.databases.workflow_db.IWorkflowDb;
import com.liveramp.databases.workflow_db.models.MapreduceCounter;
import com.liveramp.databases.workflow_db.models.StepAttempt;
import com.liveramp.databases.workflow_db.models.WorkflowAttempt;
import com.liveramp.databases.workflow_db.models.WorkflowExecution;
import com.liveramp.workflow_db_state.WorkflowQueries;
import com.liveramp.workflow_ui.util.CostUtil;
import com.rapleaf.jack.queries.Column;
import com.rapleaf.jack.queries.GenericQuery;
import com.rapleaf.jack.queries.Record;

import static com.liveramp.workflow_ui.servlet.RenderInfo.DEFAULT_EXPAND;
import static com.liveramp.workflow_ui.servlet.RenderInfo.DERIVED_COUNTERS;
import static com.liveramp.workflow_ui.util.QueryUtil.safeInt;

public class StatServlet implements JSONServlet.Processor {

  public interface DerivedCounter {
    public String getName();

    public Long getDerivedValue(TwoNestedMap<String, String, Long> counters);
  }

  @Override
  public JSONObject getData(IDatabases workflowDatabases, Map<String, String> parameters) throws Exception {
    IWorkflowDb workflowDb = workflowDatabases.getWorkflowDb();

    ThreeNestedCountingMap<Long, String, String> countersPerDay = new ThreeNestedCountingMap<>(0L);
    ThreeNestedCountingMap<Long, String, String> countersPerExecution = new ThreeNestedCountingMap<>(0L);
    ThreeNestedCountingMap<String, String, String> countersPerStep = new ThreeNestedCountingMap<>(0L);

    String stepToken = parameters.get("step_id");
    String name = parameters.get("name");
    Integer appType = safeInt(parameters.get("app_type"));
    String scope = parameters.get("scope_identifier");
    Long startedAfter = Long.parseLong(parameters.get("started_after"));
    Long startedBefore = Long.parseLong(parameters.get("started_before"));
    Integer limit = ExecutionQueryServlet.getQueryLimit(parameters);

    Set<String> tokens = null;
    if (stepToken != null) {
      tokens = Sets.newHashSet(stepToken);
    }

    List<WorkflowExecution> executions = WorkflowQueries.queryWorkflowExecutions(workflowDatabases, name, scope, appType, startedAfter, startedBefore, limit);

    Set<Long> executionIds = Sets.newHashSet();
    for (WorkflowExecution execution : executions) {
      executionIds.add(execution.getId());
    }

    GenericQuery query = WorkflowQueries.getMapreduceCounters(workflowDb, tokens, executionIds, null, null)
        .select(new ListBuilder<Column>()
            .addAll(MapreduceCounter.TBL.getAllColumns())
            .add(WorkflowAttempt.WORKFLOW_EXECUTION_ID)
            .add(StepAttempt.START_TIME)
            .add(StepAttempt.STEP_TOKEN).get());

    for (Record record : query.fetch()) {

      String group = record.getString(MapreduceCounter.GROUP);
      String counterName = record.getString(MapreduceCounter.NAME);
      Long value = record.getLong(MapreduceCounter.VALUE);

      countersPerExecution.incrementAndGet((long)record.getInt(WorkflowAttempt.WORKFLOW_EXECUTION_ID),
          group, counterName, value
      );

      countersPerDay.incrementAndGet(new DateTime(record.getLong(StepAttempt.START_TIME)).withTimeAtStartOfDay().getMillis(),
          group, counterName, value
      );

      countersPerStep.incrementAndGet(record.getString(StepAttempt.STEP_TOKEN),
          group, counterName, value
      );

    }

    for (Record record : WorkflowQueries.getStepAttempts(workflowDb, tokens, executionIds)
        .select(StepAttempt.TBL.getAllColumns()).fetch()) {

      StepAttempt.Attributes model = record.getAttributes(StepAttempt.TBL);

      if (model.getStartTime() != null && model.getEndTime() != null) {
        countersPerStep.incrementAndGet(model.getStepToken(), "Duration", "Total",
            model.getEndTime() - model.getStartTime()
        );
      }

    }

    for (DerivedCounter derivedCounter : DERIVED_COUNTERS) {
      populate(countersPerDay, derivedCounter);
      populate(countersPerExecution, derivedCounter);
      populate(countersPerStep, derivedCounter);
    }

    JSONArray executionData = new JSONArray();
    DescriptiveStatistics stats = new DescriptiveStatistics();

    if (stepToken == null) {

      for (WorkflowExecution ex : executions) {

        if (ex.getStartTime() != null && ex.getEndTime() != null) {
          TwoNestedMap<String, String, Long> counters = countersPerExecution.get(ex.getId());
          double costEst = CostUtil.getCost(counters);
          stats.addValue(costEst);

          executionData.put(new JSONObject()
              .put("execution_id", ex.getId())
              .put("start_time", ex.getStartTime())
              .put("counters", toJson(counters))
              .put("duration", ex.getEndTime() - ex.getStartTime())
              .put("estimated_cost", costEst));
        }
      }

    } else {

      for (Record record : WorkflowQueries.getStepAttempts(workflowDb, Sets.newHashSet(stepToken), executionIds)
          .select(new ListBuilder<Column>().add(WorkflowAttempt.WORKFLOW_EXECUTION_ID).addAll(StepAttempt.TBL.getAllColumns()).get())
          .fetch()) {

        Integer executionId = record.getInt(WorkflowAttempt.WORKFLOW_EXECUTION_ID);
        Long end = record.getLong(StepAttempt.END_TIME);
        Long start = record.getLong(StepAttempt.START_TIME);
        TwoNestedMap<String, String, Long> counters = countersPerExecution.get((long)executionId);

        if (end != null && start != null) {
          double costEst = CostUtil.getCost(counters);
          stats.addValue(costEst);

          executionData.put(new JSONObject()
              .put("execution_id", executionId)
              .put("start_time", start)
              .put("counters", toJson(counters))
              .put("duration", end - start)
              .put("estimated_cost", costEst));
        }
      }

    }

    JSONArray dayData = new JSONArray();
    for (Long day : countersPerDay.key1Set()) {
      dayData.put(new JSONObject()
          .put("day", day)
          .put("counters", toJson(countersPerDay.get(day))));
    }

    JSONArray stepData = new JSONArray();
    for (String step : countersPerStep.key1Set()) {
      stepData.put(new JSONObject()
          .put("step", step)
          .put("counters", toJson(countersPerStep.get(step))));
    }

    return new JSONObject()
        .put("executions", executionData)
        .put("daily", dayData)
        .put("steps", stepData)
        .put("render_info", DEFAULT_EXPAND)
        .put("cost_average", stats.getN() == 0 ? 0 : stats.getMean());
  }

  private <T> void populate(ThreeNestedCountingMap<T, String, String> countersPerX, DerivedCounter derived) {
    for (T eps : countersPerX.key1Set()) {
      countersPerX.put(eps, "Derived", derived.getName(), derived.getDerivedValue(countersPerX.get(eps)));
    }
  }

  private JSONArray toJson(TwoNestedMap<String, String, Long> couters) throws JSONException {
    JSONArray array = new JSONArray();

    if (couters != null) {

      for (String group : couters.key1Set()) {

        JSONArray names = new JSONArray();
        for (Map.Entry<String, Long> entry : couters.get(group).entrySet()) {
          names.put(new JSONObject()
              .put("name", entry.getKey())
              .put("value", entry.getValue()));
        }

        array.put(new JSONObject()
            .put("group", group)
            .put("names", names));
      }
    }

    return array;
  }

}
