package com.liveramp.workflow_core.alerting;

import java.util.Set;

import com.liveramp.java_support.alerts_handler.AlertsHandler;
import com.liveramp.java_support.alerts_handler.BufferingAlertsHandler;
import com.liveramp.java_support.alerts_handler.MailBuffer;
import com.liveramp.java_support.alerts_handler.recipients.RecipientUtils;

public class BufferingAlertsHandlerFactory implements  AlertsHandlerFactory{

  private final BufferingAlertsHandler.MessageBuffer buffer = new BufferingAlertsHandler.MessageBuffer();

  @Override
  public AlertsHandler buildHandler(Set<String> emails, MailBuffer buffer) {
    return new BufferingAlertsHandler(this.buffer, RecipientUtils.of(emails));
  }

  public BufferingAlertsHandler.MessageBuffer getBuffer() {
    return buffer;
  }
}
