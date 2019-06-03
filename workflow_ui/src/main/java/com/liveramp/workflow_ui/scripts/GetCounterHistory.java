package com.liveramp.workflow_ui.scripts;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.liveramp.databases.workflow_db.DatabasesImpl;
import com.liveramp.databases.workflow_db.IWorkflowDb;
import com.liveramp.databases.workflow_db.models.ApplicationCounterSummary;

import static com.liveramp.workflow_ui.util.QueryUtil.computeSummaries;

public class GetCounterHistory {

  public static void main(String[] args) throws IOException, SQLException {

    String app = args[0];

    IWorkflowDb db = new DatabasesImpl().getWorkflowDb();

    String[] counters = args[1].split(",");


    Multimap<String, String> countersToQuery = HashMultimap.create();
    for (String counter : counters) {
      int index = counter.lastIndexOf(".");
      String name = counter.substring(0, index);
      String group = counter.substring(index + 1);

      countersToQuery.put(name, group);

    }

    DateTimeFormatter formatter = DateTimeFormat.forPattern("MM/dd/yyyy");

    DateTime start = formatter.parseDateTime(args[2]);
    DateTime end = formatter.parseDateTime(args[3]);

    LocalDate startDate = start.toLocalDate();
    LocalDate endDate = end.toLocalDate();

    System.out.println("querying:");
    System.out.println("app: "+app);
    System.out.println("counters: "+countersToQuery);
    System.out.println("start: "+startDate);
    System.out.println("end: "+endDate);

    List<ApplicationCounterSummary> summaries = computeSummaries(db, app, countersToQuery, startDate, endDate);

    for (ApplicationCounterSummary summary : summaries) {
      System.out.println(new Date(summary.getDate())+"\t"+summary.getApplication().getName()+"\t"+summary.getGroup()+"\t"+summary.getName()+"\t"+summary.getValue());
    }


  }

}
