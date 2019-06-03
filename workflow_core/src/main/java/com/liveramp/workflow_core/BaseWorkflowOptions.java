package com.liveramp.workflow_core;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import com.liveramp.cascading_ext.resource.ResourceDeclarer;
import com.liveramp.cascading_ext.resource.ResourceDeclarerFactory;
import com.liveramp.commons.collections.properties.NestedProperties;
import com.liveramp.commons.collections.properties.OverridableProperties;
import com.liveramp.java_support.alerts_handler.AlertsHandler;
import com.liveramp.java_support.alerts_handler.MailBuffer;
import com.liveramp.workflow_core.alerting.AlertsHandlerFactory;
import com.liveramp.workflow_core.alerting.BufferingAlertsHandlerFactory;
import com.liveramp.workflow_core.info.WorkflowInfoConsumer;
import com.liveramp.workflow_state.WorkflowRunnerNotification;
import com.rapleaf.cascading_ext.workflow2.TrackerURLBuilder;
import com.rapleaf.cascading_ext.workflow2.options.HostnameProvider;
import com.rapleaf.cascading_ext.workflow2.rollback.RollbackBehavior;
import com.rapleaf.cascading_ext.workflow2.rollback.SuccessCallback;

public class BaseWorkflowOptions<T extends BaseWorkflowOptions<T>> {

  private final OverridableProperties defaultProperties;
  private final Map<Object, Object> systemProperties; //  not for putting in conf, but for visibility into config

  private int maxConcurrentSteps;
  private AlertsHandler alertsHandler;
  private Set<WorkflowRunnerNotification> enabledNotifications;
  private Set<WorkflowRunnerNotification> disabledNotifications = Sets.newHashSet();
  private String uniqueIdentifier;
  private String appName;
  private Integer appType;
  private Integer stepPollInterval;
  private String sandboxDir;
  private String description;
  private RollbackBehavior rollBackOnFailure = new RollbackBehavior.Unconditional(false);
  private AlertsHandlerFactory alertsHandlerFactory = new BufferingAlertsHandlerFactory();
  private Set<WorkflowTag> tags;

  private TrackerURLBuilder urlBuilder;

  private Class<? extends ResourceDeclarerFactory> declarerFactory;
  private ResourceDeclarer resourceDeclarer;
  private HostnameProvider hostnameProvider;

  private List<SuccessCallback> onSuccess = Lists.newArrayList();

  private OverridableProperties properties = new NestedProperties(Maps.newHashMap(), false);
  private List<WorkflowInfoConsumer> infoConsumerList = Lists.newArrayList();

  protected BaseWorkflowOptions(OverridableProperties defaultProperties) {
    this(defaultProperties, Maps.newHashMap());
  }

  protected BaseWorkflowOptions(OverridableProperties defaultProperties,
                                Map<Object, Object> systemProperties
  ) {
    this.defaultProperties = defaultProperties;
    this.systemProperties = systemProperties;
    this.tags = new HashSet<>();
  }

  public T addSuccessCallback(SuccessCallback callback) {
    onSuccess.add(callback);
    return (T)this;
  }

  public T clearSuccessCallbacks(){
    onSuccess.clear();
    return (T) this;
  }

  public List<SuccessCallback> getSuccessCallbacks() {
    return onSuccess;
  }

  public RollbackBehavior getRollBackBehavior() {
    return rollBackOnFailure;
  }

  public T setAlertsHandlerFactory(AlertsHandlerFactory alertsHandlerFactory) {
    this.alertsHandlerFactory = alertsHandlerFactory;
    return (T) this;
  }

  public AlertsHandlerFactory getAlertsHandlerFactory() {
    return alertsHandlerFactory;
  }

  public T setRollBackOnFailure(boolean rollBackOnFailure) {
    this.rollBackOnFailure = new RollbackBehavior.Unconditional(rollBackOnFailure);
    return (T)this;
  }

  public T setRollBackBehavior(RollbackBehavior behavior) {
    this.rollBackOnFailure = behavior;
    return (T)this;
  }

  public HostnameProvider getHostnameProvider() {
    return hostnameProvider;
  }

  public T setHostnameProvider(HostnameProvider hostnameProvider) {
    this.hostnameProvider = hostnameProvider;
    return (T)this;
  }

  public T setUrlBuilder(TrackerURLBuilder urlBuilder) {
    this.urlBuilder = urlBuilder;
    return (T)this;
  }

  public TrackerURLBuilder getUrlBuilder() {
    return urlBuilder;
  }


  public String getDescription() {
    return description;
  }

  public T setDescription(String description) {
    this.description = description;
    return (T)this;
  }

  public int getMaxConcurrentSteps() {
    return maxConcurrentSteps;
  }

  public T setMaxConcurrentSteps(int maxConcurrentSteps) {
    this.maxConcurrentSteps = maxConcurrentSteps;
    return (T)this;
  }

  public T setStepPollInterval(int ms) {
    this.stepPollInterval = ms;
    return (T)this;
  }

  public Integer getStepPollInterval() {
    return stepPollInterval;
  }

  public AlertsHandler getAlertsHandler() {
    return alertsHandler;
  }

  public T setAlertsHandler(AlertsHandler alertsHandler) {
    this.alertsHandler = alertsHandler;
    return (T)this;
  }

  public T setEnabledNotifications(WorkflowRunnerNotification enabledNotification,
                                   WorkflowRunnerNotification... enabledNotifications) {
    this.enabledNotifications = EnumSet.of(enabledNotification, enabledNotifications);
    return (T)this;
  }

  public T setEnabledNotifications(Set<WorkflowRunnerNotification> enabledNotifications) {
    this.enabledNotifications = EnumSet.copyOf(enabledNotifications);
    return (T)this;
  }

  public T setDisabledNotifications(WorkflowRunnerNotification disabledNotification,
                                    WorkflowRunnerNotification... disabledNotifications) {
    this.disabledNotifications = EnumSet.of(disabledNotification, disabledNotifications);
    return (T)this;
  }

  public T setNotificationLevel(Set<WorkflowRunnerNotification> notifications) {
    enabledNotifications = EnumSet.copyOf(notifications);
    return (T)this;
  }

  public Set<WorkflowRunnerNotification> getEnabledNotifications() {
    return Sets.difference(enabledNotifications, disabledNotifications);
  }

  public String getScopeIdentifier() {
    return uniqueIdentifier;
  }

  public T setUniqueIdentifier(String uniqueIdentifier) {
    if (uniqueIdentifier != null && uniqueIdentifier.equals("__NULL")) {
      throw new IllegalArgumentException("This is temporarily a reserved scope, while making scope not null");
    }
    this.uniqueIdentifier = uniqueIdentifier;
    return (T)this;
  }

  public String getAppName(){
    return appName;
  }

  public T setAppType(Integer appType){
    this.appType = appType;
    return (T) this;
  }

  public Integer getAppType() {
    return appType;
  }

  public T setAppName(String appName){

    //  prevent confusion with conflicting apptype/appnames
    if(this.appName != null){
      throw new IllegalStateException("Cannot change app name from "+this.appName+" to "+appName+"!");
    }

    this.appName = appName;
    return (T) this;
  }

  public String getSandboxDir() {
    return sandboxDir;
  }

  public T setSandboxDir(String sandboxDir) {
    this.sandboxDir = sandboxDir;
    return (T)this;
  }

  public T setResourceManagerFactory(Class<? extends ResourceDeclarerFactory> factoryClass)  {
    this.declarerFactory = factoryClass;
    //  this isn't ideal (would rather do it during initialization)
    //  but a few workflows set the declarer in the options and then immediately retrieve
    //  it and who am I to judge them.
    try {
      ResourceDeclarerFactory factory = factoryClass.newInstance();
      this.resourceDeclarer = factory.create();
    } catch(Exception e){
      throw new RuntimeException(e);
    }

    return (T) this;
  }

  public Class<? extends ResourceDeclarerFactory> getResourceManagerFactory(){
    return this.declarerFactory;
  }

  //  if you use a background workflow, you must use the factory version of this method above
  public T setResourceManager(ResourceDeclarer resourceManager) {
    this.resourceDeclarer = resourceManager;
    return (T)this;
  }

  public ResourceDeclarer getResourceManager() {
    return this.resourceDeclarer;
  }


  public T addWorkflowProperties(Map<Object, Object> propertiesMap) {
    return addWorkflowHadoopProperties(new NestedProperties(propertiesMap, false));
  }

  public T addWorkflowHadoopProperties(OverridableProperties newProperties) {
    this.properties = newProperties.override(this.properties);
    return (T)this;
  }

  public T addTag(WorkflowTag tag) {
    tags.add(tag);
    return (T)this;
  }

  public Set<WorkflowTag> getTags() {
    return tags;
  }

  public OverridableProperties getWorkflowJobProperties() {
    return properties.override(defaultProperties);
  }


  public Object getConfiguredProperty(String property) {

    //  first look at configured properties
    Map<Object, OverridableProperties.Property> map = properties.getMap();
    if (map.containsKey(property)) {
      return map.get(property).value;
    }

    //  then stuff in the jobconf
    Object value = systemProperties.get(property);
    if (value != null) {
      return value;
    }

    return null;
  }

  public T addWorkflowInfoConsumer(WorkflowInfoConsumer consumer) {
    this.infoConsumerList.add(consumer);
    return (T)this;
  }

  public List<WorkflowInfoConsumer> getWorkflowInfoConsumers() {
    return this.infoConsumerList;
  }


}
