package com.liveramp.cascading_ext.resource;


public class WriteResourceContainer<T> implements WriteResource<T> {
  Resource resource = null;

  public void setResource(WriteResource<T> writeResource) {
    resource = writeResource;
  }

  @Override
  public String getId() {
    return resource.getId();
  }
}
