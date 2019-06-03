package com.liveramp.workflow_ui.scripts;

import java.io.IOException;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liveramp.databases.workflow_db.DatabasesImpl;
import com.liveramp.databases.workflow_db.IWorkflowDb;
import com.liveramp.databases.workflow_db.models.MapreduceJob;
import com.liveramp.databases.workflow_db.models.StepAttempt;
import com.liveramp.databases.workflow_db.models.WorkflowAttempt;
import com.liveramp.databases.workflow_db.models.WorkflowExecution;

import com.rapleaf.jack.queries.GenericQuery;
import com.rapleaf.jack.queries.Record;

import static com.rapleaf.jack.queries.AggregatedColumn.COUNT;
import static com.rapleaf.jack.queries.AggregatedColumn.SUM;

public class GetStepRuntimeHistorySummary {
  private static final Logger LOG = LoggerFactory.getLogger(GetStepRuntimeHistorySummary.class);

  public static void main(String[] args) throws IOException {

    //  where application is
    String appName = args[0];

    String stepName = args[1];

    Integer dayWindow = Integer.parseInt(args[2]);

    //  for each day in last 30 days

    IWorkflowDb workflowDB = new DatabasesImpl().getWorkflowDb();

    LocalDate dayEnd = new LocalDate().minusDays(1);

    DateTimeFormatter dtfOut = DateTimeFormat.forPattern("MM/dd");

    System.out.println();
    System.out.println("DATE\tJOBS_RUN\tAVG_MAP_DURATION\tAVG_REDUCE_DURATION");
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
          .innerJoin(MapreduceJob.TBL)
          .on(MapreduceJob.STEP_ATTEMPT_ID.equalTo(StepAttempt.ID))
          .groupBy(WorkflowExecution.APPLICATION_ID)
          .select(COUNT(MapreduceJob.ID),
              SUM(MapreduceJob.AVG_MAP_DURATION),
              SUM(MapreduceJob.AVG_REDUCE_DURATION));

      for (Record record : select.fetch()) {

        Integer jobsRun = record.get(COUNT(MapreduceJob.ID));
        Long avgMapDurationSum = record.get(SUM(MapreduceJob.AVG_MAP_DURATION));
        Long avgReduceDurationSum = record.get(SUM(MapreduceJob.AVG_REDUCE_DURATION));

        System.out.println(dtfOut.print(date) + "\t" + jobsRun + "\t" + (avgMapDurationSum / jobsRun) + "\t" + (avgReduceDurationSum / jobsRun));

      }

    }

    System.out.println();

  }

}
