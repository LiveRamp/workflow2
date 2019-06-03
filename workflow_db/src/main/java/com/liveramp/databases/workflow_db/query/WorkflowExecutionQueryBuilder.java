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
import com.liveramp.databases.workflow_db.iface.IWorkflowExecutionPersistence;
import com.liveramp.databases.workflow_db.models.WorkflowExecution;


public class WorkflowExecutionQueryBuilder extends AbstractQueryBuilder<WorkflowExecution> {

  public WorkflowExecutionQueryBuilder(IWorkflowExecutionPersistence caller) {
    super(caller);
  }

  public WorkflowExecutionQueryBuilder select(WorkflowExecution._Fields... fields) {
    for (WorkflowExecution._Fields field : fields){
      addSelectedField(new FieldSelector(field));
    }
    return this;
  }

  public WorkflowExecutionQueryBuilder selectAgg(FieldSelector... aggregatedFields) {
    addSelectedFields(aggregatedFields);
    return this;
  }

  public WorkflowExecutionQueryBuilder id(Long value) {
    addId(value);
    return this;
  }

  public WorkflowExecutionQueryBuilder idIn(Collection<Long> values) {
    addIds(values);
    return this;
  }

  public WorkflowExecutionQueryBuilder whereId(IWhereOperator<Long> operator) {
    addWhereConstraint(new WhereConstraint<>(Column.fromId(null), operator, null));
    return this;
  }

  public WorkflowExecutionQueryBuilder limit(int offset, int nResults) {
    setLimit(new LimitCriterion(offset, nResults));
    return this;
  }

  public WorkflowExecutionQueryBuilder limit(int nResults) {
    setLimit(new LimitCriterion(nResults));
    return this;
  }

  public WorkflowExecutionQueryBuilder groupBy(WorkflowExecution._Fields... fields) {
    addGroupByFields(fields);
    return this;
  }

  public WorkflowExecutionQueryBuilder order() {
    this.addOrder(new OrderCriterion(QueryOrder.ASC));
    return this;
  }

  public WorkflowExecutionQueryBuilder order(QueryOrder queryOrder) {
    this.addOrder(new OrderCriterion(queryOrder));
    return this;
  }

  public WorkflowExecutionQueryBuilder orderById() {
    this.addOrder(new OrderCriterion(QueryOrder.ASC));
    return this;
  }

  public WorkflowExecutionQueryBuilder orderById(QueryOrder queryOrder) {
    this.addOrder(new OrderCriterion(queryOrder));
    return this;
  }

  public WorkflowExecutionQueryBuilder appType(Integer value) {
    addWhereConstraint(new WhereConstraint<>(WorkflowExecution._Fields.app_type, JackMatchers.equalTo(value)));
    return this;
  }

  public WorkflowExecutionQueryBuilder whereAppType(IWhereOperator<Integer> operator) {
    addWhereConstraint(new WhereConstraint<>(WorkflowExecution._Fields.app_type, operator));
    return this;
  }

  public WorkflowExecutionQueryBuilder orderByAppType() {
    this.addOrder(new OrderCriterion(WorkflowExecution._Fields.app_type, QueryOrder.ASC));
    return this;
  }

  public WorkflowExecutionQueryBuilder orderByAppType(QueryOrder queryOrder) {
    this.addOrder(new OrderCriterion(WorkflowExecution._Fields.app_type, queryOrder));
    return this;
  }

  public WorkflowExecutionQueryBuilder name(String value) {
    addWhereConstraint(new WhereConstraint<>(WorkflowExecution._Fields.name, JackMatchers.equalTo(value)));
    return this;
  }

  public WorkflowExecutionQueryBuilder whereName(IWhereOperator<String> operator) {
    addWhereConstraint(new WhereConstraint<>(WorkflowExecution._Fields.name, operator));
    return this;
  }

  public WorkflowExecutionQueryBuilder orderByName() {
    this.addOrder(new OrderCriterion(WorkflowExecution._Fields.name, QueryOrder.ASC));
    return this;
  }

  public WorkflowExecutionQueryBuilder orderByName(QueryOrder queryOrder) {
    this.addOrder(new OrderCriterion(WorkflowExecution._Fields.name, queryOrder));
    return this;
  }

  public WorkflowExecutionQueryBuilder scopeIdentifier(String value) {
    addWhereConstraint(new WhereConstraint<>(WorkflowExecution._Fields.scope_identifier, JackMatchers.equalTo(value)));
    return this;
  }

  public WorkflowExecutionQueryBuilder whereScopeIdentifier(IWhereOperator<String> operator) {
    addWhereConstraint(new WhereConstraint<>(WorkflowExecution._Fields.scope_identifier, operator));
    return this;
  }

  public WorkflowExecutionQueryBuilder orderByScopeIdentifier() {
    this.addOrder(new OrderCriterion(WorkflowExecution._Fields.scope_identifier, QueryOrder.ASC));
    return this;
  }

  public WorkflowExecutionQueryBuilder orderByScopeIdentifier(QueryOrder queryOrder) {
    this.addOrder(new OrderCriterion(WorkflowExecution._Fields.scope_identifier, queryOrder));
    return this;
  }

  public WorkflowExecutionQueryBuilder status(Integer value) {
    addWhereConstraint(new WhereConstraint<>(WorkflowExecution._Fields.status, JackMatchers.equalTo(value)));
    return this;
  }

  public WorkflowExecutionQueryBuilder whereStatus(IWhereOperator<Integer> operator) {
    addWhereConstraint(new WhereConstraint<>(WorkflowExecution._Fields.status, operator));
    return this;
  }

  public WorkflowExecutionQueryBuilder orderByStatus() {
    this.addOrder(new OrderCriterion(WorkflowExecution._Fields.status, QueryOrder.ASC));
    return this;
  }

  public WorkflowExecutionQueryBuilder orderByStatus(QueryOrder queryOrder) {
    this.addOrder(new OrderCriterion(WorkflowExecution._Fields.status, queryOrder));
    return this;
  }

  public WorkflowExecutionQueryBuilder startTime(Long value) {
    addWhereConstraint(new WhereConstraint<>(WorkflowExecution._Fields.start_time, JackMatchers.equalTo(value)));
    return this;
  }

  public WorkflowExecutionQueryBuilder whereStartTime(IWhereOperator<Long> operator) {
    addWhereConstraint(new WhereConstraint<>(WorkflowExecution._Fields.start_time, operator));
    return this;
  }

  public WorkflowExecutionQueryBuilder orderByStartTime() {
    this.addOrder(new OrderCriterion(WorkflowExecution._Fields.start_time, QueryOrder.ASC));
    return this;
  }

  public WorkflowExecutionQueryBuilder orderByStartTime(QueryOrder queryOrder) {
    this.addOrder(new OrderCriterion(WorkflowExecution._Fields.start_time, queryOrder));
    return this;
  }

  public WorkflowExecutionQueryBuilder endTime(Long value) {
    addWhereConstraint(new WhereConstraint<>(WorkflowExecution._Fields.end_time, JackMatchers.equalTo(value)));
    return this;
  }

  public WorkflowExecutionQueryBuilder whereEndTime(IWhereOperator<Long> operator) {
    addWhereConstraint(new WhereConstraint<>(WorkflowExecution._Fields.end_time, operator));
    return this;
  }

  public WorkflowExecutionQueryBuilder orderByEndTime() {
    this.addOrder(new OrderCriterion(WorkflowExecution._Fields.end_time, QueryOrder.ASC));
    return this;
  }

  public WorkflowExecutionQueryBuilder orderByEndTime(QueryOrder queryOrder) {
    this.addOrder(new OrderCriterion(WorkflowExecution._Fields.end_time, queryOrder));
    return this;
  }

  public WorkflowExecutionQueryBuilder applicationId(Integer value) {
    addWhereConstraint(new WhereConstraint<>(WorkflowExecution._Fields.application_id, JackMatchers.equalTo(value)));
    return this;
  }

  public WorkflowExecutionQueryBuilder whereApplicationId(IWhereOperator<Integer> operator) {
    addWhereConstraint(new WhereConstraint<>(WorkflowExecution._Fields.application_id, operator));
    return this;
  }

  public WorkflowExecutionQueryBuilder orderByApplicationId() {
    this.addOrder(new OrderCriterion(WorkflowExecution._Fields.application_id, QueryOrder.ASC));
    return this;
  }

  public WorkflowExecutionQueryBuilder orderByApplicationId(QueryOrder queryOrder) {
    this.addOrder(new OrderCriterion(WorkflowExecution._Fields.application_id, queryOrder));
    return this;
  }

  public WorkflowExecutionQueryBuilder poolOverride(String value) {
    addWhereConstraint(new WhereConstraint<>(WorkflowExecution._Fields.pool_override, JackMatchers.equalTo(value)));
    return this;
  }

  public WorkflowExecutionQueryBuilder wherePoolOverride(IWhereOperator<String> operator) {
    addWhereConstraint(new WhereConstraint<>(WorkflowExecution._Fields.pool_override, operator));
    return this;
  }

  public WorkflowExecutionQueryBuilder orderByPoolOverride() {
    this.addOrder(new OrderCriterion(WorkflowExecution._Fields.pool_override, QueryOrder.ASC));
    return this;
  }

  public WorkflowExecutionQueryBuilder orderByPoolOverride(QueryOrder queryOrder) {
    this.addOrder(new OrderCriterion(WorkflowExecution._Fields.pool_override, queryOrder));
    return this;
  }
}
