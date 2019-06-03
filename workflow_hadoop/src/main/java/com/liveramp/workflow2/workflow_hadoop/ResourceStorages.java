package com.liveramp.workflow2.workflow_hadoop;

import com.liveramp.cascading_ext.resource.InMemoryStorage;

public class ResourceStorages {

  public static HdfsStorage.Factory hdfsStorage() {
    return new HdfsStorage.Factory();
  }

  public static <ID> InMemoryStorage.Factory inMemoryStorage(Class<ID> idClass) {
    return new InMemoryStorage.Factory();
  }

  public static InMemoryStorage.Factory inMemoryStorage() {
    return new InMemoryStorage.Factory();
  }
}
