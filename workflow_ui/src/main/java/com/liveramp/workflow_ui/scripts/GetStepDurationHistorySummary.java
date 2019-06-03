package com.liveramp.workflow_ui.scripts;

import java.io.IOException;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.liveramp.databases.workflow_db.DatabasesImpl;
import com.liveramp.databases.workflow_db.IWorkflowDb;
import com.liveramp.databases.workflow_db.models.StepAttempt;
import com.liveramp.databases.workflow_db.models.WorkflowAttempt;
import com.liveramp.databases.workflow_db.models.WorkflowExecution;
import com.liveramp.workflow.types.StepStatus;
import com.rapleaf.jack.queries.GenericQuery;
import com.rapleaf.jack.queries.Record;

import static com.rapleaf.jack.queries.AggregatedColumn.COUNT;
import static com.rapleaf.jack.queries.AggregatedColumn.SUM;

public class GetStepDurationHistorySummary {

  public static void main(String[] args) throws IOException {

    //  where application is
    String appName = args[0];

    String stepName = args[1];

    Integer dayWindow = Integer.parseInt(args[2]);

    //  for each day in last 30 days

    IWorkflowDb workflowDB = new DatabasesImpl().getWorkflowDb();

    LocalDate dayEnd = new LocalDate().minusDays(1);

    DateTimeFormatter dtfOut = DateTimeFormat.forPattern("MM/dd");

    System.out.println("DATE\tAVG_STEP_TIME");
    for (int i = 0; i < dayWindow; i++) {
      LocalDate date = dayEnd.minusDays(i);

      long start = date.toDateTimeAtStartOfDay().toDate().getTime();
      long end = date.plusDays(1).toDateTimeAtStartOfDay().toDate().getTime();

      GenericQuery select = workflowDB.createQuery()
          .from(WorkflowExecution.TBL)
          .where(WorkflowExecution.NAME.equalTo(appName))
          .where(WorkflowExecution.END_TIME.between(start, end))
          .innerJoin(WorkflowAttempt.TBL)
          .on(WorkflowAttempt.WORKFLOW_EXECUTION_ID.equalTo(WorkflowExecution.ID.as(Integer.class)))
          .innerJoin(StepAttempt.TBL)
          .on(StepAttempt.WORKFLOW_ATTEMPT_ID.equalTo(WorkflowAttempt.ID.as(Integer.class)))
          .where(StepAttempt.STEP_TOKEN.equalTo(stepName))
          .where(StepAttempt.STEP_STATUS.equalTo(StepStatus.COMPLETED.ordinal()))
          .select(COUNT(StepAttempt.ID), SUM(StepAttempt.END_TIME.as(Long.class)), SUM(StepAttempt.START_TIME.as(Long.class)));

      for (Record record : select.fetch()) {

        Integer steps = record.get(COUNT(StepAttempt.ID));
        Long sumEnd = record.get(SUM(StepAttempt.END_TIME));
        Long sumStart = record.get(SUM(StepAttempt.START_TIME));

        long avgRuntime = (sumEnd - sumStart) / steps;

        System.out.println(dtfOut.print(date)+"\t"+avgRuntime);

      }

    }


  }
}
