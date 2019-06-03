package com.liveramp.workflow_ui.servlet.command;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import com.liveramp.databases.workflow_db.IDatabases;
import com.liveramp.databases.workflow_db.IWorkflowDb;
import com.liveramp.databases.workflow_db.models.WorkflowExecution;
import com.liveramp.workflow_db_state.controller.ExecutionController;

public class ExecutionCommandServlet extends HttpServlet {

  private final ThreadLocal<IDatabases> workflowDb;

  public ExecutionCommandServlet(ThreadLocal<IDatabases> workflowDb) {
    this.workflowDb = workflowDb;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    String id = req.getParameter("id");
    String command = req.getParameter("command");

    IWorkflowDb workflowDb = this.workflowDb.get().getWorkflowDb();
    WorkflowExecution execution = workflowDb.workflowExecutions().find(Long.parseLong(id));

    if (execution == null) {
      throw new RuntimeException("Cannot find execution: " + id);
    }

    if (command.equals("cancel")) {
      ExecutionController.cancelExecution(
          workflowDb,
          execution
      );
    }
    //  unknown
    else {
      throw new RuntimeException("Unknown command: " + command);
    }
  }
}
