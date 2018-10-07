/**
 * Autogenerated by Jack
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 */
package com.liveramp.databases.workflow_db.query;

import java.util.Collection;

import com.rapleaf.jack.queries.AbstractDeleteBuilder;
import com.rapleaf.jack.queries.where_operators.IWhereOperator;
import com.rapleaf.jack.queries.where_operators.JackMatchers;
import com.rapleaf.jack.queries.WhereConstraint;
import com.liveramp.databases.workflow_db.iface.IWorkflowAttemptDatastorePersistence;
import com.liveramp.databases.workflow_db.models.WorkflowAttemptDatastore;


public class WorkflowAttemptDatastoreDeleteBuilder extends AbstractDeleteBuilder<WorkflowAttemptDatastore> {

  public WorkflowAttemptDatastoreDeleteBuilder(IWorkflowAttemptDatastorePersistence caller) {
    super(caller);
  }

  public WorkflowAttemptDatastoreDeleteBuilder id(Long value) {
    addId(value);
    return this;
  }

  public WorkflowAttemptDatastoreDeleteBuilder idIn(Collection<Long> values) {
    addIds(values);
    return this;
  }

  public WorkflowAttemptDatastoreDeleteBuilder workflowAttemptId(Integer value) {
    addWhereConstraint(new WhereConstraint<Integer>(WorkflowAttemptDatastore._Fields.workflow_attempt_id, JackMatchers.equalTo(value)));
    return this;
  }

  public WorkflowAttemptDatastoreDeleteBuilder whereWorkflowAttemptId(IWhereOperator<Integer> operator) {
    addWhereConstraint(new WhereConstraint<Integer>(WorkflowAttemptDatastore._Fields.workflow_attempt_id, operator));
    return this;
  }

  public WorkflowAttemptDatastoreDeleteBuilder name(String value) {
    addWhereConstraint(new WhereConstraint<String>(WorkflowAttemptDatastore._Fields.name, JackMatchers.equalTo(value)));
    return this;
  }

  public WorkflowAttemptDatastoreDeleteBuilder whereName(IWhereOperator<String> operator) {
    addWhereConstraint(new WhereConstraint<String>(WorkflowAttemptDatastore._Fields.name, operator));
    return this;
  }

  public WorkflowAttemptDatastoreDeleteBuilder path(String value) {
    addWhereConstraint(new WhereConstraint<String>(WorkflowAttemptDatastore._Fields.path, JackMatchers.equalTo(value)));
    return this;
  }

  public WorkflowAttemptDatastoreDeleteBuilder wherePath(IWhereOperator<String> operator) {
    addWhereConstraint(new WhereConstraint<String>(WorkflowAttemptDatastore._Fields.path, operator));
    return this;
  }

  public WorkflowAttemptDatastoreDeleteBuilder className(String value) {
    addWhereConstraint(new WhereConstraint<String>(WorkflowAttemptDatastore._Fields.class_name, JackMatchers.equalTo(value)));
    return this;
  }

  public WorkflowAttemptDatastoreDeleteBuilder whereClassName(IWhereOperator<String> operator) {
    addWhereConstraint(new WhereConstraint<String>(WorkflowAttemptDatastore._Fields.class_name, operator));
    return this;
  }
}
