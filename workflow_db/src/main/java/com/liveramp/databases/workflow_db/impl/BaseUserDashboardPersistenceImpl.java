
/**
 * Autogenerated by Jack
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 */
package com.liveramp.databases.workflow_db.impl;

import java.sql.SQLRecoverableException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.sql.Timestamp;

import com.rapleaf.jack.AbstractDatabaseModel;
import com.rapleaf.jack.BaseDatabaseConnection;
import com.rapleaf.jack.queries.WhereConstraint;
import com.rapleaf.jack.queries.WhereClause;
import com.rapleaf.jack.util.JackUtility;
import com.liveramp.databases.workflow_db.iface.IUserDashboardPersistence;
import com.liveramp.databases.workflow_db.models.UserDashboard;
import com.liveramp.databases.workflow_db.query.UserDashboardQueryBuilder;
import com.liveramp.databases.workflow_db.query.UserDashboardDeleteBuilder;

import com.liveramp.databases.workflow_db.IDatabases;

public class BaseUserDashboardPersistenceImpl extends AbstractDatabaseModel<UserDashboard> implements IUserDashboardPersistence {
  private final IDatabases databases;

  public BaseUserDashboardPersistenceImpl(BaseDatabaseConnection conn, IDatabases databases) {
    super(conn, "user_dashboards", Arrays.<String>asList("user_id", "dashboard_id"));
    this.databases = databases;
  }

  @Override
  public UserDashboard create(Map<Enum, Object> fieldsMap) throws IOException {
    int user_id = (Integer) fieldsMap.get(UserDashboard._Fields.user_id);
    int dashboard_id = (Integer) fieldsMap.get(UserDashboard._Fields.dashboard_id);
    return create(user_id, dashboard_id);
  }

  public UserDashboard create(final int user_id, final int dashboard_id) throws IOException {
    StatementCreator statementCreator = new StatementCreator() {
      private final List<String> nonNullFields = new ArrayList<>();
      private final List<AttrSetter> statementSetters = new ArrayList<>();

      {
        int index = 1;

        nonNullFields.add("user_id");
        int fieldIndex0 = index++;
        statementSetters.add(stmt -> stmt.setInt(fieldIndex0, user_id));

        nonNullFields.add("dashboard_id");
        int fieldIndex1 = index++;
        statementSetters.add(stmt -> stmt.setInt(fieldIndex1, dashboard_id));
      }

      @Override
      public String getStatement() {
        return getInsertStatement(nonNullFields);
      }

      @Override
      public void setStatement(PreparedStatement statement) throws SQLException {
        for (AttrSetter setter : statementSetters) {
          setter.set(statement);
        }
      }
    };

    long __id = realCreate(statementCreator);
    UserDashboard newInst = new UserDashboard(__id, user_id, dashboard_id, databases);
    newInst.setCreated(true);
    cachedById.put(__id, newInst);
    clearForeignKeyCache();
    return newInst;
  }

  public UserDashboard createDefaultInstance() throws IOException {
    return create(0, 0);
  }

  public List<UserDashboard> find(Map<Enum, Object> fieldsMap) throws IOException {
    return find(null, fieldsMap);
  }

  public List<UserDashboard> find(Collection<Long> ids, Map<Enum, Object> fieldsMap) throws IOException {
    List<UserDashboard> foundList = new ArrayList<>();

    if (fieldsMap == null || fieldsMap.isEmpty()) {
      return foundList;
    }

    StringBuilder statementString = new StringBuilder();
    statementString.append("SELECT * FROM user_dashboards WHERE (");
    List<Object> nonNullValues = new ArrayList<>();
    List<UserDashboard._Fields> nonNullValueFields = new ArrayList<>();

    Iterator<Map.Entry<Enum, Object>> iter = fieldsMap.entrySet().iterator();
    while (iter.hasNext()) {
      Map.Entry<Enum, Object> entry = iter.next();
      Enum field = entry.getKey();
      Object value = entry.getValue();

      String queryValue = value != null ? " = ? " : " IS NULL";
      if (value != null) {
        nonNullValueFields.add((UserDashboard._Fields) field);
        nonNullValues.add(value);
      }

      statementString.append(field).append(queryValue);
      if (iter.hasNext()) {
        statementString.append(" AND ");
      }
    }
    if (ids != null) statementString.append(" AND ").append(getIdSetCondition(ids));
    statementString.append(")");

    int retryCount = 0;
    PreparedStatement preparedStatement;

    while (true) {
      preparedStatement = getPreparedStatement(statementString.toString());

      for (int i = 0; i < nonNullValues.size(); i++) {
        UserDashboard._Fields field = nonNullValueFields.get(i);
        try {
          switch (field) {
            case user_id:
              preparedStatement.setInt(i+1, (Integer) nonNullValues.get(i));
              break;
            case dashboard_id:
              preparedStatement.setInt(i+1, (Integer) nonNullValues.get(i));
              break;
          }
        } catch (SQLException e) {
          throw new IOException(e);
        }
      }

      try {
        executeQuery(foundList, preparedStatement);
        return foundList;
      } catch (SQLRecoverableException e) {
        if (++retryCount > AbstractDatabaseModel.MAX_CONNECTION_RETRIES) {
          throw new IOException(e);
        }
      } catch (SQLException e) {
        throw new IOException(e);
      }
    }
  }

  @Override
  protected void setStatementParameters(PreparedStatement preparedStatement, WhereClause whereClause) throws IOException {
    int index = 0;
    for (WhereConstraint constraint : whereClause.getWhereConstraints()) {
      for (Object parameter : constraint.getParameters()) {
        if (parameter == null) {
          continue;
        }
        try {
          if (constraint.isId()) {
            preparedStatement.setLong(++index, (Long)parameter);
          } else {
            UserDashboard._Fields field = (UserDashboard._Fields)constraint.getField();
            switch (field) {
              case user_id:
                preparedStatement.setInt(++index, (Integer) parameter);
                break;
              case dashboard_id:
                preparedStatement.setInt(++index, (Integer) parameter);
                break;
            }
          }
        } catch (SQLException e) {
          throw new IOException(e);
        }
      }
    }
  }

  @Override
  protected void setAttrs(UserDashboard model, PreparedStatement stmt, boolean setNull) throws SQLException {
    int index = 1;
    {
      stmt.setInt(index++, model.getUserId());
    }
    {
      stmt.setInt(index++, model.getDashboardId());
    }
    stmt.setLong(index, model.getId());
  }

  @Override
  protected UserDashboard instanceFromResultSet(ResultSet rs, Collection<Enum> selectedFields) throws SQLException {
    boolean allFields = selectedFields == null || selectedFields.isEmpty();
    long id = rs.getLong("id");
    return new UserDashboard(id,
      allFields || selectedFields.contains(UserDashboard._Fields.user_id) ? getIntOrNull(rs, "user_id") : 0,
      allFields || selectedFields.contains(UserDashboard._Fields.dashboard_id) ? getIntOrNull(rs, "dashboard_id") : 0,
      databases
    );
  }

  public List<UserDashboard> findByUserId(final int value) throws IOException {
    return find(Collections.<Enum, Object>singletonMap(UserDashboard._Fields.user_id, value));
  }

  public List<UserDashboard> findByDashboardId(final int value) throws IOException {
    return find(Collections.<Enum, Object>singletonMap(UserDashboard._Fields.dashboard_id, value));
  }

  public UserDashboardQueryBuilder query() {
    return new UserDashboardQueryBuilder(this);
  }

  public UserDashboardDeleteBuilder delete() {
    return new UserDashboardDeleteBuilder(this);
  }
}
