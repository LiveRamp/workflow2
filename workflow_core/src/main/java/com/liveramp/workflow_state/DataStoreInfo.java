package com.liveramp.workflow_state;

public class DataStoreInfo {

  private final String name;
  private final String className;
  private final String path;

  public DataStoreInfo(String name, String className, String path) {
    this.name = name;
    this.className = className;
    this.path = path;
  }

  public String getName() {
    return name;
  }
  public String getClassName() {
    return className;
  }
  public String getPath() {
    return path;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof DataStoreInfo)) {
      return false;
    }

    DataStoreInfo that = (DataStoreInfo)o;

    if (className != null ? !className.equals(that.className) : that.className != null) {
      return false;
    }
    if (name != null ? !name.equals(that.name) : that.name != null) {
      return false;
    }
    if (path != null ? !path.equals(that.path) : that.path != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = name != null ? name.hashCode() : 0;
    result = 31 * result + (className != null ? className.hashCode() : 0);
    result = 31 * result + (path != null ? path.hashCode() : 0);
    return result;
  }
}
