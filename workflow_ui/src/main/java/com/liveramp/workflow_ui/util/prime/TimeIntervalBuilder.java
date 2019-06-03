package com.liveramp.workflow_ui.util.prime;

import java.util.LinkedHashMap;

import org.joda.time.DateTime;

public class TimeIntervalBuilder implements RequestBuilder {

  private final int afterOffset;
  private final int beforeOffset;

  public TimeIntervalBuilder(int afterOffset, int beforeOffset) {
    this.afterOffset = afterOffset;
    this.beforeOffset = beforeOffset;
  }

  @Override
  public LinkedHashMap<String, String> getRequestParams() {

    LinkedHashMap<String, String> params = new LinkedHashMap<>();
    params.put("started_after", Long.toString(new DateTime()
        .toLocalDate()
        .plusDays(afterOffset)
        .toDateTimeAtStartOfDay()
        .toDate()
        .getTime()));

    params.put("started_before", Long.toString(new DateTime()
        .toLocalDate()
        .plusDays(beforeOffset)
        .toDateTimeAtStartOfDay()
        .toDate()
        .getTime()));

    return params;
  }
}
