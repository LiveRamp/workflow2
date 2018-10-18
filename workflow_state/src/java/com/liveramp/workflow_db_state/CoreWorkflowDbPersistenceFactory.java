package com.liveramp.workflow_db_state;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Optional;
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
import com.liveramp.databases.workflow_db.IDatabases;
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

  public CoreWorkflowDbPersistenceFactory(StepStateManager<S> manager) {
    super(manager);
  }


  @Override
  public synchronized InitializedDbPersistence initializeInternal(String name,
                                                                  String scopeId,
                                                                  String description,
                                                                  Integer appType,
                                                                  String host,
                                                                  String username,
                                                                  String pool,
                                                                  String priority,
                                                                  String launchDir,
                                                                  String launchJar,
                                                                  Set<WorkflowRunnerNotification> configuredNotifications,
                                                                  AlertsHandler providedHandler,
                                                                  Class<? extends ResourceDeclarerFactory> resourceFactory,
                                                                  String remote,
                                                                  String implementationBuild) throws IOException {


    DatabasesImpl databases = new DatabasesImpl();
    IWorkflowDb workflowDb = databases.getWorkflowDb();
    workflowDb.disableCaching();

    Application application = getApplication(workflowDb, name, appType);
    LOG.info("Using application: " + application);

    WorkflowExecution.Attributes execution = getExecution(workflowDb, application, name, appType, scopeId);
    LOG.info("Using workflow execution: " + execution + " id " + execution.getId());

    cleanUpRunningAttempts(databases, execution);

    WorkflowAttempt attempt = createAttempt(databases,
        host,
        username,
        description,
        pool,
        priority,
        launchDir,
        launchJar,
        providedHandler,
        configuredNotifications,
        execution,
        remote,
        implementationBuild,
        resourceFactory
    );

    assertOnlyLiveAttempt(workflowDb, execution, attempt);

    long workflowAttemptId = attempt.getId();
    LOG.info("Using new attempt: " + attempt + " id " + workflowAttemptId);

    return new InitializedDbPersistence(attempt.getId(), workflowDb, getManager().isLive(), providedHandler);
  }

  @Override
  public synchronized DbPersistence prepare(InitializedDbPersistence persistence, DirectedGraph<S, DefaultEdge> flatSteps) {

    IWorkflowDb rldb = persistence.getDb();

    try {
      rldb.setAutoCommit(false);

      long workflowAttemptId = persistence.getAttemptId();
      Long executionId = persistence.getExecutionId();
      Set<DataStoreInfo> allStores = Sets.newHashSet();

      for (S step : flatSteps.vertexSet()) {
        Collection<DataStoreInfo> values = step.getDataStores().values();
        allStores.addAll(values);
      }

      Map<DataStoreInfo, WorkflowAttemptDatastore> datastores = Maps.newHashMap();
      for (DataStoreInfo store : allStores) {
        datastores.put(store, rldb.workflowAttemptDatastores().create(
            (int)workflowAttemptId,
            store.getName(),
            store.getPath(),
            store.getClass().getName()
        ));
      }

      //  save datastores
      rldb.commit();

      Set<String> completeSteps = Sets.newHashSet();
      synchronized (persistence.getLock()) {
        completeSteps.addAll(WorkflowQueries.getCompleteSteps(rldb, executionId));
      }

      Map<String, StepAttempt> attempts = Maps.newHashMap();
      for (S step : flatSteps.vertexSet()) {
        StepAttempt stepAttempt = createStepAttempt(rldb, step, workflowAttemptId, completeSteps);
        attempts.put(stepAttempt.getStepToken(), stepAttempt);
      }

      //  save created steps
      rldb.commit();

      for (S step : flatSteps.vertexSet()) {
        StepAttempt stepAttempt = attempts.get(step.getCheckpointToken());

        @SuppressWarnings("unchecked") Collection<Map.Entry<DSAction, DataStoreInfo>> entries = step.getDataStores().entries();
        for (Map.Entry<DSAction, DataStoreInfo> entry : entries) {
          rldb.stepAttemptDatastores().create(
              stepAttempt.getId(),
              datastores.get(entry.getValue()).getIntId(),
              entry.getKey().ordinal()
          );
        }

      }

      //  save refs to steps
      rldb.commit();

      for (DefaultEdge edge : flatSteps.edgeSet()) {

        IStep dep = flatSteps.getEdgeTarget(edge);
        IStep step = flatSteps.getEdgeSource(edge);

        rldb.stepDependencies().create(
            attempts.get(step.getCheckpointToken()).getId(),
            attempts.get(dep.getCheckpointToken()).getId()
        );

      }

      //  save step deps
      rldb.commit();

      return new DbPersistence(persistence);

    } catch (Exception e) {
      rldb.rollback();
      throw new RuntimeException(e);
    } finally {
      rldb.setAutoCommit(true);
    }

  }

  private Application getApplication(IWorkflowDb rldb, String name, Integer appType) throws IOException {

    Optional<Application> application = WorkflowQueries.getApplication(rldb, name);

    if (application.isPresent()) {
      LOG.info("Using existing application");

      return application.get();
    } else {
      LOG.info("Creating new application for name: " + name + ", app type " + appType);

      Application app = rldb.applications().create(name);
      if (appType != null) {
        app.setAppType(appType);
      }
      rldb.applications().save(app);

      //  add DT to the performance notifications for all new applications

      ConfiguredNotification dtNotification = rldb.configuredNotifications()
          .create(WorkflowRunnerNotification.PERFORMANCE.ordinal())
          .setEmail("dt-workflow-alerts@liveramp.com");
      dtNotification.save();

      ApplicationConfiguredNotification appNotif = rldb.applicationConfiguredNotifications()
          .create(app.getId(), dtNotification.getId());
      appNotif.save();

      return app;
    }

  }

  private void cleanUpRunningAttempts(IDatabases databases, WorkflowExecution.Attributes execution) throws IOException {
    IWorkflowDb rldb = databases.getWorkflowDb();

    List<WorkflowAttempt> prevAttempts = WorkflowQueries.getLiveWorkflowAttempts(rldb, execution.getId());
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

  public void assertOnlyLiveAttempt(IWorkflowDb rldb, WorkflowExecution.Attributes execution, WorkflowAttempt attempt) throws IOException {
    List<WorkflowAttempt> liveAttempts = WorkflowQueries.getLiveWorkflowAttempts(rldb, execution.getId());
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

  private WorkflowAttempt createAttempt(IDatabases databases, String host, String username, String description, String pool, String priority, String launchDir, String launchJar, AlertsHandler providedHandler, Set<WorkflowRunnerNotification> configuredNotifications, WorkflowExecution.Attributes execution, String remote, String implementationBuild, Class<? extends ResourceDeclarerFactory> resourceFactory) throws IOException {
    IWorkflowDb rldb = databases.getWorkflowDb();

    Map<AlertSeverity, String> recipients = Maps.newHashMap();
    for (AlertSeverity severity : AlertSeverity.values()) {
      recipients.put(severity, getEmail(providedHandler, new EngineeringAlertRecipient(severity)));
    }
    System.out.println("RESOLVED: "+recipients);

    StepStateManager<S> manager = getManager();
    WorkflowAttempt attempt = rldb.workflowAttempts().create((int)execution.getId(), username, priority, pool, host)
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
      BackgroundAttemptInfo backgroundInfo = rldb.backgroundAttemptInfos().create(attempt.getId());

      if(resourceFactory != null){
        backgroundInfo.setResourceManagerFactory(resourceFactory.getName());
        //  this is dumb, but I don't want to clean up Resource right now to fix it.
        backgroundInfo.setResourceManagerVersionClass(InitializedDbPersistence.class.getName());
        backgroundInfo.save();
      }

    }


    for (WorkflowRunnerNotification notification : configuredNotifications) {

      ConfiguredNotification configured = buildConfiguredNotification(rldb, notification, recipients.get(notification.serverity()));
      if (configured != null) {
        configured.save();

        WorkflowAttemptConfiguredNotification attemptConfigured = rldb.workflowAttemptConfiguredNotifications().create(attempt.getId(), configured.getId());
        attemptConfigured.save();

      }
    }

    return attempt;
  }

  private ConfiguredNotification buildConfiguredNotification(IWorkflowDb rldb, WorkflowRunnerNotification notification, String emailForSeverity) throws IOException {
    if (PROVIDED_HANDLER_NOTIFICATIONS.contains(notification)) {
      return rldb.configuredNotifications().create(notification.ordinal()).setProvidedAlertsHandler(true);
    } else {
      if (emailForSeverity != null) {
        return rldb.configuredNotifications().create(notification.ordinal()).setEmail(emailForSeverity);
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


  private StepAttempt createStepAttempt(IWorkflowDb rldb, S step, Long attemptId, Set<String> completeSteps) throws IOException {

    String token = step.getCheckpointToken();

    StepAttempt model = rldb.stepAttempts().create(attemptId.intValue(), token, null, null,
        getInitialStatus(token, completeSteps).ordinal(),
        null,
        null,
        step.getActionClass(),
        ""
    );

    StepStateManager<S> manager = getManager();
    Serializable context = manager.getStepContext(step);
    if (context != null) {
      rldb.backgroundStepAttemptInfos().create(
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

  private WorkflowExecution.Attributes getExecution(IWorkflowDb rldb, Application app, String name, Integer appType, String scopeId) throws IOException {

    Set<WorkflowExecution.Attributes> incompleteExecutions = WorkflowQueries.getIncompleteExecutions(rldb, name, scopeId);

    if (incompleteExecutions.isEmpty()) {
      LOG.info("No incomplete execution found, creating new execution");

      //  new execution
      WorkflowExecution ex = rldb.workflowExecutions().create(name, WorkflowExecutionStatus.INCOMPLETE.ordinal())
          .setScopeIdentifier(scopeId)
          .setStartTime(System.currentTimeMillis())
          .setApplicationId(app.getIntId());

      if (appType != null) {
        ex.setAppType(appType);
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
