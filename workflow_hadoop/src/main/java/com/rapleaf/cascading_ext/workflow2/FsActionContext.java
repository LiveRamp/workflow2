package com.rapleaf.cascading_ext.workflow2;

import com.liveramp.cascading_ext.FileSystemHelper;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;

import java.io.IOException;

public class FsActionContext {

  private final String tmpRoot;
  private FileSystem fs;

  public FsActionContext(String parentRoot, String checkpointToken) {

    if (parentRoot != null) {
      this.tmpRoot = parentRoot + "/" + checkpointToken + "-tmp-stores";
    } else {
      this.tmpRoot = null;
    }

  }

  public final boolean hasTmpRoot(){
    return tmpRoot != null;
  }

  public final String getTmpRoot() {
    if (tmpRoot == null) {
      throw new RuntimeException("Temp root not set for action " + this.toString());
    }
    return tmpRoot;
  }

  public FileSystem getFS() throws IOException {
    if (fs == null) {
      if (tmpRoot == null) {
        fs = FileSystemHelper.getFS();
      } else {
        fs = FileSystemHelper.getFileSystemForPath(tmpRoot, new Configuration());
      }
    }

    return fs;
  }

}
