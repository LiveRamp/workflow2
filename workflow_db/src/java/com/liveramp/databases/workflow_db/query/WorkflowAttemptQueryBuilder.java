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
import com.liveramp.databases.workflow_db.iface.IWorkflowAttemptPersistence;
import com.liveramp.databases.workflow_db.models.WorkflowAttempt;


public class WorkflowAttemptQueryBuilder extends AbstractQueryBuilder<WorkflowAttempt> {

  public WorkflowAttemptQueryBuilder(IWorkflowAttemptPersistence caller) {
    super(caller);
  }

  public WorkflowAttemptQueryBuilder select(WorkflowAttempt._Fields... fields) {
    for (WorkflowAttempt._Fields field : fields){
      addSelectedField(new FieldSelector(field));
    }
    return this;
  }

  public WorkflowAttemptQueryBuilder selectAgg(FieldSelector... aggregatedFields) {
    addSelectedFields(aggregatedFields);
    return this;
  }

  public WorkflowAttemptQueryBuilder id(Long value) {
    addId(value);
    return this;
  }

  public WorkflowAttemptQueryBuilder idIn(Collection<Long> values) {
    addIds(values);
    return this;
  }

  public WorkflowAttemptQueryBuilder whereId(IWhereOperator<Long> operator) {
    addWhereConstraint(new WhereConstraint<>(Column.fromId(null), operator, null));
    return this;
  }

  public WorkflowAttemptQueryBuilder limit(int offset, int nResults) {
    setLimit(new LimitCriterion(offset, nResults));
    return this;
  }

  public WorkflowAttemptQueryBuilder limit(int nResults) {
    setLimit(new LimitCriterion(nResults));
    return this;
  }

  public WorkflowAttemptQueryBuilder groupBy(WorkflowAttempt._Fields... fields) {
    addGroupByFields(fields);
    return this;
  }

  public WorkflowAttemptQueryBuilder order() {
    this.addOrder(new OrderCriterion(QueryOrder.ASC));
    return this;
  }

  public WorkflowAttemptQueryBuilder order(QueryOrder queryOrder) {
    this.addOrder(new OrderCriterion(queryOrder));
    return this;
  }

  public WorkflowAttemptQueryBuilder orderById() {
    this.addOrder(new OrderCriterion(QueryOrder.ASC));
    return this;
  }

  public WorkflowAttemptQueryBuilder orderById(QueryOrder queryOrder) {
    this.addOrder(new OrderCriterion(queryOrder));
    return this;
  }

  public WorkflowAttemptQueryBuilder workflowExecutionId(Integer value) {
    addWhereConstraint(new WhereConstraint<>(WorkflowAttempt._Fields.workflow_execution_id, JackMatchers.equalTo(value)));
    return this;
  }

  public WorkflowAttemptQueryBuilder whereWorkflowExecutionId(IWhereOperator<Integer> operator) {
    addWhereConstraint(new WhereConstraint<>(WorkflowAttempt._Fields.workflow_execution_id, operator));
    return this;
  }

  public WorkflowAttemptQueryBuilder orderByWorkflowExecutionId() {
    this.addOrder(new OrderCriterion(WorkflowAttempt._Fields.workflow_execution_id, QueryOrder.ASC));
    return this;
  }

  public WorkflowAttemptQueryBuilder orderByWorkflowExecutionId(QueryOrder queryOrder) {
    this.addOrder(new OrderCriterion(WorkflowAttempt._Fields.workflow_execution_id, queryOrder));
    return this;
  }

  public WorkflowAttemptQueryBuilder systemUser(String value) {
    addWhereConstraint(new WhereConstraint<>(WorkflowAttempt._Fields.system_user, JackMatchers.equalTo(value)));
    return this;
  }

  public WorkflowAttemptQueryBuilder whereSystemUser(IWhereOperator<String> operator) {
    addWhereConstraint(new WhereConstraint<>(WorkflowAttempt._Fields.system_user, operator));
    return this;
  }

  public WorkflowAttemptQueryBuilder orderBySystemUser() {
    this.addOrder(new OrderCriterion(WorkflowAttempt._Fields.system_user, QueryOrder.ASC));
    return this;
  }

  public WorkflowAttemptQueryBuilder orderBySystemUser(QueryOrder queryOrder) {
    this.addOrder(new OrderCriterion(WorkflowAttempt._Fields.system_user, queryOrder));
    return this;
  }

  public WorkflowAttemptQueryBuilder shutdownReason(String value) {
    addWhereConstraint(new WhereConstraint<>(WorkflowAttempt._Fields.shutdown_reason, JackMatchers.equalTo(value)));
    return this;
  }

  public WorkflowAttemptQueryBuilder whereShutdownReason(IWhereOperator<String> operator) {
    addWhereConstraint(new WhereConstraint<>(WorkflowAttempt._Fields.shutdown_reason, operator));
    return this;
  }

  public WorkflowAttemptQueryBuilder orderByShutdownReason() {
    this.addOrder(new OrderCriterion(WorkflowAttempt._Fields.shutdown_reason, QueryOrder.ASC));
    return this;
  }

  public WorkflowAttemptQueryBuilder orderByShutdownReason(QueryOrder queryOrder) {
    this.addOrder(new OrderCriterion(WorkflowAttempt._Fields.shutdown_reason, queryOrder));
    return this;
  }

  public WorkflowAttemptQueryBuilder priority(String value) {
    addWhereConstraint(new WhereConstraint<>(WorkflowAttempt._Fields.priority, JackMatchers.equalTo(value)));
    return this;
  }

  public WorkflowAttemptQueryBuilder wherePriority(IWhereOperator<String> operator) {
    addWhereConstraint(new WhereConstraint<>(WorkflowAttempt._Fields.priority, operator));
    return this;
  }

  public WorkflowAttemptQueryBuilder orderByPriority() {
    this.addOrder(new OrderCriterion(WorkflowAttempt._Fields.priority, QueryOrder.ASC));
    return this;
  }

  public WorkflowAttemptQueryBuilder orderByPriority(QueryOrder queryOrder) {
    this.addOrder(new OrderCriterion(WorkflowAttempt._Fields.priority, queryOrder));
    return this;
  }

  public WorkflowAttemptQueryBuilder pool(String value) {
    addWhereConstraint(new WhereConstraint<>(WorkflowAttempt._Fields.pool, JackMatchers.equalTo(value)));
    return this;
  }

  public WorkflowAttemptQueryBuilder wherePool(IWhereOperator<String> operator) {
    addWhereConstraint(new WhereConstraint<>(WorkflowAttempt._Fields.pool, operator));
    return this;
  }

  public WorkflowAttemptQueryBuilder orderByPool() {
    this.addOrder(new OrderCriterion(WorkflowAttempt._Fields.pool, QueryOrder.ASC));
    return this;
  }

  public WorkflowAttemptQueryBuilder orderByPool(QueryOrder queryOrder) {
    this.addOrder(new OrderCriterion(WorkflowAttempt._Fields.pool, queryOrder));
    return this;
  }

  public WorkflowAttemptQueryBuilder host(String value) {
    addWhereConstraint(new WhereConstraint<>(WorkflowAttempt._Fields.host, JackMatchers.equalTo(value)));
    return this;
  }

  public WorkflowAttemptQueryBuilder whereHost(IWhereOperator<String> operator) {
    addWhereConstraint(new WhereConstraint<>(WorkflowAttempt._Fields.host, operator));
    return this;
  }

  public WorkflowAttemptQueryBuilder orderByHost() {
    this.addOrder(new OrderCriterion(WorkflowAttempt._Fields.host, QueryOrder.ASC));
    return this;
  }

  public WorkflowAttemptQueryBuilder orderByHost(QueryOrder queryOrder) {
    this.addOrder(new OrderCriterion(WorkflowAttempt._Fields.host, queryOrder));
    return this;
  }

  public WorkflowAttemptQueryBuilder startTime(Long value) {
    addWhereConstraint(new WhereConstraint<>(WorkflowAttempt._Fields.start_time, JackMatchers.equalTo(value)));
    return this;
  }

  public WorkflowAttemptQueryBuilder whereStartTime(IWhereOperator<Long> operator) {
    addWhereConstraint(new WhereConstraint<>(WorkflowAttempt._Fields.start_time, operator));
    return this;
  }

  public WorkflowAttemptQueryBuilder orderByStartTime() {
    this.addOrder(new OrderCriterion(WorkflowAttempt._Fields.start_time, QueryOrder.ASC));
    return this;
  }

  public WorkflowAttemptQueryBuilder orderByStartTime(QueryOrder queryOrder) {
    this.addOrder(new OrderCriterion(WorkflowAttempt._Fields.start_time, queryOrder));
    return this;
  }

  public WorkflowAttemptQueryBuilder endTime(Long value) {
    addWhereConstraint(new WhereConstraint<>(WorkflowAttempt._Fields.end_time, JackMatchers.equalTo(value)));
    return this;
  }

  public WorkflowAttemptQueryBuilder whereEndTime(IWhereOperator<Long> operator) {
    addWhereConstraint(new WhereConstraint<>(WorkflowAttempt._Fields.end_time, operator));
    return this;
  }

  public WorkflowAttemptQueryBuilder orderByEndTime() {
    this.addOrder(new OrderCriterion(WorkflowAttempt._Fields.end_time, QueryOrder.ASC));
    return this;
  }

  public WorkflowAttemptQueryBuilder orderByEndTime(QueryOrder queryOrder) {
    this.addOrder(new OrderCriterion(WorkflowAttempt._Fields.end_time, queryOrder));
    return this;
  }

  public WorkflowAttemptQueryBuilder status(Integer value) {
    addWhereConstraint(new WhereConstraint<>(WorkflowAttempt._Fields.status, JackMatchers.equalTo(value)));
    return this;
  }

  public WorkflowAttemptQueryBuilder whereStatus(IWhereOperator<Integer> operator) {
    addWhereConstraint(new WhereConstraint<>(WorkflowAttempt._Fields.status, operator));
    return this;
  }

  public WorkflowAttemptQueryBuilder orderByStatus() {
    this.addOrder(new OrderCriterion(WorkflowAttempt._Fields.status, QueryOrder.ASC));
    return this;
  }

  public WorkflowAttemptQueryBuilder orderByStatus(QueryOrder queryOrder) {
    this.addOrder(new OrderCriterion(WorkflowAttempt._Fields.status, queryOrder));
    return this;
  }

  public WorkflowAttemptQueryBuilder lastHeartbeat(Long value) {
    addWhereConstraint(new WhereConstraint<>(WorkflowAttempt._Fields.last_heartbeat, JackMatchers.equalTo(value)));
    return this;
  }

  public WorkflowAttemptQueryBuilder whereLastHeartbeat(IWhereOperator<Long> operator) {
    addWhereConstraint(new WhereConstraint<>(WorkflowAttempt._Fields.last_heartbeat, operator));
    return this;
  }

  public WorkflowAttemptQueryBuilder orderByLastHeartbeat() {
    this.addOrder(new OrderCriterion(WorkflowAttempt._Fields.last_heartbeat, QueryOrder.ASC));
    return this;
  }

  public WorkflowAttemptQueryBuilder orderByLastHeartbeat(QueryOrder queryOrder) {
    this.addOrder(new OrderCriterion(WorkflowAttempt._Fields.last_heartbeat, queryOrder));
    return this;
  }

  public WorkflowAttemptQueryBuilder launchDir(String value) {
    addWhereConstraint(new WhereConstraint<>(WorkflowAttempt._Fields.launch_dir, JackMatchers.equalTo(value)));
    return this;
  }

  public WorkflowAttemptQueryBuilder whereLaunchDir(IWhereOperator<String> operator) {
    addWhereConstraint(new WhereConstraint<>(WorkflowAttempt._Fields.launch_dir, operator));
    return this;
  }

  public WorkflowAttemptQueryBuilder orderByLaunchDir() {
    this.addOrder(new OrderCriterion(WorkflowAttempt._Fields.launch_dir, QueryOrder.ASC));
    return this;
  }

  public WorkflowAttemptQueryBuilder orderByLaunchDir(QueryOrder queryOrder) {
    this.addOrder(new OrderCriterion(WorkflowAttempt._Fields.launch_dir, queryOrder));
    return this;
  }

  public WorkflowAttemptQueryBuilder launchJar(String value) {
    addWhereConstraint(new WhereConstraint<>(WorkflowAttempt._Fields.launch_jar, JackMatchers.equalTo(value)));
    return this;
  }

  public WorkflowAttemptQueryBuilder whereLaunchJar(IWhereOperator<String> operator) {
    addWhereConstraint(new WhereConstraint<>(WorkflowAttempt._Fields.launch_jar, operator));
    return this;
  }

  public WorkflowAttemptQueryBuilder orderByLaunchJar() {
    this.addOrder(new OrderCriterion(WorkflowAttempt._Fields.launch_jar, QueryOrder.ASC));
    return this;
  }

  public WorkflowAttemptQueryBuilder orderByLaunchJar(QueryOrder queryOrder) {
    this.addOrder(new OrderCriterion(WorkflowAttempt._Fields.launch_jar, queryOrder));
    return this;
  }

  public WorkflowAttemptQueryBuilder errorEmail(String value) {
    addWhereConstraint(new WhereConstraint<>(WorkflowAttempt._Fields.error_email, JackMatchers.equalTo(value)));
    return this;
  }

  public WorkflowAttemptQueryBuilder whereErrorEmail(IWhereOperator<String> operator) {
    addWhereConstraint(new WhereConstraint<>(WorkflowAttempt._Fields.error_email, operator));
    return this;
  }

  public WorkflowAttemptQueryBuilder orderByErrorEmail() {
    this.addOrder(new OrderCriterion(WorkflowAttempt._Fields.error_email, QueryOrder.ASC));
    return this;
  }

  public WorkflowAttemptQueryBuilder orderByErrorEmail(QueryOrder queryOrder) {
    this.addOrder(new OrderCriterion(WorkflowAttempt._Fields.error_email, queryOrder));
    return this;
  }

  public WorkflowAttemptQueryBuilder infoEmail(String value) {
    addWhereConstraint(new WhereConstraint<>(WorkflowAttempt._Fields.info_email, JackMatchers.equalTo(value)));
    return this;
  }

  public WorkflowAttemptQueryBuilder whereInfoEmail(IWhereOperator<String> operator) {
    addWhereConstraint(new WhereConstraint<>(WorkflowAttempt._Fields.info_email, operator));
    return this;
  }

  public WorkflowAttemptQueryBuilder orderByInfoEmail() {
    this.addOrder(new OrderCriterion(WorkflowAttempt._Fields.info_email, QueryOrder.ASC));
    return this;
  }

  public WorkflowAttemptQueryBuilder orderByInfoEmail(QueryOrder queryOrder) {
    this.addOrder(new OrderCriterion(WorkflowAttempt._Fields.info_email, queryOrder));
    return this;
  }

  public WorkflowAttemptQueryBuilder scmRemote(String value) {
    addWhereConstraint(new WhereConstraint<>(WorkflowAttempt._Fields.scm_remote, JackMatchers.equalTo(value)));
    return this;
  }

  public WorkflowAttemptQueryBuilder whereScmRemote(IWhereOperator<String> operator) {
    addWhereConstraint(new WhereConstraint<>(WorkflowAttempt._Fields.scm_remote, operator));
    return this;
  }

  public WorkflowAttemptQueryBuilder orderByScmRemote() {
    this.addOrder(new OrderCriterion(WorkflowAttempt._Fields.scm_remote, QueryOrder.ASC));
    return this;
  }

  public WorkflowAttemptQueryBuilder orderByScmRemote(QueryOrder queryOrder) {
    this.addOrder(new OrderCriterion(WorkflowAttempt._Fields.scm_remote, queryOrder));
    return this;
  }

  public WorkflowAttemptQueryBuilder commitRevision(String value) {
    addWhereConstraint(new WhereConstraint<>(WorkflowAttempt._Fields.commit_revision, JackMatchers.equalTo(value)));
    return this;
  }

  public WorkflowAttemptQueryBuilder whereCommitRevision(IWhereOperator<String> operator) {
    addWhereConstraint(new WhereConstraint<>(WorkflowAttempt._Fields.commit_revision, operator));
    return this;
  }

  public WorkflowAttemptQueryBuilder orderByCommitRevision() {
    this.addOrder(new OrderCriterion(WorkflowAttempt._Fields.commit_revision, QueryOrder.ASC));
    return this;
  }

  public WorkflowAttemptQueryBuilder orderByCommitRevision(QueryOrder queryOrder) {
    this.addOrder(new OrderCriterion(WorkflowAttempt._Fields.commit_revision, queryOrder));
    return this;
  }

  public WorkflowAttemptQueryBuilder description(String value) {
    addWhereConstraint(new WhereConstraint<>(WorkflowAttempt._Fields.description, JackMatchers.equalTo(value)));
    return this;
  }

  public WorkflowAttemptQueryBuilder whereDescription(IWhereOperator<String> operator) {
    addWhereConstraint(new WhereConstraint<>(WorkflowAttempt._Fields.description, operator));
    return this;
  }

  public WorkflowAttemptQueryBuilder orderByDescription() {
    this.addOrder(new OrderCriterion(WorkflowAttempt._Fields.description, QueryOrder.ASC));
    return this;
  }

  public WorkflowAttemptQueryBuilder orderByDescription(QueryOrder queryOrder) {
    this.addOrder(new OrderCriterion(WorkflowAttempt._Fields.description, queryOrder));
    return this;
  }
}
