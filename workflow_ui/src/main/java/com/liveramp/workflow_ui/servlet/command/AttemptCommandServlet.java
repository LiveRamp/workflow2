package com.liveramp.workflow_ui.servlet.command;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liveramp.databases.workflow_db.IDatabases;
import com.liveramp.workflow_db_state.DbPersistence;

public class AttemptCommandServlet extends HttpServlet {
  private static final Logger LOG = LoggerFactory.getLogger(AttemptCommandServlet.class);

  private final ThreadLocal<IDatabases> workflowDb;

  public AttemptCommandServlet(ThreadLocal<IDatabases> workflowDb ) {
    this.workflowDb = workflowDb;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    String command = req.getParameter("command");

    Integer id = Integer.parseInt(req.getParameter("id"));
    LOG.info("Processing command " + command + " for workflow attempt " + id);

    DbPersistence persistence = DbPersistence.queryPersistence(id, workflowDb.get().getWorkflowDb());

    if (command.equals("shutdown")) {
      persistence.markShutdownRequested(req.getParameter("reason"));
    } else if (command.equals("set_pool")) {
      persistence.markPool(req.getParameter("pool"));
    } else if (command.equals("set_priority")) {
      persistence.markPriority(req.getParameter("priority"));
    } else if (command.equals("revert_step")) {
      persistence.markStepReverted(req.getParameter("step"));
    } else if (command.equals("manually_complete_step")){
      persistence.markStepManuallyCompleted(req.getParameter("step"));
    } else {
      throw new RuntimeException("Unknown command " + command);
    }


  }
}
