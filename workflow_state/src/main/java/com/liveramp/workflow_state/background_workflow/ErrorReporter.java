package com.liveramp.workflow_state.background_workflow;

import java.util.List;

import com.google.common.collect.Lists;

public interface ErrorReporter {
  void reportError(String title, String message, String hostname, String tag);

  public static class InMemoryReporter implements ErrorReporter {

    private final List<Message> messages = Lists.newArrayList();

    @Override
    public void reportError(String title, String message, String hostname, String tag) {
      messages.add(new Message(title, message, hostname, tag));
    }

    public List<Message> getSentEvents() {
      return messages;
    }
  }

}
