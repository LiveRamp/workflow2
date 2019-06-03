package com.liveramp.workflow_ui.scripts;

import java.io.IOException;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.liveramp.commons.Accessors;
import com.liveramp.databases.workflow_db.DatabasesImpl;
import com.liveramp.databases.workflow_db.IWorkflowDb;
import com.liveramp.databases.workflow_db.models.Application;
import com.liveramp.databases.workflow_db.models.ApplicationCounterSummary;
import com.liveramp.databases.workflow_db.models.WorkflowExecution;
import com.liveramp.workflow.types.WorkflowExecutionStatus;
import com.rapleaf.jack.queries.Record;
import com.rapleaf.jack.queries.Records;

import static com.rapleaf.jack.queries.AggregatedColumn.COUNT;

public class SummarizerPrinter {

  public static void main(String[] args) throws IOException {

    //  where application is
    String appName = args[0];

    Integer dayWindow = Integer.parseInt(args[2]);

    String counterGroup = args[3];

    String counterName = args[4];

    //  for each day in last 30 days
    IWorkflowDb workflowDB = new DatabasesImpl().getWorkflowDb();

    Application app = Accessors.only(workflowDB.applications().findByName(appName));

    LocalDate dayEnd = new LocalDate().minusDays(1);

    DateTimeFormatter dtfOut = DateTimeFormat.forPattern("MM/dd");

    Records records = workflowDB.createQuery().from(ApplicationCounterSummary.TBL)
        .where(ApplicationCounterSummary.APPLICATION_ID.equalTo((int)app.getId()))
        .where(ApplicationCounterSummary.GROUP.equalTo(counterGroup))
        .where(ApplicationCounterSummary.NAME.equalTo(counterName))
        .where(ApplicationCounterSummary.DATE.greaterThan(dayEnd.minusDays(dayWindow).toDate().getTime()))
        .select(ApplicationCounterSummary.TBL.getAllColumns())
        .fetch();

    for (Record record : records) {
      Long date = record.get(ApplicationCounterSummary.DATE);
      LocalDate day = new LocalDate(date);

      long start = day.toDateTimeAtStartOfDay().toDate().getTime();
      long end = day.plusDays(1).toDateTimeAtStartOfDay().toDate().getTime();

      Records result = workflowDB.createQuery().from(WorkflowExecution.TBL)
          .where(WorkflowExecution.NAME.equalTo(appName))
          .where(WorkflowExecution.END_TIME.between(start, end))
          .where(WorkflowExecution.STATUS.equalTo(WorkflowExecutionStatus.COMPLETE.ordinal()))
          .select(COUNT(WorkflowExecution.ID))
          .fetch();

      Integer numExecutions = Accessors.only(result).get(COUNT(WorkflowExecution.ID));
      Long counterSum = record.get(ApplicationCounterSummary.VALUE);

      if(numExecutions != 0) {
        Long averageSum = counterSum / numExecutions;
        System.out.println(dtfOut.print(date) + "\t" + averageSum + "\t" + numExecutions);
      }
    }

  }
}
