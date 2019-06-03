
/**
 * Autogenerated by Jack
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 */
package com.liveramp.databases.workflow_db.models;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.rapleaf.jack.AssociationType;
import com.rapleaf.jack.AttributesWithId;
import com.rapleaf.jack.BelongsToAssociation;
import com.rapleaf.jack.DefaultAssociationMetadata;
import com.rapleaf.jack.HasManyAssociation;
import com.rapleaf.jack.HasOneAssociation;
import com.rapleaf.jack.IAssociationMetadata;
import com.rapleaf.jack.IModelAssociationMetadata;
import com.rapleaf.jack.ModelIdWrapper;
import com.rapleaf.jack.ModelWithId;
import com.rapleaf.jack.queries.AbstractTable;
import com.rapleaf.jack.queries.Column;

import com.liveramp.databases.workflow_db.IDatabases;
import com.rapleaf.jack.util.JackUtility;

public class MapreduceJobTaskException extends ModelWithId<MapreduceJobTaskException, IDatabases> implements Comparable<MapreduceJobTaskException>{
  
  public static final long serialVersionUID = 6374733905407260494L;

  public static class Tbl extends AbstractTable<MapreduceJobTaskException.Attributes, MapreduceJobTaskException> {
    public final Column<Long> ID;
    public final Column<Integer> MAPREDUCE_JOB_ID;
    public final Column<String> TASK_ATTEMPT_ID;
    public final Column<String> EXCEPTION;
    public final Column<String> HOST_URL;

    private Tbl(String alias) {
      super("mapreduce_job_task_exceptions", alias, MapreduceJobTaskException.Attributes.class, MapreduceJobTaskException.class);
      this.ID = Column.fromId(alias);
      this.MAPREDUCE_JOB_ID = Column.fromField(alias, _Fields.mapreduce_job_id, Integer.class);
      this.TASK_ATTEMPT_ID = Column.fromField(alias, _Fields.task_attempt_id, String.class);
      this.EXCEPTION = Column.fromField(alias, _Fields.exception, String.class);
      this.HOST_URL = Column.fromField(alias, _Fields.host_url, String.class);
      Collections.addAll(this.allColumns, ID, MAPREDUCE_JOB_ID, TASK_ATTEMPT_ID, EXCEPTION, HOST_URL);
    }

    public static Tbl as(String alias) {
      return new Tbl(alias);
    }
  }

  public static final Tbl TBL = new Tbl("mapreduce_job_task_exceptions");
  public static final Column<Long> ID = TBL.ID;
  public static final Column<Integer> MAPREDUCE_JOB_ID = TBL.MAPREDUCE_JOB_ID;
  public static final Column<String> TASK_ATTEMPT_ID = TBL.TASK_ATTEMPT_ID;
  public static final Column<String> EXCEPTION = TBL.EXCEPTION;
  public static final Column<String> HOST_URL = TBL.HOST_URL;

  private final Attributes attributes;

  private transient MapreduceJobTaskException.Id cachedTypedId;

  // Associations
  private BelongsToAssociation<MapreduceJob> __assoc_mapreduce_job;

  public enum _Fields {
    mapreduce_job_id,
    task_attempt_id,
    exception,
    host_url,
  }

  @Override
  public MapreduceJobTaskException.Id getTypedId() {
    if (cachedTypedId == null) {
      cachedTypedId = new MapreduceJobTaskException.Id(this.getId());
    }
    return cachedTypedId;
  }

  public MapreduceJobTaskException(long id, final Integer mapreduce_job_id, final String task_attempt_id, final String exception, final String host_url, IDatabases databases) {
    super(databases);
    attributes = new Attributes(id, mapreduce_job_id, task_attempt_id, exception, host_url);
    this.__assoc_mapreduce_job = new BelongsToAssociation<>(databases.getWorkflowDb().mapreduceJobs(), getMapreduceJobId() == null ? null : getMapreduceJobId().longValue());
  }

  public MapreduceJobTaskException(long id, final Integer mapreduce_job_id, final String task_attempt_id, final String exception, final String host_url) {
    super(null);
    attributes = new Attributes(id, mapreduce_job_id, task_attempt_id, exception, host_url);
  }
  
  public MapreduceJobTaskException(long id, IDatabases databases) {
    super(databases);
    attributes = new Attributes(id);
    this.__assoc_mapreduce_job = new BelongsToAssociation<>(databases.getWorkflowDb().mapreduceJobs(), getMapreduceJobId() == null ? null : getMapreduceJobId().longValue());
  }

  public MapreduceJobTaskException(long id) {
    super(null);
    attributes = new Attributes(id);
  }

  public static MapreduceJobTaskException newDefaultInstance(long id) {
    return new MapreduceJobTaskException(id);
  }

  public MapreduceJobTaskException(Attributes attributes, IDatabases databases) {
    super(databases);
    this.attributes = attributes;

    if (databases != null) {
      this.__assoc_mapreduce_job = new BelongsToAssociation<>(databases.getWorkflowDb().mapreduceJobs(), getMapreduceJobId() == null ? null : getMapreduceJobId().longValue());
    }
  }

  public MapreduceJobTaskException(Attributes attributes) {
    this(attributes, (IDatabases) null);
  }

  public MapreduceJobTaskException(long id, Map<Enum, Object> fieldsMap) {
    super(null);
    attributes = new Attributes(id, fieldsMap);
  }

  public MapreduceJobTaskException (MapreduceJobTaskException other) {
    this(other, (IDatabases)null);
  }

  public MapreduceJobTaskException (MapreduceJobTaskException other, IDatabases databases) {
    super(databases);
    attributes = new Attributes(other.getAttributes());

    if (databases != null) {
      this.__assoc_mapreduce_job = new BelongsToAssociation<>(databases.getWorkflowDb().mapreduceJobs(), getMapreduceJobId() == null ? null : getMapreduceJobId().longValue());
    }
  }

  public Attributes getAttributes() {
    return attributes;
  }

  public Integer getMapreduceJobId() {
    return attributes.getMapreduceJobId();
  }

  public MapreduceJobTaskException setMapreduceJobId(Integer newval) {
    attributes.setMapreduceJobId(newval);
    if(__assoc_mapreduce_job != null){
      this.__assoc_mapreduce_job.setOwnerId(newval);
    }
    cachedHashCode = 0;
    return this;
  }

  public String getTaskAttemptId() {
    return attributes.getTaskAttemptId();
  }

  public MapreduceJobTaskException setTaskAttemptId(String newval) {
    attributes.setTaskAttemptId(newval);
    cachedHashCode = 0;
    return this;
  }

  public String getException() {
    return attributes.getException();
  }

  public MapreduceJobTaskException setException(String newval) {
    attributes.setException(newval);
    cachedHashCode = 0;
    return this;
  }

  public String getHostUrl() {
    return attributes.getHostUrl();
  }

  public MapreduceJobTaskException setHostUrl(String newval) {
    attributes.setHostUrl(newval);
    cachedHashCode = 0;
    return this;
  }

  public void setField(_Fields field, Object value) {
    switch (field) {
      case mapreduce_job_id:
        setMapreduceJobId((Integer)value);
        break;
      case task_attempt_id:
        setTaskAttemptId((String)value);
        break;
      case exception:
        setException((String)value);
        break;
      case host_url:
        setHostUrl((String)value);
        break;
      default:
        throw new IllegalStateException("Invalid field: " + field);
    }
  }
  
  public void setField(String fieldName, Object value) {
    if (fieldName.equals("mapreduce_job_id")) {
      setMapreduceJobId((Integer)  value);
      return;
    }
    if (fieldName.equals("task_attempt_id")) {
      setTaskAttemptId((String)  value);
      return;
    }
    if (fieldName.equals("exception")) {
      setException((String)  value);
      return;
    }
    if (fieldName.equals("host_url")) {
      setHostUrl((String)  value);
      return;
    }
    throw new IllegalStateException("Invalid field: " + fieldName);
  }

  public static Class getFieldType(_Fields field) {
    switch (field) {
      case mapreduce_job_id:
        return Integer.class;
      case task_attempt_id:
        return String.class;
      case exception:
        return String.class;
      case host_url:
        return String.class;
      default:
        throw new IllegalStateException("Invalid field: " + field);
    }    
  }

  public static Class getFieldType(String fieldName) {    
    if (fieldName.equals("mapreduce_job_id")) {
      return Integer.class;
    }
    if (fieldName.equals("task_attempt_id")) {
      return String.class;
    }
    if (fieldName.equals("exception")) {
      return String.class;
    }
    if (fieldName.equals("host_url")) {
      return String.class;
    }
    throw new IllegalStateException("Invalid field name: " + fieldName);
  }

  public MapreduceJob getMapreduceJob() throws IOException {
    return __assoc_mapreduce_job.get();
  }

  @Override
  public Object getField(String fieldName) {
    if (fieldName.equals("id")) {
      return getId();
    }
    if (fieldName.equals("mapreduce_job_id")) {
      return getMapreduceJobId();
    }
    if (fieldName.equals("task_attempt_id")) {
      return getTaskAttemptId();
    }
    if (fieldName.equals("exception")) {
      return getException();
    }
    if (fieldName.equals("host_url")) {
      return getHostUrl();
    }
    throw new IllegalStateException("Invalid field name: " + fieldName);
  }

  public Object getField(_Fields field) {
    switch (field) {
      case mapreduce_job_id:
        return getMapreduceJobId();
      case task_attempt_id:
        return getTaskAttemptId();
      case exception:
        return getException();
      case host_url:
        return getHostUrl();
    }
    throw new IllegalStateException("Invalid field: " + field);
  }

  public boolean hasField(String fieldName) {
    if (fieldName.equals("id")) {
      return true;
    }
    if (fieldName.equals("mapreduce_job_id")) {
      return true;
    }
    if (fieldName.equals("task_attempt_id")) {
      return true;
    }
    if (fieldName.equals("exception")) {
      return true;
    }
    if (fieldName.equals("host_url")) {
      return true;
    }
    return false;
  }

  public static Object getDefaultValue(_Fields field) {
    switch (field) {
      case mapreduce_job_id:
        return null;
      case task_attempt_id:
        return null;
      case exception:
        return null;
      case host_url:
        return null;
    }
    throw new IllegalStateException("Invalid field: " + field);
  }

  @Override
  public Set<Enum> getFieldSet() {
    Set set = EnumSet.allOf(_Fields.class);
    return set;
  }

  @Override
  public MapreduceJobTaskException getCopy() {
    return getCopy(databases);
  }

  @Override
  public MapreduceJobTaskException getCopy(IDatabases databases) {
    return new MapreduceJobTaskException(this, databases);
  }

  @Override
  public boolean save() throws IOException {
    return databases.getWorkflowDb().mapreduceJobTaskExceptions().save(this);
  }

  public MapreduceJob createMapreduceJob(final String job_identifier, final String job_name, final String tracking_url) throws IOException {
 
    MapreduceJob newMapreduceJob = databases.getWorkflowDb().mapreduceJobs().create(job_identifier, job_name, tracking_url);
    setMapreduceJobId(JackUtility.safeLongToInt(newMapreduceJob.getId()));
    save();
    __assoc_mapreduce_job.clearCache();
    return newMapreduceJob;
  }

  public MapreduceJob createMapreduceJob(final Long step_attempt_id, final String job_identifier, final String job_name, final String tracking_url, final Long avg_map_duration, final Long median_map_duration, final Long max_map_duration, final Long min_map_duration, final Long stdev_map_duration, final Long avg_reduce_duration, final Long median_reduce_duration, final Long max_reduce_duration, final Long min_reduce_duration, final Long stdev_reduce_duration, final Integer tasks_sampled, final Integer tasks_failed_in_sample) throws IOException {
 
    MapreduceJob newMapreduceJob = databases.getWorkflowDb().mapreduceJobs().create(step_attempt_id, job_identifier, job_name, tracking_url, avg_map_duration, median_map_duration, max_map_duration, min_map_duration, stdev_map_duration, avg_reduce_duration, median_reduce_duration, max_reduce_duration, min_reduce_duration, stdev_reduce_duration, tasks_sampled, tasks_failed_in_sample);
    setMapreduceJobId(JackUtility.safeLongToInt(newMapreduceJob.getId()));
    save();
    __assoc_mapreduce_job.clearCache();
    return newMapreduceJob;
  }

  public MapreduceJob createMapreduceJob() throws IOException {
 
    MapreduceJob newMapreduceJob = databases.getWorkflowDb().mapreduceJobs().create("", "", "");
    setMapreduceJobId(JackUtility.safeLongToInt(newMapreduceJob.getId()));
    save();
    __assoc_mapreduce_job.clearCache();
    return newMapreduceJob;
  }

  public String toString() {
    return "<MapreduceJobTaskException"
        + " id: " + this.getId()
        + " mapreduce_job_id: " + getMapreduceJobId()
        + " task_attempt_id: " + getTaskAttemptId()
        + " exception: " + getException()
        + " host_url: " + getHostUrl()
        + ">";
  }

  public void unsetAssociations() {
    unsetDatabaseReference();
    __assoc_mapreduce_job = null;
  }

  public int compareTo(MapreduceJobTaskException that) {
    return Long.valueOf(this.getId()).compareTo(that.getId());
  }
  
  
  public static class Attributes extends AttributesWithId {
    
    public static final long serialVersionUID = -5736916663614960002L;

    // Fields
    private Integer __mapreduce_job_id;
    private String __task_attempt_id;
    private String __exception;
    private String __host_url;

    public Attributes(long id) {
      super(id);
    }

    public Attributes(long id, final Integer mapreduce_job_id, final String task_attempt_id, final String exception, final String host_url) {
      super(id);
      this.__mapreduce_job_id = mapreduce_job_id;
      this.__task_attempt_id = task_attempt_id;
      this.__exception = exception;
      this.__host_url = host_url;
    }

    public static Attributes newDefaultInstance(long id) {
      return new Attributes(id);
    }

    public Attributes(long id, Map<Enum, Object> fieldsMap) {
      super(id);
      Integer mapreduce_job_id = (Integer)fieldsMap.get(MapreduceJobTaskException._Fields.mapreduce_job_id);
      String task_attempt_id = (String)fieldsMap.get(MapreduceJobTaskException._Fields.task_attempt_id);
      String exception = (String)fieldsMap.get(MapreduceJobTaskException._Fields.exception);
      String host_url = (String)fieldsMap.get(MapreduceJobTaskException._Fields.host_url);
      this.__mapreduce_job_id = mapreduce_job_id;
      this.__task_attempt_id = task_attempt_id;
      this.__exception = exception;
      this.__host_url = host_url;
    }

    public Attributes(Attributes other) {
      super(other.getId());
      this.__mapreduce_job_id = other.getMapreduceJobId();
      this.__task_attempt_id = other.getTaskAttemptId();
      this.__exception = other.getException();
      this.__host_url = other.getHostUrl();
    }

    public Integer getMapreduceJobId() {
      return __mapreduce_job_id;
    }

    public Attributes setMapreduceJobId(Integer newval) {
      this.__mapreduce_job_id = newval;
      cachedHashCode = 0;
      return this;
    }

    public String getTaskAttemptId() {
      return __task_attempt_id;
    }

    public Attributes setTaskAttemptId(String newval) {
      this.__task_attempt_id = newval;
      cachedHashCode = 0;
      return this;
    }

    public String getException() {
      return __exception;
    }

    public Attributes setException(String newval) {
      this.__exception = newval;
      cachedHashCode = 0;
      return this;
    }

    public String getHostUrl() {
      return __host_url;
    }

    public Attributes setHostUrl(String newval) {
      this.__host_url = newval;
      cachedHashCode = 0;
      return this;
    }

    public void setField(_Fields field, Object value) {
      switch (field) {
        case mapreduce_job_id:
          setMapreduceJobId((Integer)value);
          break;
        case task_attempt_id:
          setTaskAttemptId((String)value);
          break;
        case exception:
          setException((String)value);
          break;
        case host_url:
          setHostUrl((String)value);
          break;
        default:
          throw new IllegalStateException("Invalid field: " + field);
      }
    }

    public void setField(String fieldName, Object value) {
      if (fieldName.equals("mapreduce_job_id")) {
        setMapreduceJobId((Integer)value);
        return;
      }
      if (fieldName.equals("task_attempt_id")) {
        setTaskAttemptId((String)value);
        return;
      }
      if (fieldName.equals("exception")) {
        setException((String)value);
        return;
      }
      if (fieldName.equals("host_url")) {
        setHostUrl((String)value);
        return;
      }
      throw new IllegalStateException("Invalid field: " + fieldName);
    }

    public static Class getFieldType(_Fields field) {
      switch (field) {
        case mapreduce_job_id:
          return Integer.class;
        case task_attempt_id:
          return String.class;
        case exception:
          return String.class;
        case host_url:
          return String.class;
        default:
          throw new IllegalStateException("Invalid field: " + field);
      }    
    }

    public static Class getFieldType(String fieldName) {    
      if (fieldName.equals("mapreduce_job_id")) {
        return Integer.class;
      }
      if (fieldName.equals("task_attempt_id")) {
        return String.class;
      }
      if (fieldName.equals("exception")) {
        return String.class;
      }
      if (fieldName.equals("host_url")) {
        return String.class;
      }
      throw new IllegalStateException("Invalid field name: " + fieldName);
    }

    @Override
    public Object getField(String fieldName) {
      if (fieldName.equals("id")) {
        return getId();
      }
      if (fieldName.equals("mapreduce_job_id")) {
        return getMapreduceJobId();
      }
      if (fieldName.equals("task_attempt_id")) {
        return getTaskAttemptId();
      }
      if (fieldName.equals("exception")) {
        return getException();
      }
      if (fieldName.equals("host_url")) {
        return getHostUrl();
      }
      throw new IllegalStateException("Invalid field name: " + fieldName);
    }

    public Object getField(_Fields field) {
      switch (field) {
        case mapreduce_job_id:
          return getMapreduceJobId();
        case task_attempt_id:
          return getTaskAttemptId();
        case exception:
          return getException();
        case host_url:
          return getHostUrl();
      }
      throw new IllegalStateException("Invalid field: " + field);
    }

    public boolean hasField(String fieldName) {
      if (fieldName.equals("id")) {
        return true;
      }
      if (fieldName.equals("mapreduce_job_id")) {
        return true;
      }
      if (fieldName.equals("task_attempt_id")) {
        return true;
      }
      if (fieldName.equals("exception")) {
        return true;
      }
      if (fieldName.equals("host_url")) {
        return true;
      }
      return false;
    }

    public static Object getDefaultValue(_Fields field) {
      switch (field) {
        case mapreduce_job_id:
          return null;
        case task_attempt_id:
          return null;
        case exception:
          return null;
        case host_url:
          return null;
      }
      throw new IllegalStateException("Invalid field: " + field);
    }
    
    @Override
    public Set<Enum> getFieldSet() {
      Set set = EnumSet.allOf(_Fields.class);
      return set;
    }
    
    public String toString() {
      return "<MapreduceJobTaskException.Attributes"
          + " mapreduce_job_id: " + getMapreduceJobId()
          + " task_attempt_id: " + getTaskAttemptId()
          + " exception: " + getException()
          + " host_url: " + getHostUrl()
          + ">";
    }
  }

  public static class Id implements ModelIdWrapper<MapreduceJobTaskException.Id> {
    public static final long serialVersionUID = 1L;

    private final long id;

    public Id(Long id) {
      this.id = id;
    }

    @Override
    public Long getId() {
      return id;
    }

    @Override
    public int compareTo(Id other) {
      return this.getId().compareTo(other.getId());
    }

    @Override
    public boolean equals(Object other) {
      if (other instanceof Id) {
        return this.getId().equals(((Id)other).getId());
      }
      return false;
    }

    @Override
    public int hashCode() {
      return this.getId().hashCode();
    }

    @Override
    public String toString() {
      return "<MapreduceJobTaskException.Id: " + this.getId() + ">";
    }
  }

  public static Set<Attributes> convertToAttributesSet(Collection<MapreduceJobTaskException> models) {
    return models.stream()
        .map(MapreduceJobTaskException::getAttributes)
        .collect(Collectors.toSet());
  }

  public static class AssociationMetadata implements IModelAssociationMetadata {

    private List<IAssociationMetadata> meta = new ArrayList<>();

    public AssociationMetadata(){
      meta.add(new DefaultAssociationMetadata(AssociationType.BELONGS_TO, MapreduceJobTaskException.class, MapreduceJob.class, "mapreduce_job_id"));
    }

    @Override
    public List<IAssociationMetadata> getAssociationMetadata() {
      return meta;
    }
  }

}
