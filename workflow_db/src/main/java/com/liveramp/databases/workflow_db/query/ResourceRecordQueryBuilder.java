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
import com.liveramp.databases.workflow_db.iface.IResourceRecordPersistence;
import com.liveramp.databases.workflow_db.models.ResourceRecord;


public class ResourceRecordQueryBuilder extends AbstractQueryBuilder<ResourceRecord> {

  public ResourceRecordQueryBuilder(IResourceRecordPersistence caller) {
    super(caller);
  }

  public ResourceRecordQueryBuilder select(ResourceRecord._Fields... fields) {
    for (ResourceRecord._Fields field : fields){
      addSelectedField(new FieldSelector(field));
    }
    return this;
  }

  public ResourceRecordQueryBuilder selectAgg(FieldSelector... aggregatedFields) {
    addSelectedFields(aggregatedFields);
    return this;
  }

  public ResourceRecordQueryBuilder id(Long value) {
    addId(value);
    return this;
  }

  public ResourceRecordQueryBuilder idIn(Collection<Long> values) {
    addIds(values);
    return this;
  }

  public ResourceRecordQueryBuilder whereId(IWhereOperator<Long> operator) {
    addWhereConstraint(new WhereConstraint<>(Column.fromId(null), operator, null));
    return this;
  }

  public ResourceRecordQueryBuilder limit(int offset, int nResults) {
    setLimit(new LimitCriterion(offset, nResults));
    return this;
  }

  public ResourceRecordQueryBuilder limit(int nResults) {
    setLimit(new LimitCriterion(nResults));
    return this;
  }

  public ResourceRecordQueryBuilder groupBy(ResourceRecord._Fields... fields) {
    addGroupByFields(fields);
    return this;
  }

  public ResourceRecordQueryBuilder order() {
    this.addOrder(new OrderCriterion(QueryOrder.ASC));
    return this;
  }

  public ResourceRecordQueryBuilder order(QueryOrder queryOrder) {
    this.addOrder(new OrderCriterion(queryOrder));
    return this;
  }

  public ResourceRecordQueryBuilder orderById() {
    this.addOrder(new OrderCriterion(QueryOrder.ASC));
    return this;
  }

  public ResourceRecordQueryBuilder orderById(QueryOrder queryOrder) {
    this.addOrder(new OrderCriterion(queryOrder));
    return this;
  }

  public ResourceRecordQueryBuilder name(String value) {
    addWhereConstraint(new WhereConstraint<>(ResourceRecord._Fields.name, JackMatchers.equalTo(value)));
    return this;
  }

  public ResourceRecordQueryBuilder whereName(IWhereOperator<String> operator) {
    addWhereConstraint(new WhereConstraint<>(ResourceRecord._Fields.name, operator));
    return this;
  }

  public ResourceRecordQueryBuilder orderByName() {
    this.addOrder(new OrderCriterion(ResourceRecord._Fields.name, QueryOrder.ASC));
    return this;
  }

  public ResourceRecordQueryBuilder orderByName(QueryOrder queryOrder) {
    this.addOrder(new OrderCriterion(ResourceRecord._Fields.name, queryOrder));
    return this;
  }

  public ResourceRecordQueryBuilder resourceRootId(Integer value) {
    addWhereConstraint(new WhereConstraint<>(ResourceRecord._Fields.resource_root_id, JackMatchers.equalTo(value)));
    return this;
  }

  public ResourceRecordQueryBuilder whereResourceRootId(IWhereOperator<Integer> operator) {
    addWhereConstraint(new WhereConstraint<>(ResourceRecord._Fields.resource_root_id, operator));
    return this;
  }

  public ResourceRecordQueryBuilder orderByResourceRootId() {
    this.addOrder(new OrderCriterion(ResourceRecord._Fields.resource_root_id, QueryOrder.ASC));
    return this;
  }

  public ResourceRecordQueryBuilder orderByResourceRootId(QueryOrder queryOrder) {
    this.addOrder(new OrderCriterion(ResourceRecord._Fields.resource_root_id, queryOrder));
    return this;
  }

  public ResourceRecordQueryBuilder json(String value) {
    addWhereConstraint(new WhereConstraint<>(ResourceRecord._Fields.json, JackMatchers.equalTo(value)));
    return this;
  }

  public ResourceRecordQueryBuilder whereJson(IWhereOperator<String> operator) {
    addWhereConstraint(new WhereConstraint<>(ResourceRecord._Fields.json, operator));
    return this;
  }

  public ResourceRecordQueryBuilder orderByJson() {
    this.addOrder(new OrderCriterion(ResourceRecord._Fields.json, QueryOrder.ASC));
    return this;
  }

  public ResourceRecordQueryBuilder orderByJson(QueryOrder queryOrder) {
    this.addOrder(new OrderCriterion(ResourceRecord._Fields.json, queryOrder));
    return this;
  }

  public ResourceRecordQueryBuilder createdAt(Long value) {
    addWhereConstraint(new WhereConstraint<>(ResourceRecord._Fields.created_at, JackMatchers.equalTo(value)));
    return this;
  }

  public ResourceRecordQueryBuilder whereCreatedAt(IWhereOperator<Long> operator) {
    addWhereConstraint(new WhereConstraint<>(ResourceRecord._Fields.created_at, operator));
    return this;
  }

  public ResourceRecordQueryBuilder orderByCreatedAt() {
    this.addOrder(new OrderCriterion(ResourceRecord._Fields.created_at, QueryOrder.ASC));
    return this;
  }

  public ResourceRecordQueryBuilder orderByCreatedAt(QueryOrder queryOrder) {
    this.addOrder(new OrderCriterion(ResourceRecord._Fields.created_at, queryOrder));
    return this;
  }

  public ResourceRecordQueryBuilder classPath(String value) {
    addWhereConstraint(new WhereConstraint<>(ResourceRecord._Fields.class_path, JackMatchers.equalTo(value)));
    return this;
  }

  public ResourceRecordQueryBuilder whereClassPath(IWhereOperator<String> operator) {
    addWhereConstraint(new WhereConstraint<>(ResourceRecord._Fields.class_path, operator));
    return this;
  }

  public ResourceRecordQueryBuilder orderByClassPath() {
    this.addOrder(new OrderCriterion(ResourceRecord._Fields.class_path, QueryOrder.ASC));
    return this;
  }

  public ResourceRecordQueryBuilder orderByClassPath(QueryOrder queryOrder) {
    this.addOrder(new OrderCriterion(ResourceRecord._Fields.class_path, queryOrder));
    return this;
  }
}
