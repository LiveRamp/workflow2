
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
import com.liveramp.databases.workflow_db.iface.IWorkflowAttemptPersistence;
import com.liveramp.databases.workflow_db.models.WorkflowAttempt;
import com.liveramp.databases.workflow_db.query.WorkflowAttemptQueryBuilder;
import com.liveramp.databases.workflow_db.query.WorkflowAttemptDeleteBuilder;

import com.liveramp.databases.workflow_db.IDatabases;

public class BaseWorkflowAttemptPersistenceImpl extends AbstractDatabaseModel<WorkflowAttempt> implements IWorkflowAttemptPersistence {
  private final IDatabases databases;

  public BaseWorkflowAttemptPersistenceImpl(BaseDatabaseConnection conn, IDatabases databases) {
    super(conn, "workflow_attempts", Arrays.<String>asList("workflow_execution_id", "system_user", "shutdown_reason", "priority", "pool", "host", "start_time", "end_time", "status", "last_heartbeat", "launch_dir", "launch_jar", "error_email", "info_email", "scm_remote", "commit_revision", "description", "last_heartbeat_epoch"));
    this.databases = databases;
  }

  @Override
  public WorkflowAttempt create(Map<Enum, Object> fieldsMap) throws IOException {
    int workflow_execution_id = (Integer) fieldsMap.get(WorkflowAttempt._Fields.workflow_execution_id);
    String system_user = (String) fieldsMap.get(WorkflowAttempt._Fields.system_user);
    String shutdown_reason = (String) fieldsMap.get(WorkflowAttempt._Fields.shutdown_reason);
    String priority = (String) fieldsMap.get(WorkflowAttempt._Fields.priority);
    String pool = (String) fieldsMap.get(WorkflowAttempt._Fields.pool);
    String host = (String) fieldsMap.get(WorkflowAttempt._Fields.host);
    Long start_time = (Long) fieldsMap.get(WorkflowAttempt._Fields.start_time);
    Long end_time = (Long) fieldsMap.get(WorkflowAttempt._Fields.end_time);
    Integer status = (Integer) fieldsMap.get(WorkflowAttempt._Fields.status);
    Long last_heartbeat = (Long) fieldsMap.get(WorkflowAttempt._Fields.last_heartbeat);
    String launch_dir = (String) fieldsMap.get(WorkflowAttempt._Fields.launch_dir);
    String launch_jar = (String) fieldsMap.get(WorkflowAttempt._Fields.launch_jar);
    String error_email = (String) fieldsMap.get(WorkflowAttempt._Fields.error_email);
    String info_email = (String) fieldsMap.get(WorkflowAttempt._Fields.info_email);
    String scm_remote = (String) fieldsMap.get(WorkflowAttempt._Fields.scm_remote);
    String commit_revision = (String) fieldsMap.get(WorkflowAttempt._Fields.commit_revision);
    String description = (String) fieldsMap.get(WorkflowAttempt._Fields.description);
    Long last_heartbeat_epoch = (Long) fieldsMap.get(WorkflowAttempt._Fields.last_heartbeat_epoch);
    return create(workflow_execution_id, system_user, shutdown_reason, priority, pool, host, start_time, end_time, status, last_heartbeat, launch_dir, launch_jar, error_email, info_email, scm_remote, commit_revision, description, last_heartbeat_epoch);
  }

  public WorkflowAttempt create(final int workflow_execution_id, final String system_user, final String shutdown_reason, final String priority, final String pool, final String host, final Long start_time, final Long end_time, final Integer status, final Long last_heartbeat, final String launch_dir, final String launch_jar, final String error_email, final String info_email, final String scm_remote, final String commit_revision, final String description, final Long last_heartbeat_epoch) throws IOException {
    StatementCreator statementCreator = new StatementCreator() {
      private final List<String> nonNullFields = new ArrayList<>();
      private final List<AttrSetter> statementSetters = new ArrayList<>();

      {
        int index = 1;

        nonNullFields.add("workflow_execution_id");
        int fieldIndex0 = index++;
        statementSetters.add(stmt -> stmt.setInt(fieldIndex0, workflow_execution_id));

        nonNullFields.add("system_user");
        int fieldIndex1 = index++;
        statementSetters.add(stmt -> stmt.setString(fieldIndex1, system_user));

        if (shutdown_reason != null) {
          nonNullFields.add("shutdown_reason");
          int fieldIndex2 = index++;
          statementSetters.add(stmt -> stmt.setString(fieldIndex2, shutdown_reason));
        }

        nonNullFields.add("priority");
        int fieldIndex3 = index++;
        statementSetters.add(stmt -> stmt.setString(fieldIndex3, priority));

        nonNullFields.add("pool");
        int fieldIndex4 = index++;
        statementSetters.add(stmt -> stmt.setString(fieldIndex4, pool));

        nonNullFields.add("host");
        int fieldIndex5 = index++;
        statementSetters.add(stmt -> stmt.setString(fieldIndex5, host));

        if (start_time != null) {
          nonNullFields.add("start_time");
          int fieldIndex6 = index++;
          statementSetters.add(stmt -> stmt.setTimestamp(fieldIndex6, new Timestamp(start_time)));
        }

        if (end_time != null) {
          nonNullFields.add("end_time");
          int fieldIndex7 = index++;
          statementSetters.add(stmt -> stmt.setTimestamp(fieldIndex7, new Timestamp(end_time)));
        }

        if (status != null) {
          nonNullFields.add("status");
          int fieldIndex8 = index++;
          statementSetters.add(stmt -> stmt.setInt(fieldIndex8, status));
        }

        if (last_heartbeat != null) {
          nonNullFields.add("last_heartbeat");
          int fieldIndex9 = index++;
          statementSetters.add(stmt -> stmt.setTimestamp(fieldIndex9, new Timestamp(last_heartbeat)));
        }

        if (launch_dir != null) {
          nonNullFields.add("launch_dir");
          int fieldIndex10 = index++;
          statementSetters.add(stmt -> stmt.setString(fieldIndex10, launch_dir));
        }

        if (launch_jar != null) {
          nonNullFields.add("launch_jar");
          int fieldIndex11 = index++;
          statementSetters.add(stmt -> stmt.setString(fieldIndex11, launch_jar));
        }

        if (error_email != null) {
          nonNullFields.add("error_email");
          int fieldIndex12 = index++;
          statementSetters.add(stmt -> stmt.setString(fieldIndex12, error_email));
        }

        if (info_email != null) {
          nonNullFields.add("info_email");
          int fieldIndex13 = index++;
          statementSetters.add(stmt -> stmt.setString(fieldIndex13, info_email));
        }

        if (scm_remote != null) {
          nonNullFields.add("scm_remote");
          int fieldIndex14 = index++;
          statementSetters.add(stmt -> stmt.setString(fieldIndex14, scm_remote));
        }

        if (commit_revision != null) {
          nonNullFields.add("commit_revision");
          int fieldIndex15 = index++;
          statementSetters.add(stmt -> stmt.setString(fieldIndex15, commit_revision));
        }

        if (description != null) {
          nonNullFields.add("description");
          int fieldIndex16 = index++;
          statementSetters.add(stmt -> stmt.setString(fieldIndex16, description));
        }

        if (last_heartbeat_epoch != null) {
          nonNullFields.add("last_heartbeat_epoch");
          int fieldIndex17 = index++;
          statementSetters.add(stmt -> stmt.setLong(fieldIndex17, last_heartbeat_epoch));
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
    WorkflowAttempt newInst = new WorkflowAttempt(__id, workflow_execution_id, system_user, shutdown_reason, priority, pool, host, start_time, end_time, status, last_heartbeat, launch_dir, launch_jar, error_email, info_email, scm_remote, commit_revision, description, last_heartbeat_epoch, databases);
    newInst.setCreated(true);
    cachedById.put(__id, newInst);
    clearForeignKeyCache();
    return newInst;
  }

  public WorkflowAttempt create(final int workflow_execution_id, final String system_user, final String priority, final String pool, final String host) throws IOException {
    StatementCreator statementCreator = new StatementCreator() {
      private final List<String> nonNullFields = new ArrayList<>();
      private final List<AttrSetter> statementSetters = new ArrayList<>();

      {
        int index = 1;

        nonNullFields.add("workflow_execution_id");
        int fieldIndex0 = index++;
        statementSetters.add(stmt -> stmt.setInt(fieldIndex0, workflow_execution_id));

        nonNullFields.add("system_user");
        int fieldIndex1 = index++;
        statementSetters.add(stmt -> stmt.setString(fieldIndex1, system_user));

        nonNullFields.add("priority");
        int fieldIndex3 = index++;
        statementSetters.add(stmt -> stmt.setString(fieldIndex3, priority));

        nonNullFields.add("pool");
        int fieldIndex4 = index++;
        statementSetters.add(stmt -> stmt.setString(fieldIndex4, pool));

        nonNullFields.add("host");
        int fieldIndex5 = index++;
        statementSetters.add(stmt -> stmt.setString(fieldIndex5, host));
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
    WorkflowAttempt newInst = new WorkflowAttempt(__id, workflow_execution_id, system_user, null, priority, pool, host, null, null, null, null, null, null, null, null, null, null, null, null, databases);
    newInst.setCreated(true);
    cachedById.put(__id, newInst);
    clearForeignKeyCache();
    return newInst;
  }

  public WorkflowAttempt createDefaultInstance() throws IOException {
    return create(0, "", "", "", "");
  }

  public List<WorkflowAttempt> find(Map<Enum, Object> fieldsMap) throws IOException {
    return find(null, fieldsMap);
  }

  public List<WorkflowAttempt> find(Collection<Long> ids, Map<Enum, Object> fieldsMap) throws IOException {
    List<WorkflowAttempt> foundList = new ArrayList<>();

    if (fieldsMap == null || fieldsMap.isEmpty()) {
      return foundList;
    }

    StringBuilder statementString = new StringBuilder();
    statementString.append("SELECT * FROM workflow_attempts WHERE (");
    List<Object> nonNullValues = new ArrayList<>();
    List<WorkflowAttempt._Fields> nonNullValueFields = new ArrayList<>();

    Iterator<Map.Entry<Enum, Object>> iter = fieldsMap.entrySet().iterator();
    while (iter.hasNext()) {
      Map.Entry<Enum, Object> entry = iter.next();
      Enum field = entry.getKey();
      Object value = entry.getValue();

      String queryValue = value != null ? " = ? " : " IS NULL";
      if (value != null) {
        nonNullValueFields.add((WorkflowAttempt._Fields) field);
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
        WorkflowAttempt._Fields field = nonNullValueFields.get(i);
        try {
          switch (field) {
            case workflow_execution_id:
              preparedStatement.setInt(i+1, (Integer) nonNullValues.get(i));
              break;
            case system_user:
              preparedStatement.setString(i+1, (String) nonNullValues.get(i));
              break;
            case shutdown_reason:
              preparedStatement.setString(i+1, (String) nonNullValues.get(i));
              break;
            case priority:
              preparedStatement.setString(i+1, (String) nonNullValues.get(i));
              break;
            case pool:
              preparedStatement.setString(i+1, (String) nonNullValues.get(i));
              break;
            case host:
              preparedStatement.setString(i+1, (String) nonNullValues.get(i));
              break;
            case start_time:
              preparedStatement.setTimestamp(i+1, new Timestamp((Long) nonNullValues.get(i)));
              break;
            case end_time:
              preparedStatement.setTimestamp(i+1, new Timestamp((Long) nonNullValues.get(i)));
              break;
            case status:
              preparedStatement.setInt(i+1, (Integer) nonNullValues.get(i));
              break;
            case last_heartbeat:
              preparedStatement.setTimestamp(i+1, new Timestamp((Long) nonNullValues.get(i)));
              break;
            case launch_dir:
              preparedStatement.setString(i+1, (String) nonNullValues.get(i));
              break;
            case launch_jar:
              preparedStatement.setString(i+1, (String) nonNullValues.get(i));
              break;
            case error_email:
              preparedStatement.setString(i+1, (String) nonNullValues.get(i));
              break;
            case info_email:
              preparedStatement.setString(i+1, (String) nonNullValues.get(i));
              break;
            case scm_remote:
              preparedStatement.setString(i+1, (String) nonNullValues.get(i));
              break;
            case commit_revision:
              preparedStatement.setString(i+1, (String) nonNullValues.get(i));
              break;
            case description:
              preparedStatement.setString(i+1, (String) nonNullValues.get(i));
              break;
            case last_heartbeat_epoch:
              preparedStatement.setLong(i+1, (Long) nonNullValues.get(i));
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
            WorkflowAttempt._Fields field = (WorkflowAttempt._Fields)constraint.getField();
            switch (field) {
              case workflow_execution_id:
                preparedStatement.setInt(++index, (Integer) parameter);
                break;
              case system_user:
                preparedStatement.setString(++index, (String) parameter);
                break;
              case shutdown_reason:
                preparedStatement.setString(++index, (String) parameter);
                break;
              case priority:
                preparedStatement.setString(++index, (String) parameter);
                break;
              case pool:
                preparedStatement.setString(++index, (String) parameter);
                break;
              case host:
                preparedStatement.setString(++index, (String) parameter);
                break;
              case start_time:
                preparedStatement.setTimestamp(++index, new Timestamp((Long) parameter));
                break;
              case end_time:
                preparedStatement.setTimestamp(++index, new Timestamp((Long) parameter));
                break;
              case status:
                preparedStatement.setInt(++index, (Integer) parameter);
                break;
              case last_heartbeat:
                preparedStatement.setTimestamp(++index, new Timestamp((Long) parameter));
                break;
              case launch_dir:
                preparedStatement.setString(++index, (String) parameter);
                break;
              case launch_jar:
                preparedStatement.setString(++index, (String) parameter);
                break;
              case error_email:
                preparedStatement.setString(++index, (String) parameter);
                break;
              case info_email:
                preparedStatement.setString(++index, (String) parameter);
                break;
              case scm_remote:
                preparedStatement.setString(++index, (String) parameter);
                break;
              case commit_revision:
                preparedStatement.setString(++index, (String) parameter);
                break;
              case description:
                preparedStatement.setString(++index, (String) parameter);
                break;
              case last_heartbeat_epoch:
                preparedStatement.setLong(++index, (Long) parameter);
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
  protected void setAttrs(WorkflowAttempt model, PreparedStatement stmt, boolean setNull) throws SQLException {
    int index = 1;
    {
      stmt.setInt(index++, model.getWorkflowExecutionId());
    }
    {
      stmt.setString(index++, model.getSystemUser());
    }
    if (setNull && model.getShutdownReason() == null) {
      stmt.setNull(index++, java.sql.Types.CHAR);
    } else if (model.getShutdownReason() != null) {
      stmt.setString(index++, model.getShutdownReason());
    }
    {
      stmt.setString(index++, model.getPriority());
    }
    {
      stmt.setString(index++, model.getPool());
    }
    {
      stmt.setString(index++, model.getHost());
    }
    if (setNull && model.getStartTime() == null) {
      stmt.setNull(index++, java.sql.Types.DATE);
    } else if (model.getStartTime() != null) {
      stmt.setTimestamp(index++, new Timestamp(model.getStartTime()));
    }
    if (setNull && model.getEndTime() == null) {
      stmt.setNull(index++, java.sql.Types.DATE);
    } else if (model.getEndTime() != null) {
      stmt.setTimestamp(index++, new Timestamp(model.getEndTime()));
    }
    if (setNull && model.getStatus() == null) {
      stmt.setNull(index++, java.sql.Types.INTEGER);
    } else if (model.getStatus() != null) {
      stmt.setInt(index++, model.getStatus());
    }
    if (setNull && model.getLastHeartbeat() == null) {
      stmt.setNull(index++, java.sql.Types.DATE);
    } else if (model.getLastHeartbeat() != null) {
      stmt.setTimestamp(index++, new Timestamp(model.getLastHeartbeat()));
    }
    if (setNull && model.getLaunchDir() == null) {
      stmt.setNull(index++, java.sql.Types.CHAR);
    } else if (model.getLaunchDir() != null) {
      stmt.setString(index++, model.getLaunchDir());
    }
    if (setNull && model.getLaunchJar() == null) {
      stmt.setNull(index++, java.sql.Types.CHAR);
    } else if (model.getLaunchJar() != null) {
      stmt.setString(index++, model.getLaunchJar());
    }
    if (setNull && model.getErrorEmail() == null) {
      stmt.setNull(index++, java.sql.Types.CHAR);
    } else if (model.getErrorEmail() != null) {
      stmt.setString(index++, model.getErrorEmail());
    }
    if (setNull && model.getInfoEmail() == null) {
      stmt.setNull(index++, java.sql.Types.CHAR);
    } else if (model.getInfoEmail() != null) {
      stmt.setString(index++, model.getInfoEmail());
    }
    if (setNull && model.getScmRemote() == null) {
      stmt.setNull(index++, java.sql.Types.CHAR);
    } else if (model.getScmRemote() != null) {
      stmt.setString(index++, model.getScmRemote());
    }
    if (setNull && model.getCommitRevision() == null) {
      stmt.setNull(index++, java.sql.Types.CHAR);
    } else if (model.getCommitRevision() != null) {
      stmt.setString(index++, model.getCommitRevision());
    }
    if (setNull && model.getDescription() == null) {
      stmt.setNull(index++, java.sql.Types.CHAR);
    } else if (model.getDescription() != null) {
      stmt.setString(index++, model.getDescription());
    }
    if (setNull && model.getLastHeartbeatEpoch() == null) {
      stmt.setNull(index++, java.sql.Types.INTEGER);
    } else if (model.getLastHeartbeatEpoch() != null) {
      stmt.setLong(index++, model.getLastHeartbeatEpoch());
    }
    stmt.setLong(index, model.getId());
  }

  @Override
  protected WorkflowAttempt instanceFromResultSet(ResultSet rs, Collection<Enum> selectedFields) throws SQLException {
    boolean allFields = selectedFields == null || selectedFields.isEmpty();
    long id = rs.getLong("id");
    return new WorkflowAttempt(id,
      allFields || selectedFields.contains(WorkflowAttempt._Fields.workflow_execution_id) ? getIntOrNull(rs, "workflow_execution_id") : 0,
      allFields || selectedFields.contains(WorkflowAttempt._Fields.system_user) ? rs.getString("system_user") : "",
      allFields || selectedFields.contains(WorkflowAttempt._Fields.shutdown_reason) ? rs.getString("shutdown_reason") : null,
      allFields || selectedFields.contains(WorkflowAttempt._Fields.priority) ? rs.getString("priority") : "",
      allFields || selectedFields.contains(WorkflowAttempt._Fields.pool) ? rs.getString("pool") : "",
      allFields || selectedFields.contains(WorkflowAttempt._Fields.host) ? rs.getString("host") : "",
      allFields || selectedFields.contains(WorkflowAttempt._Fields.start_time) ? getDateAsLong(rs, "start_time") : null,
      allFields || selectedFields.contains(WorkflowAttempt._Fields.end_time) ? getDateAsLong(rs, "end_time") : null,
      allFields || selectedFields.contains(WorkflowAttempt._Fields.status) ? getIntOrNull(rs, "status") : null,
      allFields || selectedFields.contains(WorkflowAttempt._Fields.last_heartbeat) ? getDateAsLong(rs, "last_heartbeat") : null,
      allFields || selectedFields.contains(WorkflowAttempt._Fields.launch_dir) ? rs.getString("launch_dir") : null,
      allFields || selectedFields.contains(WorkflowAttempt._Fields.launch_jar) ? rs.getString("launch_jar") : null,
      allFields || selectedFields.contains(WorkflowAttempt._Fields.error_email) ? rs.getString("error_email") : null,
      allFields || selectedFields.contains(WorkflowAttempt._Fields.info_email) ? rs.getString("info_email") : null,
      allFields || selectedFields.contains(WorkflowAttempt._Fields.scm_remote) ? rs.getString("scm_remote") : null,
      allFields || selectedFields.contains(WorkflowAttempt._Fields.commit_revision) ? rs.getString("commit_revision") : null,
      allFields || selectedFields.contains(WorkflowAttempt._Fields.description) ? rs.getString("description") : null,
      allFields || selectedFields.contains(WorkflowAttempt._Fields.last_heartbeat_epoch) ? getLongOrNull(rs, "last_heartbeat_epoch") : null,
      databases
    );
  }

  public List<WorkflowAttempt> findByWorkflowExecutionId(final int value) throws IOException {
    return find(Collections.<Enum, Object>singletonMap(WorkflowAttempt._Fields.workflow_execution_id, value));
  }

  public List<WorkflowAttempt> findBySystemUser(final String value) throws IOException {
    return find(Collections.<Enum, Object>singletonMap(WorkflowAttempt._Fields.system_user, value));
  }

  public List<WorkflowAttempt> findByShutdownReason(final String value) throws IOException {
    return find(Collections.<Enum, Object>singletonMap(WorkflowAttempt._Fields.shutdown_reason, value));
  }

  public List<WorkflowAttempt> findByPriority(final String value) throws IOException {
    return find(Collections.<Enum, Object>singletonMap(WorkflowAttempt._Fields.priority, value));
  }

  public List<WorkflowAttempt> findByPool(final String value) throws IOException {
    return find(Collections.<Enum, Object>singletonMap(WorkflowAttempt._Fields.pool, value));
  }

  public List<WorkflowAttempt> findByHost(final String value) throws IOException {
    return find(Collections.<Enum, Object>singletonMap(WorkflowAttempt._Fields.host, value));
  }

  public List<WorkflowAttempt> findByStartTime(final Long value) throws IOException {
    return find(Collections.<Enum, Object>singletonMap(WorkflowAttempt._Fields.start_time, value));
  }

  public List<WorkflowAttempt> findByEndTime(final Long value) throws IOException {
    return find(Collections.<Enum, Object>singletonMap(WorkflowAttempt._Fields.end_time, value));
  }

  public List<WorkflowAttempt> findByStatus(final Integer value) throws IOException {
    return find(Collections.<Enum, Object>singletonMap(WorkflowAttempt._Fields.status, value));
  }

  public List<WorkflowAttempt> findByLastHeartbeat(final Long value) throws IOException {
    return find(Collections.<Enum, Object>singletonMap(WorkflowAttempt._Fields.last_heartbeat, value));
  }

  public List<WorkflowAttempt> findByLaunchDir(final String value) throws IOException {
    return find(Collections.<Enum, Object>singletonMap(WorkflowAttempt._Fields.launch_dir, value));
  }

  public List<WorkflowAttempt> findByLaunchJar(final String value) throws IOException {
    return find(Collections.<Enum, Object>singletonMap(WorkflowAttempt._Fields.launch_jar, value));
  }

  public List<WorkflowAttempt> findByErrorEmail(final String value) throws IOException {
    return find(Collections.<Enum, Object>singletonMap(WorkflowAttempt._Fields.error_email, value));
  }

  public List<WorkflowAttempt> findByInfoEmail(final String value) throws IOException {
    return find(Collections.<Enum, Object>singletonMap(WorkflowAttempt._Fields.info_email, value));
  }

  public List<WorkflowAttempt> findByScmRemote(final String value) throws IOException {
    return find(Collections.<Enum, Object>singletonMap(WorkflowAttempt._Fields.scm_remote, value));
  }

  public List<WorkflowAttempt> findByCommitRevision(final String value) throws IOException {
    return find(Collections.<Enum, Object>singletonMap(WorkflowAttempt._Fields.commit_revision, value));
  }

  public List<WorkflowAttempt> findByDescription(final String value) throws IOException {
    return find(Collections.<Enum, Object>singletonMap(WorkflowAttempt._Fields.description, value));
  }

  public List<WorkflowAttempt> findByLastHeartbeatEpoch(final Long value) throws IOException {
    return find(Collections.<Enum, Object>singletonMap(WorkflowAttempt._Fields.last_heartbeat_epoch, value));
  }

  public WorkflowAttemptQueryBuilder query() {
    return new WorkflowAttemptQueryBuilder(this);
  }

  public WorkflowAttemptDeleteBuilder delete() {
    return new WorkflowAttemptDeleteBuilder(this);
  }
}
