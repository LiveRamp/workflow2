package com.liveramp.workflow2.workflow_hadoop;

import org.apache.hadoop.fs.Path;

public interface TmpDirFilter {
  public boolean isSkipTrash(Path dir);

  public static class Never implements TmpDirFilter {

    @Override
    public boolean isSkipTrash(Path dir) {
      return false;
    }

  }

}
