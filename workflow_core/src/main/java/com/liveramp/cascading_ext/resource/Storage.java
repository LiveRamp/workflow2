package com.liveramp.cascading_ext.resource;

import java.io.IOException;

//  TODO this should be a factory -- pretend with forResourceRoot for now
public interface Storage {

  <T> void store(String name, T object);

  <T> T retrieve(String name);

  boolean isStored(String name);

  public static interface Factory<RESOURCE_ROOT> {

    Storage forResourceRoot(RESOURCE_ROOT root) throws IOException;

  }

}
