package com.liveramp.workflow_db_state;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang.SerializationUtils;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liveramp.cascading_ext.resource.ResourceDeclarerFactory;
import com.liveramp.databases.workflow_db.DatabasesImpl;
import com.liveramp.databases.workflow_db.IWorkflowDb;
import com.liveramp.databases.workflow_db.models.Application;
import com.liveramp.databases.workflow_db.models.ApplicationConfiguredNotification;
import com.liveramp.databases.workflow_db.models.BackgroundAttemptInfo;
import com.liveramp.databases.workflow_db.models.ConfiguredNotification;
import com.liveramp.databases.workflow_db.models.StepAttempt;
import com.liveramp.databases.workflow_db.models.WorkflowAttempt;
import com.liveramp.databases.workflow_db.models.WorkflowAttemptConfiguredNotification;
import com.liveramp.databases.workflow_db.models.WorkflowAttemptDatastore;
import com.liveramp.databases.workflow_db.models.WorkflowExecution;
import com.liveramp.java_support.alerts_handler.AlertsHandler;
import com.liveramp.java_support.alerts_handler.recipients.AlertRecipient;
import com.liveramp.java_support.alerts_handler.recipients.AlertSeverity;
import com.liveramp.java_support.alerts_handler.recipients.EngineeringAlertRecipient;
import com.liveramp.workflow.types.StepStatus;
import com.liveramp.workflow.types.WorkflowAttemptStatus;
import com.liveramp.workflow.types.WorkflowExecutionStatus;
import com.liveramp.workflow_core.BaseWorkflowOptions;
import com.liveramp.workflow_core.StepStateManager;
import com.liveramp.workflow_core.WorkflowTag;
import com.liveramp.workflow_state.DSAction;
import com.liveramp.workflow_state.DataStoreInfo;
import com.liveramp.workflow_state.IStep;
import com.liveramp.workflow_state.WorkflowRunnerNotification;
import com.rapleaf.cascading_ext.workflow2.state.InitializedWorkflow;
import com.rapleaf.cascading_ext.workflow2.state.WorkflowPersistenceFactory;

public abstract class CoreWorkflowDbPersistenceFactory<S extends IStep,
    OPTS extends BaseWorkflowOptions<OPTS>,
    WORKFLOW extends InitializedWorkflow<S, InitializedDbPersistence, OPTS>> extends WorkflowPersistenceFactory<S, InitializedDbPersistence, OPTS, WORKFLOW> {

  private static final Logger LOG = LoggerFactory.getLogger(DbPersistence.class);
  Supplier<IWorkflowDb> workflowDbSupplier;

  public CoreWorkflowDbPersistenceFactory(StepStateManager<S> manager) {
    this(manager, () -> new DatabasesImpl().getWorkflowDb());
  }

  public CoreWorkflowDbPersistenceFactory(StepStateManager<S> manager, Supplier<IWorkflowDb> supplier) {
    super(manager);
    this.workflowDbSupplier = supplier;
  }

  @Override
  public synchronized InitializedDbPersistence initializeInternal(String name,
                                                                  OPTS options,
                                                                  String host,
                                                                  String username,
                                                                  String pool,
                                                                  String priority,
                                                                  String launchDir,
                                                                  String launchJar,
                                                                  String remote,
                                                                  String implementationBuild
  ) throws IOException {
    IWorkflowDb workflowDb = workflowDbSupplier.get();
    workflowDb.disableCaching();

    Application application = getApplication(workflowDb, name, options.getAppType());
    LOG.info("Using application: " + application);

    WorkflowExecution.Attributes execution = getOrCreateExecution(workflowDb, application, name, options.getAppType(), options.getScopeIdentifier(), options.getTags());
    LOG.info("Using workflow execution: " + execution + " id " + execution.getId());

    cleanUpRunningAttempts(workflowDb, execution);

    WorkflowAttempt attempt = createAttempt(workflowDb,
        host,
        username,
        options.getDescription(),
        pool,
        priority,
        launchDir,
        launchJar,
        options.getAlertsHandler(),
        options.getEnabledNotifications(),
        execution,
        remote,
        implementationBuild,
        options.getResourceManagerFactory()
    );

    assertOnlyLiveAttempt(workflowDb, execution, attempt);

    long workflowAttemptId = attempt.getId();
    LOG.info("Using new attempt: " + attempt + " id " + workflowAttemptId);

    return new InitializedDbPersistence(attempt.getId(), workflowDb, getManager().isLive(), options.getAlertsHandler());
  }

  @Override
  public synchronized DbPersistence prepare(InitializedDbPersistence persistence, DirectedGraph<S, DefaultEdge> flatSteps) {

    IWorkflowDb workflowDb = persistence.getDb();

    try {
      workflowDb.setAutoCommit(false);

      long workflowAttemptId = persistence.getAttemptId();
      Long executionId = persistence.getExecutionId();
      Set<DataStoreInfo> allStores = Sets.newHashSet();

      for (S step : flatSteps.vertexSet()) {
        Collection<DataStoreInfo> values = step.getDataStores().values();
        allStores.addAll(values);
      }

      Map<DataStoreInfo, WorkflowAttemptDatastore> datastores = Maps.newHashMap();
      for (DataStoreInfo store : allStores) {
        datastores.put(store, workflowDb.workflowAttemptDatastores().create(
            (int)workflowAttemptId,
            store.getName(),
            store.getPath(),
            store.getClass().getName()
        ));
      }

      //  save datastores
      workflowDb.commit();

      Set<String> completeSteps = Sets.newHashSet();
      synchronized (persistence.getLock()) {
        completeSteps.addAll(WorkflowQueries.getCompleteSteps(workflowDb, executionId));
      }

      Map<String, StepAttempt> attempts = Maps.newHashMap();
      for (S step : flatSteps.vertexSet()) {
        StepAttempt stepAttempt = createStepAttempt(workflowDb, step, workflowAttemptId, completeSteps);
        attempts.put(stepAttempt.getStepToken(), stepAttempt);
      }

      //  save created steps
      workflowDb.commit();

      for (S step : flatSteps.vertexSet()) {
        StepAttempt stepAttempt = attempts.get(step.getCheckpointToken());

        @SuppressWarnings("unchecked") Collection<Map.Entry<DSAction, DataStoreInfo>> entries = step.getDataStores().entries();
        for (Map.Entry<DSAction, DataStoreInfo> entry : entries) {
          WorkflowAttemptDatastore workflowAttemptDatastore = datastores.get(entry.getValue());
          workflowDb.stepAttemptDatastores().create(
              stepAttempt.getId(),
              workflowAttemptDatastore.getId(),
              entry.getKey().ordinal()
          );
        }

      }

      //  save refs to steps
      workflowDb.commit();

      for (DefaultEdge edge : flatSteps.edgeSet()) {

        IStep dep = flatSteps.getEdgeTarget(edge);
        IStep step = flatSteps.getEdgeSource(edge);

        workflowDb.stepDependencies().create(
            attempts.get(step.getCheckpointToken()).getId(),
            attempts.get(dep.getCheckpointToken()).getId()
        );

      }

      //  save step deps
      workflowDb.commit();

      return new DbPersistence(persistence);

    } catch (Exception e) {
      workflowDb.rollback();
      throw new RuntimeException(e);
    } finally {
      workflowDb.setAutoCommit(true);
    }

  }

  private Application getApplication(IWorkflowDb workflowDb, String name, Integer appType) throws IOException {

    Optional<Application> application = WorkflowQueries.getApplication(workflowDb, name);

    if (application.isPresent()) {
      LOG.info("Using existing application");

      return application.get();
    } else {
      LOG.info("Creating new application for name: " + name + ", app type " + appType);

      Application app = workflowDb.applications().create(name);
      if (appType != null) {
        app.setAppType(appType);
      }
      workflowDb.applications().save(app);

      //  add DT to the performance notifications for all new applications

      ConfiguredNotification dtNotification = workflowDb.configuredNotifications()
          .create(WorkflowRunnerNotification.PERFORMANCE.ordinal())
          .setEmail("dt-workflow-alerts@liveramp.com");
      dtNotification.save();

      ApplicationConfiguredNotification appNotif = workflowDb.applicationConfiguredNotifications()
          .create(app.getId(), dtNotification.getId());
      appNotif.save();

      return app;
    }

  }

  private void cleanUpRunningAttempts(IWorkflowDb workflowDb, WorkflowExecution.Attributes execution) throws IOException {

    List<WorkflowAttempt> prevAttempts = WorkflowQueries.getLiveWorkflowAttempts(workflowDb, execution.getId());
    LOG.info("Found previous attempts: " + prevAttempts);

    for (WorkflowAttempt attempt : prevAttempts) {

      //  check to see if any of these workflows are still marked as alive
      Assertions.assertDead(attempt);

      //  otherwise it is safe to clean up
      LOG.info("Marking old running attempt as FAILED: " + attempt);
      attempt
          .setStatus(WorkflowAttemptStatus.FAILED.ordinal())
          .setEndTime(System.currentTimeMillis())
          .save();

      //  and mark any step that was still running as failed
      for (StepAttempt step : attempt.getStepAttempt()) {
        if (step.getStepStatus() == StepStatus.RUNNING.ordinal()) {
          LOG.info("Marking old runing step as FAILED: " + step);
          step
              .setStepStatus(StepStatus.FAILED.ordinal())
              .setFailureCause("Unknown, failed by cleanup.")
              .setEndTime(System.currentTimeMillis())
              .save();
        }
      }

    }

  }

  public void assertOnlyLiveAttempt(IWorkflowDb workflowDb, WorkflowExecution.Attributes execution, WorkflowAttempt attempt) throws IOException {
    List<WorkflowAttempt> liveAttempts = WorkflowQueries.getLiveWorkflowAttempts(workflowDb, execution.getId());
    if (liveAttempts.size() != 1) {
      attempt.setStatus(WorkflowAttemptStatus.FAILED.ordinal()).save();
      throw new RuntimeException("Multiple live attempts found for workflow execution! " + liveAttempts + " Not starting workflow.");
    }
  }

  private static final Set<WorkflowRunnerNotification> PROVIDED_HANDLER_NOTIFICATIONS = Sets.newHashSet(
      WorkflowRunnerNotification.START,
      WorkflowRunnerNotification.SUCCESS,
      WorkflowRunnerNotification.FAILURE,
      WorkflowRunnerNotification.STEP_FAILURE,
      WorkflowRunnerNotification.SHUTDOWN,
      WorkflowRunnerNotification.INTERNAL_ERROR
  );

  private String truncateDescription(String str) {
    if (str == null) {
      return null;
    }

    if (str.length() > 255) {
      return str.substring(0, 255);
    }

    return str;
  }

  private WorkflowAttempt createAttempt(IWorkflowDb workflowDb, String host, String username, String description, String pool, String priority, String launchDir, String launchJar, AlertsHandler providedHandler, Set<WorkflowRunnerNotification> configuredNotifications, WorkflowExecution.Attributes execution, String remote, String implementationBuild, Class<? extends ResourceDeclarerFactory> resourceFactory) throws IOException {

    Map<AlertSeverity, String> recipients = Maps.newHashMap();
    for (AlertSeverity severity : AlertSeverity.values()) {
      recipients.put(severity, getEmail(providedHandler, new EngineeringAlertRecipient(severity)));
    }

    StepStateManager<S> manager = getManager();
    WorkflowAttempt attempt = workflowDb.workflowAttempts().create((int)execution.getId(), username, priority, pool, host)
        .setStatus(WorkflowAttemptStatus.INITIALIZING.ordinal())
        .setDescription(truncateDescription(description))
        .setLaunchDir(launchDir)
        .setLaunchJar(launchJar)
        .setErrorEmail(recipients.get(AlertSeverity.ERROR))     //  TODO remove these on attempt once notifications redone
        .setInfoEmail(recipients.get(AlertSeverity.INFO))
        .setScmRemote(remote)
        .setCommitRevision(implementationBuild);

    if (manager.isLive()) {
      attempt.setLastHeartbeat(System.currentTimeMillis());
    }else{
      attempt.setLastHeartbeat(0L);
    }

    attempt.save();

    if(!manager.isLive()) {
      BackgroundAttemptInfo backgroundInfo = workflowDb.backgroundAttemptInfos().create(attempt.getId());

      if(resourceFactory != null){
        backgroundInfo.setResourceManagerFactory(resourceFactory.getName());
        //  this is dumb, but I don't want to clean up Resource right now to fix it.
        backgroundInfo.setResourceManagerVersionClass(InitializedDbPersistence.class.getName());
        backgroundInfo.save();
      }

    }


    for (WorkflowRunnerNotification notification : configuredNotifications) {

      ConfiguredNotification configured = buildConfiguredNotification(workflowDb, notification, recipients.get(notification.serverity()));
      if (configured != null) {
        configured.save();

        WorkflowAttemptConfiguredNotification attemptConfigured = workflowDb.workflowAttemptConfiguredNotifications().create(attempt.getId(), configured.getId());
        attemptConfigured.save();

      }
    }

    return attempt;
  }

  private ConfiguredNotification buildConfiguredNotification(IWorkflowDb workflowDb, WorkflowRunnerNotification notification, String emailForSeverity) throws IOException {
    if (PROVIDED_HANDLER_NOTIFICATIONS.contains(notification)) {
      return workflowDb.configuredNotifications().create(notification.ordinal()).setProvidedAlertsHandler(true);
    } else {
      if (emailForSeverity != null) {
        return workflowDb.configuredNotifications().create(notification.ordinal()).setEmail(emailForSeverity);
      }
    }

    return null;
  }


  private static String getEmail(AlertsHandler handler, AlertRecipient recipient) {
    List<String> emails = handler.resolveRecipients(Lists.newArrayList(recipient)).getEmailRecipients();
    if (emails.isEmpty()) {
      return null;
    }
    return emails.get(0);
  }


  private StepAttempt createStepAttempt(IWorkflowDb workflowDb, S step, Long attemptId, Set<String> completeSteps) throws IOException {

    String token = step.getCheckpointToken();

    StepAttempt model = workflowDb.stepAttempts().create(attemptId.intValue(), token, null, null,
        getInitialStatus(token, completeSteps).ordinal(),
        null,
        null,
        step.getActionClass(),
        ""
    );

    StepStateManager<S> manager = getManager();
    Serializable context = manager.getStepContext(step);
    if (context != null) {
      workflowDb.backgroundStepAttemptInfos().create(
          model.getId(),
          SerializationUtils.serialize(context),
          System.currentTimeMillis(),
          manager.getPrereqCheckCooldown(step)
      );
    }

    return model;

  }

  private StepStatus getInitialStatus(String stepId, Set<String> completeTokens) throws IOException {

    if (completeTokens.contains(stepId)) {
      return StepStatus.SKIPPED;
    }

    return StepStatus.WAITING;
  }

  private WorkflowExecution.Attributes getOrCreateExecution(IWorkflowDb workflowDb, Application app, String name, Integer appType, String scopeId, Set<WorkflowTag> tags) throws IOException {

    Set<WorkflowExecution.Attributes> incompleteExecutions = WorkflowQueries.getIncompleteExecutions(workflowDb, name, scopeId);

    if (incompleteExecutions.isEmpty()) {
      LOG.info("No incomplete execution found, creating new execution");

      //  new execution
      WorkflowExecution ex = workflowDb.workflowExecutions().create(name, WorkflowExecutionStatus.INCOMPLETE.ordinal())
          .setScopeIdentifier(scopeId)
          .setStartTime(System.currentTimeMillis())
          .setApplicationId(app.getIntId());

      if (appType != null) {
        ex.setAppType(appType);
      }

      for (WorkflowTag tag : tags) {
        workflowDb.executionTags().create(ex.getId(), tag.getKey(), tag.getVal());
      }

      ex.save();

      return ex.getAttributes();

    } else {
      //  only one, return it
      WorkflowExecution.Attributes prevExecution = incompleteExecutions.iterator().next();
      LOG.info("Found previous execution " + prevExecution + " id " + prevExecution.getId());

      return prevExecution;

    }

  }

}
