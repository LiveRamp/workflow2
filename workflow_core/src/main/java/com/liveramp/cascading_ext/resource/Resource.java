package com.liveramp.cascading_ext.resource;

import java.io.Serializable;

public interface Resource<T> extends Serializable{
  String getId();
}
