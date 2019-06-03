package com.liveramp.cascading_ext.resource;

import java.io.IOException;

public class InMemoryResourceManager {
  public static ResourceDeclarer create() throws IOException {
    return new ResourceDeclarerContainer<>(
        new ResourceDeclarerContainer.MethodNameTagger(),
        new RootManager<>(
            new InMemoryStorageRootDeterminer(),
            new InMemoryStorage.Factory())
    );
  }
}
