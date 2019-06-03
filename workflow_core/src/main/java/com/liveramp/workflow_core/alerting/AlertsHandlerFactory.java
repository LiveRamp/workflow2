package com.liveramp.workflow_core.alerting;

import java.util.Set;

import com.liveramp.java_support.alerts_handler.AlertsHandler;
import com.liveramp.java_support.alerts_handler.MailBuffer;

public interface AlertsHandlerFactory {
  public AlertsHandler buildHandler(Set<String> emails, MailBuffer buffer);
}

