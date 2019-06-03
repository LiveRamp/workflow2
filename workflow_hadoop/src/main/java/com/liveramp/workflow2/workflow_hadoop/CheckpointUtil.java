package com.liveramp.workflow2.workflow_hadoop;

import java.io.IOException;

import com.google.gson.Gson;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.liveramp.cascading_ext.FileSystemHelper;
import com.liveramp.cascading_ext.fs.TrashHelper;
import com.rapleaf.cascading_ext.hdfs_utils.HdfsGsonHelper;

public class CheckpointUtil {
  public static final String EXECUTION_ID_OBJ = "__EXECUTION_ID";

  public static boolean existCheckpoints(Path checkpointDir) throws IOException {
    int checkpoints = 0;
    for (FileStatus fileStatus : FileSystemHelper.safeListStatus(checkpointDir)) {
      if (!fileStatus.getPath().getName().equals(EXECUTION_ID_OBJ)) {
        checkpoints++;
      }
    }

    return checkpoints > 0;
  }

  public static void clearCheckpoints(FileSystem fs, Path checkpointDir) throws IOException {
    for (FileStatus status : FileSystemHelper.safeListStatus(fs, checkpointDir)) {
      if (!status.getPath().getName().equals(EXECUTION_ID_OBJ)) {
        TrashHelper.deleteUsingTrashIfEnabled(fs, status.getPath());
      }
    }
  }

  public static long getLatestExecutionId(FileSystem fs, Path checkpointDir) throws IOException {

    Gson gson = new Gson();
    Path objPath = new Path(checkpointDir, EXECUTION_ID_OBJ);

    if (fs.exists(objPath)) {
      return HdfsGsonHelper.read(fs, gson, objPath, Long.class);
    } else {
      return 0L;
    }

  }

  public static void writeExecutionId(Long id, FileSystem fs, Path checkpointDir) throws IOException {

    Gson gson = new Gson();
    Path objPath = new Path(checkpointDir, EXECUTION_ID_OBJ);

    HdfsGsonHelper.write(fs, gson, id, objPath);

  }

}
