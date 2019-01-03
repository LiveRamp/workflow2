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
import com.liveramp.databases.workflow_db.iface.IStepAttemptPersistence;
import com.liveramp.databases.workflow_db.models.StepAttempt;


public class StepAttemptQueryBuilder extends AbstractQueryBuilder<StepAttempt> {

  public StepAttemptQueryBuilder(IStepAttemptPersistence caller) {
    super(caller);
  }

  public StepAttemptQueryBuilder select(StepAttempt._Fields... fields) {
    for (StepAttempt._Fields field : fields){
      addSelectedField(new FieldSelector(field));
    }
    return this;
  }

  public StepAttemptQueryBuilder selectAgg(FieldSelector... aggregatedFields) {
    addSelectedFields(aggregatedFields);
    return this;
  }

  public StepAttemptQueryBuilder id(Long value) {
    addId(value);
    return this;
  }

  public StepAttemptQueryBuilder idIn(Collection<Long> values) {
    addIds(values);
    return this;
  }

  public StepAttemptQueryBuilder whereId(IWhereOperator<Long> operator) {
    addWhereConstraint(new WhereConstraint<>(Column.fromId(null), operator, null));
    return this;
  }

  public StepAttemptQueryBuilder limit(int offset, int nResults) {
    setLimit(new LimitCriterion(offset, nResults));
    return this;
  }

  public StepAttemptQueryBuilder limit(int nResults) {
    setLimit(new LimitCriterion(nResults));
    return this;
  }

  public StepAttemptQueryBuilder groupBy(StepAttempt._Fields... fields) {
    addGroupByFields(fields);
    return this;
  }

  public StepAttemptQueryBuilder order() {
    this.addOrder(new OrderCriterion(QueryOrder.ASC));
    return this;
  }

  public StepAttemptQueryBuilder order(QueryOrder queryOrder) {
    this.addOrder(new OrderCriterion(queryOrder));
    return this;
  }

  public StepAttemptQueryBuilder orderById() {
    this.addOrder(new OrderCriterion(QueryOrder.ASC));
    return this;
  }

  public StepAttemptQueryBuilder orderById(QueryOrder queryOrder) {
    this.addOrder(new OrderCriterion(queryOrder));
    return this;
  }

  public StepAttemptQueryBuilder workflowAttemptId(Integer value) {
    addWhereConstraint(new WhereConstraint<>(StepAttempt._Fields.workflow_attempt_id, JackMatchers.equalTo(value)));
    return this;
  }

  public StepAttemptQueryBuilder whereWorkflowAttemptId(IWhereOperator<Integer> operator) {
    addWhereConstraint(new WhereConstraint<>(StepAttempt._Fields.workflow_attempt_id, operator));
    return this;
  }

  public StepAttemptQueryBuilder orderByWorkflowAttemptId() {
    this.addOrder(new OrderCriterion(StepAttempt._Fields.workflow_attempt_id, QueryOrder.ASC));
    return this;
  }

  public StepAttemptQueryBuilder orderByWorkflowAttemptId(QueryOrder queryOrder) {
    this.addOrder(new OrderCriterion(StepAttempt._Fields.workflow_attempt_id, queryOrder));
    return this;
  }

  public StepAttemptQueryBuilder stepToken(String value) {
    addWhereConstraint(new WhereConstraint<>(StepAttempt._Fields.step_token, JackMatchers.equalTo(value)));
    return this;
  }

  public StepAttemptQueryBuilder whereStepToken(IWhereOperator<String> operator) {
    addWhereConstraint(new WhereConstraint<>(StepAttempt._Fields.step_token, operator));
    return this;
  }

  public StepAttemptQueryBuilder orderByStepToken() {
    this.addOrder(new OrderCriterion(StepAttempt._Fields.step_token, QueryOrder.ASC));
    return this;
  }

  public StepAttemptQueryBuilder orderByStepToken(QueryOrder queryOrder) {
    this.addOrder(new OrderCriterion(StepAttempt._Fields.step_token, queryOrder));
    return this;
  }

  public StepAttemptQueryBuilder startTime(Long value) {
    addWhereConstraint(new WhereConstraint<>(StepAttempt._Fields.start_time, JackMatchers.equalTo(value)));
    return this;
  }

  public StepAttemptQueryBuilder whereStartTime(IWhereOperator<Long> operator) {
    addWhereConstraint(new WhereConstraint<>(StepAttempt._Fields.start_time, operator));
    return this;
  }

  public StepAttemptQueryBuilder orderByStartTime() {
    this.addOrder(new OrderCriterion(StepAttempt._Fields.start_time, QueryOrder.ASC));
    return this;
  }

  public StepAttemptQueryBuilder orderByStartTime(QueryOrder queryOrder) {
    this.addOrder(new OrderCriterion(StepAttempt._Fields.start_time, queryOrder));
    return this;
  }

  public StepAttemptQueryBuilder endTime(Long value) {
    addWhereConstraint(new WhereConstraint<>(StepAttempt._Fields.end_time, JackMatchers.equalTo(value)));
    return this;
  }

  public StepAttemptQueryBuilder whereEndTime(IWhereOperator<Long> operator) {
    addWhereConstraint(new WhereConstraint<>(StepAttempt._Fields.end_time, operator));
    return this;
  }

  public StepAttemptQueryBuilder orderByEndTime() {
    this.addOrder(new OrderCriterion(StepAttempt._Fields.end_time, QueryOrder.ASC));
    return this;
  }

  public StepAttemptQueryBuilder orderByEndTime(QueryOrder queryOrder) {
    this.addOrder(new OrderCriterion(StepAttempt._Fields.end_time, queryOrder));
    return this;
  }

  public StepAttemptQueryBuilder stepStatus(Integer value) {
    addWhereConstraint(new WhereConstraint<>(StepAttempt._Fields.step_status, JackMatchers.equalTo(value)));
    return this;
  }

  public StepAttemptQueryBuilder whereStepStatus(IWhereOperator<Integer> operator) {
    addWhereConstraint(new WhereConstraint<>(StepAttempt._Fields.step_status, operator));
    return this;
  }

  public StepAttemptQueryBuilder orderByStepStatus() {
    this.addOrder(new OrderCriterion(StepAttempt._Fields.step_status, QueryOrder.ASC));
    return this;
  }

  public StepAttemptQueryBuilder orderByStepStatus(QueryOrder queryOrder) {
    this.addOrder(new OrderCriterion(StepAttempt._Fields.step_status, queryOrder));
    return this;
  }

  public StepAttemptQueryBuilder failureCause(String value) {
    addWhereConstraint(new WhereConstraint<>(StepAttempt._Fields.failure_cause, JackMatchers.equalTo(value)));
    return this;
  }

  public StepAttemptQueryBuilder whereFailureCause(IWhereOperator<String> operator) {
    addWhereConstraint(new WhereConstraint<>(StepAttempt._Fields.failure_cause, operator));
    return this;
  }

  public StepAttemptQueryBuilder orderByFailureCause() {
    this.addOrder(new OrderCriterion(StepAttempt._Fields.failure_cause, QueryOrder.ASC));
    return this;
  }

  public StepAttemptQueryBuilder orderByFailureCause(QueryOrder queryOrder) {
    this.addOrder(new OrderCriterion(StepAttempt._Fields.failure_cause, queryOrder));
    return this;
  }

  public StepAttemptQueryBuilder failureTrace(String value) {
    addWhereConstraint(new WhereConstraint<>(StepAttempt._Fields.failure_trace, JackMatchers.equalTo(value)));
    return this;
  }

  public StepAttemptQueryBuilder whereFailureTrace(IWhereOperator<String> operator) {
    addWhereConstraint(new WhereConstraint<>(StepAttempt._Fields.failure_trace, operator));
    return this;
  }

  public StepAttemptQueryBuilder orderByFailureTrace() {
    this.addOrder(new OrderCriterion(StepAttempt._Fields.failure_trace, QueryOrder.ASC));
    return this;
  }

  public StepAttemptQueryBuilder orderByFailureTrace(QueryOrder queryOrder) {
    this.addOrder(new OrderCriterion(StepAttempt._Fields.failure_trace, queryOrder));
    return this;
  }

  public StepAttemptQueryBuilder actionClass(String value) {
    addWhereConstraint(new WhereConstraint<>(StepAttempt._Fields.action_class, JackMatchers.equalTo(value)));
    return this;
  }

  public StepAttemptQueryBuilder whereActionClass(IWhereOperator<String> operator) {
    addWhereConstraint(new WhereConstraint<>(StepAttempt._Fields.action_class, operator));
    return this;
  }

  public StepAttemptQueryBuilder orderByActionClass() {
    this.addOrder(new OrderCriterion(StepAttempt._Fields.action_class, QueryOrder.ASC));
    return this;
  }

  public StepAttemptQueryBuilder orderByActionClass(QueryOrder queryOrder) {
    this.addOrder(new OrderCriterion(StepAttempt._Fields.action_class, queryOrder));
    return this;
  }

  public StepAttemptQueryBuilder statusMessage(String value) {
    addWhereConstraint(new WhereConstraint<>(StepAttempt._Fields.status_message, JackMatchers.equalTo(value)));
    return this;
  }

  public StepAttemptQueryBuilder whereStatusMessage(IWhereOperator<String> operator) {
    addWhereConstraint(new WhereConstraint<>(StepAttempt._Fields.status_message, operator));
    return this;
  }

  public StepAttemptQueryBuilder orderByStatusMessage() {
    this.addOrder(new OrderCriterion(StepAttempt._Fields.status_message, QueryOrder.ASC));
    return this;
  }

  public StepAttemptQueryBuilder orderByStatusMessage(QueryOrder queryOrder) {
    this.addOrder(new OrderCriterion(StepAttempt._Fields.status_message, queryOrder));
    return this;
  }
}