package com.liveramp.workflow2.workflow_hadoop;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.junit.Test;

import com.liveramp.workflow2.workflow_hadoop.CheckpointUtil;

import com.rapleaf.cascading_ext.workflow2.WorkflowTestCase;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class CheckpointUtilIT extends WorkflowTestCase {

  @Test
  public void exist() throws Exception {

    FileSystem fs = new Path(getTestRoot()).getFileSystem(new Configuration());

    Path dirPath = new Path(getTestRoot() + "/checkpoints");
    assertEquals(0L, CheckpointUtil.getLatestExecutionId(fs, dirPath));

    CheckpointUtil.writeExecutionId(1L, fs, dirPath);
    assertEquals(1L, CheckpointUtil.getLatestExecutionId(fs, dirPath));

    assertFalse(CheckpointUtil.existCheckpoints(dirPath));

    fs.createNewFile(new Path(dirPath, "step"));

    assertTrue(CheckpointUtil.existCheckpoints(dirPath));

    CheckpointUtil.clearCheckpoints(fs, dirPath);

    assertFalse(CheckpointUtil.existCheckpoints(dirPath));
    assertEquals(1L, CheckpointUtil.getLatestExecutionId(fs, dirPath));

  }



}