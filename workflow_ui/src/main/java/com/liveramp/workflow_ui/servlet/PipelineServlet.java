package com.liveramp.workflow_ui.servlet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.apache.commons.collections.comparators.ReverseComparator;
import org.json.JSONArray;
import org.json.JSONObject;

import com.liveramp.commons.Accessors;
import com.liveramp.commons.collections.lightweight_trie.ImmutableStringRadixTreeMap;
import com.liveramp.commons.collections.lightweight_trie.StringRadixTreeMap;
import com.liveramp.commons.collections.map.NestedMultimap;
import com.liveramp.databases.workflow_db.IDatabases;
import com.liveramp.databases.workflow_db.models.Application;
import com.liveramp.databases.workflow_db.models.WorkflowAttempt;
import com.liveramp.databases.workflow_db.models.WorkflowAttemptDatastore;
import com.liveramp.databases.workflow_db.models.WorkflowExecution;
import com.liveramp.workflow_db_state.WorkflowQueries;
import com.liveramp.workflow_db_state.json.WorkflowJSON;
import com.liveramp.workflow_state.DSAction;

import static com.liveramp.workflow_ui.util.QueryUtil.safeLong;

public class PipelineServlet implements JSONServlet.Processor {

  @Override
  public JSONObject getData(IDatabases workflowDb, Map<String, String> parameters) throws Exception {

    //  get all workflow executions in the past 2 weeks + latest attempt

    Long after = safeLong(parameters.get("started_after"));
    Long before = safeLong(parameters.get("started_before"));

    Multimap<WorkflowExecution, WorkflowAttempt> executionsToAttempts = WorkflowQueries.getExecutionsToAttempts(workflowDb, null, null, null, null, after, before, null, null);

    Multimap<Long, WorkflowExecution> executionsByApp = HashMultimap.create();
    Map<Long, WorkflowAttempt> executionToMostRecentAttempt = Maps.newHashMap();
    for (WorkflowExecution execution : executionsToAttempts.keySet()) {
      ArrayList<WorkflowAttempt> attempts = Lists.newArrayList(executionsToAttempts.get(execution));
      //noinspection unchecked
      Collections.sort(attempts, new ReverseComparator());
      WorkflowAttempt lastAttempt = Accessors.first(attempts);

      if (lastAttempt.getPool() == null) {
        executionsByApp.put((long)execution.getApplicationId(), execution);
        executionToMostRecentAttempt.put(execution.getId(), lastAttempt);
      }
    }

    Map<Long, WorkflowExecution> appToMostRecentExecution = Maps.newHashMap();
    for (Long application : executionsByApp.keySet()) {
      List<WorkflowExecution> executions = Lists.newArrayList(executionsByApp.get(application));
      //noinspection unchecked
      Collections.sort(executions, new ReverseComparator());
      appToMostRecentExecution.put(application, Accessors.first(executions));
    }

    Map<Long, Application> applicationById = Maps.newHashMap();
    for (Application application : workflowDb.getWorkflowDb().applications().query()
        .idIn(executionsByApp.keySet())
        .find()) {
      applicationById.put(application.getId(), application);
    }

    NestedMultimap<Long, DSAction, WorkflowAttemptDatastore> stores = WorkflowQueries.getApplicationIdToWorkflowAttemptDatastores(workflowDb, after, before);

    Multimap<String, Long> pathToProducers = HashMultimap.create();
    Multimap<String, Long> pathToConsumers = HashMultimap.create();

    Map<String, String> pathToStoreName = Maps.newHashMap();
    Map<String, String> pathToStoreClass = Maps.newHashMap();

    StringRadixTreeMap<Boolean> prefixMap = new StringRadixTreeMap<>();

    for (Long application : stores.k1Set()) {
      for (Map.Entry<DSAction, WorkflowAttemptDatastore> entry : stores.get(application).entries()) {

        WorkflowAttemptDatastore store = entry.getValue();
        String path = store.getPath();

        path = path.replaceAll("[[0-9]*/]+$", "");

        //  there's no good reason for this to be empty except misconfigured datastores, but
        //  someone managed to do it 
        if (!path.isEmpty()) {

          pathToStoreName.put(path, store.getName());
          pathToStoreClass.put(path, store.getClassName());

          prefixMap.put(path, true);

        }
      }
    }

    ImmutableStringRadixTreeMap<Boolean> processedMap = new ImmutableStringRadixTreeMap<>(prefixMap);

    for (Long application : stores.k1Set()) {
      for (Map.Entry<DSAction, WorkflowAttemptDatastore> entry : stores.get(application).entries()) {

        WorkflowAttemptDatastore store = entry.getValue();
        DSAction action = entry.getKey();

        List<String> matches = Lists.newArrayList(processedMap.getPartialMatches(store.getPath()));

        Collections.sort(matches, new Comparator<String>() {
          @Override
          public int compare(String o1, String o2) {
            return o1.length() - o2.length();
          }
        });
        String canonicalPath = Accessors.first(matches);

        if (DSAction.INPUT.contains(action)) {
          pathToConsumers.put(canonicalPath, application);
        } else if (DSAction.OUTPUT.contains(action)) {
          pathToProducers.put(canonicalPath, application);
        }

      }
    }

    NestedMultimap<Long, Long, String> producerToConsumerToPaths = new NestedMultimap<>();
    for (String path : pathToProducers.keySet()) {
      for (Long producer : pathToProducers.get(path)) {
        for (Long consumer : pathToConsumers.get(path)) {
          producerToConsumerToPaths.put(producer, consumer, path);
        }
      }
    }

    JSONArray links = new JSONArray();
    for (Long producer : producerToConsumerToPaths.k1Set()) {
      for (Long consumer : producerToConsumerToPaths.get(producer).keySet()) {

        if (!producer.equals(consumer) && applicationById.containsKey(producer) && applicationById.containsKey(consumer)) {

          JSONArray connections = new JSONArray();

          for (String path : producerToConsumerToPaths.get(producer).get(consumer)) {
            connections.put(new JSONObject()
                .put("path", path)
                .put("name", pathToStoreName.get(path))
                .put("class", pathToStoreClass.get(path)));
          }

          links.put(new JSONObject()
              .put("producer", producer)
              .put("consumer", consumer)
              .put("connections", connections));

        }
      }
    }

    JSONObject applications = new JSONObject();

    for (Map.Entry<Long, Application> app : applicationById.entrySet()) {
      Long appId = app.getKey();
      WorkflowExecution execution = appToMostRecentExecution.get(appId);

      applications.put(Long.toString(appId), new JSONObject()
          .put("application", WorkflowJSON.toJSON(applicationById.get(appId).getAttributes()))
          .put("execution", WorkflowJSON.toJSON(execution.getAttributes()))
          .put("attempt", WorkflowJSON.toJSON(executionToMostRecentAttempt.get(execution.getId()).getAttributes())));

    }

    return new JSONObject()
        .put("applications", applications)
        .put("links", links);

  }


}
