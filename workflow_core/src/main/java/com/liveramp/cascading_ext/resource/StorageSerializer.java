package com.liveramp.cascading_ext.resource;

public interface StorageSerializer {
  public String serialize(Object o);

  public Object deSerialize(String s);
}
