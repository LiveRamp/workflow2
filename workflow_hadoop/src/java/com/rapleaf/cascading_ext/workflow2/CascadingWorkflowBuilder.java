package com.rapleaf.cascading_ext.workflow2;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cascading.flow.FlowListener;
import cascading.flow.planner.Scope;
import cascading.pipe.Pipe;
import cascading.tuple.Fields;

import com.liveramp.cascading_tools.EmptyListener;
import com.rapleaf.cascading_ext.HRap;
import com.rapleaf.cascading_ext.datastore.DataStore;
import com.rapleaf.cascading_ext.datastore.TupleDataStore;
import com.rapleaf.cascading_ext.datastore.internal.DataStoreBuilder;
import com.rapleaf.cascading_ext.msj_tap.store.PartitionableDataStore;
import com.rapleaf.cascading_ext.pipe.PipeFactory;
import com.rapleaf.cascading_ext.tap.TapFactory;
import com.rapleaf.cascading_ext.tap.TapFactory.SimpleFactory;
import com.rapleaf.cascading_ext.workflow2.SinkBinding.DSSink;
import com.rapleaf.cascading_ext.workflow2.action.FutureCascadingAction;

public class CascadingWorkflowBuilder {
  private static final Logger LOG = LoggerFactory.getLogger(CascadingWorkflowBuilder.class);

  private final String flowName;
  private final DataStoreBuilder dsBuilder;
  private final Set<Step> subSteps = Sets.newHashSet();
  private Map<String, BindInfo> bindInfoMap;
  private Multimap<String, Step> pipenameToParentStep;        //  what step(s) does the pipe depend on

  private Map<Object, Object> flowProperties;
  private int checkpointCount = 0;

  //  TODO kill this once we figure out the cascading internal NPE (upgrade past 2.5.1 maybe?)
  private boolean skipCompleteListener = false;

  public CascadingWorkflowBuilder(String workingDir, String flowName) {
    this(workingDir, flowName, Maps.newHashMap(), false);
  }

  public CascadingWorkflowBuilder(String workingDir, String flowName, Map<Object, Object> flowProperties, boolean skipCompleteListener) {
    this.dsBuilder = new DataStoreBuilder(workingDir + "/temp-stores");
    this.flowName = flowName;
    this.flowProperties = Maps.newHashMap(flowProperties);
    this.bindInfoMap = Maps.newHashMap();
    this.pipenameToParentStep = HashMultimap.create();
    this.skipCompleteListener = skipCompleteListener;
  }

  //  bind source and sink taps to the flow.  This will set the creates / readsFrom datastores as well as
  //  create the necessary actions

  public Pipe bindSource(String name, SourceStoreBinding sourceStoreBinding, ActionCallback callback) {
    bindInfoMap.put(name, new BindInfo(sourceStoreBinding.getTapFactory(), callback, Lists.newArrayList(sourceStoreBinding.getStores())));
    return sourceStoreBinding.getPipe(new Pipe(name));
  }

  //  checkpoints

  public Pipe addCheckpoint(Pipe endPipe, Fields fields) throws IOException {
    return addCheckpoint(endPipe, getNextStepName(), fields, new EmptyListener());
  }

  public Pipe addCheckpoint(Pipe endPipe, Fields fields, FlowListener flowCompletedCallback) throws IOException {
    return addCheckpoint(endPipe, getNextStepName(), fields, flowCompletedCallback);
  }

  public Pipe addCheckpoint(Pipe endPipe, String checkpointName, Fields fields) throws IOException {
    return addCheckpoint(endPipe, checkpointName, fields, new EmptyListener());
  }

  public Pipe addCheckpoint(Pipe endPipe) throws IOException {
    return addCheckpoint(endPipe, getNextStepName(), determineOutputFields(endPipe), new EmptyListener());
  }

  public Pipe addCheckpoint(Pipe endPipe, FlowListener flowCompletedCallback) throws IOException {
    return addCheckpoint(endPipe, getNextStepName(), determineOutputFields(endPipe), flowCompletedCallback);
  }

  public Pipe addCheckpoint(Pipe endPipe, String checkpointName) throws IOException {
    return addCheckpoint(endPipe, checkpointName, determineOutputFields(endPipe), new EmptyListener());
  }

  public Pipe addCheckpoint(Pipe endPipe, String checkpointName, FlowListener flowListener) throws IOException {
    return addCheckpoint(endPipe, checkpointName, determineOutputFields(endPipe), flowListener);
  }

  public Pipe addCheckpoint(Pipe endPipe, String checkpointName, Fields fields, FlowListener flowListener) throws IOException {
    return addCheckpoint(endPipe, checkpointName, fields, flowListener, emptySupplier());
  }

  public Pipe addCheckpoint(Pipe endPipe, String checkpointName, Fields fields, FlowListener flowListener,
                            Supplier<Map<Object, Object>> propertiesSupplier) throws IOException {
    LOG.info("determined output fields to be " + fields + " for step " + checkpointName);
    TupleDataStore checkpointStore = dsBuilder.getTupleDataStore(checkpointName, fields);

    Step step = completeFlows(checkpointName, Lists.newArrayList(new DSSink(endPipe, checkpointStore)), flowListener,
        propertiesSupplier);

    String nextPipeName = "tail-" + checkpointName;

    subSteps.add(step);
    pipenameToParentStep.put(nextPipeName, step);

    bindInfoMap.put(nextPipeName, new BindInfo(new SimpleFactory(checkpointStore), new ActionCallback.Default(), Lists.<DataStore>newArrayList(checkpointStore)));

    return new Pipe(nextPipeName);
  }

  //  build the workflow or multi step action

  public Step buildTail(Pipe output, DataStore outputStore) {
    return buildTail(getNextStepName(), output, outputStore);
  }

  public Step buildNullTail(Pipe output) {
    return buildTail(getNextStepName(), Lists.newArrayList(new SinkBinding.EmptySink(output)));
  }

  public Step buildTail(String tailStepName, Pipe output, DataStore outputStore) {
    return buildTail(tailStepName, Lists.newArrayList(new DSSink(output, outputStore)), new EmptyListener(),
        emptySupplier());
  }

  public Step buildPartitionedTail(String tailStepName, Pipe output, PartitionableDataStore outputStore, PartitionFactory structure) {
    return buildTail(tailStepName, Lists.newArrayList(new SinkBinding.PartitionedSink(output, outputStore, structure)), new EmptyListener(),
        emptySupplier());
  }

  public Step buildTail(String tailStepName, Pipe output, DataStore outputStore, FlowListener listener) {
    return buildTail(tailStepName, Lists.newArrayList(new DSSink(output, outputStore)), listener, emptySupplier());
  }

  public Step buildTail(String tailStepName, List<? extends SinkBinding> sinks) {
    return buildTail(tailStepName, sinks, new EmptyListener(), emptySupplier());
  }

  public Step buildTail(String tailStepName, List<? extends SinkBinding> sinks, FlowListener listener,
                        Supplier<Map<Object, Object>> propertiesSupplier) {
    Step tail = completeFlows(tailStepName, sinks, listener, propertiesSupplier);

    List<Step> steps = Lists.newArrayList(subSteps);
    steps.add(tail);

    return tail;
  }

  private Supplier<Map<Object, Object>> emptySupplier() {
    return Collections::emptyMap;
  }

  //  internal stuff

  private String getNextStepName() {
    return "step-" + (checkpointCount++);
  }

  private Step completeFlows(String checkpointName, List<? extends SinkBinding> sinkBindings, FlowListener flowListener,
                             Supplier<Map<Object, Object>> propertiesSupplier) {

    Map<String, BindInfo> scopedBind = Maps.newHashMap();
    Map<String, TapFactory> sinks = Maps.newHashMap();
    List<DataStore> sinkStores = Lists.newArrayList();

    List<Step> previousSteps = Lists.newArrayList();
    List<Pipe> pipes = Lists.newArrayList();

    for (SinkBinding sinkBinding : sinkBindings) {
      Pipe pipe = sinkBinding.getPipe();
      Pipe[] heads = pipe.getHeads();

      String pipeName = pipe.getName();
      if (sinks.containsKey(pipeName)) {
        throw new RuntimeException("Pipe with name " + pipeName + " already exists!");
      }

      scopedBind.putAll(getBindMap(heads));

      previousSteps.addAll(getPreviousSteps(heads));
      pipes.add(pipe);

      sinks.put(pipeName, sinkBinding.getTapFactory());

      sinkStores.addAll(sinkBinding.getOutputStores());

    }

    FutureCascadingAction action = new FutureCascadingAction(
        checkpointName,
        flowName,
        scopedBind,
        sinks,
        pipes,
        sinkStores,
        flowProperties,
        propertiesSupplier,
        flowListener,
        skipCompleteListener
    );

    return new Step(action, previousSteps);
  }

  private List<Step> getPreviousSteps(Pipe[] heads) {
    List<Step> output = Lists.newArrayList();
    for (Pipe head : heads) {
      if (pipenameToParentStep.containsKey(head.getName())) {
        output.addAll(pipenameToParentStep.get(head.getName()));
      }
    }
    return output;
  }

  private Fields determineOutputFields(Pipe tail) throws IOException {
    return getScope(tail).getIncomingTapFields();
  }

  private Scope getScope(Pipe tail) throws IOException {
    Pipe[] previousPipes = tail.getPrevious();
    if (previousPipes.length == 0) {
      Fields sourceFields;
      if (bindInfoMap.containsKey(tail.getName())) {
        sourceFields = bindInfoMap.get(tail.getName()).getTapFactory().getSourceFields();
      } else {
        throw new RuntimeException("Cannot find head pipe name " + tail.getName() + " in any source map during field resolution");
      }
      Scope scope = new Scope(sourceFields);
      scope.setName(tail.getName());
      return scope;
    } else {
      Set<Scope> scopes = Sets.newHashSet();
      for (Pipe previous : previousPipes) {
        Scope scope = getScope(previous);
        scope.setName(previous.getName());
        scopes.add(scope);
      }
      return tail.outgoingScopeFor(scopes);
    }
  }

  private Map<String, BindInfo> getBindMap(Pipe[] heads) {
    Map<String, BindInfo> inputStores = Maps.newHashMap();
    for (Pipe head : heads) {
      String name = head.getName();

      if (bindInfoMap.containsKey(name)) {
        inputStores.put(name, bindInfoMap.get(name));
      } else {
        throw new RuntimeException("Could not find store for head pipe " + name);
      }

    }
    return inputStores;
  }


}
