package com.liveramp.cascading_ext.resource.storage;

import java.io.File;
import java.io.IOException;

import org.codehaus.plexus.util.FileUtils;

import com.liveramp.cascading_ext.resource.RootDeterminer;

public class FileSystemRootDeterminer implements RootDeterminer<String> {

  public static final String RESOURCE_SUB_DIR = "/resources/";
  private final String workflowRoot;

  public FileSystemRootDeterminer(String workflowRoot) {
    this.workflowRoot = workflowRoot + RESOURCE_SUB_DIR;
  }

  @Override
  public String getResourceRoot(long version, String versionType) throws IOException {

    String versionRoot = workflowRoot+"/"+versionType+"/"+version;

    //  this was started using a version, so continue using it
    if(FileUtils.fileExists(versionRoot)){
      return versionRoot;
    }

    //  we're starting from scratch.  create the version dir and return it
    FileUtils.forceMkdir(new File(versionRoot));
    return versionRoot;

  }

}
