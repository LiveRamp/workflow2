package com.liveramp.cascading_ext.resource;

import java.io.IOException;

public class RootManager<RESOURCE_ROOT> {

  private final RootDeterminer<RESOURCE_ROOT> rootRootDeterminer;
  private final Storage.Factory<RESOURCE_ROOT> storage;

  public RootManager(RootDeterminer<RESOURCE_ROOT> rootDeterminer,
                     Storage.Factory<RESOURCE_ROOT> storage) throws IOException {
    this.rootRootDeterminer = rootDeterminer;
    this.storage = storage;
  }

  public Storage getStorage(long version, String versionType) throws IOException {
    RESOURCE_ROOT root = rootRootDeterminer.getResourceRoot(version, versionType);
    return storage.forResourceRoot(root);
  }

}
