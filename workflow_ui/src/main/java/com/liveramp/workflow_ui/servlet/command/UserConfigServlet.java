package com.liveramp.workflow_ui.servlet.command;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import com.liveramp.commons.Accessors;
import com.liveramp.databases.workflow_db.IDatabases;
import com.liveramp.databases.workflow_db.IWorkflowDb;
import com.liveramp.databases.workflow_db.iface.IUserPersistence;
import com.liveramp.databases.workflow_db.models.User;
import com.liveramp.workflow_db_state.WorkflowQueries;
import com.liveramp.workflow_ui.util.QueryUtil;
import com.rapleaf.jack.queries.where_operators.EqualTo;

public class UserConfigServlet extends HttpServlet {
  private static final Logger LOG = LoggerFactory.getLogger(UserConfigServlet.class);


  private final ThreadLocal<IDatabases> workflowDbLocal;
  private final String domain;

  public UserConfigServlet(ThreadLocal<IDatabases> workflowDb, String domain) {
    this.workflowDbLocal = workflowDb;
    this.domain = domain;
  }


  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    try {

      req.getSession().setMaxInactiveInterval((int) TimeUnit.DAYS.toSeconds(30l));

      String name = getUsername();

      IWorkflowDb workflowDb = workflowDbLocal.get().getWorkflowDb();
      IUserPersistence users = workflowDb.users();
      User user = getOrCreate(name, users);

      JSONArray dashboardInfo = QueryUtil.getDashboardConfigurations(
          WorkflowQueries.getUserDashboardQuery(workflowDb, user)
      );

      resp.getWriter().write(new JSONObject()
          .put("username", user.getUsername())
          .put("notification_email", user.getNotificationEmail())
          .put("dashboards", dashboardInfo).toString());

    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }

  private String getUsername() {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (principal instanceof UserDetails) {
      UserDetails ldapUser = (UserDetails)principal;
      return ldapUser.getUsername().toLowerCase().trim();
    } else {
      return "unknown";
    }
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    String cmd = req.getParameter("cmd");
    IWorkflowDb db = workflowDbLocal.get().getWorkflowDb();
    String username = getUsername();

    if (cmd.equals("add_dashboard")) {
      String name = req.getParameter("dashboard_name");

      db.userDashboards().create(
          Accessors.only(db.users().findByUsername(username)).getIntId(),
          Accessors.only(db.dashboards().findByName(name)).getIntId()
      );

    } else if (cmd.equals("remove_dashboard")) {
      String name = req.getParameter("dashboard_name");

      db.userDashboards().delete()
          .whereUserId(new EqualTo<>(Accessors.only(db.users().findByUsername(username)).getIntId()))
          .whereDashboardId(new EqualTo<>(Accessors.only(db.dashboards().findByName(name)).getIntId()))
          .execute();

    }

  }

  private User getOrCreate(String name, IUserPersistence users) throws IOException {

    List<User> matches = users.findByUsername(name);

    if (matches.isEmpty()) {
      LOG.info("Creating User for name: " + name);
      return users.create(name, name + "@"+domain);
    } else {
      return Accessors.only(matches);
    }
  }
}
