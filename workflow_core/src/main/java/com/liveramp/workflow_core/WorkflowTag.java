package com.liveramp.workflow_core;

import java.util.Objects;

public class WorkflowTag {
  private final String key;
  private final String val;

  public static WorkflowTag of(String key, String val) {
    return new WorkflowTag(key, val);
  }

  private WorkflowTag(String key, String val) {
    this.key = key;
    this.val = val;
  }

  public String getKey() {
    return key;
  }

  public String getVal() {
    return val;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    WorkflowTag that = (WorkflowTag)o;
    return Objects.equals(key, that.key) &&
        Objects.equals(val, that.val);
  }

  @Override
  public int hashCode() {
    return Objects.hash(key, val);
  }
}
