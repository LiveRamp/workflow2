package com.rapleaf.cascading_ext.workflow2;

import java.io.IOException;

import org.apache.http.client.utils.URIBuilder;

import com.liveramp.workflow_state.WorkflowStatePersistence;

public class DbTrackerURLBuilder implements TrackerURLBuilder{

  private final String host;

  public DbTrackerURLBuilder(String host) {
    this.host = host;
  }

  @Override
  public String buildURL(WorkflowStatePersistence persistence) throws IOException {
    return new URIBuilder()
        .setHost(host)
        .setPath("/workflow.html")
        .addParameter("id", Long.toString(persistence.getAttemptId())).toString();
  }

}
