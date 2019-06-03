package com.liveramp.cascading_ext.resource;

class ResourceImpl<T> implements WriteResource<T> {

  final String id;

  public ResourceImpl(String id) {
    this.id = id;
  }

  @Override
  public String getId() {
    return id;
  }
}
