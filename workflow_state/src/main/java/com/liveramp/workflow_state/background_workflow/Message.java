package com.liveramp.workflow_state.background_workflow;

public class Message {

  private final String title;
  private final String message;
  private final String hostname;
  private final String tag;

  Message(String title, String message, String hostname, String tag) {
    this.title = title;
    this.message = message;
    this.hostname = hostname;
    this.tag = tag;
  }

  public String getTitle() {
    return title;
  }

  public String getMessage() {
    return message;
  }

  public String getHostname() {
    return hostname;
  }

  public String getTag() {
    return tag;
  }
}
