package com.liveramp.cascading_ext.resource;

public class ReadResourceContainer<T> implements ReadResource<T> {
  Resource resource = null;

  public void setResource(ReadResource<T> readResource) {
    resource = readResource;
  }

  @Override
  public String getId() {
    return resource.getId();
  }
}
