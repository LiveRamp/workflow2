package com.liveramp.cascading_ext.resource;

import java.io.IOException;

public class InMemoryStorageRootDeterminer implements RootDeterminer<Void> {
  @Override
  public Void getResourceRoot(long version, String versionType) throws IOException {
    return null;
  }
}
