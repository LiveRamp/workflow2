package com.rapleaf.cascading_ext.workflow2.state;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.codec.binary.Hex;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liveramp.cascading_ext.FileSystemHelper;
import com.liveramp.cascading_ext.resource.ResourceManager;
import com.liveramp.commons.util.BytesUtils;
import com.liveramp.commons.util.MultiShutdownHook;
import com.liveramp.workflow.types.StepStatus;
import com.liveramp.workflow_core.JVMState;
import com.liveramp.workflow2.workflow_hadoop.CheckpointUtil;
import com.liveramp.workflow_state.StepState;
import com.liveramp.workflow_state.WorkflowStatePersistence;
import com.rapleaf.cascading_ext.workflow2.Step;
import com.rapleaf.cascading_ext.workflow2.options.HadoopWorkflowOptions;

public class HdfsCheckpointPersistence extends WorkflowPersistenceFactory<Step, HdfsInitializedPersistence, HadoopWorkflowOptions, HadoopWorkflow> {

  private static final Logger LOG = LoggerFactory.getLogger(HdfsPersistenceContainer.class);

  private final String checkpointDir;
  private final boolean deleteOnSuccess;

  public HdfsCheckpointPersistence(String checkpointDir) {
    this(checkpointDir, true);
  }

  public HdfsCheckpointPersistence(String checkpointDir, boolean deleteOnSuccess) {
    super(new JVMState());
    this.checkpointDir = checkpointDir;
    this.deleteOnSuccess = deleteOnSuccess;
  }

  @Override
  public HadoopWorkflow construct(String workflowName, HadoopWorkflowOptions options, HdfsInitializedPersistence hdfsInitializedPersistence, ResourceManager manager, MultiShutdownHook hook) {
    return new HadoopWorkflow(workflowName, options, hdfsInitializedPersistence, this, manager, hook);
  }

  @Override
  public HdfsInitializedPersistence initializeInternal(String name,
                                                       HadoopWorkflowOptions options,
                                                       String host,
                                                       String username,
                                                       String pool,
                                                       String priority,
                                                       String launchDir,
                                                       String launchJar,
                                                       String remote,
                                                       String implementationBuild
  ) throws IOException {

    FileSystem fs = FileSystemHelper.getFileSystemForPath(checkpointDir);

    Path checkpointDirPath = new Path(checkpointDir);
    LOG.info("Creating checkpoint dir " + checkpointDir);
    fs.mkdirs(checkpointDirPath);

    long currentExecution = getAttemptExecutionId(fs, checkpointDirPath);
    LOG.info("Writing execution ID to state:  "+currentExecution);

    CheckpointUtil.writeExecutionId(currentExecution, fs, checkpointDirPath);

    return new HdfsInitializedPersistence(currentExecution, name, priority, pool, host, username, options.getAlertsHandler(), options.getEnabledNotifications(), fs);
  }

  private long getAttemptExecutionId(FileSystem fs, Path checkpointDirPath) throws IOException {

    long latest = CheckpointUtil.getLatestExecutionId(fs, checkpointDirPath);

    //  we are resuming
    if (CheckpointUtil.existCheckpoints(checkpointDirPath)) {
      LOG.info("Resuming execution, using ID "+latest);
      return latest;
    }
    //  new execution
    else {

      long next = latest + 1;
      LOG.info("New execution, using ID "+next);

      return next;
    }

  }

  @Override
  public WorkflowStatePersistence prepare(HdfsInitializedPersistence persistence,
                                          DirectedGraph<Step, DefaultEdge> flatSteps) {

    FileSystem fs = persistence.getFs();

    Map<String, StepState> statuses = Maps.newHashMap();

    try {

      for (Step val : flatSteps.vertexSet()) {

        Set<String> dependencies = Sets.newHashSet();
        for (DefaultEdge edge : flatSteps.outgoingEdgesOf(val)) {
          dependencies.add(flatSteps.getEdgeTarget(edge).getCheckpointToken());
        }

        statuses.put(val.getCheckpointToken(), new StepState(
            val.getCheckpointToken(),
            StepStatus.WAITING,
            val.getActionClass(),
            dependencies,
            val.getDataStores()
        ));

      }


      for (FileStatus status : FileSystemHelper.safeListStatus(fs, new Path(checkpointDir))) {
        String token = status.getPath().getName();
        if (statuses.containsKey(token)) {
          statuses.get(token).setStatus(StepStatus.SKIPPED);
        } else {
          LOG.info("Skipping obsolete token " + token);
        }
      }

      return new HdfsPersistenceContainer(
          checkpointDir,
          deleteOnSuccess,
          Hex.encodeHexString(BytesUtils.uuidToBytes(UUID.randomUUID())),
          statuses,
          persistence
      );

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
