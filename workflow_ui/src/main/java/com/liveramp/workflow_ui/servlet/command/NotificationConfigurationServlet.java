package com.liveramp.workflow_ui.servlet.command;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

import com.google.common.collect.Sets;

import com.liveramp.databases.workflow_db.IDatabases;
import com.liveramp.databases.workflow_db.IWorkflowDb;
import com.liveramp.workflow_db_state.controller.ApplicationController;
import com.liveramp.workflow_db_state.controller.ExecutionController;
import com.liveramp.workflow_state.WorkflowRunnerNotification;

public class NotificationConfigurationServlet extends HttpServlet {

  private final ThreadLocal<IDatabases> databases;

  public NotificationConfigurationServlet(ThreadLocal<IDatabases> workflowDb) {
    this.databases = workflowDb;
  }

  private String get(String param, HttpServletRequest req) {
    String value = req.getParameter(param);
    if (value == null) {
      throw new IllegalArgumentException("Parameter " + param + " not found!");
    }
    return value;
  }

  private boolean contains(String param, HttpServletRequest req) {
    return req.getParameter(param) != null;
  }

  private static final String APP_NAME = "application_name";
  private static final String EX_ID = "execution_id";

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    IWorkflowDb worflowDb = databases.get().getWorkflowDb();

    String command = get("command", req);
    String email = get("email", req);

    switch (command) {
      case "add":

        Set<WorkflowRunnerNotification> notifications = Sets.newHashSet();
        for (String notification : req.getParameterValues("notification")) {
          notifications.add(WorkflowRunnerNotification.valueOf(notification));
        }

        if (contains(APP_NAME, req)) {
          ApplicationController.addConfiguredNotifications(worflowDb, get(APP_NAME, req), email, notifications);
        } else if (contains(EX_ID, req)) {
          ExecutionController.addConfiguredNotifications(worflowDb, Long.parseLong(get(EX_ID, req)), email, notifications);
        } else {
          throw new IllegalArgumentException();
        }
        break;
      case "remove":
        if (contains(APP_NAME, req)) {
          ApplicationController.removeConfiguredNotifications(worflowDb, get(APP_NAME, req), email);
        } else if (contains(EX_ID, req)) {
          ExecutionController.removeConfiguredNotifications(worflowDb, Long.parseLong(get(EX_ID, req)), email);
        } else {
          throw new IllegalArgumentException();
        }
        break;
      default:
        throw new IllegalArgumentException("command " + command + " not  found!");
    }

  }

}
