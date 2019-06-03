package com.liveramp.workflow_monitor.alerts.execution.scripts;

import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liveramp.commons.collections.CountingMap;
import com.liveramp.commons.collections.map.MultimapBuilder;
import com.liveramp.databases.workflow_db.DatabasesImpl;
import com.liveramp.databases.workflow_db.models.MapreduceCounter;
import com.liveramp.databases.workflow_db.models.MapreduceJob;
import com.liveramp.databases.workflow_db.models.StepAttempt;
import com.liveramp.databases.workflow_db.models.WorkflowExecution;
import com.liveramp.workflow.types.StepStatus;
import com.liveramp.workflow_db_state.WorkflowQueries;
import com.liveramp.workflow_db_state.jack.JackUtil;

import static com.liveramp.workflow_monitor.alerts.execution.MapreduceJobAlertGenerator.JOB_COUNTER_GROUP;
import static com.liveramp.workflow_monitor.alerts.execution.MapreduceJobAlertGenerator.LAUNCHED_MAPS;
import static com.liveramp.workflow_monitor.alerts.execution.MapreduceJobAlertGenerator.LAUNCHED_REDUCES;

public class AppsByShortTasks {
  private static final Logger LOG = LoggerFactory.getLogger(AppsByShortTasks.class);

  private static final Multimap<String, String> COUNTERS = MultimapBuilder
      .of(JOB_COUNTER_GROUP, LAUNCHED_MAPS)
      .put(JOB_COUNTER_GROUP, LAUNCHED_MAPS)
      .get();

  private static final Long MAP_MS_CUTOFF = 20000l;
  private static final Long REDUCE_MS_CUTOFF = 20000l;

  @SuppressWarnings("Duplicates")
  public static void main(String[] args) throws IOException {

    //  finished in last hour
    long endTime = System.currentTimeMillis();
    long jobWindow = endTime - Duration.ofHours(12).toMillis();

    DatabasesImpl db = new DatabasesImpl();

    Map<Long, MapreduceJob> jobs = JackUtil.byId(WorkflowQueries.getCompleteMapreduceJobs(db,
        jobWindow,
        endTime
    ));
    LOG.info("Found  " + jobs.size() + " complete jobs");

    Set<Long> stepAttemptIds = stepAttemptIds(jobs.values());

    Multimap<Integer, MapreduceCounter> countersByJob = JackUtil.by(WorkflowQueries.getAllJobCounters(db,
        jobWindow,
        endTime,
        COUNTERS.keySet(),
        Sets.newHashSet(COUNTERS.values())),
        MapreduceCounter._Fields.mapreduce_job_id
    );

    Map<Long, Long> stepAttemptToExecution = WorkflowQueries.getStepAttemptIdtoWorkflowExecutionId(db, stepAttemptIds);
    Map<Long, StepAttempt> stepsById = JackUtil.byId(db.getWorkflowDb().stepAttempts().query().idIn(stepAttemptIds).find());

    Map<Long, WorkflowExecution> relevantExecutions = JackUtil.byId(WorkflowQueries.getExecutionsForStepAttempts(db, stepAttemptIds));


    CountingMap<String> shortTasksByApp = new CountingMap<>();
    CountingMap<String> totalTasksByApp = new CountingMap<>();

    long globalTasks = 0;
    long globalShortTasks = 0;

    for (Long jobID : jobs.keySet()) {

      //  get avg map time and avg reduce time

      MapreduceJob job = jobs.get(jobID);
      Long mapDuration = job.getAvgMapDuration();
      Long reduceDuration = job.getAvgReduceDuration();

      Collection<MapreduceCounter> counters = countersByJob.get(jobID.intValue());

      MapreduceCounter mapsCount = get(counters, JOB_COUNTER_GROUP, LAUNCHED_MAPS);
      MapreduceCounter reducesCount = get(counters, JOB_COUNTER_GROUP, LAUNCHED_REDUCES);

      StepAttempt step = stepsById.get(job.getStepAttemptId().longValue());
      Long execution = stepAttemptToExecution.get(step.getId());

      String appName = relevantExecutions.get(execution).getName();

      if (step.getStepStatus() == StepStatus.COMPLETED.getValue()) {

        if (mapsCount != null) {
          long maps = mapsCount.getValue();
          globalTasks += maps;

          totalTasksByApp.increment(appName, maps);

          if (mapDuration != null) {
            if (mapDuration < MAP_MS_CUTOFF) {
              globalShortTasks += maps;

              shortTasksByApp.increment(appName, maps);
            }
          } else {
            LOG.warn("No known avg duration for job: " + job);
          }
        }

        if (reducesCount != null) {
          long reduces = reducesCount.getValue();
          globalTasks += reduces;

          totalTasksByApp.increment(appName, reduces);

          if (reduceDuration != null) {
            if (reduceDuration < REDUCE_MS_CUTOFF) {
              globalShortTasks += reduces;

              shortTasksByApp.increment(appName, reduces);
            }
          }
        }
      }

    }

    Map<String, Long> shortTasks = shortTasksByApp.get();
    Map<String, Long> totalTasks = totalTasksByApp.get();

    List<AppEntry> entries = Lists.newArrayList();
    for (Map.Entry<String, Long> entry : shortTasks.entrySet()) {
      Long totalCount = totalTasks.get(entry.getKey());
      Long shortCount = shortTasks.get(entry.getKey());
      entries.add(new AppEntry(
          entry.getKey(),
          totalCount,
          shortCount,
          shortCount.doubleValue() / totalCount.doubleValue(),
          totalCount.doubleValue() / globalTasks,
          shortCount.doubleValue() / globalShortTasks

      ));

    }

    entries.sort((o1, o2) -> new CompareToBuilder()
        .append(o1.globalShortTasks, o2.globalShortTasks)
        .append(o1.name, o2.name)
        .toComparison());

    System.out.println("name\ttotal tasks\tshort tasks\t%app short tasks\t% global tasks\t%global short tasks");
    for (AppEntry entry : entries) {
      System.out.println(entry);
    }

  }

  private static class AppEntry {

    private final String name;
    private final Long totalTasks;
    private final Long shortTasks;
    private final Double appShortTasks;
    private final Double globalTasks;
    private final Double globalShortTasks;

    private AppEntry(String name, Long totalTasks, Long shortTasks,
                     Double appShortTasks,
                     Double globalTasks,
                     Double globalShortTasks) {
      this.name = name;
      this.totalTasks = totalTasks;
      this.shortTasks = shortTasks;
      this.globalTasks = globalTasks;
      this.appShortTasks = appShortTasks;
      this.globalShortTasks = globalShortTasks;
    }


    public String toString() {
      return StringUtils.join(Lists.newArrayList(
          name,
          totalTasks,
          shortTasks,
          appShortTasks,
          globalTasks,
          globalShortTasks
      ), "\t");
    }
  }

  private static MapreduceCounter get(Collection<MapreduceCounter> counters, String group, String name) {
    for (MapreduceCounter counter : counters) {
      if (counter.getGroup().equals(group) && counter.getName().equals(name)) {
        return counter;
      }
    }
    return null;
  }

  private static Set<Long> stepAttemptIds(Collection<MapreduceJob> jobs) {
    Set<Long> stepAttemptIds = Sets.newHashSet();
    for (MapreduceJob job : jobs) {
      stepAttemptIds.add(Long.valueOf(job.getStepAttemptId()));
    }
    return stepAttemptIds;
  }


}
