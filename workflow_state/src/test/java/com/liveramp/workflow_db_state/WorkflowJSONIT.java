package com.liveramp.workflow_db_state;

import org.json.JSONObject;
import org.junit.Test;

import com.liveramp.databases.workflow_db.DatabasesImpl;
import com.liveramp.databases.workflow_db.models.WorkflowAttempt;
import com.liveramp.workflow.types.WorkflowAttemptStatus;
import com.liveramp.workflow_db_state.json.WorkflowJSON;

import static org.junit.Assert.assertEquals;

public class WorkflowJSONIT extends WorkflowDbStateTestCase {

  @Test
  public void testToJSON() throws Exception {

    WorkflowAttempt obj = new DatabasesImpl().getWorkflowDb().workflowAttempts().create(
        1, "ben", "default", "default", "bll"
    );

    obj.setStatus(WorkflowAttemptStatus.FAILED.ordinal());

    JSONObject str = WorkflowJSON.toJSON(obj.getAttributes());

    assertEquals("FAILED", str.getString("status"));

  }
}