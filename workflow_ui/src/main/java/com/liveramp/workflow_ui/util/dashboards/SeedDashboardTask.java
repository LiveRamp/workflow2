package com.liveramp.workflow_ui.util.dashboards;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liveramp.commons.Accessors;
import com.liveramp.databases.workflow_db.IDatabases;
import com.liveramp.databases.workflow_db.IWorkflowDb;
import com.liveramp.databases.workflow_db.models.Dashboard;
import com.liveramp.databases.workflow_db.models.DashboardApplication;
import com.liveramp.databases.workflow_db.models.WorkflowAttempt;
import com.liveramp.databases.workflow_db.models.WorkflowExecution;

import com.rapleaf.jack.queries.Record;
import com.rapleaf.jack.queries.where_operators.EqualTo;

import static com.rapleaf.jack.queries.AggregatedColumn.COUNT;

public class SeedDashboardTask extends TimerTask {
  private static final Logger LOG = LoggerFactory.getLogger(SeedDashboardTask.class);

  private static final Pattern EMAIL_MATCH = Pattern.compile("([0-9a-zA-Z\\-.]+)(\\+[0-9a-zA-Z\\-.]+)?@[0-9a-zA-Z\\-.]+");

  private final ThreadLocal<IDatabases> threadLocalDb;
  private final int searchWindowMs;

  public SeedDashboardTask(ThreadLocal<IDatabases> db, int searchWindowMs) {
    this.threadLocalDb = db;
    this.searchWindowMs = searchWindowMs;
  }

  public static Multimap<Integer, String> getAppEmails(IWorkflowDb db, DateTime finishedSince) throws IOException {

    Multimap<Integer, String> appEmails = HashMultimap.create();

    for (Record record : db.createQuery()
        .from(WorkflowExecution.TBL)
        .where(WorkflowAttempt.END_TIME.greaterThan(finishedSince.getMillis()))
        .innerJoin(WorkflowAttempt.TBL)
        .on(WorkflowAttempt.WORKFLOW_EXECUTION_ID.equalTo(WorkflowExecution.ID.as(Integer.class)))
        .groupBy(WorkflowExecution.APPLICATION_ID, WorkflowAttempt.INFO_EMAIL, WorkflowAttempt.ERROR_EMAIL)
        .select(WorkflowExecution.APPLICATION_ID, WorkflowAttempt.INFO_EMAIL, WorkflowAttempt.ERROR_EMAIL, COUNT(WorkflowAttempt.ID))
        .fetch()) {

      Integer appId = record.get(WorkflowExecution.APPLICATION_ID);

      String errorEmail = record.get(WorkflowAttempt.ERROR_EMAIL);
      if (errorEmail != null) {
        appEmails.put(appId, errorEmail);
      }

      String infoEmail = record.get(WorkflowAttempt.INFO_EMAIL);
      if (infoEmail != null) {
        appEmails.put(appId, infoEmail);
      }

    }

    return appEmails;
  }

  public static void assignToDashboards(IWorkflowDb db, Multimap<Integer, String> appEmails) throws IOException {

    for (Map.Entry<Integer, String> entry : appEmails.entries()) {

      Matcher matcher = EMAIL_MATCH.matcher(entry.getValue());

      if (matcher.matches()) {
        String list = matcher.group(1);

        List<Dashboard> forName = db.dashboards().findByName(list);

        if (forName.isEmpty()) {
          LOG.info("Creating dashboard for name: " + list);
          db.dashboards().create(list);
        }

        Dashboard dash = Accessors.only(db.dashboards().findByName(list));
        Integer appId = entry.getKey();

        List<DashboardApplication> dashApps = db.dashboardApplications().query()
            .whereApplicationId(new EqualTo<>(appId))
            .whereDashboardId(new EqualTo<>(dash.getIntId()))
            .find();

        if (dashApps.isEmpty()) {
          LOG.info("Linking dashboard " + list + " to app " + list);
          db.dashboardApplications().create(
              dash.getIntId(),
              appId
          );
        }

      }

    }

  }

  @Override
  public void run() {
    try {
      LOG.info("Searching for executions to auto-assign to dashboards");
      IWorkflowDb db = this.threadLocalDb.get().getWorkflowDb();
      assignToDashboards(db, getAppEmails(db, DateTime.now().minusMillis(this.searchWindowMs)));
      LOG.info("Done auto assigning dashboards");
    } catch (IOException e) {
      LOG.error("Error updating dashboards", e);
      throw new RuntimeException(e);
    }
  }
}
