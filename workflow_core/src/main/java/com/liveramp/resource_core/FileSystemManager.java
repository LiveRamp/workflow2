package com.liveramp.resource_core;

import java.io.IOException;

import com.liveramp.cascading_ext.resource.ResourceDeclarer;
import com.liveramp.cascading_ext.resource.ResourceDeclarerContainer;
import com.liveramp.cascading_ext.resource.RootManager;
import com.liveramp.cascading_ext.resource.storage.FileSystemRootDeterminer;
import com.liveramp.cascading_ext.resource.storage.FileSystemStorage;

public class FileSystemManager {

  public static ResourceDeclarer fileSystemResourceManager(String workflowRoot) throws IOException {
    return new ResourceDeclarerContainer<>(
        new ResourceDeclarerContainer.MethodNameTagger(),
        new RootManager<>(
            new FileSystemRootDeterminer(workflowRoot),
            new FileSystemStorage.Factory())
    );
  }
}
