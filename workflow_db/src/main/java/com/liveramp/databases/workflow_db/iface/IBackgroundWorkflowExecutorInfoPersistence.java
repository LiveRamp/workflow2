
/**
 * Autogenerated by Jack
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 */
package com.liveramp.databases.workflow_db.iface;

import com.liveramp.databases.workflow_db.models.BackgroundWorkflowExecutorInfo;
import com.liveramp.databases.workflow_db.query.BackgroundWorkflowExecutorInfoQueryBuilder;
import com.liveramp.databases.workflow_db.query.BackgroundWorkflowExecutorInfoDeleteBuilder;

import java.io.IOException;
import java.util.Map;
import java.util.List;

import com.rapleaf.jack.IModelPersistence;

public interface IBackgroundWorkflowExecutorInfoPersistence extends IModelPersistence<BackgroundWorkflowExecutorInfo> {
  BackgroundWorkflowExecutorInfo create(final String host, final int status, final long last_heartbeat, final Long last_heartbeat_epoch) throws IOException;
  BackgroundWorkflowExecutorInfo create(final String host, final int status, final long last_heartbeat) throws IOException;

  BackgroundWorkflowExecutorInfo createDefaultInstance() throws IOException;
  List<BackgroundWorkflowExecutorInfo> findByHost(String value)  throws IOException;
  List<BackgroundWorkflowExecutorInfo> findByStatus(int value)  throws IOException;
  List<BackgroundWorkflowExecutorInfo> findByLastHeartbeat(long value)  throws IOException;
  List<BackgroundWorkflowExecutorInfo> findByLastHeartbeatEpoch(Long value)  throws IOException;

  BackgroundWorkflowExecutorInfoQueryBuilder query();

  BackgroundWorkflowExecutorInfoDeleteBuilder delete();
}
