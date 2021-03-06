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
import com.liveramp.databases.workflow_db.iface.IDashboardApplicationPersistence;
import com.liveramp.databases.workflow_db.models.DashboardApplication;


public class DashboardApplicationQueryBuilder extends AbstractQueryBuilder<DashboardApplication> {

  public DashboardApplicationQueryBuilder(IDashboardApplicationPersistence caller) {
    super(caller);
  }

  public DashboardApplicationQueryBuilder select(DashboardApplication._Fields... fields) {
    for (DashboardApplication._Fields field : fields){
      addSelectedField(new FieldSelector(field));
    }
    return this;
  }

  public DashboardApplicationQueryBuilder selectAgg(FieldSelector... aggregatedFields) {
    addSelectedFields(aggregatedFields);
    return this;
  }

  public DashboardApplicationQueryBuilder id(Long value) {
    addId(value);
    return this;
  }

  public DashboardApplicationQueryBuilder idIn(Collection<Long> values) {
    addIds(values);
    return this;
  }

  public DashboardApplicationQueryBuilder whereId(IWhereOperator<Long> operator) {
    addWhereConstraint(new WhereConstraint<>(Column.fromId(null), operator, null));
    return this;
  }

  public DashboardApplicationQueryBuilder limit(int offset, int nResults) {
    setLimit(new LimitCriterion(offset, nResults));
    return this;
  }

  public DashboardApplicationQueryBuilder limit(int nResults) {
    setLimit(new LimitCriterion(nResults));
    return this;
  }

  public DashboardApplicationQueryBuilder groupBy(DashboardApplication._Fields... fields) {
    addGroupByFields(fields);
    return this;
  }

  public DashboardApplicationQueryBuilder order() {
    this.addOrder(new OrderCriterion(QueryOrder.ASC));
    return this;
  }

  public DashboardApplicationQueryBuilder order(QueryOrder queryOrder) {
    this.addOrder(new OrderCriterion(queryOrder));
    return this;
  }

  public DashboardApplicationQueryBuilder orderById() {
    this.addOrder(new OrderCriterion(QueryOrder.ASC));
    return this;
  }

  public DashboardApplicationQueryBuilder orderById(QueryOrder queryOrder) {
    this.addOrder(new OrderCriterion(queryOrder));
    return this;
  }

  public DashboardApplicationQueryBuilder dashboardId(Integer value) {
    addWhereConstraint(new WhereConstraint<>(DashboardApplication._Fields.dashboard_id, JackMatchers.equalTo(value)));
    return this;
  }

  public DashboardApplicationQueryBuilder whereDashboardId(IWhereOperator<Integer> operator) {
    addWhereConstraint(new WhereConstraint<>(DashboardApplication._Fields.dashboard_id, operator));
    return this;
  }

  public DashboardApplicationQueryBuilder orderByDashboardId() {
    this.addOrder(new OrderCriterion(DashboardApplication._Fields.dashboard_id, QueryOrder.ASC));
    return this;
  }

  public DashboardApplicationQueryBuilder orderByDashboardId(QueryOrder queryOrder) {
    this.addOrder(new OrderCriterion(DashboardApplication._Fields.dashboard_id, queryOrder));
    return this;
  }

  public DashboardApplicationQueryBuilder applicationId(Integer value) {
    addWhereConstraint(new WhereConstraint<>(DashboardApplication._Fields.application_id, JackMatchers.equalTo(value)));
    return this;
  }

  public DashboardApplicationQueryBuilder whereApplicationId(IWhereOperator<Integer> operator) {
    addWhereConstraint(new WhereConstraint<>(DashboardApplication._Fields.application_id, operator));
    return this;
  }

  public DashboardApplicationQueryBuilder orderByApplicationId() {
    this.addOrder(new OrderCriterion(DashboardApplication._Fields.application_id, QueryOrder.ASC));
    return this;
  }

  public DashboardApplicationQueryBuilder orderByApplicationId(QueryOrder queryOrder) {
    this.addOrder(new OrderCriterion(DashboardApplication._Fields.application_id, queryOrder));
    return this;
  }
}
