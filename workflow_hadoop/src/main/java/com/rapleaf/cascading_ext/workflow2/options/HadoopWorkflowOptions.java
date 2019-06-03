package com.rapleaf.cascading_ext.workflow2.options;

import java.util.Collections;
import java.util.Map;

import com.google.common.collect.Maps;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.MRJobConfig;

import com.liveramp.cascading_ext.CascadingUtil;
import com.liveramp.cascading_ext.megadesk.MockStoreReaderLockProvider;
import com.liveramp.cascading_ext.megadesk.StoreReaderLockProvider;
import com.liveramp.commons.collections.properties.NestedProperties;
import com.liveramp.commons.collections.properties.OverridableProperties;
import com.liveramp.workflow.backpressure.FlowSubmissionController;
import com.liveramp.workflow2.workflow_hadoop.TmpDirFilter;
import com.liveramp.workflow_core.BaseWorkflowOptions;
import com.liveramp.workflow_core.CoreOptions;
import com.rapleaf.cascading_ext.workflow2.RuntimePropertiesBuilder;

public class HadoopWorkflowOptions extends BaseWorkflowOptions<HadoopWorkflowOptions> {

  private StoreReaderLockProvider lockProvider;
  private FlowSubmissionController flowSubmissionController;
  private CascadingUtil cascadingUtil;
  private TmpDirFilter tmpDirFilter;
  private RuntimePropertiesBuilder runtimePropertiesBuilder;
  private boolean failOnCounterFetch = true;

  protected HadoopWorkflowOptions(OverridableProperties defaultProperties,
                                  Map<Object, Object> systemProperties,
                                  CascadingUtil cascadingUtil) {
    super(defaultProperties, systemProperties);

    this.tmpDirFilter = new TmpDirFilter.Never();
    this.flowSubmissionController = new FlowSubmissionController.SubmitImmediately();
    this.runtimePropertiesBuilder = new RuntimePropertiesBuilder.None();
    this.cascadingUtil = cascadingUtil;
  }

  public static Map<Object, Object> toProperties(JobConf conf) {
    Map<Object, Object> props = Maps.newHashMap();
    for (Map.Entry<String, String> entry : conf) {
      props.put(entry.getKey(), entry.getValue());
    }
    return props;
  }

  public CascadingUtil getCascadingUtil() {
    return cascadingUtil;
  }

  public TmpDirFilter getTmpDirFilter() {
    return tmpDirFilter;
  }

  public HadoopWorkflowOptions setTmpDirFilter(TmpDirFilter filter){
    this.tmpDirFilter = filter;
    return this;
  }

  public StoreReaderLockProvider getLockProvider() {
    return lockProvider;
  }

  public HadoopWorkflowOptions setLockProvider(StoreReaderLockProvider lockProvider) {
    this.lockProvider = lockProvider;
    return this;
  }

  public HadoopWorkflowOptions setRuntimePropertiesBuilder(RuntimePropertiesBuilder builder){
    this.runtimePropertiesBuilder = builder;
    return this;
  }

  public HadoopWorkflowOptions setFlowSubmissionController(FlowSubmissionController flowSubmissionController) {
    this.flowSubmissionController = flowSubmissionController;
    return this;
  }

  public RuntimePropertiesBuilder getRuntimePropertiesBuilder() {
    return runtimePropertiesBuilder;
  }

  public FlowSubmissionController getFlowSubmissionController() {
    return flowSubmissionController;
  }


  public HadoopWorkflowOptions setFailOnCounterFetch(boolean value){
    this.failOnCounterFetch = value;
    return this;
  }

  public boolean isFailOnCounterFetch() {
    return failOnCounterFetch;
  }

  protected static void configureTest(HadoopWorkflowOptions options) {

    CoreOptions.configureTest(options);

    options
        .setLockProvider(new MockStoreReaderLockProvider())
        .addWorkflowProperties(Collections.<Object, Object>singletonMap(MRJobConfig.QUEUE_NAME, "test"))
        .setFlowSubmissionController(new FlowSubmissionController.SubmitImmediately());

  }

  public static HadoopWorkflowOptions test() {
    HadoopWorkflowOptions opts = new HadoopWorkflowOptions(
        new NestedProperties(),
        Collections.emptyMap(),
        CascadingUtil.get());
    configureTest(opts);
    return opts;
  }


}
