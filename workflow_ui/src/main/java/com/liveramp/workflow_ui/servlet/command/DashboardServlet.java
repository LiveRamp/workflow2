package com.liveramp.workflow_ui.servlet.command;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liveramp.commons.Accessors;
import com.liveramp.databases.workflow_db.IDatabases;
import com.liveramp.databases.workflow_db.IWorkflowDb;
import com.liveramp.databases.workflow_db.iface.IDashboardPersistence;
import com.liveramp.databases.workflow_db.models.Dashboard;
import com.liveramp.workflow_db_state.WorkflowQueries;
import com.liveramp.workflow_ui.util.QueryUtil;
import com.rapleaf.jack.queries.where_operators.EqualTo;

public class DashboardServlet extends HttpServlet {
  private static final Logger LOG = LoggerFactory.getLogger(DashboardServlet.class);

  private final ThreadLocal<IDatabases> workflowDb;

  public DashboardServlet(ThreadLocal<IDatabases> workflowDb) {
    this.workflowDb = workflowDb;
  }

  @Override
  //  list all dashboards
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    try {

      String cmd = req.getParameter("cmd");
      String dashName = req.getParameter("name");

      if (cmd.equals("get_config")) {

        resp.getWriter().write(new JSONObject().put(
            "dashboards",
            QueryUtil.getDashboardConfigurations(WorkflowQueries.getDashboardQuery(
                workflowDb.get().getWorkflowDb(),
                dashName
            ))
        ).toString());

      } else if (cmd.equals("get_status")) {

        resp.getWriter().write(new JSONObject().put(
            "statuses",
            QueryUtil.getDashboardStatus(workflowDb.get().getWorkflowDb(), DateTime.now().minusHours(1).getMillis(), dashName)
        ).toString());

      }

    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  //  create new dashboard
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    try {

      resp.getWriter().append(new JSONObject().put(
          "succeeded",
          executeCmd(req)
      ).toString());

    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
  }

  private boolean executeCmd(HttpServletRequest req) throws IOException, JSONException {
    String name = req.getParameter("name");
    String cmd = req.getParameter("cmd");


    IWorkflowDb db = workflowDb.get().getWorkflowDb();

    if (name != null && !name.isEmpty()) {
      if (cmd.equals("create")) {
        db.dashboards().create(name);
        return true;
      } else if (cmd.equals("add_application")) {
        db.dashboardApplications().create(
            Accessors.only(db.dashboards().findByName(name)).getIntId(),
            Accessors.only(db.applications().findByName(req.getParameter("application_name"))).getIntId()
        );
        return true;
      } else if (cmd.equals("delete_application")) {
        db.dashboardApplications().delete()
            .whereDashboardId(new EqualTo<Integer>(Accessors.only(db.dashboards().findByName(name)).getIntId()))
            .whereApplicationId(new EqualTo<Integer>(Accessors.only(db.applications().findByName(req.getParameter("application_name"))).getIntId()))
            .execute();
        return true;
      }
    }

    return false;
  }

  @Override
  protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    try {

      resp.getWriter().write(new JSONObject().put(
          "deleted",
          tryDelete(req.getParameter("name"))
          ).toString()
      );

    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
  }

  private boolean tryDelete(String name) throws IOException, JSONException {

    if (name != null) {

      if (!name.isEmpty()) {
        IWorkflowDb db = workflowDb.get().getWorkflowDb();
        IDashboardPersistence dashboards = db.dashboards();
        List<Dashboard> matches = dashboards.findByName(name);

        if (!matches.isEmpty()) {
          Dashboard dash = Accessors.only(matches);
          LOG.info("Deleting dashboard: " + dash);

          //  delete associated dash apps
          db.dashboardApplications().delete()
              .whereDashboardId(new EqualTo<>(dash.getIntId()))
              .execute();

          db.userDashboards().delete()
              .whereDashboardId(new EqualTo<>(dash.getIntId()))
              .execute();

          dashboards.delete(dash);

          return true;
        }
      }
    }

    return false;
  }
}
