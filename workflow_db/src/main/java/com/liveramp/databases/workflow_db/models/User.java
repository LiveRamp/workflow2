
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

public class User extends ModelWithId<User, IDatabases> implements Comparable<User>{
  
  public static final long serialVersionUID = 4398900334759283010L;

  public static class Tbl extends AbstractTable<User.Attributes, User> {
    public final Column<Long> ID;
    public final Column<String> USERNAME;
    public final Column<String> NOTIFICATION_EMAIL;

    private Tbl(String alias) {
      super("users", alias, User.Attributes.class, User.class);
      this.ID = Column.fromId(alias);
      this.USERNAME = Column.fromField(alias, _Fields.username, String.class);
      this.NOTIFICATION_EMAIL = Column.fromField(alias, _Fields.notification_email, String.class);
      Collections.addAll(this.allColumns, ID, USERNAME, NOTIFICATION_EMAIL);
    }

    public static Tbl as(String alias) {
      return new Tbl(alias);
    }
  }

  public static final Tbl TBL = new Tbl("users");
  public static final Column<Long> ID = TBL.ID;
  public static final Column<String> USERNAME = TBL.USERNAME;
  public static final Column<String> NOTIFICATION_EMAIL = TBL.NOTIFICATION_EMAIL;

  private final Attributes attributes;

  private transient User.Id cachedTypedId;

  // Associations
  private HasManyAssociation<UserDashboard> __assoc_user_dashboards;
  private HasManyAssociation<Dashboard> __assoc_dashboards;

  public enum _Fields {
    username,
    notification_email,
  }

  @Override
  public User.Id getTypedId() {
    if (cachedTypedId == null) {
      cachedTypedId = new User.Id(this.getId());
    }
    return cachedTypedId;
  }

  public User(long id, final String username, final String notification_email, IDatabases databases) {
    super(databases);
    attributes = new Attributes(id, username, notification_email);
    this.__assoc_user_dashboards = new HasManyAssociation<>(databases.getWorkflowDb().userDashboards(), "user_id", getId());
    this.__assoc_dashboards = new HasManyAssociation<>(databases.getWorkflowDb().dashboards(), "user_id", getId());
  }

  public User(long id, final String username, final String notification_email) {
    super(null);
    attributes = new Attributes(id, username, notification_email);
  }

  public static User newDefaultInstance(long id) {
    return new User(id, "", "");
  }

  public User(Attributes attributes, IDatabases databases) {
    super(databases);
    this.attributes = attributes;

    if (databases != null) {
      this.__assoc_user_dashboards = new HasManyAssociation<>(databases.getWorkflowDb().userDashboards(), "user_id", getId());
      this.__assoc_dashboards = new HasManyAssociation<>(databases.getWorkflowDb().dashboards(), "user_id", getId());
    }
  }

  public User(Attributes attributes) {
    this(attributes, (IDatabases) null);
  }

  public User(long id, Map<Enum, Object> fieldsMap) {
    super(null);
    attributes = new Attributes(id, fieldsMap);
  }

  public User (User other) {
    this(other, (IDatabases)null);
  }

  public User (User other, IDatabases databases) {
    super(databases);
    attributes = new Attributes(other.getAttributes());

    if (databases != null) {
      this.__assoc_user_dashboards = new HasManyAssociation<>(databases.getWorkflowDb().userDashboards(), "user_id", getId());
      this.__assoc_dashboards = new HasManyAssociation<>(databases.getWorkflowDb().dashboards(), "user_id", getId());
    }
  }

  public Attributes getAttributes() {
    return attributes;
  }

  public String getUsername() {
    return attributes.getUsername();
  }

  public User setUsername(String newval) {
    attributes.setUsername(newval);
    cachedHashCode = 0;
    return this;
  }

  public String getNotificationEmail() {
    return attributes.getNotificationEmail();
  }

  public User setNotificationEmail(String newval) {
    attributes.setNotificationEmail(newval);
    cachedHashCode = 0;
    return this;
  }

  public void setField(_Fields field, Object value) {
    switch (field) {
      case username:
        setUsername((String)value);
        break;
      case notification_email:
        setNotificationEmail((String)value);
        break;
      default:
        throw new IllegalStateException("Invalid field: " + field);
    }
  }
  
  public void setField(String fieldName, Object value) {
    if (fieldName.equals("username")) {
      setUsername((String)  value);
      return;
    }
    if (fieldName.equals("notification_email")) {
      setNotificationEmail((String)  value);
      return;
    }
    throw new IllegalStateException("Invalid field: " + fieldName);
  }

  public static Class getFieldType(_Fields field) {
    switch (field) {
      case username:
        return String.class;
      case notification_email:
        return String.class;
      default:
        throw new IllegalStateException("Invalid field: " + field);
    }    
  }

  public static Class getFieldType(String fieldName) {    
    if (fieldName.equals("username")) {
      return String.class;
    }
    if (fieldName.equals("notification_email")) {
      return String.class;
    }
    throw new IllegalStateException("Invalid field name: " + fieldName);
  }

  public List<UserDashboard> getUserDashboards() throws IOException {
    return __assoc_user_dashboards.get();
  }

  public List<Dashboard> getDashboards() throws IOException {
    return __assoc_dashboards.get();
  }

  @Override
  public Object getField(String fieldName) {
    if (fieldName.equals("id")) {
      return getId();
    }
    if (fieldName.equals("username")) {
      return getUsername();
    }
    if (fieldName.equals("notification_email")) {
      return getNotificationEmail();
    }
    throw new IllegalStateException("Invalid field name: " + fieldName);
  }

  public Object getField(_Fields field) {
    switch (field) {
      case username:
        return getUsername();
      case notification_email:
        return getNotificationEmail();
    }
    throw new IllegalStateException("Invalid field: " + field);
  }

  public boolean hasField(String fieldName) {
    if (fieldName.equals("id")) {
      return true;
    }
    if (fieldName.equals("username")) {
      return true;
    }
    if (fieldName.equals("notification_email")) {
      return true;
    }
    return false;
  }

  public static Object getDefaultValue(_Fields field) {
    switch (field) {
      case username:
        return null;
      case notification_email:
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
  public User getCopy() {
    return getCopy(databases);
  }

  @Override
  public User getCopy(IDatabases databases) {
    return new User(this, databases);
  }

  @Override
  public boolean save() throws IOException {
    return databases.getWorkflowDb().users().save(this);
  }

  public String toString() {
    return "<User"
        + " id: " + this.getId()
        + " username: " + getUsername()
        + " notification_email: " + getNotificationEmail()
        + ">";
  }

  public void unsetAssociations() {
    unsetDatabaseReference();
    __assoc_user_dashboards = null;
    __assoc_dashboards = null;
  }

  public int compareTo(User that) {
    return Long.valueOf(this.getId()).compareTo(that.getId());
  }
  
  
  public static class Attributes extends AttributesWithId {
    
    public static final long serialVersionUID = 4371739416569918865L;

    // Fields
    private String __username;
    private String __notification_email;

    public Attributes(long id) {
      super(id);
    }

    public Attributes(long id, final String username, final String notification_email) {
      super(id);
      this.__username = username;
      this.__notification_email = notification_email;
    }

    public static Attributes newDefaultInstance(long id) {
      return new Attributes(id, "", "");
    }

    public Attributes(long id, Map<Enum, Object> fieldsMap) {
      super(id);
      String username = (String)fieldsMap.get(User._Fields.username);
      String notification_email = (String)fieldsMap.get(User._Fields.notification_email);
      this.__username = username;
      this.__notification_email = notification_email;
    }

    public Attributes(Attributes other) {
      super(other.getId());
      this.__username = other.getUsername();
      this.__notification_email = other.getNotificationEmail();
    }

    public String getUsername() {
      return __username;
    }

    public Attributes setUsername(String newval) {
      this.__username = newval;
      cachedHashCode = 0;
      return this;
    }

    public String getNotificationEmail() {
      return __notification_email;
    }

    public Attributes setNotificationEmail(String newval) {
      this.__notification_email = newval;
      cachedHashCode = 0;
      return this;
    }

    public void setField(_Fields field, Object value) {
      switch (field) {
        case username:
          setUsername((String)value);
          break;
        case notification_email:
          setNotificationEmail((String)value);
          break;
        default:
          throw new IllegalStateException("Invalid field: " + field);
      }
    }

    public void setField(String fieldName, Object value) {
      if (fieldName.equals("username")) {
        setUsername((String)value);
        return;
      }
      if (fieldName.equals("notification_email")) {
        setNotificationEmail((String)value);
        return;
      }
      throw new IllegalStateException("Invalid field: " + fieldName);
    }

    public static Class getFieldType(_Fields field) {
      switch (field) {
        case username:
          return String.class;
        case notification_email:
          return String.class;
        default:
          throw new IllegalStateException("Invalid field: " + field);
      }    
    }

    public static Class getFieldType(String fieldName) {    
      if (fieldName.equals("username")) {
        return String.class;
      }
      if (fieldName.equals("notification_email")) {
        return String.class;
      }
      throw new IllegalStateException("Invalid field name: " + fieldName);
    }

    @Override
    public Object getField(String fieldName) {
      if (fieldName.equals("id")) {
        return getId();
      }
      if (fieldName.equals("username")) {
        return getUsername();
      }
      if (fieldName.equals("notification_email")) {
        return getNotificationEmail();
      }
      throw new IllegalStateException("Invalid field name: " + fieldName);
    }

    public Object getField(_Fields field) {
      switch (field) {
        case username:
          return getUsername();
        case notification_email:
          return getNotificationEmail();
      }
      throw new IllegalStateException("Invalid field: " + field);
    }

    public boolean hasField(String fieldName) {
      if (fieldName.equals("id")) {
        return true;
      }
      if (fieldName.equals("username")) {
        return true;
      }
      if (fieldName.equals("notification_email")) {
        return true;
      }
      return false;
    }

    public static Object getDefaultValue(_Fields field) {
      switch (field) {
        case username:
          return null;
        case notification_email:
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
      return "<User.Attributes"
          + " username: " + getUsername()
          + " notification_email: " + getNotificationEmail()
          + ">";
    }
  }

  public static class Id implements ModelIdWrapper<User.Id> {
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
      return "<User.Id: " + this.getId() + ">";
    }
  }

  public static Set<Attributes> convertToAttributesSet(Collection<User> models) {
    return models.stream()
        .map(User::getAttributes)
        .collect(Collectors.toSet());
  }

  public static class AssociationMetadata implements IModelAssociationMetadata {

    private List<IAssociationMetadata> meta = new ArrayList<>();

    public AssociationMetadata(){
      meta.add(new DefaultAssociationMetadata(AssociationType.HAS_MANY, User.class, UserDashboard.class, "user_id"));
      meta.add(new DefaultAssociationMetadata(AssociationType.HAS_MANY, User.class, Dashboard.class, "user_id"));
    }

    @Override
    public List<IAssociationMetadata> getAssociationMetadata() {
      return meta;
    }
  }

}