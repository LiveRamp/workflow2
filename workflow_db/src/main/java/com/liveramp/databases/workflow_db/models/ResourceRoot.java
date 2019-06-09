
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

public class ResourceRoot extends ModelWithId<ResourceRoot, IDatabases> implements Comparable<ResourceRoot>{
  
  public static final long serialVersionUID = -1648774263051010369L;

  public static class Tbl extends AbstractTable<ResourceRoot.Attributes, ResourceRoot> {
    public final Column<Long> ID;
    public final Column<String> NAME;
    public final Column<Long> CREATED_AT;
    public final Column<Long> UPDATED_AT;
    public final Column<String> SCOPE_IDENTIFIER;
    public final Column<Long> VERSION;
    public final Column<String> VERSION_TYPE;

    private Tbl(String alias) {
      super("resource_roots", alias, ResourceRoot.Attributes.class, ResourceRoot.class);
      this.ID = Column.fromId(alias);
      this.NAME = Column.fromField(alias, _Fields.name, String.class);
      this.CREATED_AT = Column.fromTimestamp(alias, _Fields.created_at);
      this.UPDATED_AT = Column.fromTimestamp(alias, _Fields.updated_at);
      this.SCOPE_IDENTIFIER = Column.fromField(alias, _Fields.scope_identifier, String.class);
      this.VERSION = Column.fromField(alias, _Fields.version, Long.class);
      this.VERSION_TYPE = Column.fromField(alias, _Fields.version_type, String.class);
      Collections.addAll(this.allColumns, ID, NAME, CREATED_AT, UPDATED_AT, SCOPE_IDENTIFIER, VERSION, VERSION_TYPE);
    }

    public static Tbl as(String alias) {
      return new Tbl(alias);
    }
  }

  public static final Tbl TBL = new Tbl("resource_roots");
  public static final Column<Long> ID = TBL.ID;
  public static final Column<String> NAME = TBL.NAME;
  public static final Column<Long> CREATED_AT = TBL.CREATED_AT;
  public static final Column<Long> UPDATED_AT = TBL.UPDATED_AT;
  public static final Column<String> SCOPE_IDENTIFIER = TBL.SCOPE_IDENTIFIER;
  public static final Column<Long> VERSION = TBL.VERSION;
  public static final Column<String> VERSION_TYPE = TBL.VERSION_TYPE;

  private final Attributes attributes;

  private transient ResourceRoot.Id cachedTypedId;

  // Associations
  private HasManyAssociation<ResourceRecord> __assoc_resource_records;

  public enum _Fields {
    name,
    created_at,
    updated_at,
    scope_identifier,
    version,
    version_type,
  }

  @Override
  public ResourceRoot.Id getTypedId() {
    if (cachedTypedId == null) {
      cachedTypedId = new ResourceRoot.Id(this.getId());
    }
    return cachedTypedId;
  }

  public ResourceRoot(long id, final String name, final Long created_at, final Long updated_at, final String scope_identifier, final Long version, final String version_type, IDatabases databases) {
    super(databases);
    attributes = new Attributes(id, name, created_at, updated_at, scope_identifier, version, version_type);
    this.__assoc_resource_records = new HasManyAssociation<>(databases.getWorkflowDb().resourceRecords(), "resource_root_id", getId());
  }

  public ResourceRoot(long id, final String name, final Long created_at, final Long updated_at, final String scope_identifier, final Long version, final String version_type) {
    super(null);
    attributes = new Attributes(id, name, created_at, updated_at, scope_identifier, version, version_type);
  }
  
  public ResourceRoot(long id, IDatabases databases) {
    super(databases);
    attributes = new Attributes(id);
    this.__assoc_resource_records = new HasManyAssociation<>(databases.getWorkflowDb().resourceRecords(), "resource_root_id", getId());
  }

  public ResourceRoot(long id) {
    super(null);
    attributes = new Attributes(id);
  }

  public static ResourceRoot newDefaultInstance(long id) {
    return new ResourceRoot(id);
  }

  public ResourceRoot(Attributes attributes, IDatabases databases) {
    super(databases);
    this.attributes = attributes;

    if (databases != null) {
      this.__assoc_resource_records = new HasManyAssociation<>(databases.getWorkflowDb().resourceRecords(), "resource_root_id", getId());
    }
  }

  public ResourceRoot(Attributes attributes) {
    this(attributes, (IDatabases) null);
  }

  public ResourceRoot(long id, Map<Enum, Object> fieldsMap) {
    super(null);
    attributes = new Attributes(id, fieldsMap);
  }

  public ResourceRoot (ResourceRoot other) {
    this(other, (IDatabases)null);
  }

  public ResourceRoot (ResourceRoot other, IDatabases databases) {
    super(databases);
    attributes = new Attributes(other.getAttributes());

    if (databases != null) {
      this.__assoc_resource_records = new HasManyAssociation<>(databases.getWorkflowDb().resourceRecords(), "resource_root_id", getId());
    }
  }

  public Attributes getAttributes() {
    return attributes;
  }

  public String getName() {
    return attributes.getName();
  }

  public ResourceRoot setName(String newval) {
    attributes.setName(newval);
    cachedHashCode = 0;
    return this;
  }

  public Long getCreatedAt() {
    return attributes.getCreatedAt();
  }

  public ResourceRoot setCreatedAt(Long newval) {
    attributes.setCreatedAt(newval);
    cachedHashCode = 0;
    return this;
  }

  public Long getUpdatedAt() {
    return attributes.getUpdatedAt();
  }

  public ResourceRoot setUpdatedAt(Long newval) {
    attributes.setUpdatedAt(newval);
    cachedHashCode = 0;
    return this;
  }

  public String getScopeIdentifier() {
    return attributes.getScopeIdentifier();
  }

  public ResourceRoot setScopeIdentifier(String newval) {
    attributes.setScopeIdentifier(newval);
    cachedHashCode = 0;
    return this;
  }

  public Long getVersion() {
    return attributes.getVersion();
  }

  public ResourceRoot setVersion(Long newval) {
    attributes.setVersion(newval);
    cachedHashCode = 0;
    return this;
  }

  public String getVersionType() {
    return attributes.getVersionType();
  }

  public ResourceRoot setVersionType(String newval) {
    attributes.setVersionType(newval);
    cachedHashCode = 0;
    return this;
  }

  public void setField(_Fields field, Object value) {
    switch (field) {
      case name:
        setName((String)value);
        break;
      case created_at:
        setCreatedAt((Long)value);
        break;
      case updated_at:
        setUpdatedAt((Long)value);
        break;
      case scope_identifier:
        setScopeIdentifier((String)value);
        break;
      case version:
        setVersion((Long)value);
        break;
      case version_type:
        setVersionType((String)value);
        break;
      default:
        throw new IllegalStateException("Invalid field: " + field);
    }
  }
  
  public void setField(String fieldName, Object value) {
    if (fieldName.equals("name")) {
      setName((String)  value);
      return;
    }
    if (fieldName.equals("created_at")) {
      setCreatedAt((Long)  value);
      return;
    }
    if (fieldName.equals("updated_at")) {
      setUpdatedAt((Long)  value);
      return;
    }
    if (fieldName.equals("scope_identifier")) {
      setScopeIdentifier((String)  value);
      return;
    }
    if (fieldName.equals("version")) {
      setVersion((Long)  value);
      return;
    }
    if (fieldName.equals("version_type")) {
      setVersionType((String)  value);
      return;
    }
    throw new IllegalStateException("Invalid field: " + fieldName);
  }

  public static Class getFieldType(_Fields field) {
    switch (field) {
      case name:
        return String.class;
      case created_at:
        return Long.class;
      case updated_at:
        return Long.class;
      case scope_identifier:
        return String.class;
      case version:
        return Long.class;
      case version_type:
        return String.class;
      default:
        throw new IllegalStateException("Invalid field: " + field);
    }    
  }

  public static Class getFieldType(String fieldName) {    
    if (fieldName.equals("name")) {
      return String.class;
    }
    if (fieldName.equals("created_at")) {
      return Long.class;
    }
    if (fieldName.equals("updated_at")) {
      return Long.class;
    }
    if (fieldName.equals("scope_identifier")) {
      return String.class;
    }
    if (fieldName.equals("version")) {
      return Long.class;
    }
    if (fieldName.equals("version_type")) {
      return String.class;
    }
    throw new IllegalStateException("Invalid field name: " + fieldName);
  }

  public List<ResourceRecord> getResourceRecords() throws IOException {
    return __assoc_resource_records.get();
  }

  @Override
  public Object getField(String fieldName) {
    if (fieldName.equals("id")) {
      return getId();
    }
    if (fieldName.equals("name")) {
      return getName();
    }
    if (fieldName.equals("created_at")) {
      return getCreatedAt();
    }
    if (fieldName.equals("updated_at")) {
      return getUpdatedAt();
    }
    if (fieldName.equals("scope_identifier")) {
      return getScopeIdentifier();
    }
    if (fieldName.equals("version")) {
      return getVersion();
    }
    if (fieldName.equals("version_type")) {
      return getVersionType();
    }
    throw new IllegalStateException("Invalid field name: " + fieldName);
  }

  public Object getField(_Fields field) {
    switch (field) {
      case name:
        return getName();
      case created_at:
        return getCreatedAt();
      case updated_at:
        return getUpdatedAt();
      case scope_identifier:
        return getScopeIdentifier();
      case version:
        return getVersion();
      case version_type:
        return getVersionType();
    }
    throw new IllegalStateException("Invalid field: " + field);
  }

  public boolean hasField(String fieldName) {
    if (fieldName.equals("id")) {
      return true;
    }
    if (fieldName.equals("name")) {
      return true;
    }
    if (fieldName.equals("created_at")) {
      return true;
    }
    if (fieldName.equals("updated_at")) {
      return true;
    }
    if (fieldName.equals("scope_identifier")) {
      return true;
    }
    if (fieldName.equals("version")) {
      return true;
    }
    if (fieldName.equals("version_type")) {
      return true;
    }
    return false;
  }

  public static Object getDefaultValue(_Fields field) {
    switch (field) {
      case name:
        return null;
      case created_at:
        return null;
      case updated_at:
        return null;
      case scope_identifier:
        return null;
      case version:
        return null;
      case version_type:
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
  public ResourceRoot getCopy() {
    return getCopy(databases);
  }

  @Override
  public ResourceRoot getCopy(IDatabases databases) {
    return new ResourceRoot(this, databases);
  }

  @Override
  public boolean save() throws IOException {
    return databases.getWorkflowDb().resourceRoots().save(this);
  }

  public String toString() {
    return "<ResourceRoot"
        + " id: " + this.getId()
        + " name: " + getName()
        + " created_at: " + getCreatedAt()
        + " updated_at: " + getUpdatedAt()
        + " scope_identifier: " + getScopeIdentifier()
        + " version: " + getVersion()
        + " version_type: " + getVersionType()
        + ">";
  }

  public void unsetAssociations() {
    unsetDatabaseReference();
    __assoc_resource_records = null;
  }

  public int compareTo(ResourceRoot that) {
    return Long.valueOf(this.getId()).compareTo(that.getId());
  }
  
  
  public static class Attributes extends AttributesWithId {
    
    public static final long serialVersionUID = -2227891514620149012L;

    // Fields
    private String __name;
    private Long __created_at;
    private Long __updated_at;
    private String __scope_identifier;
    private Long __version;
    private String __version_type;

    public Attributes(long id) {
      super(id);
    }

    public Attributes(long id, final String name, final Long created_at, final Long updated_at, final String scope_identifier, final Long version, final String version_type) {
      super(id);
      this.__name = name;
      this.__created_at = created_at;
      this.__updated_at = updated_at;
      this.__scope_identifier = scope_identifier;
      this.__version = version;
      this.__version_type = version_type;
    }

    public static Attributes newDefaultInstance(long id) {
      return new Attributes(id);
    }

    public Attributes(long id, Map<Enum, Object> fieldsMap) {
      super(id);
      String name = (String)fieldsMap.get(ResourceRoot._Fields.name);
      Long created_at = (Long)fieldsMap.get(ResourceRoot._Fields.created_at);
      Long updated_at = (Long)fieldsMap.get(ResourceRoot._Fields.updated_at);
      String scope_identifier = (String)fieldsMap.get(ResourceRoot._Fields.scope_identifier);
      Long version = (Long)fieldsMap.get(ResourceRoot._Fields.version);
      String version_type = (String)fieldsMap.get(ResourceRoot._Fields.version_type);
      this.__name = name;
      this.__created_at = created_at;
      this.__updated_at = updated_at;
      this.__scope_identifier = scope_identifier;
      this.__version = version;
      this.__version_type = version_type;
    }

    public Attributes(Attributes other) {
      super(other.getId());
      this.__name = other.getName();
      this.__created_at = other.getCreatedAt();
      this.__updated_at = other.getUpdatedAt();
      this.__scope_identifier = other.getScopeIdentifier();
      this.__version = other.getVersion();
      this.__version_type = other.getVersionType();
    }

    public String getName() {
      return __name;
    }

    public Attributes setName(String newval) {
      this.__name = newval;
      cachedHashCode = 0;
      return this;
    }

    public Long getCreatedAt() {
      return __created_at;
    }

    public Attributes setCreatedAt(Long newval) {
      this.__created_at = newval;
      cachedHashCode = 0;
      return this;
    }

    public Long getUpdatedAt() {
      return __updated_at;
    }

    public Attributes setUpdatedAt(Long newval) {
      this.__updated_at = newval;
      cachedHashCode = 0;
      return this;
    }

    public String getScopeIdentifier() {
      return __scope_identifier;
    }

    public Attributes setScopeIdentifier(String newval) {
      this.__scope_identifier = newval;
      cachedHashCode = 0;
      return this;
    }

    public Long getVersion() {
      return __version;
    }

    public Attributes setVersion(Long newval) {
      this.__version = newval;
      cachedHashCode = 0;
      return this;
    }

    public String getVersionType() {
      return __version_type;
    }

    public Attributes setVersionType(String newval) {
      this.__version_type = newval;
      cachedHashCode = 0;
      return this;
    }

    public void setField(_Fields field, Object value) {
      switch (field) {
        case name:
          setName((String)value);
          break;
        case created_at:
          setCreatedAt((Long)value);
          break;
        case updated_at:
          setUpdatedAt((Long)value);
          break;
        case scope_identifier:
          setScopeIdentifier((String)value);
          break;
        case version:
          setVersion((Long)value);
          break;
        case version_type:
          setVersionType((String)value);
          break;
        default:
          throw new IllegalStateException("Invalid field: " + field);
      }
    }

    public void setField(String fieldName, Object value) {
      if (fieldName.equals("name")) {
        setName((String)value);
        return;
      }
      if (fieldName.equals("created_at")) {
        setCreatedAt((Long)value);
        return;
      }
      if (fieldName.equals("updated_at")) {
        setUpdatedAt((Long)value);
        return;
      }
      if (fieldName.equals("scope_identifier")) {
        setScopeIdentifier((String)value);
        return;
      }
      if (fieldName.equals("version")) {
        setVersion((Long)value);
        return;
      }
      if (fieldName.equals("version_type")) {
        setVersionType((String)value);
        return;
      }
      throw new IllegalStateException("Invalid field: " + fieldName);
    }

    public static Class getFieldType(_Fields field) {
      switch (field) {
        case name:
          return String.class;
        case created_at:
          return Long.class;
        case updated_at:
          return Long.class;
        case scope_identifier:
          return String.class;
        case version:
          return Long.class;
        case version_type:
          return String.class;
        default:
          throw new IllegalStateException("Invalid field: " + field);
      }    
    }

    public static Class getFieldType(String fieldName) {    
      if (fieldName.equals("name")) {
        return String.class;
      }
      if (fieldName.equals("created_at")) {
        return Long.class;
      }
      if (fieldName.equals("updated_at")) {
        return Long.class;
      }
      if (fieldName.equals("scope_identifier")) {
        return String.class;
      }
      if (fieldName.equals("version")) {
        return Long.class;
      }
      if (fieldName.equals("version_type")) {
        return String.class;
      }
      throw new IllegalStateException("Invalid field name: " + fieldName);
    }

    @Override
    public Object getField(String fieldName) {
      if (fieldName.equals("id")) {
        return getId();
      }
      if (fieldName.equals("name")) {
        return getName();
      }
      if (fieldName.equals("created_at")) {
        return getCreatedAt();
      }
      if (fieldName.equals("updated_at")) {
        return getUpdatedAt();
      }
      if (fieldName.equals("scope_identifier")) {
        return getScopeIdentifier();
      }
      if (fieldName.equals("version")) {
        return getVersion();
      }
      if (fieldName.equals("version_type")) {
        return getVersionType();
      }
      throw new IllegalStateException("Invalid field name: " + fieldName);
    }

    public Object getField(_Fields field) {
      switch (field) {
        case name:
          return getName();
        case created_at:
          return getCreatedAt();
        case updated_at:
          return getUpdatedAt();
        case scope_identifier:
          return getScopeIdentifier();
        case version:
          return getVersion();
        case version_type:
          return getVersionType();
      }
      throw new IllegalStateException("Invalid field: " + field);
    }

    public boolean hasField(String fieldName) {
      if (fieldName.equals("id")) {
        return true;
      }
      if (fieldName.equals("name")) {
        return true;
      }
      if (fieldName.equals("created_at")) {
        return true;
      }
      if (fieldName.equals("updated_at")) {
        return true;
      }
      if (fieldName.equals("scope_identifier")) {
        return true;
      }
      if (fieldName.equals("version")) {
        return true;
      }
      if (fieldName.equals("version_type")) {
        return true;
      }
      return false;
    }

    public static Object getDefaultValue(_Fields field) {
      switch (field) {
        case name:
          return null;
        case created_at:
          return null;
        case updated_at:
          return null;
        case scope_identifier:
          return null;
        case version:
          return null;
        case version_type:
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
      return "<ResourceRoot.Attributes"
          + " name: " + getName()
          + " created_at: " + getCreatedAt()
          + " updated_at: " + getUpdatedAt()
          + " scope_identifier: " + getScopeIdentifier()
          + " version: " + getVersion()
          + " version_type: " + getVersionType()
          + ">";
    }
  }

  public static class Id implements ModelIdWrapper<ResourceRoot.Id> {
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
      return "<ResourceRoot.Id: " + this.getId() + ">";
    }
  }

  public static Set<Attributes> convertToAttributesSet(Collection<ResourceRoot> models) {
    return models.stream()
        .map(ResourceRoot::getAttributes)
        .collect(Collectors.toSet());
  }

  public static class AssociationMetadata implements IModelAssociationMetadata {

    private List<IAssociationMetadata> meta = new ArrayList<>();

    public AssociationMetadata(){
      meta.add(new DefaultAssociationMetadata(AssociationType.HAS_MANY, ResourceRoot.class, ResourceRecord.class, "resource_root_id"));
    }

    @Override
    public List<IAssociationMetadata> getAssociationMetadata() {
      return meta;
    }
  }

}
