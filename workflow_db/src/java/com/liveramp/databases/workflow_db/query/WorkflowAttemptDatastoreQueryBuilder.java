/**
 * Autogenerated by Jack
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 */
package com.liveramp.databases.workflow_db.query;

import java.util.Collection;

import com.rapleaf.jack.queries.AbstractQueryBuilder;
import com.rapleaf.jack.queries.Column;
import com.rapleaf.jack.queries.FieldSelector;
import com.rapleaf.jack.queries.where_operators.IWhereOperator;
import com.rapleaf.jack.queries.where_operators.JackMatchers;
import com.rapleaf.jack.queries.WhereConstraint;
import com.rapleaf.jack.queries.QueryOrder;
import com.rapleaf.jack.queries.OrderCriterion;
import com.rapleaf.jack.queries.LimitCriterion;
import com.liveramp.databases.workflow_db.iface.IWorkflowAttemptDatastorePersistence;
import com.liveramp.databases.workflow_db.models.WorkflowAttemptDatastore;


public class WorkflowAttemptDatastoreQueryBuilder extends AbstractQueryBuilder<WorkflowAttemptDatastore> {

  public WorkflowAttemptDatastoreQueryBuilder(IWorkflowAttemptDatastorePersistence caller) {
    super(caller);
  }

  public WorkflowAttemptDatastoreQueryBuilder select(WorkflowAttemptDatastore._Fields... fields) {
    for (WorkflowAttemptDatastore._Fields field : fields){
      addSelectedField(new FieldSelector(field));
    }
    return this;
  }

  public WorkflowAttemptDatastoreQueryBuilder selectAgg(FieldSelector... aggregatedFields) {
    addSelectedFields(aggregatedFields);
    return this;
  }

  public WorkflowAttemptDatastoreQueryBuilder id(Long value) {
    addId(value);
    return this;
  }

  public WorkflowAttemptDatastoreQueryBuilder idIn(Collection<Long> values) {
    addIds(values);
    return this;
  }

  public WorkflowAttemptDatastoreQueryBuilder whereId(IWhereOperator<Long> operator) {
    addWhereConstraint(new WhereConstraint<>(Column.fromId(null), operator, null));
    return this;
  }

  public WorkflowAttemptDatastoreQueryBuilder limit(int offset, int nResults) {
    setLimit(new LimitCriterion(offset, nResults));
    return this;
  }

  public WorkflowAttemptDatastoreQueryBuilder limit(int nResults) {
    setLimit(new LimitCriterion(nResults));
    return this;
  }

  public WorkflowAttemptDatastoreQueryBuilder groupBy(WorkflowAttemptDatastore._Fields... fields) {
    addGroupByFields(fields);
    return this;
  }

  public WorkflowAttemptDatastoreQueryBuilder order() {
    this.addOrder(new OrderCriterion(QueryOrder.ASC));
    return this;
  }

  public WorkflowAttemptDatastoreQueryBuilder order(QueryOrder queryOrder) {
    this.addOrder(new OrderCriterion(queryOrder));
    return this;
  }

  public WorkflowAttemptDatastoreQueryBuilder orderById() {
    this.addOrder(new OrderCriterion(QueryOrder.ASC));
    return this;
  }

  public WorkflowAttemptDatastoreQueryBuilder orderById(QueryOrder queryOrder) {
    this.addOrder(new OrderCriterion(queryOrder));
    return this;
  }

  public WorkflowAttemptDatastoreQueryBuilder workflowAttemptId(Integer value) {
    addWhereConstraint(new WhereConstraint<>(WorkflowAttemptDatastore._Fields.workflow_attempt_id, JackMatchers.equalTo(value)));
    return this;
  }

  public WorkflowAttemptDatastoreQueryBuilder whereWorkflowAttemptId(IWhereOperator<Integer> operator) {
    addWhereConstraint(new WhereConstraint<>(WorkflowAttemptDatastore._Fields.workflow_attempt_id, operator));
    return this;
  }

  public WorkflowAttemptDatastoreQueryBuilder orderByWorkflowAttemptId() {
    this.addOrder(new OrderCriterion(WorkflowAttemptDatastore._Fields.workflow_attempt_id, QueryOrder.ASC));
    return this;
  }

  public WorkflowAttemptDatastoreQueryBuilder orderByWorkflowAttemptId(QueryOrder queryOrder) {
    this.addOrder(new OrderCriterion(WorkflowAttemptDatastore._Fields.workflow_attempt_id, queryOrder));
    return this;
  }

  public WorkflowAttemptDatastoreQueryBuilder name(String value) {
    addWhereConstraint(new WhereConstraint<>(WorkflowAttemptDatastore._Fields.name, JackMatchers.equalTo(value)));
    return this;
  }

  public WorkflowAttemptDatastoreQueryBuilder whereName(IWhereOperator<String> operator) {
    addWhereConstraint(new WhereConstraint<>(WorkflowAttemptDatastore._Fields.name, operator));
    return this;
  }

  public WorkflowAttemptDatastoreQueryBuilder orderByName() {
    this.addOrder(new OrderCriterion(WorkflowAttemptDatastore._Fields.name, QueryOrder.ASC));
    return this;
  }

  public WorkflowAttemptDatastoreQueryBuilder orderByName(QueryOrder queryOrder) {
    this.addOrder(new OrderCriterion(WorkflowAttemptDatastore._Fields.name, queryOrder));
    return this;
  }

  public WorkflowAttemptDatastoreQueryBuilder path(String value) {
    addWhereConstraint(new WhereConstraint<>(WorkflowAttemptDatastore._Fields.path, JackMatchers.equalTo(value)));
    return this;
  }

  public WorkflowAttemptDatastoreQueryBuilder wherePath(IWhereOperator<String> operator) {
    addWhereConstraint(new WhereConstraint<>(WorkflowAttemptDatastore._Fields.path, operator));
    return this;
  }

  public WorkflowAttemptDatastoreQueryBuilder orderByPath() {
    this.addOrder(new OrderCriterion(WorkflowAttemptDatastore._Fields.path, QueryOrder.ASC));
    return this;
  }

  public WorkflowAttemptDatastoreQueryBuilder orderByPath(QueryOrder queryOrder) {
    this.addOrder(new OrderCriterion(WorkflowAttemptDatastore._Fields.path, queryOrder));
    return this;
  }

  public WorkflowAttemptDatastoreQueryBuilder className(String value) {
    addWhereConstraint(new WhereConstraint<>(WorkflowAttemptDatastore._Fields.class_name, JackMatchers.equalTo(value)));
    return this;
  }

  public WorkflowAttemptDatastoreQueryBuilder whereClassName(IWhereOperator<String> operator) {
    addWhereConstraint(new WhereConstraint<>(WorkflowAttemptDatastore._Fields.class_name, operator));
    return this;
  }

  public WorkflowAttemptDatastoreQueryBuilder orderByClassName() {
    this.addOrder(new OrderCriterion(WorkflowAttemptDatastore._Fields.class_name, QueryOrder.ASC));
    return this;
  }

  public WorkflowAttemptDatastoreQueryBuilder orderByClassName(QueryOrder queryOrder) {
    this.addOrder(new OrderCriterion(WorkflowAttemptDatastore._Fields.class_name, queryOrder));
    return this;
  }
}
