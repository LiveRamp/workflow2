
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
import com.liveramp.databases.workflow_db.iface.IMapreduceJobTaskExceptionPersistence;
import com.liveramp.databases.workflow_db.models.MapreduceJobTaskException;
import com.liveramp.databases.workflow_db.query.MapreduceJobTaskExceptionQueryBuilder;
import com.liveramp.databases.workflow_db.query.MapreduceJobTaskExceptionDeleteBuilder;

import com.liveramp.databases.workflow_db.IDatabases;

public class BaseMapreduceJobTaskExceptionPersistenceImpl extends AbstractDatabaseModel<MapreduceJobTaskException> implements IMapreduceJobTaskExceptionPersistence {
  private final IDatabases databases;

  public BaseMapreduceJobTaskExceptionPersistenceImpl(BaseDatabaseConnection conn, IDatabases databases) {
    super(conn, "mapreduce_job_task_exceptions", Arrays.<String>asList("mapreduce_job_id", "task_attempt_id", "exception", "host_url"));
    this.databases = databases;
  }

  @Override
  public MapreduceJobTaskException create(Map<Enum, Object> fieldsMap) throws IOException {
    Integer mapreduce_job_id = (Integer) fieldsMap.get(MapreduceJobTaskException._Fields.mapreduce_job_id);
    String task_attempt_id = (String) fieldsMap.get(MapreduceJobTaskException._Fields.task_attempt_id);
    String exception = (String) fieldsMap.get(MapreduceJobTaskException._Fields.exception);
    String host_url = (String) fieldsMap.get(MapreduceJobTaskException._Fields.host_url);
    return create(mapreduce_job_id, task_attempt_id, exception, host_url);
  }

  public MapreduceJobTaskException create(final Integer mapreduce_job_id, final String task_attempt_id, final String exception, final String host_url) throws IOException {
    StatementCreator statementCreator = new StatementCreator() {
      private final List<String> nonNullFields = new ArrayList<>();
      private final List<AttrSetter> statementSetters = new ArrayList<>();

      {
        int index = 1;

        if (mapreduce_job_id != null) {
          nonNullFields.add("mapreduce_job_id");
          int fieldIndex0 = index++;
          statementSetters.add(stmt -> stmt.setInt(fieldIndex0, mapreduce_job_id));
        }

        if (task_attempt_id != null) {
          nonNullFields.add("task_attempt_id");
          int fieldIndex1 = index++;
          statementSetters.add(stmt -> stmt.setString(fieldIndex1, task_attempt_id));
        }

        if (exception != null) {
          nonNullFields.add("exception");
          int fieldIndex2 = index++;
          statementSetters.add(stmt -> stmt.setString(fieldIndex2, exception));
        }

        if (host_url != null) {
          nonNullFields.add("host_url");
          int fieldIndex3 = index++;
          statementSetters.add(stmt -> stmt.setString(fieldIndex3, host_url));
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
    MapreduceJobTaskException newInst = new MapreduceJobTaskException(__id, mapreduce_job_id, task_attempt_id, exception, host_url, databases);
    newInst.setCreated(true);
    cachedById.put(__id, newInst);
    clearForeignKeyCache();
    return newInst;
  }

  public MapreduceJobTaskException create() throws IOException {
    StatementCreator statementCreator = new StatementCreator() {
      private final List<String> nonNullFields = new ArrayList<>();
      private final List<AttrSetter> statementSetters = new ArrayList<>();

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
    MapreduceJobTaskException newInst = new MapreduceJobTaskException(__id, null, null, null, null, databases);
    newInst.setCreated(true);
    cachedById.put(__id, newInst);
    clearForeignKeyCache();
    return newInst;
  }

  public MapreduceJobTaskException createDefaultInstance() throws IOException {
    return create();
  }

  public List<MapreduceJobTaskException> find(Map<Enum, Object> fieldsMap) throws IOException {
    return find(null, fieldsMap);
  }

  public List<MapreduceJobTaskException> find(Collection<Long> ids, Map<Enum, Object> fieldsMap) throws IOException {
    List<MapreduceJobTaskException> foundList = new ArrayList<>();

    if (fieldsMap == null || fieldsMap.isEmpty()) {
      return foundList;
    }

    StringBuilder statementString = new StringBuilder();
    statementString.append("SELECT * FROM mapreduce_job_task_exceptions WHERE (");
    List<Object> nonNullValues = new ArrayList<>();
    List<MapreduceJobTaskException._Fields> nonNullValueFields = new ArrayList<>();

    Iterator<Map.Entry<Enum, Object>> iter = fieldsMap.entrySet().iterator();
    while (iter.hasNext()) {
      Map.Entry<Enum, Object> entry = iter.next();
      Enum field = entry.getKey();
      Object value = entry.getValue();

      String queryValue = value != null ? " = ? " : " IS NULL";
      if (value != null) {
        nonNullValueFields.add((MapreduceJobTaskException._Fields) field);
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
        MapreduceJobTaskException._Fields field = nonNullValueFields.get(i);
        try {
          switch (field) {
            case mapreduce_job_id:
              preparedStatement.setInt(i+1, (Integer) nonNullValues.get(i));
              break;
            case task_attempt_id:
              preparedStatement.setString(i+1, (String) nonNullValues.get(i));
              break;
            case exception:
              preparedStatement.setString(i+1, (String) nonNullValues.get(i));
              break;
            case host_url:
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
            MapreduceJobTaskException._Fields field = (MapreduceJobTaskException._Fields)constraint.getField();
            switch (field) {
              case mapreduce_job_id:
                preparedStatement.setInt(++index, (Integer) parameter);
                break;
              case task_attempt_id:
                preparedStatement.setString(++index, (String) parameter);
                break;
              case exception:
                preparedStatement.setString(++index, (String) parameter);
                break;
              case host_url:
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
  protected void setAttrs(MapreduceJobTaskException model, PreparedStatement stmt, boolean setNull) throws SQLException {
    int index = 1;
    if (setNull && model.getMapreduceJobId() == null) {
      stmt.setNull(index++, java.sql.Types.INTEGER);
    } else if (model.getMapreduceJobId() != null) {
      stmt.setInt(index++, model.getMapreduceJobId());
    }
    if (setNull && model.getTaskAttemptId() == null) {
      stmt.setNull(index++, java.sql.Types.CHAR);
    } else if (model.getTaskAttemptId() != null) {
      stmt.setString(index++, model.getTaskAttemptId());
    }
    if (setNull && model.getException() == null) {
      stmt.setNull(index++, java.sql.Types.CHAR);
    } else if (model.getException() != null) {
      stmt.setString(index++, model.getException());
    }
    if (setNull && model.getHostUrl() == null) {
      stmt.setNull(index++, java.sql.Types.CHAR);
    } else if (model.getHostUrl() != null) {
      stmt.setString(index++, model.getHostUrl());
    }
    stmt.setLong(index, model.getId());
  }

  @Override
  protected MapreduceJobTaskException instanceFromResultSet(ResultSet rs, Collection<Enum> selectedFields) throws SQLException {
    boolean allFields = selectedFields == null || selectedFields.isEmpty();
    long id = rs.getLong("id");
    return new MapreduceJobTaskException(id,
      allFields || selectedFields.contains(MapreduceJobTaskException._Fields.mapreduce_job_id) ? getIntOrNull(rs, "mapreduce_job_id") : null,
      allFields || selectedFields.contains(MapreduceJobTaskException._Fields.task_attempt_id) ? rs.getString("task_attempt_id") : null,
      allFields || selectedFields.contains(MapreduceJobTaskException._Fields.exception) ? rs.getString("exception") : null,
      allFields || selectedFields.contains(MapreduceJobTaskException._Fields.host_url) ? rs.getString("host_url") : null,
      databases
    );
  }

  public List<MapreduceJobTaskException> findByMapreduceJobId(final Integer value) throws IOException {
    return find(Collections.<Enum, Object>singletonMap(MapreduceJobTaskException._Fields.mapreduce_job_id, value));
  }

  public List<MapreduceJobTaskException> findByTaskAttemptId(final String value) throws IOException {
    return find(Collections.<Enum, Object>singletonMap(MapreduceJobTaskException._Fields.task_attempt_id, value));
  }

  public List<MapreduceJobTaskException> findByException(final String value) throws IOException {
    return find(Collections.<Enum, Object>singletonMap(MapreduceJobTaskException._Fields.exception, value));
  }

  public List<MapreduceJobTaskException> findByHostUrl(final String value) throws IOException {
    return find(Collections.<Enum, Object>singletonMap(MapreduceJobTaskException._Fields.host_url, value));
  }

  public MapreduceJobTaskExceptionQueryBuilder query() {
    return new MapreduceJobTaskExceptionQueryBuilder(this);
  }

  public MapreduceJobTaskExceptionDeleteBuilder delete() {
    return new MapreduceJobTaskExceptionDeleteBuilder(this);
  }
}
