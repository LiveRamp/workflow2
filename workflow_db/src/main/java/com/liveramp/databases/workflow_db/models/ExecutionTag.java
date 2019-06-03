
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

public class ExecutionTag extends ModelWithId<ExecutionTag, IDatabases> implements Comparable<ExecutionTag>{
  
  public static final long serialVersionUID = 4882020992182674970L;

  public static class Tbl extends AbstractTable<ExecutionTag.Attributes, ExecutionTag> {
    public final Column<Long> ID;
    public final Column<Long> WORKFLOW_EXECUTION_ID;
    public final Column<String> TAG;
    public final Column<String> VALUE;

    private Tbl(String alias) {
      super("execution_tags", alias, ExecutionTag.Attributes.class, ExecutionTag.class);
      this.ID = Column.fromId(alias);
      this.WORKFLOW_EXECUTION_ID = Column.fromField(alias, _Fields.workflow_execution_id, Long.class);
      this.TAG = Column.fromField(alias, _Fields.tag, String.class);
      this.VALUE = Column.fromField(alias, _Fields.value, String.class);
      Collections.addAll(this.allColumns, ID, WORKFLOW_EXECUTION_ID, TAG, VALUE);
    }

    public static Tbl as(String alias) {
      return new Tbl(alias);
    }
  }

  public static final Tbl TBL = new Tbl("execution_tags");
  public static final Column<Long> ID = TBL.ID;
  public static final Column<Long> WORKFLOW_EXECUTION_ID = TBL.WORKFLOW_EXECUTION_ID;
  public static final Column<String> TAG = TBL.TAG;
  public static final Column<String> VALUE = TBL.VALUE;

  private final Attributes attributes;

  private transient ExecutionTag.Id cachedTypedId;

  // Associations
  private BelongsToAssociation<WorkflowExecution> __assoc_workflow_execution;

  public enum _Fields {
    workflow_execution_id,
    tag,
    value,
  }

  @Override
  public ExecutionTag.Id getTypedId() {
    if (cachedTypedId == null) {
      cachedTypedId = new ExecutionTag.Id(this.getId());
    }
    return cachedTypedId;
  }

  public ExecutionTag(long id, final long workflow_execution_id, final String tag, final String value, IDatabases databases) {
    super(databases);
    attributes = new Attributes(id, workflow_execution_id, tag, value);
    this.__assoc_workflow_execution = new BelongsToAssociation<>(databases.getWorkflowDb().workflowExecutions(), getWorkflowExecutionId());
  }

  public ExecutionTag(long id, final long workflow_execution_id, final String tag, final String value) {
    super(null);
    attributes = new Attributes(id, workflow_execution_id, tag, value);
  }

  public static ExecutionTag newDefaultInstance(long id) {
    return new ExecutionTag(id, 0L, "", "");
  }

  public ExecutionTag(Attributes attributes, IDatabases databases) {
    super(databases);
    this.attributes = attributes;

    if (databases != null) {
      this.__assoc_workflow_execution = new BelongsToAssociation<>(databases.getWorkflowDb().workflowExecutions(), getWorkflowExecutionId());
    }
  }

  public ExecutionTag(Attributes attributes) {
    this(attributes, (IDatabases) null);
  }

  public ExecutionTag(long id, Map<Enum, Object> fieldsMap) {
    super(null);
    attributes = new Attributes(id, fieldsMap);
  }

  public ExecutionTag (ExecutionTag other) {
    this(other, (IDatabases)null);
  }

  public ExecutionTag (ExecutionTag other, IDatabases databases) {
    super(databases);
    attributes = new Attributes(other.getAttributes());

    if (databases != null) {
      this.__assoc_workflow_execution = new BelongsToAssociation<>(databases.getWorkflowDb().workflowExecutions(), getWorkflowExecutionId());
    }
  }

  public Attributes getAttributes() {
    return attributes;
  }

  public long getWorkflowExecutionId() {
    return attributes.getWorkflowExecutionId();
  }

  public ExecutionTag setWorkflowExecutionId(long newval) {
    attributes.setWorkflowExecutionId(newval);
    if(__assoc_workflow_execution != null){
      this.__assoc_workflow_execution.setOwnerId(newval);
    }
    cachedHashCode = 0;
    return this;
  }

  public String getTag() {
    return attributes.getTag();
  }

  public ExecutionTag setTag(String newval) {
    attributes.setTag(newval);
    cachedHashCode = 0;
    return this;
  }

  public String getValue() {
    return attributes.getValue();
  }

  public ExecutionTag setValue(String newval) {
    attributes.setValue(newval);
    cachedHashCode = 0;
    return this;
  }

  public void setField(_Fields field, Object value) {
    switch (field) {
      case workflow_execution_id:
        setWorkflowExecutionId((Long)value);
        break;
      case tag:
        setTag((String)value);
        break;
      case value:
        setValue((String)value);
        break;
      default:
        throw new IllegalStateException("Invalid field: " + field);
    }
  }
  
  public void setField(String fieldName, Object value) {
    if (fieldName.equals("workflow_execution_id")) {
      setWorkflowExecutionId((Long)  value);
      return;
    }
    if (fieldName.equals("tag")) {
      setTag((String)  value);
      return;
    }
    if (fieldName.equals("value")) {
      setValue((String)  value);
      return;
    }
    throw new IllegalStateException("Invalid field: " + fieldName);
  }

  public static Class getFieldType(_Fields field) {
    switch (field) {
      case workflow_execution_id:
        return long.class;
      case tag:
        return String.class;
      case value:
        return String.class;
      default:
        throw new IllegalStateException("Invalid field: " + field);
    }    
  }

  public static Class getFieldType(String fieldName) {    
    if (fieldName.equals("workflow_execution_id")) {
      return long.class;
    }
    if (fieldName.equals("tag")) {
      return String.class;
    }
    if (fieldName.equals("value")) {
      return String.class;
    }
    throw new IllegalStateException("Invalid field name: " + fieldName);
  }

  public WorkflowExecution getWorkflowExecution() throws IOException {
    return __assoc_workflow_execution.get();
  }

  @Override
  public Object getField(String fieldName) {
    if (fieldName.equals("id")) {
      return getId();
    }
    if (fieldName.equals("workflow_execution_id")) {
      return getWorkflowExecutionId();
    }
    if (fieldName.equals("tag")) {
      return getTag();
    }
    if (fieldName.equals("value")) {
      return getValue();
    }
    throw new IllegalStateException("Invalid field name: " + fieldName);
  }

  public Object getField(_Fields field) {
    switch (field) {
      case workflow_execution_id:
        return getWorkflowExecutionId();
      case tag:
        return getTag();
      case value:
        return getValue();
    }
    throw new IllegalStateException("Invalid field: " + field);
  }

  public boolean hasField(String fieldName) {
    if (fieldName.equals("id")) {
      return true;
    }
    if (fieldName.equals("workflow_execution_id")) {
      return true;
    }
    if (fieldName.equals("tag")) {
      return true;
    }
    if (fieldName.equals("value")) {
      return true;
    }
    return false;
  }

  public static Object getDefaultValue(_Fields field) {
    switch (field) {
      case workflow_execution_id:
        return null;
      case tag:
        return null;
      case value:
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
  public ExecutionTag getCopy() {
    return getCopy(databases);
  }

  @Override
  public ExecutionTag getCopy(IDatabases databases) {
    return new ExecutionTag(this, databases);
  }

  @Override
  public boolean save() throws IOException {
    return databases.getWorkflowDb().executionTags().save(this);
  }

  public WorkflowExecution createWorkflowExecution(final String name, final int status) throws IOException {
 
    WorkflowExecution newWorkflowExecution = databases.getWorkflowDb().workflowExecutions().create(name, status);
    setWorkflowExecutionId(newWorkflowExecution.getId());
    save();
    __assoc_workflow_execution.clearCache();
    return newWorkflowExecution;
  }

  public WorkflowExecution createWorkflowExecution(final Integer app_type, final String name, final String scope_identifier, final int status, final Long start_time, final Long end_time, final Integer application_id, final String pool_override) throws IOException {
 
    WorkflowExecution newWorkflowExecution = databases.getWorkflowDb().workflowExecutions().create(app_type, name, scope_identifier, status, start_time, end_time, application_id, pool_override);
    setWorkflowExecutionId(newWorkflowExecution.getId());
    save();
    __assoc_workflow_execution.clearCache();
    return newWorkflowExecution;
  }

  public WorkflowExecution createWorkflowExecution() throws IOException {
 
    WorkflowExecution newWorkflowExecution = databases.getWorkflowDb().workflowExecutions().create("", 0);
    setWorkflowExecutionId(newWorkflowExecution.getId());
    save();
    __assoc_workflow_execution.clearCache();
    return newWorkflowExecution;
  }

  public String toString() {
    return "<ExecutionTag"
        + " id: " + this.getId()
        + " workflow_execution_id: " + getWorkflowExecutionId()
        + " tag: " + getTag()
        + " value: " + getValue()
        + ">";
  }

  public void unsetAssociations() {
    unsetDatabaseReference();
    __assoc_workflow_execution = null;
  }

  public int compareTo(ExecutionTag that) {
    return Long.valueOf(this.getId()).compareTo(that.getId());
  }
  
  
  public static class Attributes extends AttributesWithId {
    
    public static final long serialVersionUID = 8368428344107186165L;

    // Fields
    private long __workflow_execution_id;
    private String __tag;
    private String __value;

    public Attributes(long id) {
      super(id);
    }

    public Attributes(long id, final long workflow_execution_id, final String tag, final String value) {
      super(id);
      this.__workflow_execution_id = workflow_execution_id;
      this.__tag = tag;
      this.__value = value;
    }

    public static Attributes newDefaultInstance(long id) {
      return new Attributes(id, 0L, "", "");
    }

    public Attributes(long id, Map<Enum, Object> fieldsMap) {
      super(id);
      long workflow_execution_id = (Long)fieldsMap.get(ExecutionTag._Fields.workflow_execution_id);
      String tag = (String)fieldsMap.get(ExecutionTag._Fields.tag);
      String value = (String)fieldsMap.get(ExecutionTag._Fields.value);
      this.__workflow_execution_id = workflow_execution_id;
      this.__tag = tag;
      this.__value = value;
    }

    public Attributes(Attributes other) {
      super(other.getId());
      this.__workflow_execution_id = other.getWorkflowExecutionId();
      this.__tag = other.getTag();
      this.__value = other.getValue();
    }

    public long getWorkflowExecutionId() {
      return __workflow_execution_id;
    }

    public Attributes setWorkflowExecutionId(long newval) {
      this.__workflow_execution_id = newval;
      cachedHashCode = 0;
      return this;
    }

    public String getTag() {
      return __tag;
    }

    public Attributes setTag(String newval) {
      this.__tag = newval;
      cachedHashCode = 0;
      return this;
    }

    public String getValue() {
      return __value;
    }

    public Attributes setValue(String newval) {
      this.__value = newval;
      cachedHashCode = 0;
      return this;
    }

    public void setField(_Fields field, Object value) {
      switch (field) {
        case workflow_execution_id:
          setWorkflowExecutionId((Long)value);
          break;
        case tag:
          setTag((String)value);
          break;
        case value:
          setValue((String)value);
          break;
        default:
          throw new IllegalStateException("Invalid field: " + field);
      }
    }

    public void setField(String fieldName, Object value) {
      if (fieldName.equals("workflow_execution_id")) {
        setWorkflowExecutionId((Long)value);
        return;
      }
      if (fieldName.equals("tag")) {
        setTag((String)value);
        return;
      }
      if (fieldName.equals("value")) {
        setValue((String)value);
        return;
      }
      throw new IllegalStateException("Invalid field: " + fieldName);
    }

    public static Class getFieldType(_Fields field) {
      switch (field) {
        case workflow_execution_id:
          return long.class;
        case tag:
          return String.class;
        case value:
          return String.class;
        default:
          throw new IllegalStateException("Invalid field: " + field);
      }    
    }

    public static Class getFieldType(String fieldName) {    
      if (fieldName.equals("workflow_execution_id")) {
        return long.class;
      }
      if (fieldName.equals("tag")) {
        return String.class;
      }
      if (fieldName.equals("value")) {
        return String.class;
      }
      throw new IllegalStateException("Invalid field name: " + fieldName);
    }

    @Override
    public Object getField(String fieldName) {
      if (fieldName.equals("id")) {
        return getId();
      }
      if (fieldName.equals("workflow_execution_id")) {
        return getWorkflowExecutionId();
      }
      if (fieldName.equals("tag")) {
        return getTag();
      }
      if (fieldName.equals("value")) {
        return getValue();
      }
      throw new IllegalStateException("Invalid field name: " + fieldName);
    }

    public Object getField(_Fields field) {
      switch (field) {
        case workflow_execution_id:
          return getWorkflowExecutionId();
        case tag:
          return getTag();
        case value:
          return getValue();
      }
      throw new IllegalStateException("Invalid field: " + field);
    }

    public boolean hasField(String fieldName) {
      if (fieldName.equals("id")) {
        return true;
      }
      if (fieldName.equals("workflow_execution_id")) {
        return true;
      }
      if (fieldName.equals("tag")) {
        return true;
      }
      if (fieldName.equals("value")) {
        return true;
      }
      return false;
    }

    public static Object getDefaultValue(_Fields field) {
      switch (field) {
        case workflow_execution_id:
          return null;
        case tag:
          return null;
        case value:
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
      return "<ExecutionTag.Attributes"
          + " workflow_execution_id: " + getWorkflowExecutionId()
          + " tag: " + getTag()
          + " value: " + getValue()
          + ">";
    }
  }

  public static class Id implements ModelIdWrapper<ExecutionTag.Id> {
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
      return "<ExecutionTag.Id: " + this.getId() + ">";
    }
  }

  public static Set<Attributes> convertToAttributesSet(Collection<ExecutionTag> models) {
    return models.stream()
        .map(ExecutionTag::getAttributes)
        .collect(Collectors.toSet());
  }

  public static class AssociationMetadata implements IModelAssociationMetadata {

    private List<IAssociationMetadata> meta = new ArrayList<>();

    public AssociationMetadata(){
      meta.add(new DefaultAssociationMetadata(AssociationType.BELONGS_TO, ExecutionTag.class, WorkflowExecution.class, "workflow_execution_id"));
    }

    @Override
    public List<IAssociationMetadata> getAssociationMetadata() {
      return meta;
    }
  }

}
