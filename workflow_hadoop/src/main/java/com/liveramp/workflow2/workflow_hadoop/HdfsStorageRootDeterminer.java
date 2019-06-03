package com.liveramp.workflow2.workflow_hadoop;

import java.io.IOException;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.liveramp.cascading_ext.FileSystemHelper;
import com.liveramp.cascading_ext.resource.RootDeterminer;

public class HdfsStorageRootDeterminer implements RootDeterminer<String> {

  public static final String RESOURCE_SUB_DIR = "/resources/";
  private final String workflowRoot;
  private final FileSystem fs;

  public HdfsStorageRootDeterminer(String workflowRoot){
    this(FileSystemHelper.getFileSystemForPath(workflowRoot), workflowRoot);
  }

  public HdfsStorageRootDeterminer(FileSystem fs, String workflowRoot) {
    this.workflowRoot = workflowRoot + RESOURCE_SUB_DIR;
    this.fs = fs;
  }

  @Override
  public String getResourceRoot(long version, String versionType) throws IOException {

    String versionRoot = workflowRoot+"/"+versionType+"/"+version;
    Path versionRootPath = new Path(versionRoot);

    //  this was started using a version, so continue using it
    if(fs.exists(versionRootPath)){
      return versionRoot;
    }

    //  we're starting from scratch.  create the version dir and return it
    fs.mkdirs(versionRootPath);
    return versionRoot;

  }

}
