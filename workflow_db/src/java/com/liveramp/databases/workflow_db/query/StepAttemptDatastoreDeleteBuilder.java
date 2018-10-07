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
import com.liveramp.databases.workflow_db.iface.IStepAttemptDatastorePersistence;
import com.liveramp.databases.workflow_db.models.StepAttemptDatastore;


public class StepAttemptDatastoreDeleteBuilder extends AbstractDeleteBuilder<StepAttemptDatastore> {

  public StepAttemptDatastoreDeleteBuilder(IStepAttemptDatastorePersistence caller) {
    super(caller);
  }

  public StepAttemptDatastoreDeleteBuilder id(Long value) {
    addId(value);
    return this;
  }

  public StepAttemptDatastoreDeleteBuilder idIn(Collection<Long> values) {
    addIds(values);
    return this;
  }

  public StepAttemptDatastoreDeleteBuilder stepAttemptId(Long value) {
    addWhereConstraint(new WhereConstraint<Long>(StepAttemptDatastore._Fields.step_attempt_id, JackMatchers.equalTo(value)));
    return this;
  }

  public StepAttemptDatastoreDeleteBuilder whereStepAttemptId(IWhereOperator<Long> operator) {
    addWhereConstraint(new WhereConstraint<Long>(StepAttemptDatastore._Fields.step_attempt_id, operator));
    return this;
  }

  public StepAttemptDatastoreDeleteBuilder workflowAttemptDatastoreId(Integer value) {
    addWhereConstraint(new WhereConstraint<Integer>(StepAttemptDatastore._Fields.workflow_attempt_datastore_id, JackMatchers.equalTo(value)));
    return this;
  }

  public StepAttemptDatastoreDeleteBuilder whereWorkflowAttemptDatastoreId(IWhereOperator<Integer> operator) {
    addWhereConstraint(new WhereConstraint<Integer>(StepAttemptDatastore._Fields.workflow_attempt_datastore_id, operator));
    return this;
  }

  public StepAttemptDatastoreDeleteBuilder dsAction(Integer value) {
    addWhereConstraint(new WhereConstraint<Integer>(StepAttemptDatastore._Fields.ds_action, JackMatchers.equalTo(value)));
    return this;
  }

  public StepAttemptDatastoreDeleteBuilder whereDsAction(IWhereOperator<Integer> operator) {
    addWhereConstraint(new WhereConstraint<Integer>(StepAttemptDatastore._Fields.ds_action, operator));
    return this;
  }
}
