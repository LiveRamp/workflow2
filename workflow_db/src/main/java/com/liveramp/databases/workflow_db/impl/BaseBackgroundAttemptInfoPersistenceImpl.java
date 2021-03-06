
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
import com.liveramp.databases.workflow_db.iface.IBackgroundAttemptInfoPersistence;
import com.liveramp.databases.workflow_db.models.BackgroundAttemptInfo;
import com.liveramp.databases.workflow_db.query.BackgroundAttemptInfoQueryBuilder;
import com.liveramp.databases.workflow_db.query.BackgroundAttemptInfoDeleteBuilder;

import com.liveramp.databases.workflow_db.IDatabases;

public class BaseBackgroundAttemptInfoPersistenceImpl extends AbstractDatabaseModel<BackgroundAttemptInfo> implements IBackgroundAttemptInfoPersistence {
  private final IDatabases databases;

  public BaseBackgroundAttemptInfoPersistenceImpl(BaseDatabaseConnection conn, IDatabases databases) {
    super(conn, "background_attempt_infos", Arrays.<String>asList("workflow_attempt_id", "resource_manager_factory", "resource_manager_version_class"));
    this.databases = databases;
  }

  @Override
  public BackgroundAttemptInfo create(Map<Enum, Object> fieldsMap) throws IOException {
    long workflow_attempt_id = (Long) fieldsMap.get(BackgroundAttemptInfo._Fields.workflow_attempt_id);
    String resource_manager_factory = (String) fieldsMap.get(BackgroundAttemptInfo._Fields.resource_manager_factory);
    String resource_manager_version_class = (String) fieldsMap.get(BackgroundAttemptInfo._Fields.resource_manager_version_class);
    return create(workflow_attempt_id, resource_manager_factory, resource_manager_version_class);
  }

  public BackgroundAttemptInfo create(final long workflow_attempt_id, final String resource_manager_factory, final String resource_manager_version_class) throws IOException {
    StatementCreator statementCreator = new StatementCreator() {
      private final List<String> nonNullFields = new ArrayList<>();
      private final List<AttrSetter> statementSetters = new ArrayList<>();

      {
        int index = 1;

        nonNullFields.add("workflow_attempt_id");
        int fieldIndex0 = index++;
        statementSetters.add(stmt -> stmt.setLong(fieldIndex0, workflow_attempt_id));

        if (resource_manager_factory != null) {
          nonNullFields.add("resource_manager_factory");
          int fieldIndex1 = index++;
          statementSetters.add(stmt -> stmt.setString(fieldIndex1, resource_manager_factory));
        }

        if (resource_manager_version_class != null) {
          nonNullFields.add("resource_manager_version_class");
          int fieldIndex2 = index++;
          statementSetters.add(stmt -> stmt.setString(fieldIndex2, resource_manager_version_class));
        }
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
    BackgroundAttemptInfo newInst = new BackgroundAttemptInfo(__id, workflow_attempt_id, resource_manager_factory, resource_manager_version_class, databases);
    newInst.setCreated(true);
    cachedById.put(__id, newInst);
    clearForeignKeyCache();
    return newInst;
  }

  public BackgroundAttemptInfo create(final long workflow_attempt_id) throws IOException {
    StatementCreator statementCreator = new StatementCreator() {
      private final List<String> nonNullFields = new ArrayList<>();
      private final List<AttrSetter> statementSetters = new ArrayList<>();

      {
        int index = 1;

        nonNullFields.add("workflow_attempt_id");
        int fieldIndex0 = index++;
        statementSetters.add(stmt -> stmt.setLong(fieldIndex0, workflow_attempt_id));
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
    BackgroundAttemptInfo newInst = new BackgroundAttemptInfo(__id, workflow_attempt_id, null, null, databases);
    newInst.setCreated(true);
    cachedById.put(__id, newInst);
    clearForeignKeyCache();
    return newInst;
  }

  public BackgroundAttemptInfo createDefaultInstance() throws IOException {
    return create(0L);
  }

  public List<BackgroundAttemptInfo> find(Map<Enum, Object> fieldsMap) throws IOException {
    return find(null, fieldsMap);
  }

  public List<BackgroundAttemptInfo> find(Collection<Long> ids, Map<Enum, Object> fieldsMap) throws IOException {
    List<BackgroundAttemptInfo> foundList = new ArrayList<>();

    if (fieldsMap == null || fieldsMap.isEmpty()) {
      return foundList;
    }

    StringBuilder statementString = new StringBuilder();
    statementString.append("SELECT * FROM background_attempt_infos WHERE (");
    List<Object> nonNullValues = new ArrayList<>();
    List<BackgroundAttemptInfo._Fields> nonNullValueFields = new ArrayList<>();

    Iterator<Map.Entry<Enum, Object>> iter = fieldsMap.entrySet().iterator();
    while (iter.hasNext()) {
      Map.Entry<Enum, Object> entry = iter.next();
      Enum field = entry.getKey();
      Object value = entry.getValue();

      String queryValue = value != null ? " = ? " : " IS NULL";
      if (value != null) {
        nonNullValueFields.add((BackgroundAttemptInfo._Fields) field);
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
        BackgroundAttemptInfo._Fields field = nonNullValueFields.get(i);
        try {
          switch (field) {
            case workflow_attempt_id:
              preparedStatement.setLong(i+1, (Long) nonNullValues.get(i));
              break;
            case resource_manager_factory:
              preparedStatement.setString(i+1, (String) nonNullValues.get(i));
              break;
            case resource_manager_version_class:
              preparedStatement.setString(i+1, (String) nonNullValues.get(i));
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
            BackgroundAttemptInfo._Fields field = (BackgroundAttemptInfo._Fields)constraint.getField();
            switch (field) {
              case workflow_attempt_id:
                preparedStatement.setLong(++index, (Long) parameter);
                break;
              case resource_manager_factory:
                preparedStatement.setString(++index, (String) parameter);
                break;
              case resource_manager_version_class:
                preparedStatement.setString(++index, (String) parameter);
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
  protected void setAttrs(BackgroundAttemptInfo model, PreparedStatement stmt, boolean setNull) throws SQLException {
    int index = 1;
    {
      stmt.setLong(index++, model.getWorkflowAttemptId());
    }
    if (setNull && model.getResourceManagerFactory() == null) {
      stmt.setNull(index++, java.sql.Types.CHAR);
    } else if (model.getResourceManagerFactory() != null) {
      stmt.setString(index++, model.getResourceManagerFactory());
    }
    if (setNull && model.getResourceManagerVersionClass() == null) {
      stmt.setNull(index++, java.sql.Types.CHAR);
    } else if (model.getResourceManagerVersionClass() != null) {
      stmt.setString(index++, model.getResourceManagerVersionClass());
    }
    stmt.setLong(index, model.getId());
  }

  @Override
  protected BackgroundAttemptInfo instanceFromResultSet(ResultSet rs, Collection<Enum> selectedFields) throws SQLException {
    boolean allFields = selectedFields == null || selectedFields.isEmpty();
    long id = rs.getLong("id");
    return new BackgroundAttemptInfo(id,
      allFields || selectedFields.contains(BackgroundAttemptInfo._Fields.workflow_attempt_id) ? getLongOrNull(rs, "workflow_attempt_id") : 0L,
      allFields || selectedFields.contains(BackgroundAttemptInfo._Fields.resource_manager_factory) ? rs.getString("resource_manager_factory") : null,
      allFields || selectedFields.contains(BackgroundAttemptInfo._Fields.resource_manager_version_class) ? rs.getString("resource_manager_version_class") : null,
      databases
    );
  }

  public List<BackgroundAttemptInfo> findByWorkflowAttemptId(final long value) throws IOException {
    return find(Collections.<Enum, Object>singletonMap(BackgroundAttemptInfo._Fields.workflow_attempt_id, value));
  }

  public List<BackgroundAttemptInfo> findByResourceManagerFactory(final String value) throws IOException {
    return find(Collections.<Enum, Object>singletonMap(BackgroundAttemptInfo._Fields.resource_manager_factory, value));
  }

  public List<BackgroundAttemptInfo> findByResourceManagerVersionClass(final String value) throws IOException {
    return find(Collections.<Enum, Object>singletonMap(BackgroundAttemptInfo._Fields.resource_manager_version_class, value));
  }

  public BackgroundAttemptInfoQueryBuilder query() {
    return new BackgroundAttemptInfoQueryBuilder(this);
  }

  public BackgroundAttemptInfoDeleteBuilder delete() {
    return new BackgroundAttemptInfoDeleteBuilder(this);
  }
}
