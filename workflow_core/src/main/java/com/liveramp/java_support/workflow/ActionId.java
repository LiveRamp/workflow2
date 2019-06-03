package com.liveramp.java_support.workflow;

public class ActionId {

  private String relativeName;
  private String parentPrefix;

  public ActionId(String relativeName) {
    this.relativeName = relativeName;
  }

  public ActionId setRelativeName(String relativeName) {
    this.relativeName = relativeName;
    return this;
  }


  public ActionId setParentPrefix(String parentName) {
    this.parentPrefix = parentName;
    return this;
  }

  public String resolve() {

    if(relativeName == null){
      throw new RuntimeException("Cannot resolve ID without relative name set");
    }

    if (parentPrefix == null) {
      throw new RuntimeException("Cannot resolve ID without parent set");
    }

    return parentPrefix + relativeName;
  }

  public String getRelativeName() {
    return relativeName;
  }

  @Override
  public String toString() {
    return "ActionId{" +
        "relativeName='" + relativeName + '\'' +
        ", parentPrefix='" + parentPrefix + '\'' +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ActionId)) {
      return false;
    }

    ActionId actionId = (ActionId)o;

    if (parentPrefix != null ? !parentPrefix.equals(actionId.parentPrefix) : actionId.parentPrefix != null) {
      return false;
    }
    if (relativeName != null ? !relativeName.equals(actionId.relativeName) : actionId.relativeName != null) {
      return false;
    }

    return true;
  }

  @Override
  //  IMPORTANT it is very important that parent prefix not end up in this hash code.  I don't like it either.  But
  //  it's inserted into a set before it has a value, so if we change it after insertion , we lose it in the map
  public int hashCode() {
    return relativeName != null ? relativeName.hashCode() : 0;
  }
}