package com.liveramp.workflow_ui.servlet;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.liveramp.commons.collections.list.ListBuilder;
import com.liveramp.commons.collections.nested_map.TwoNestedCountingMap;
import com.liveramp.commons.collections.set.SetBuilder;
import com.liveramp.databases.workflow_db.models.MapreduceJob;
import com.liveramp.databases.workflow_db.models.MapreduceJobTaskException;
import com.liveramp.databases.workflow_db.models.StepAttempt;
import com.liveramp.databases.workflow_db.models.WorkflowAttempt;
import com.liveramp.databases.workflow_db.models.WorkflowExecution;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liveramp.databases.workflow_db.IDatabases;
import com.liveramp.databases.workflow_db.IWorkflowDb;

import com.rapleaf.jack.queries.Column;
import com.rapleaf.jack.queries.QueryOrder;
import com.rapleaf.jack.queries.Record;
import com.rapleaf.jack.queries.Records;


public class TaskExceptionServlet implements JSONServlet.Processor {
  private static final Logger LOG = LoggerFactory.getLogger(TaskExceptionServlet.class);
  private static final Integer QUERY_LIMIT = 100;
  private static final Integer EXCEPTION_LENGTH_LIMIT = 500;

  @Override
  public JSONObject getData(IDatabases workflowDb, Map<String, String> parameters) throws Exception {
    IWorkflowDb db = workflowDb.getWorkflowDb();
    int queryLimit = getQueryLimit(parameters);

    JSONArray rawRows = new JSONArray();
    JSONArray hostRows = new JSONArray();
    JSONArray exceptionRows = new JSONArray();
    JSONArray appRows = new JSONArray();

    //all exceptions
    Records all_exceptions = db.createQuery()
        .from(MapreduceJobTaskException.TBL)
        .innerJoin(MapreduceJob.TBL)
        .on(MapreduceJobTaskException.MAPREDUCE_JOB_ID.equalTo(MapreduceJob.ID.as(Integer.class)))
        .innerJoin(StepAttempt.TBL)
        .on(MapreduceJob.STEP_ATTEMPT_ID.equalTo(StepAttempt.ID))
        .innerJoin(WorkflowAttempt.TBL)
        .on(StepAttempt.WORKFLOW_ATTEMPT_ID.equalTo(WorkflowAttempt.ID.as(Integer.class)))
        .innerJoin(WorkflowExecution.TBL)
        .on(WorkflowAttempt.WORKFLOW_EXECUTION_ID.equalTo(WorkflowExecution.ID.as(Integer.class)))
        .orderBy(MapreduceJobTaskException.ID, QueryOrder.DESC)
        .limit(queryLimit)
        .select(new ListBuilder<Column>()
            .addAll(MapreduceJobTaskException.TBL.getAllColumns())
            .add(WorkflowExecution.NAME)
            .get())
        .fetch();

    Multimap<Long, JSONObject> mrIdsToTasks = HashMultimap.create();

    TwoNestedCountingMap<String, String> exceptionToHostToCount = new TwoNestedCountingMap<>(0L);
    TwoNestedCountingMap<String, String> hostToExceptionToCount = new TwoNestedCountingMap<>(0L);
    TwoNestedCountingMap<String, String> appToExceptionToCount = new TwoNestedCountingMap<>(0L);

    for (Record exception : all_exceptions) {
      if (exception != null) {

        //gathering information about the exception
        JSONObject exception_json = new JSONObject();
        long mr_id = (long)exception.getInt(MapreduceJobTaskException.MAPREDUCE_JOB_ID);
        String exception_text = exception.getString(MapreduceJobTaskException.EXCEPTION);

        String shrunk_exception_text = shrink(
            exception_text,
            EXCEPTION_LENGTH_LIMIT
        );

        String hostUrl = exception.getString(MapreduceJobTaskException.HOST_URL);
        String appName = exception.getString(WorkflowExecution.NAME);


        if (hostUrl == null) {
          hostUrl = "null";
        } else {
          hostUrl = noPort(hostUrl);
        }

        exception_json
            .put("mr_job_id", mr_id)
            .put("task_attempt_id", exception.getString(MapreduceJobTaskException.TASK_ATTEMPT_ID))
            .put("exception", shrunk_exception_text)
            .put("host_url", hostUrl);

        mrIdsToTasks.put(mr_id, exception_json);

        String scrubbedException = scrub(shrunk_exception_text);

        appToExceptionToCount.incrementAndGet(appName, scrubbedException, 1L);

        //removing null host_urls is cleaner
        if (!hostUrl.equals("null")) {

          // the point of this is to build a map of 'scrubbed exceptions,' i.e., exceptions with numbers removed (to
          // un-unique the exceptions on attempt ids, for instance), to the number of times each has occurred on given
          // hosts

          exceptionToHostToCount.incrementAndGet(scrubbedException, hostUrl, 1L);
          hostToExceptionToCount.incrementAndGet(hostUrl, scrubbedException, 1L);

        }
      }
    }

    for (String exception_key : exceptionToHostToCount.key1Set()) {
      Map<String, Long> host_map = exceptionToHostToCount.get(exception_key);
      JSONObject exception_host_json = new JSONObject();
      exception_host_json.put("exception", exception_key);
      JSONArray host_strings = new JSONArray();
      for (String host_key : host_map.keySet()) {
        host_strings.put(host_map.get(host_key).toString());
      }
      exception_host_json.put("hosts", host_strings);
      exceptionRows.put(exception_host_json);
    }

    for (String host_key : hostToExceptionToCount.key1Set()) {
      Map<String, Long> exception_map = hostToExceptionToCount.get(host_key);
      JSONArray exceptionStrings = new JSONArray();
      for (String exceptionKey : exception_map.keySet()) {
        exceptionStrings.put(exceptionKey
        );
      }

      int total = 0;
      for (Map.Entry<String, Long> entries : exception_map.entrySet()) {
        total += entries.getValue();
      }

      hostRows.put(new JSONObject()
          .put("host", host_key)
          .put("exceptions", exceptionStrings)
          .put("exceptions_count", total)
      );
    }

    for (String app : appToExceptionToCount.key1Set()) {
      Map<String, Long> byException = appToExceptionToCount.get(app);
      appRows.put(new JSONObject()
          .put("app", app)
          .put("count", byException.entrySet().stream().mapToLong(Map.Entry::getValue).sum())
          .put("by_exception", byException)
      );
    }

    Records names_and_identifiers = db.createQuery()
        .from(MapreduceJob.TBL)
        .where(MapreduceJob.ID.in(mrIdsToTasks.keySet()))
        .select(MapreduceJob.ID, MapreduceJob.JOB_NAME, MapreduceJob.STEP_ATTEMPT_ID, MapreduceJob.TRACKING_URL)
        .fetch();

    Map<Long, Set<Long>> step_attempt_ids_to_mr_ids = new HashMap<>();
    // attach names and identifiers to exceptions
    for (Record record : names_and_identifiers) {
      if (record != null) {

        long mr_id = record.get(MapreduceJob.ID);
        long step_attempt_id = record.get(MapreduceJob.STEP_ATTEMPT_ID);
        String tracking_url = record.get(MapreduceJob.TRACKING_URL);
        String job_name = record.get(MapreduceJob.JOB_NAME);

        if (step_attempt_ids_to_mr_ids.containsKey(step_attempt_id)) {
          step_attempt_ids_to_mr_ids.get(step_attempt_id).add(mr_id);
        } else {
          step_attempt_ids_to_mr_ids.put(step_attempt_id, new SetBuilder<Long>().add(mr_id).get());
        }

        for (JSONObject exception : mrIdsToTasks.get(mr_id)) {
          exception.put("job_name", job_name);
          exception.put("tracking_url", tracking_url);
        }

      }
    }

    Records step_attempts = db.createQuery()
        .from(StepAttempt.TBL)
        .where(StepAttempt.ID.in(step_attempt_ids_to_mr_ids.keySet()))
        .select(StepAttempt.WORKFLOW_ATTEMPT_ID, StepAttempt.ID, StepAttempt.STEP_TOKEN, StepAttempt.END_TIME)
        .fetch();

    for (Record record : step_attempts) {
      if (record != null) {
        long step_attempt_id = record.get(StepAttempt.ID);
        long workflow_attempt_id = record.get(StepAttempt.WORKFLOW_ATTEMPT_ID).longValue();
        String step_token = record.get(StepAttempt.STEP_TOKEN);
        Long time = record.get(StepAttempt.END_TIME);
        for (Long mr_id : step_attempt_ids_to_mr_ids.get(step_attempt_id)) {
          for (JSONObject exception : mrIdsToTasks.get(mr_id)) {
            if (time != null) {
              exception.put("time", time);
            } else {
              exception.put("time", "null");
            }
            rawRows.put(
                exception
                    .put("step_token", step_token)
                    .put("workflow_attempt_id", workflow_attempt_id)
            );
          }
        }

      }
    }


    return new JSONObject()
        .put("all", rawRows)
        .put("by_host", hostRows)
        .put("by_exception", exceptionRows)
        .put("by_app", appRows)
        .put("limit", queryLimit);
  }

  private static int getQueryLimit(Map<String, String> parameters) {
    String limit = parameters.get("limit");
    if (limit != null) {
      return Integer.parseInt(limit);
    }
    return QUERY_LIMIT;
  }


  private static String shrink(String str, Integer len) {
    if (str.length() > len - 4) {
      return str.substring(0, len - 4) + " ...";
    }
    return str;
  }

  private static String scrub(String exception) {
    return exception
        .replaceAll("[0-9]", "i");
  }

  private static String noPort(String url) {
    return url.replaceFirst("http://", "").replaceFirst(":[-\\d]+$", "");
  }
}
