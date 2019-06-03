
/**
 * Autogenerated by Jack
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 */
package com.liveramp.databases.workflow_db.iface;

import com.liveramp.databases.workflow_db.models.WorkflowAttemptConfiguredNotification;
import com.liveramp.databases.workflow_db.query.WorkflowAttemptConfiguredNotificationQueryBuilder;
import com.liveramp.databases.workflow_db.query.WorkflowAttemptConfiguredNotificationDeleteBuilder;

import java.io.IOException;
import java.util.Map;
import java.util.List;

import com.rapleaf.jack.IModelPersistence;

public interface IWorkflowAttemptConfiguredNotificationPersistence extends IModelPersistence<WorkflowAttemptConfiguredNotification> {
  WorkflowAttemptConfiguredNotification create(final long workflow_attempt_id, final long configured_notification_id) throws IOException;

  WorkflowAttemptConfiguredNotification createDefaultInstance() throws IOException;
  List<WorkflowAttemptConfiguredNotification> findByWorkflowAttemptId(long value)  throws IOException;
  List<WorkflowAttemptConfiguredNotification> findByConfiguredNotificationId(long value)  throws IOException;

  WorkflowAttemptConfiguredNotificationQueryBuilder query();

  WorkflowAttemptConfiguredNotificationDeleteBuilder delete();
}
